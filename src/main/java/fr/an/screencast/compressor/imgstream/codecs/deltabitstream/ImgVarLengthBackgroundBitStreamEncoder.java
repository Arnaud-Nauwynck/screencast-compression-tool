package fr.an.screencast.compressor.imgstream.codecs.deltabitstream;

import fr.an.util.encoder.huffman.HuffmanTable;
import fr.an.util.encoder.structio.BitStreamStructDataOutput;

public class ImgVarLengthBackgroundBitStreamEncoder {

    private BitStreamStructDataOutput bitsStructOutput;
    
    private ColorBgSegmentArray allocatedSegmentArray = new ColorBgSegmentArray();
    
    // ------------------------------------------------------------------------

    public ImgVarLengthBackgroundBitStreamEncoder(BitStreamStructDataOutput bitsStructOutput) {
        this.bitsStructOutput = bitsStructOutput;
    }

    // ------------------------------------------------------------------------

    public void writeImgData_naive(int[] imgData) {
        // encode HuffmanTable codes first !
        final int len = imgData.length;
        // encode data
        for (int i = 0; i < len; i++) {
            int rgbValue = imgData[i];
            bitsStructOutput.writeInt(rgbValue);
        }
    }

    public void writeImgData_Huffman(int[] imgData, HuffmanTable<Integer> huffmanTable) {
        // encode HuffmanTable codes first !
        final int len = imgData.length;
        huffmanTable.writeEncode(bitsStructOutput, len, (out,value) -> out.writeInt(value));
        // encode data
        for (int i = 0; i < len; i++) {
            int rgbValue = imgData[i];
            huffmanTable.writeEncodeSymbol(bitsStructOutput, rgbValue);
        }
    }


    public void writeImgData_divideVarLengthWithBg(final int[] imgData, final int BgColor) {
        if (allocatedSegmentArray == null) {
            allocatedSegmentArray = new ColorBgSegmentArray();
        }
        ColorBgSegmentArray segmentArray = allocatedSegmentArray;
        // compute varlength alternance of color / Bg color
        
        segmentArray.computeSegmentsForImgAndBgColor(imgData, BgColor);
        segmentArray.computeSegmentsHuffmanTableAndMinMaxLens();
        
        int len = imgData.length;
        int segmentsCount = segmentArray.size();
        bitsStructOutput.writeIntMinMax(0, len, segmentsCount);
        
        bitsStructOutput.writeIntMinMax(0, len - segmentsCount*1, segmentArray.maxSegmentLenPlusBgLen);
        bitsStructOutput.writeIntMinMax(0, segmentArray.maxSegmentLenPlusBgLen, segmentArray.minSegmentLenPlusBgLen);
        
        bitsStructOutput.writeIntMinMax(0, segmentArray.maxSegmentLenPlusBgLen, segmentArray.maxSegmentBgLen);
        bitsStructOutput.writeIntMinMax(0, segmentArray.maxSegmentBgLen, segmentArray.minSegmentBgLen);
        
        int knownMaxRemainMaxLen = segmentArray.maxSegmentLenPlusBgLen; // ??? remainMaxLen - segmentArray.maxSegmentBgLen - segmentsCount * segmentArray.minSegmentBgLen;
        bitsStructOutput.writeIntMinMax(0, knownMaxRemainMaxLen, segmentArray.maxSegmentLen);
        bitsStructOutput.writeIntMinMax(0, segmentArray.maxSegmentLen, segmentArray.minSegmentLen);
        
        // encode var length data ... using recursive Divide & Conquer on mid position
        recursiveEncodeVarLengthWithBg(0, ColorBgSegmentArray.iterAt(1), ColorBgSegmentArray.iterAt(segmentsCount), segmentArray,
            segmentArray.fgColorHuffmanTable,
            segmentArray.minSegmentLen, segmentArray.maxSegmentLen, 
            segmentArray.minSegmentBgLen, segmentArray.maxSegmentBgLen,
            segmentArray.minSegmentLenPlusBgLen, segmentArray.maxSegmentLenPlusBgLen);
    }
    
    
    /*pp*/ void recursiveEncodeVarLengthWithBg(
            int posFrom, int segmentIterFrom, int segmentIterTo, 
            ColorBgSegmentArray varLenArray, 
            HuffmanTable<Integer> fgColorHuffmanTable, 
            int minSegmentLen, int maxSegmentLen,
            int minSegmentBgLen, int maxSegmentBgLen,
            int minSegmentLenPlusBgLen, int maxSegmentLenPlusBgLen) {
        assert segmentIterFrom < segmentIterTo;
        // int iterPrev = segmentIterFrom - ColorBgSegmentArray.iterIncr();
        // assert posFrom == varLenArray.getPos(iterPrev) + varLenArray.getLen(iterPrev) + varLenArray.getBgLen(iterPrev);
        int posTo = varLenArray.getPos(segmentIterTo);

//        if (segmentIterFrom + ColorBgSegmentArray.iterIncr() == segmentIterTo) {
//            // degenerated case: only 1 point .. encode without recursion
//            final int iter = segmentIterFrom;
//            int pos = varLenArray.getPos(iter);
//            bitsStructOutput.writeIntMinMax(posFrom, posTo, pos);
//            int remainLen = posTo - pos;
//            int segmentLen = varLenArray.getLen(iter);
//            bitsStructOutput.writeIntMinMax(minSegmentLen, Math.min(maxSegmentLen, remainLen), segmentLen);
//            assert remainLen - segmentLen == varLenArray.getBgLen(iter); 
//            // bitsStructOutput.writeIntMinMax(minSegmentBgLen, remainLen, varLenArray.getBgLen(iter));
//            int color = varLenArray.getColor(iter); 
//            varLenArray.fgColorHuffmanTable.writeEncodeSymbol(bitsStructOutput, color);
//
//            return;
//        }
        
        // Divide & Conquer ...
        // split using mid point
        final int iterMid = (segmentIterFrom + segmentIterTo) >>> 1;
        
        // encode mid segment : pos,len,bgLen,color
        final int posMid = varLenArray.getPos(iterMid);
        final int segmentLenMid = varLenArray.getLen(iterMid);
        final int segmentBgLenMid = varLenArray.getBgLen(iterMid);
        final int colorMid = varLenArray.getColor(iterMid); 

        // encode mid segment pos
        // => compute known min/max for pos ... better than [posFrom, posTo( 
        // knowing that spaces must be reserved for inserting N segments with minLen/minBgLen
        final int leftRemainSegmentCount = (iterMid - segmentIterFrom) >>> 2; // TOCHECK
        final int rightRemainSegmentCount = (segmentIterTo - iterMid) >>> 2; // TOCHECK
        int knownMinForPosMid = posFrom + leftRemainSegmentCount * minSegmentLenPlusBgLen;
        int knownMaxForPosMid = posTo - rightRemainSegmentCount * minSegmentLenPlusBgLen;
        
        bitsStructOutput.writeIntMinMax(knownMinForPosMid, knownMaxForPosMid, posMid);
        
        
        int remainRight = posTo - posMid;
        // encode mid segment len
        // TOOPTIM:   remainRight -= ?
        bitsStructOutput.writeIntMinMax(minSegmentLen, Math.min(maxSegmentLen, remainRight), segmentLenMid);
        // encode mid segment bgLen
        remainRight -= segmentLenMid;
        bitsStructOutput.writeIntMinMax(minSegmentBgLen, Math.min(maxSegmentBgLen, remainRight), segmentBgLenMid );
        // encode mid segment color
        varLenArray.fgColorHuffmanTable.writeEncodeSymbol(bitsStructOutput, colorMid);

        // recurse on left part
        if (segmentIterFrom != iterMid) {
            // OPTIM? .. may re-encode better remaining min/max (& recompute huffmanTable).. 
            recursiveEncodeVarLengthWithBg(
                posFrom, segmentIterFrom, iterMid, 
                varLenArray, 
                fgColorHuffmanTable, 
                minSegmentLen, maxSegmentLen,
                minSegmentBgLen, maxSegmentBgLen,
                minSegmentLenPlusBgLen, maxSegmentLenPlusBgLen);
        }
        
        // recurse on right part
        int posMidRight = posMid + segmentLenMid + segmentBgLenMid;
        int iterMidRight = iterMid + ColorBgSegmentArray.iterIncr();
        if (iterMidRight != segmentIterTo) {
            recursiveEncodeVarLengthWithBg(
                posMidRight, iterMidRight, segmentIterTo, 
                varLenArray, 
                fgColorHuffmanTable, 
                minSegmentLen, maxSegmentLen,
                minSegmentBgLen, maxSegmentBgLen,
                minSegmentLenPlusBgLen, maxSegmentLenPlusBgLen);
        }
    }

    
}

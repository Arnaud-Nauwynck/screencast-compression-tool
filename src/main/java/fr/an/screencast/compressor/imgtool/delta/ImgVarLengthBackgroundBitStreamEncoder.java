package fr.an.screencast.compressor.imgtool.delta;

import fr.an.util.encoder.huffman.HuffmanTable;
import fr.an.util.encoder.structio.BitStreamStructDataOutput;

public class ImgVarLengthBackgroundBitStreamEncoder {

    private BitStreamStructDataOutput bitsStructOutput;
    
    private int[] bufferVarLengthPos;
    private int[] bufferVarLengthColor;
    private int[] bufferVarLengthLen;
    private int[] bufferVarLengthBackgroundLen;
    
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


    public void writeImgData_divideVarLengthWithBackground(final int[] imgData, final int backgroundColor) {
        if (bufferVarLengthPos == null || bufferVarLengthPos.length < imgData.length) {
            bufferVarLengthPos = new int[imgData.length];
            bufferVarLengthColor = new int[imgData.length];
            bufferVarLengthLen = new int[imgData.length];
            bufferVarLengthBackgroundLen = new int[imgData.length];
        }
        
        // compute varlength alternance of color / background color
        final int[] varLengthPos = bufferVarLengthPos;
        final int[] varLengthColor = bufferVarLengthColor;
        final int[] varLengthLen = bufferVarLengthLen;
        final int[] varLengthBackgroundLen = bufferVarLengthBackgroundLen;
        
        final int len = imgData.length;
        int idx = 0;
        int segmentIdx = 0;
        // iterate on backgroundColor  (until color != backgroundColor)
        for(; idx < len; idx++) {
            if (backgroundColor != imgData[idx]) {
                break;
            }
        }
        if (idx != 0) {
            varLengthPos[0] = 0;
            varLengthColor[0] = 0;
            varLengthLen[0] = 0;
            varLengthBackgroundLen[0] = idx;
            segmentIdx++;
        }
        
        for(; idx < len;) {
            varLengthPos[segmentIdx] = idx;
            final int currColor = imgData[idx];
            bufferVarLengthColor[segmentIdx] = currColor;
            // iterate until color != currColor
            idx++;
            for(; idx < len; idx++) {
                if (currColor != imgData[idx]) {
                    break;
                }
            }
            int startBackgroundIdx = idx; 
            bufferVarLengthLen[segmentIdx] = idx - varLengthPos[segmentIdx];
            // iterate in background
            for(; idx < len; idx++) {
                if (backgroundColor != imgData[idx]) {
                    break;
                }
            }
            varLengthBackgroundLen[segmentIdx] = idx - startBackgroundIdx;
            segmentIdx++;
        }
        final int segmentCount = segmentIdx;
        
        // compute huffman table of color frequency per segments (not per absolute count)
        // also compute min/max for varLengthLen[.] and varLengthBackgroundLen[.]
        HuffmanTable<Integer> fgColorHuffmanTable = new HuffmanTable<Integer>();
        int minVarLengthLen = Integer.MAX_VALUE;
        int maxVarLengthLen = Integer.MIN_VALUE;
        int minVarLengthBackgroundLen = varLengthBackgroundLen[0];
        int maxVarLengthBackgroundLen = varLengthBackgroundLen[0];
        for(int i = 1; i < segmentCount; i++) {
            fgColorHuffmanTable.incrSymbolFreq(bufferVarLengthColor[i], 1);
            
            minVarLengthLen = Math.min(minVarLengthLen, varLengthLen[i]);
            maxVarLengthLen = Math.max(maxVarLengthLen, varLengthLen[i]);
            minVarLengthBackgroundLen = Math.min(minVarLengthBackgroundLen, varLengthBackgroundLen[i]);
            maxVarLengthBackgroundLen = Math.max(maxVarLengthBackgroundLen, varLengthBackgroundLen[i]);
        }
        fgColorHuffmanTable.compute();
        
        bitsStructOutput.writeIntMinMax(0, len, segmentCount);
        bitsStructOutput.writeIntMinMax(0, len, minVarLengthLen); //TOOPTIM?
        bitsStructOutput.writeIntMinMax(0, len, maxVarLengthLen); //TOOPTIM?
        bitsStructOutput.writeIntMinMax(0, len, minVarLengthBackgroundLen); //TOOPTIM?
        bitsStructOutput.writeIntMinMax(0, len, maxVarLengthBackgroundLen); //TOOPTIM?
        
        // encode var length data ... using recursive Divide & Conquer on mid position
        recursiveEncodeVarLengthWithBackground(1, segmentCount,
            varLengthPos, varLengthColor, varLengthLen, varLengthBackgroundLen,
            fgColorHuffmanTable,
            minVarLengthLen, maxVarLengthLen, minVarLengthBackgroundLen, maxVarLengthBackgroundLen);
    }
    
    private void recursiveEncodeVarLengthWithBackground(int fromSegment, int toSegment, 
            int[] varLengthPos, int[] varLengthColor, int[] varLengthLen, int[] varLengthBackgroundLen, 
            HuffmanTable<Integer> fgColorHuffmanTable, 
            int minVarLengthLen, int maxVarLengthLen,
            int minVarLengthBackgroundLen, int maxVarLengthBackgroundLen) {
        
//        TODO 
        
    }

}

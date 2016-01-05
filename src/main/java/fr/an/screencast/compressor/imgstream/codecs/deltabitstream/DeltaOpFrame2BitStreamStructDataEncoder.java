package fr.an.screencast.compressor.imgstream.codecs.deltabitstream;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.zip.DeflaterOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.compressor.imgtool.delta.DeltaOperation;
import fr.an.screencast.compressor.imgtool.delta.FrameDeltaDetailed;
import fr.an.screencast.compressor.imgtool.delta.FrameRectDelta;
import fr.an.screencast.compressor.imgtool.delta.FrameRectDeltaDetailed;
import fr.an.screencast.compressor.imgtool.delta.ops.AddAndDrawGlyphRectDeltaOp;
import fr.an.screencast.compressor.imgtool.delta.ops.AddGlyphDeltaOp;
import fr.an.screencast.compressor.imgtool.delta.ops.DrawGlyphRectDeltaOp;
import fr.an.screencast.compressor.imgtool.delta.ops.DrawRectImageDeltaOp;
import fr.an.screencast.compressor.imgtool.delta.ops.FillRectDeltaOp;
import fr.an.screencast.compressor.imgtool.delta.ops.RestorePrevImageRectDeltaOp;
import fr.an.screencast.compressor.imgtool.delta.ops.StopDoNothingDeltaOperation;
import fr.an.screencast.compressor.imgtool.glyph.GlyphIndexOrCode;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.util.bits.OutputStreamToBitOutputStream;
import fr.an.util.bits.RuntimeIOException;
import fr.an.util.encoder.huffman.HuffmanBitsCode;
import fr.an.util.encoder.huffman.HuffmanTable;
import fr.an.util.encoder.structio.BitStreamStructDataOutput;

public class DeltaOpFrame2BitStreamStructDataEncoder {
    
    private static final Logger LOG = LoggerFactory.getLogger(DeltaOpFrame2BitStreamStructDataEncoder.class);
    private static final boolean DEBUG = false;
    
    private File outputFile;
    
    private OutputStreamToBitOutputStream bitStreamOutput;
    private BitStreamStructDataOutput bitsStructOutput;
    
    private Dim dim;
    private Pt dimPt;
    
    private HuffmanTable<Class<? extends DeltaOperation>> deltaOpClassHuffmanTable = new HuffmanTable<Class<? extends DeltaOperation>>();
    
    
    private long sumRectImgBytes; 
    private long sumRectImgBytesPrintFreq = 1024*1024; // print "m" every 1M of raw uncompressed rect image  
    private long sumRectImgBytesPrintModulo = 0;
    
    private ImgVarLengthBackgroundBitStreamEncoder imgVarLengthBitEncoder;
    
    // ------------------------------------------------------------------------

    public DeltaOpFrame2BitStreamStructDataEncoder(File outputFile) {
        this.outputFile = outputFile;
    }

    // ------------------------------------------------------------------------

    public void init(Dim dim) {
        this.dim = dim;
        this.dimPt = new Pt(dim.width, dim.height);

        try {
            OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
            this.bitStreamOutput = new OutputStreamToBitOutputStream(outputStream);
        } catch (FileNotFoundException ex) {
            throw new RuntimeIOException("Faile dto write to file " + outputFile, ex);
        }
        this.bitsStructOutput = new BitStreamStructDataOutput(bitStreamOutput);
        
        // TODO TOOPTIM, for now, use hard-coded deltaOp class & frequency
        deltaOpClassHuffmanTable.addSymbol(DrawRectImageDeltaOp.class, 20);
        deltaOpClassHuffmanTable.addSymbol(RestorePrevImageRectDeltaOp.class, 10);
        deltaOpClassHuffmanTable.addSymbol(FillRectDeltaOp.class, 1);
        deltaOpClassHuffmanTable.addSymbol(AddGlyphDeltaOp.class, 1);
        deltaOpClassHuffmanTable.addSymbol(DrawGlyphRectDeltaOp.class, 5);
        deltaOpClassHuffmanTable.addSymbol(AddAndDrawGlyphRectDeltaOp.class, 10);
        deltaOpClassHuffmanTable.addSymbol(StopDoNothingDeltaOperation.class, 2);
        
        deltaOpClassHuffmanTable.compute();
        
        imgVarLengthBitEncoder = new ImgVarLengthBackgroundBitStreamEncoder(bitsStructOutput);
    }

    public void close() {
        if (bitStreamOutput != null) {
            bitStreamOutput.close();
            bitStreamOutput = null;
            bitsStructOutput = null;
        }
    }
    
    public void encodeDeltaOpFrame(FrameDeltaDetailed frameDeltaDetailed,  
            BufferedImage imageRGB) {
        List<FrameRectDeltaDetailed> rectDeltaDetaileds = frameDeltaDetailed.getFrameRectDeltaDetaileds();
        bitsStructOutput.writeInt(rectDeltaDetaileds != null? rectDeltaDetaileds.size() : 0);
        if (! rectDeltaDetaileds.isEmpty()) {
            Pt currPos = new Pt(0, 0); 
            for (FrameRectDeltaDetailed rectDeltaDetailed : rectDeltaDetaileds) {
                // bitsStructOutput.writeBit(true); // hasMoreRect
                FrameRectDelta rectDelta = rectDeltaDetailed.getResultFrameRectDelta(); 
                Rect deltaRect = rectDelta.getRect();
                
                List<DeltaOperation> deltaOps = rectDelta.getDeltaOperations();
                // bitsStructOutput.writeInt(deltaOps != null? deltaOps.size() : 0);
                
                for(DeltaOperation deltaOp : deltaOps) {
                    // bitsStructOutput.writeBit(true); // hasMoreOp
                    
                    Class<? extends DeltaOperation> deltaOpClass = deltaOp.getClass();
                    deltaOpClassHuffmanTable.writeEncodeSymbol(bitsStructOutput, deltaOpClass);
                    
                    if (deltaOp instanceof DrawRectImageDeltaOp) {
                        DrawRectImageDeltaOp op2 = (DrawRectImageDeltaOp) deltaOp;
                        writeDrawRectImageDeltaOp(op2, rectDeltaDetailed, currPos);
                    } else if (deltaOp instanceof RestorePrevImageRectDeltaOp) {
                        RestorePrevImageRectDeltaOp op2 = (RestorePrevImageRectDeltaOp) deltaOp;
                        writeRestorePrevImageRectDeltaOp(op2, frameDeltaDetailed, currPos, dimPt);
                    } else if (deltaOp instanceof FillRectDeltaOp) {
                        FillRectDeltaOp op2 = (FillRectDeltaOp) deltaOp;
                        writeFillRectDeltaOp(op2, currPos);
                    } else if (deltaOp instanceof AddGlyphDeltaOp) {
                        writeAddGlyphDeltaOp((AddGlyphDeltaOp) deltaOp);
                    } else if (deltaOp instanceof DrawGlyphRectDeltaOp) {
                        writeDrawGlyphRectDeltaOp((DrawGlyphRectDeltaOp) deltaOp, currPos);
                    } else if (deltaOp instanceof AddAndDrawGlyphRectDeltaOp) {
                        writeAddAndDrawGlyphRectDeltaOp((AddAndDrawGlyphRectDeltaOp) deltaOp, currPos);
                        
//                    } else if (deltaOp instanceof DrawLineDeltaOp) {
//                        // TODO unused yet..
//                    } else if (deltaOp instanceof MostUsedColorFillRectDeltaOp) {
//                        // TODO unused yet..
//                    } else if (deltaOp instanceof VideoInverseColorRectDeltaOp) {
//                        // TODO unused yet..
                    } else {
                        LOG.warn("unrecognised deltaOp: " + deltaOp);
                    }
                    
                }
                deltaOpClassHuffmanTable.writeEncodeSymbol(bitsStructOutput, StopDoNothingDeltaOperation.class);
                
                currPos.y = deltaRect.getFromY();
            }
        }
        // bitsStructOutput.writeBit(false); // hasMoreRect
    }

    private void writeAddAndDrawGlyphRectDeltaOp(AddAndDrawGlyphRectDeltaOp op, Pt currPos) {
        Rect opRect = op.getRect();
        int[] glyphData = op.getGlyphData();
        
        writeRectWithConstraints(opRect, currPos, dimPt);
        writeEncodeImgData(glyphData);
    }

    private void writeDrawGlyphRectDeltaOp(DrawGlyphRectDeltaOp op, Pt currPos) {
        Rect opRect = op.getRect();
        GlyphIndexOrCode glyphIndexOrCode = op.getGlyphIndexOrCode();
        
        writeRectWithConstraints(opRect, currPos, dimPt);
        boolean isYoung = glyphIndexOrCode.isYoung();
        bitsStructOutput.writeBit(isYoung);
        if (isYoung) {
            int glyphIndex = glyphIndexOrCode.getYoungIndex();
            int maxIndex = 1024*8; // Integer.MAX_VALUE; // TODO OPTIM
            bitsStructOutput.writeIntMinMax(0, maxIndex, glyphIndex);
        } else {
            HuffmanBitsCode code = glyphIndexOrCode.getOldHuffmanCode();
            code.writeCodeTo(bitsStructOutput);
        }
    }

    private void writeAddGlyphDeltaOp(AddGlyphDeltaOp op) {
        Dim glyphDim = op.getGlyphDim();
        int[] glyphData = op.getGlyphData();
        
        writeDimMinMax(new Dim(1,1), dim, glyphDim);
        writeEncodeImgData(glyphData);
    }

    private void writeFillRectDeltaOp(FillRectDeltaOp op, Pt currPos) {
        Rect opRect = op.getRect();
        int color = op.getFillColor();
        
        writeRectWithConstraints(opRect, currPos, dimPt);
        // bitsStructOutput.writeIntMinMax(0, 255, RGBUtils.redOf(color)); ...
        bitsStructOutput.writeInt(color);
    }

    private void writeDrawRectImageDeltaOp(DrawRectImageDeltaOp op, FrameRectDeltaDetailed rectDeltaDetailed, Pt currPos) {
        Rect opRect = op.getRect();
        int[] rectImg = op.getImg();
        
        int area = opRect.getArea();
        if (area != rectImg.length) {
            System.out.println(area + " != drawRectImageOp " + opRect); 
        }
        
        if (DEBUG) {
            System.out.println(area + ": drawRectImageOp " + opRect + " (area:" + (area*3/1024) + " ko)");
        }
        
        long imgBytes = 3 * area; 
        sumRectImgBytes += imgBytes; 
        sumRectImgBytesPrintModulo += imgBytes;
        while (sumRectImgBytesPrintModulo > sumRectImgBytesPrintFreq) {
            sumRectImgBytesPrintModulo -= sumRectImgBytesPrintFreq;
            System.out.print("m");
        }
        
        writeRectWithConstraints(opRect, currPos, dimPt);
        
        writeEncodeImgData(rectImg);
        
//        // TODO ... may use varlength encoding + no repeat of background (most used color)
//
//        // compute most used color => background
//        int backgroundColor;
//        {
//            ColorToLocationStatsMap colorStats = rectDeltaDetailed.getColorStats();
//            if (colorStats == null) {
//                // LOG.info("colorStat not computed.. need reeval backgroundColor");
//                backgroundColor = RGBUtils.greyRgb2Int(255);
//            } else {
//                ValueLocationStats mostUsedColor = colorStats.findMostUsedColor();
//                if (mostUsedColor == null) {
//                    // LOG.info("should not occur: null most used color");
//                    backgroundColor = RGBUtils.greyRgb2Int(255);
//                } else {
//                    backgroundColor = mostUsedColor.getValue();
//                }
//            }
//        }
//        
////        // encode rectImg using huffman
////        // reuse already computed color map analysis from rectDeltaDetailed
////        HuffmanTable<Integer> colorsHuffmanTable = new HuffmanTable<Integer>();
////        for(ValueLocationStats colorStat : colorStats.getValueToLocationStats().values()) {
////            colorsHuffmanTable.addSymbol(colorStat.getValue(), colorStat.getCount());
////        }
////        colorsHuffmanTable.compute();
////        writeImgData(rectImg, colorsHuffmanTable);
//        
////        writeImgData_naive(rectImg);
//        
//        imgVarLengthBitEncoder.writeImgData_divideVarLengthWithBg(rectImg, backgroundColor);
//        
    }

    private void writeEncodeImgData(int[] rectImg) {
        byte[] gzipBytes = RGBUtils.intRGBsToGzipBytes(rectImg);
        
        bitsStructOutput.writeIntMinMax(0, rectImg.length, gzipBytes.length);
        bitsStructOutput.writeBytes(gzipBytes, gzipBytes.length);
    }


    private void writeRestorePrevImageRectDeltaOp(RestorePrevImageRectDeltaOp op, 
            FrameDeltaDetailed frameDeltaDetailed,     
            Pt fromPos, Pt toPos) {
        Rect restoreRect = op.getRect();
        int restorePrevFrameOffsetAbs = - op.getPrevFrameOffset();
        Pt restorePrevFrameLocation = op.getPrevFrameLocation();
        
        writeRectWithConstraints(restoreRect, fromPos, toPos);
        bitsStructOutput.writeUIntLt16ElseMax(frameDeltaDetailed.getFrameIndex(), restorePrevFrameOffsetAbs);
        writePt(restorePrevFrameLocation, toPos);
        writePt(restorePrevFrameLocation, toPos);
    }

    private void writePt(Pt pt, Pt toPos) {
        bitsStructOutput.writeIntMinMax(0, toPos.x, pt.x); 
        bitsStructOutput.writeIntMinMax(0, toPos.y, pt.y); 
    }


    private void writeRectWithConstraints(Rect rect, Pt fromPos, Pt toPos) {
        bitsStructOutput.writeIntMinMax(fromPos.x, toPos.x, rect.fromX);
        bitsStructOutput.writeIntMinMax(rect.fromX, toPos.x, rect.toX);
        bitsStructOutput.writeIntMinMax(fromPos.y, toPos.y, rect.fromY);
        bitsStructOutput.writeIntMinMax(rect.fromY, toPos.y, rect.toY);
    }

    private void writeDimMinMax(Dim minDim, Dim maxDim, Dim dim) {
        bitsStructOutput.writeIntMinMax(minDim.width, maxDim.width, dim.width); 
        bitsStructOutput.writeIntMinMax(minDim.height, maxDim.height, dim.height); 
    }
    
}

package fr.an.screencast.compressor.imgtool.delta;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.compressor.imgtool.color.ColorToLocationStatsMap;
import fr.an.screencast.compressor.imgtool.color.ColorToLocationStatsMap.ValueLocationStats;
import fr.an.screencast.compressor.imgtool.delta.ops.DrawRectImageDeltaOp;
import fr.an.screencast.compressor.imgtool.delta.ops.FillRectDeltaOp;
import fr.an.screencast.compressor.imgtool.delta.ops.RestorePrevImageRectDeltaOp;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.util.bits.OutputStreamToBitOutputStream;
import fr.an.util.bits.RuntimeIOException;
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
        
        // TODO hard-coded deltaOp class & frequency
        deltaOpClassHuffmanTable.addSymbol(DrawRectImageDeltaOp.class, 10);
        deltaOpClassHuffmanTable.addSymbol(RestorePrevImageRectDeltaOp.class, 5);
        deltaOpClassHuffmanTable.addSymbol(FillRectDeltaOp.class, 1);
        
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
                    bitsStructOutput.writeBit(true); // hasMoreOp
                    if (deltaOp instanceof DrawRectImageDeltaOp) {
                        DrawRectImageDeltaOp op2 = (DrawRectImageDeltaOp) deltaOp;
                        writeDrawRectImageDeltaOp(op2, rectDeltaDetailed, currPos);
                    } else if (deltaOp instanceof RestorePrevImageRectDeltaOp) {
                        RestorePrevImageRectDeltaOp op2 = (RestorePrevImageRectDeltaOp) deltaOp;
                        writeRestorePrevImageRectDeltaOp(op2, frameDeltaDetailed, currPos, dimPt);
                    } else if (deltaOp instanceof FillRectDeltaOp) {
                        FillRectDeltaOp op2 = (FillRectDeltaOp) deltaOp;
                        writeFillRectDeltaOp(op2, currPos);
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
                bitsStructOutput.writeBit(false); // hasMoreOp
                
                currPos.y = deltaRect.getFromY();
            }
        }
        // bitsStructOutput.writeBit(false); // hasMoreRect
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
        
        // TODO ... should use varlength encoding + no repeat of background (most used color)

        // compute most used color => background
        ColorToLocationStatsMap colorStats = rectDeltaDetailed.getColorStats();
        ValueLocationStats mostUsedColor = colorStats.findMostUsedColor();
        if (mostUsedColor == null) {
            LOG.error("should not occur!");
            return;
        }
        final int backgroundColor = mostUsedColor.getValue();
        
//        // encode rectImg using huffman
//        // reuse already computed color map analysis from rectDeltaDetailed
//        HuffmanTable<Integer> colorsHuffmanTable = new HuffmanTable<Integer>();
//        for(ValueLocationStats colorStat : colorStats.getValueToLocationStats().values()) {
//            colorsHuffmanTable.addSymbol(colorStat.getValue(), colorStat.getCount());
//        }
//        colorsHuffmanTable.compute();
//        writeImgData(rectImg, colorsHuffmanTable);
        
//        writeImgData_naive(rectImg);
        
        imgVarLengthBitEncoder.writeImgData_divideVarLengthWithBackground(rectImg, backgroundColor);
        
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

    
}
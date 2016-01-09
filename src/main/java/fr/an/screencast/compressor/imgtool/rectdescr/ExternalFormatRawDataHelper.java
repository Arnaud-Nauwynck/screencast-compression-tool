package fr.an.screencast.compressor.imgtool.rectdescr;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import fr.an.screencast.compressor.imgtool.utils.BufferedImageUtils;
import fr.an.screencast.compressor.imgtool.utils.ImageIOUtils;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.util.encoder.huffman.HuffmanBitsCode;
import fr.an.util.encoder.huffman.HuffmanTable;
import fr.an.util.encoder.structio.StructDataInput;
import fr.an.util.encoder.structio.StructDataOutput;

/**
 * External Format Raw data helper class, for gzip, png, jpeg, ... 
 */
public class ExternalFormatRawDataHelper {

    private ByteArrayOutputStream buffer1 = new ByteArrayOutputStream();
    private ByteArrayOutputStream buffer2 = new ByteArrayOutputStream();
    
    private HuffmanTable<String> externalFormatHuffmanTable;
    
    // ------------------------------------------------------------------------

    public ExternalFormatRawDataHelper() {
        this.externalFormatHuffmanTable = new HuffmanTable<String>();
        externalFormatHuffmanTable.addSymbol("raw", 1);
        externalFormatHuffmanTable.addSymbol("gzip", 2);
        externalFormatHuffmanTable.addSymbol("png", 10);
        
        externalFormatHuffmanTable.compute();
    }

    // ------------------------------------------------------------------------

    
    public void writeRGBData(StructDataOutput out, Dim dim, int[] data) {
        String bestFormat = "raw";
        final int maxBytesLen = 3 * data.length;
        int bestLen = maxBytesLen;
        // TODO ... use ByteBuffer bb = ByteBuffer.allocate(data.length*3);
        ByteArrayOutputStream bestBuffer = buffer1;
        ByteArrayOutputStream tmpBuffer = buffer2;
        ByteArrayOutputStream swapBuffer = null;
        int tmplen;
        tmpBuffer.reset();
        
        // test as gzip
        RGBUtils.intRGBsToGzipBytes(data, tmpBuffer);
        tmplen = tmpBuffer.size();
        if (tmplen < bestLen) {
            bestLen = tmplen;
            bestFormat = "gzip";
            swapBuffer = bestBuffer; bestBuffer = tmpBuffer; tmpBuffer = swapBuffer;
            tmpBuffer.reset();
        }

        BufferedImage img = BufferedImageUtils.copyImage(dim, data);
        // test as "png"
        ImageIOUtils.writeTo(tmpBuffer, img, "png");
        tmplen = tmpBuffer.size();
        if (tmplen < bestLen) {
            bestLen = tmplen;
            bestFormat = "png";
            swapBuffer = bestBuffer; bestBuffer = tmpBuffer; tmpBuffer = swapBuffer;  
            tmpBuffer.reset();
        }
        
        if (bestFormat.equals("raw")) {
            bestBuffer.reset();
            try {
                RGBUtils.intRGBsTo(bestBuffer, data);
            } catch(IOException ex) {
                throw new RuntimeException("Failed", ex);
            }
        }
        
        HuffmanBitsCode formatSymbolCode = externalFormatHuffmanTable.getSymbolCode(bestFormat);
        out.writeHuffmanCode(formatSymbolCode);

        // TODO aboid realloc byte[] use ByteBuffer..
        byte[] bytes = bestBuffer.toByteArray();
        
        out.writeIntMinMax(1, maxBytesLen+1, bestLen);
        out.writeBytes(bytes, bestLen);
    }

    public int[] readRGBData(StructDataInput in, Dim dim) {
        int[] res = new int[dim.getArea()];
        readRGBData(res, in, dim);
        return res;
    }
    
    public void readRGBData(int[] res, StructDataInput in, Dim dim) {
        String formatCode = in.readDecodeHuffmanCode(externalFormatHuffmanTable);
        final int maxBytesLen = 3 * dim.getArea();
        int encodeBytesLen = in.readIntMinMax(1, maxBytesLen+1);
        
        byte[] encodeBytes = new byte[encodeBytesLen];
        in.readBytes(encodeBytes, 0, encodeBytes.length);
        ByteArrayInputStream encodedInput = new ByteArrayInputStream(encodeBytes); 

        int alpha = 255;
        switch(formatCode) {
        case "raw":
            RGBUtils.bytesToIntRGBs(res, encodeBytes, alpha);
            // in.readInts(res, 0, dataLen);
            break;
        case "gzip":
            RGBUtils.gzipBytesToIntRGBs(res, encodedInput, alpha);
            break;
        case "png":
            BufferedImage img = ImageIOUtils.read(null, "png", encodedInput);
            BufferedImageUtils.copyImage(res, img);
            break;
        }
    }

    
}

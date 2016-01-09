package fr.an.screencast.compressor.imgstream.codecs.deltabitstream;

import java.awt.image.BufferedImage;

import org.junit.Assert;
import org.junit.Test;

import fr.an.bitwise4j.encoder.huffman.HuffmanTable;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.ImageTstUtils;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;

public class ColorBgSegmentArrayTest {

    private static final boolean DEBUG = false;

    private static final int[] TEST_imgData0 = new int[] {
        0, 0, 0, 0, 0, 111, 111, 0, 0, 0, 0, 111, 111, 222, 333, 333, 0, 0,  111, 111, 0, 0 //
        // 1  2  3  4  5    6    7  8  9 10  11   12   13   14   15   16 17  18   19   20 21
    };
    //       seg[0]    seg[1]                seg[2]   seg[3] seg[4]          seg[5]          
    // pos:   0        5                     11       13     14              18
    // color: -1       111                   111      222    333             111
    // len:   XX       2                     2        1      2               2
    // bgLen: 5        4                     0        0      2               2
    // ln+bgLn 5       6                     2        1      4               4
    
    @Test
    public void testImgToVarLengthArrays() {
        // Prepare
        int backgroundColor = 0;
        ColorBgSegmentArray sut = new ColorBgSegmentArray(); 
        // Perform
        sut.computeSegmentsForImgAndBgColor(TEST_imgData0, backgroundColor);
        // Post-check
        Assert.assertEquals(6, sut.size());
        assertEqualsSeg(0, 0, -1, 5, sut, 0);
        assertEqualsSeg(5, 2, 111, 4, sut, 4);
        assertEqualsSeg(11, 2, 111, 0, sut, 8);
        assertEqualsSeg(13, 1, 222, 0, sut, 12);
        assertEqualsSeg(14, 2, 333, 2, sut, 16);
        assertEqualsSeg(18, 2, 111, 2, sut, 20);
    }
    
    private static void assertEqualsSeg(int expectedPos, int expectedLen, int expectedColor, int expectedBgLen, 
            ColorBgSegmentArray actualVarLenArray, int iter) {
        Assert.assertEquals(expectedPos, actualVarLenArray.getPos(iter));
        Assert.assertEquals(expectedLen, actualVarLenArray.getLen(iter));
        Assert.assertEquals(expectedColor, actualVarLenArray.getColor(iter));
        Assert.assertEquals(expectedBgLen, actualVarLenArray.getBgLen(iter));
    }
    
    @Test
    public void testComputeSegmentsHuffmanTableAndMinMaxLens() {
        // Perform
        int backgroundColor = 0;
        ColorBgSegmentArray sut = new ColorBgSegmentArray(); 
        sut.computeSegmentsForImgAndBgColor(TEST_imgData0, backgroundColor);
        // Perform
        sut.computeSegmentsHuffmanTableAndMinMaxLens();
        // Post-check
        HuffmanTable<Integer> huffmanTable = sut.fgColorHuffmanTable;
        Assert.assertEquals(3, huffmanTable.getSymbolCount());
        Assert.assertEquals("1", huffmanTable.getSymbolCode(111).toString());
        Assert.assertEquals("00", huffmanTable.getSymbolCode(222).toString());
        Assert.assertEquals("01", huffmanTable.getSymbolCode(333).toString());
        
        Assert.assertEquals(6, sut.maxSegmentLenPlusBgLen);
        Assert.assertEquals(1, sut.minSegmentLenPlusBgLen);
        Assert.assertEquals(2, sut.maxSegmentLen);
        Assert.assertEquals(1, sut.minSegmentLen);
        Assert.assertEquals(5, sut.maxSegmentBgLen);
        Assert.assertEquals(0, sut.minSegmentBgLen);
    }


    
    @Test
    public void testComputeSegmentsForImgAndBgColor_screen_eclipse_1920x1080() throws Exception {
        // Prepare
        BufferedImage img = ImageTstUtils.getImageTest_1920x1080();
        int[] imgData = ImageRasterUtils.toInts(img);
        int backgroundColor = RGBUtils.greyRgb2Int(255);
        ColorBgSegmentArray sut = new ColorBgSegmentArray();
        // Perform
        sut.computeSegmentsForImgAndBgColor(imgData, backgroundColor);
        sut.computeSegmentsHuffmanTableAndMinMaxLens();
        // Post-check
        if (DEBUG) {
            System.out.println(sut);
        }
    }

}

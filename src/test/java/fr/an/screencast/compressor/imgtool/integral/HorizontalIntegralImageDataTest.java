package fr.an.screencast.compressor.imgtool.integral;

import org.junit.Assert;
import org.junit.Test;

import fr.an.screencast.compressor.imgtool.utils.ImageDataAssert;
import fr.an.screencast.compressor.imgtool.utils.RasterImageFunctions;
import fr.an.screencast.compressor.utils.Dim;

public class HorizontalIntegralImageDataTest {

    private static final Dim dim = new Dim(2, 2);
    private static final int[] srcData = new int[] {
        1, 2, //
        3, 4 //
    };
    
    private static final int[] expectedIntegral = new int[] {
        1, 3, //
        3, 7 //
    };
    

    @Test
    public void testSetComputeFrom() {
        // Prepare
        HorizontalIntegralImageData sut = new HorizontalIntegralImageData(dim);
        // Perform
        sut.setComputeFrom(RasterImageFunctions.of(dim, srcData));
        // Post-check
        ImageDataAssert.assertEquals(expectedIntegral, sut);
    }
    
    @Test
    public void testIntegralHorizontalLine() {
        // Prepare
        HorizontalIntegralImageData sut = new HorizontalIntegralImageData(dim);
        sut.setCopyData(expectedIntegral);
        // Perform
        // Post-check
        Assert.assertEquals(1, sut.integralHorizontalLine(0, 0, 0));
        Assert.assertEquals(1+2, sut.integralHorizontalLine(0, 0, 1));
        Assert.assertEquals(2, sut.integralHorizontalLine(1, 0, 1));

        Assert.assertEquals(3, sut.integralHorizontalLine(0, 1, 0));
        Assert.assertEquals(3+4, sut.integralHorizontalLine(0, 1, 1));
        Assert.assertEquals(4, sut.integralHorizontalLine(1, 1, 1));
    }

    @Test
    public void testFindFirstLinePt() {
        // Prepare
        Dim dim2 = new Dim(8, 5);
        int[] src2BinData = new int[] {
            0, 0, 0, 0, 0, 0, 0, 0, //
            0, 0, 0, 1, 0, 1, 1, 0, //
            1, 0, 1, 0, 0, 1, 0, 0, //
            0, 0, 0, 0, 0, 0, 0, 1, //
            0, 1, 1, 1, 1, 1, 1, 1
        };
        int[] expected2BinIntegral = new int[] {
            0, 0, 0, 0, 0, 0, 0, 0, //
            0, 0, 0, 1, 1, 2, 3, 3, //
            1, 1, 2, 2, 2, 3, 3, 3, //
            0, 0, 0, 0, 0, 0, 0, 1, //
            0, 1, 2, 3, 4, 5, 6, 7
        };
        HorizontalIntegralImageData sut = new HorizontalIntegralImageData(dim2);
        sut.setComputeFrom(RasterImageFunctions.of(dim2, src2BinData));
        // sut.setCopyData(expected2BinIntegral);
        ImageDataAssert.assertEquals(expected2BinIntegral, sut);

        // Perform
        Assert.assertEquals(-1, sut.findFirstLinePt(0, true));
        Assert.assertEquals(3, sut.findFirstLinePt(1, true));
        Assert.assertEquals(0, sut.findFirstLinePt(2, true));
        Assert.assertEquals(7, sut.findFirstLinePt(3, true));
        Assert.assertEquals(1, sut.findFirstLinePt(4, true));
        // Post-check

    }
    
}

package fr.an.screencast.compressor.imgtool.integral;

import org.junit.Assert;
import org.junit.Test;

import fr.an.screencast.compressor.imgtool.utils.ImageDataAssert;
import fr.an.screencast.compressor.imgtool.utils.RasterImageFunctions;
import fr.an.screencast.compressor.utils.Dim;

public class HorizontalIntegralImageDataTest {

    private Dim dim = new Dim(2, 2);
    private int[] srcData = new int[] {
        1, 2, //
        3, 4 //
    };
    
    private int[] expectedIntegral = new int[] {
        1, 3, //
        3, 7 //
    };
    
    private HorizontalIntegralImageData sut = new HorizontalIntegralImageData(dim);

    @Test
    public void testSetComputeFrom() {
        // Prepare
        // Perform
        sut.setComputeFrom(RasterImageFunctions.of(dim, srcData));
        // Post-check
        ImageDataAssert.assertEquals(expectedIntegral, sut);
    }
    
    @Test
    public void testIntegralHorizontalLine() {
        // Prepare
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

    
}

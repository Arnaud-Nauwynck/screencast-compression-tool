package fr.an.screencast.compressor.imgtool.integral;

import org.junit.Assert;
import org.junit.Test;

import fr.an.screencast.compressor.imgtool.utils.ImageDataAssert;
import fr.an.screencast.compressor.imgtool.utils.RasterImageFunctions;
import fr.an.screencast.compressor.utils.Dim;

public class VerticalIntegralImageDataTest {

    private Dim dim = new Dim(2, 2);
    private int[] srcData = new int[] {
        1, 2, //
        3, 4 //
    };
    
    private int[] expectedIntegral = new int[] {
        1, 2, //
        4, 6 //
    };
    
    private VerticalIntegralImageData sut = new VerticalIntegralImageData(dim);

    @Test
    public void testSetComputeFrom() {
        // Prepare
        // Perform
        sut.setComputeFrom(RasterImageFunctions.of(dim, srcData));
        // Post-check
        ImageDataAssert.assertEquals(expectedIntegral, sut);
    }
    

    @Test
    public void testIntegralVerticalLine() {
        // Prepare
        sut.setCopyData(expectedIntegral);
        // Perform
        // Post-check
        Assert.assertEquals(1, sut.integralVerticalLine(0, 0, 0));
        Assert.assertEquals(1+3, sut.integralVerticalLine(0, 0, 1));
        Assert.assertEquals(3, sut.integralVerticalLine(0, 1, 1));

        Assert.assertEquals(2, sut.integralVerticalLine(1, 0, 0));
        Assert.assertEquals(2+4, sut.integralVerticalLine(1, 0, 1));
        Assert.assertEquals(4, sut.integralVerticalLine(1, 1, 1));
    }

    
}

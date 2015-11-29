package fr.an.screencast.compressor.imgtool.integral;

import org.junit.Assert;
import org.junit.Test;

import fr.an.screencast.compressor.imgtool.utils.ImageDataAssert;
import fr.an.screencast.compressor.imgtool.utils.RasterImageFunctions;
import fr.an.screencast.compressor.utils.Dim;

public class IntegralImageDataTest {

    private Dim dim = new Dim(2, 2);
    private int[] srcData = new int[] {
        1, 2, //
        3, 4 //
    };
    
    private int[] expectedIntegral = new int[] {
        1, 3, //
        4, 10 //
    };
    
    private IntegralImageData sut = new IntegralImageData(dim);

    @Test
    public void testSetComputeFrom() {
        // Prepare
        // Perform
        sut.setComputeFrom(RasterImageFunctions.of(dim, srcData));
        // Post-check
        ImageDataAssert.assertEquals(expectedIntegral, sut);
    }
    
    @Test
    public void testIntegralPt2PtInclude() {
        // Prepare
        sut.setCopyData(expectedIntegral);
        // Perform
        // Post-check
        for(int y = 0, idx_xy = 0; y < dim.height; y++) {
            for(int x = 0; x < dim.width; x++,idx_xy++) {
                Assert.assertEquals(srcData[idx_xy], sut.integralPt2PtInclude(x, y, x, y));
            }
        }
        Assert.assertEquals(1+2, sut.integralPt2PtInclude(0, 0, 1, 0));
        Assert.assertEquals(1+3, sut.integralPt2PtInclude(0, 0, 0, 1));
        Assert.assertEquals(1+2+3+4, sut.integralPt2PtInclude(0, 0, 1, 1));
    }

}

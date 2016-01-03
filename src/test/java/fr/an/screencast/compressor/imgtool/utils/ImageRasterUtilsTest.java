package fr.an.screencast.compressor.imgtool.utils;

import org.junit.Assert;
import org.junit.Test;

import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;

public class ImageRasterUtilsTest {

    @Test
    public void testDrawRectImg() {
        // Prepare
        Dim destDim = new Dim(2, 2);
        int[] dest = new int[4];
        Pt destLocation = new Pt(0, 0);
        Dim srcDim = new Dim(5, 4);
        int[] src = new int[] {
            0, 1, 2, 3, 4, //
            1, 2, 3, 4, 5, //
            2, 3, 4, 5, 6, //
            3, 4, 5, 6, 7, //
        };
        // Perform
        Rect srcROI = Rect.newPtToPt(1, 1, 3, 3);
        ImageRasterUtils.drawRectImg(destDim, dest, destLocation, srcDim, src, srcROI);
        // Post-check
        ImageDataAssert.assertEquals(new int[] { 2, 3, 3, 4 }, dest, destDim);
    }

    @Test
    public void testFillAlpha() {
        // Prepare
        int[] data = new int[] { RGBUtils.greyRgb2Int(236) };
        // Perform
        ImageRasterUtils.fillAlpha(data);
        // Post-check
        Assert.assertEquals(RGBUtils.rgb2Int256(236, 236, 236, 255), data[0]);
    }

}

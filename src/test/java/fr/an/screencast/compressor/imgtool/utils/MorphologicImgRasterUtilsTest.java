package fr.an.screencast.compressor.imgtool.utils;

import org.junit.Test;

import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Rect;

public class MorphologicImgRasterUtilsTest {

    @Test
    public void testDilateMaxRGB() {
        // Prepare
        Dim dim = new Dim(8, 5);
        int g = RGBUtils.greyRgb2Int(0);
        int g50 = RGBUtils.greyRgb2Int(50);
        int g100 = RGBUtils.greyRgb2Int(100);
        int g250 = RGBUtils.greyRgb2Int(250);
        int[] src = new int[] {
            g, g,  g, g, g,   g, g,   g, //
            g, g50,g, g, g,   g, g,   g , //
            g, g,  g, g, g100,g, g,   g , //
            g, g,  g, g, g,   g, g250,g , //
            g, g,  g, g, g,   g, g,   g, //
        };
        int[] dest = new int[dim.width*dim.height];
        Rect roi = Rect.newDim(dim);
        ImageRasterUtils.copyRect(dest, src, dim.width, dim.height, roi); // border
        // Perform
        MorphologicImgRasterUtils.dilateMaxRGB(dest, src, dim.width, dim.height, roi);
        // Post-check
        int[] expected = new int[] {
            g, g,  g,  g,   g,   g,   g,   g, // 
            g, g50,g50,g100,g100,g100,g,   g, //
            g, g50,g50,g100,g100,g250,g250,g, //
            g, g,  g,  g100,g100,g250,g250,g, //
            g, g,  g,  g,   g,   g,   g,   g, //
        };
        ImageDataAssert.assertEquals(expected, dest, dim.width, dim.height);
    }

    
    @Test
    public void testErodeMinRGB() {
        // Prepare
        Dim dim = new Dim(8, 5);
        int g0 = RGBUtils.greyRgb2Int(0);
        int g50 = RGBUtils.greyRgb2Int(50);
        int g250 = RGBUtils.greyRgb2Int(250);
        int w = RGBUtils.greyRgb2Int(255);
        int[] src = new int[] {
            w, w, w, w, w,  w, w,   w, //
            w, g0,w, w, w,  w, w,   w , //
            w, w, w, w, g50,w, w,   w , //
            w, w, w, w, w,  w, g250,w , //
            w, w, w, w, w,  w, w,   w, //
        };
        int[] dest = new int[dim.width*dim.height];
        Rect roi = Rect.newDim(dim);
        ImageRasterUtils.copyRect(dest, src, dim.width, dim.height, roi); // border
        // Perform
        MorphologicImgRasterUtils.erodeMinRGB(dest, src, dim.width, dim.height, roi);
        // Post-check
        int[] expected = new int[] {
            w, w,  w,  w,  w,  w,   w,   w, // 
            w, g0, g0, g50,g50,g50, w,   w, //
            w, g0, g0, g50,g50,g50,g250,w, //
            w, w,  w,  g50,g50,g50,g250,w, //
            w, w,  w,  w,  w,  w,  w,    w, //
        };
        ImageDataAssert.assertEquals(expected, dest, dim.width, dim.height);
    }
    
    
    
    @Test
    public void testDilateErodeRGB() {
        // Prepare
        Dim dim = new Dim(8, 5);
        int g = RGBUtils.greyRgb2Int(0);
        int g50 = RGBUtils.greyRgb2Int(50);
        int g100 = RGBUtils.greyRgb2Int(100);
        int g250 = RGBUtils.greyRgb2Int(250);
        int[] src = new int[] {
            g, g,  g, g, g,   g, g,   g, //
            g, g50,g, g, g,   g, g,   g , //
            g, g,  g, g, g100,g, g,   g , //
            g, g,  g, g, g,   g, g250,g , //
            g, g,  g, g, g,   g, g,   g, //
        };
        int[] intermediate = new int[dim.width*dim.height];
        Rect roi = Rect.newDim(dim);
        ImageRasterUtils.copyRect(intermediate, src, dim.width, dim.height, roi); // border
        // Perform
        MorphologicImgRasterUtils.dilateMaxRGB(intermediate, src, dim.width, dim.height, roi);
        // Post-check
        int[] intermediateExpected = new int[] {
            g, g,  g,  g,   g,   g,   g,   g, // 
            g, g50,g50,g100,g100,g100,g,   g, //
            g, g50,g50,g100,g100,g250,g250,g, //
            g, g,  g,  g100,g100,g250,g250,g, //
            g, g,  g,  g,   g,   g,   g,   g, //
        };
        ImageDataAssert.assertEquals(intermediateExpected, intermediate, dim.width, dim.height);

        int[] dest = new int[dim.width*dim.height];
        ImageRasterUtils.copyRect(dest, intermediate, dim.width, dim.height, roi); // border
        // Perform
        MorphologicImgRasterUtils.erodeMinRGB(dest, intermediate, dim.width, dim.height, roi);
        // Post-check
        int[] expected = new int[] {
            g, g,  g,  g,   g,   g,   g,   g, // 
            g, g,  g,  g,   g,   g,   g,   g, //
            g, g,  g,  g,   g100,g,   g,   g, //
            g, g,  g,  g,   g,   g,   g,   g, //
            g, g,  g,  g,   g,   g,   g,   g, //
        };
        ImageDataAssert.assertEquals(expected, dest, dim.width, dim.height);
    }
}

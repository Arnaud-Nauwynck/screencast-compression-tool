package fr.an.screencast.compressor.imgtool.utils;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.Assert;
import org.junit.Test;

import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Rect;

public class RGBUtilsTest {

    @Test
    public void testRgb2r() {
        int value = 0x11223344;
        Assert.assertEquals(0x22, RGBUtils.redOf(value));
        Assert.assertEquals(0x33, RGBUtils.greenOf(value));
        Assert.assertEquals(0x44, RGBUtils.blueOf(value));
        Assert.assertEquals(0x11, RGBUtils.alphaOf(value));
        Assert.assertEquals(value, RGBUtils.rgb2Int(RGBUtils.redOf(value), RGBUtils.greenOf(value), RGBUtils.blueOf(value), RGBUtils.alphaOf(value)));
    }
    
    @Test
    public void testToStringFixed() {
        int value = 0x11223344;
        String res = RGBUtils.toStringFixed(value);
        Assert.assertEquals("034,051,068", res);
    }
    
    @Test
    public void testDumpFixedRGBString() {
        // Prepare
        Dim dim = new Dim(4, 4);
        int c1 = RGBUtils.greyRgb2Int(1);
        int c2 = RGBUtils.greyRgb2Int(255);
        int[] imgData = new int[] {
            c1, c2, c1, c1, //
            c2, c2, c2, c2, //
            c1, c2, c1, c1, //
            c1, c2, c1, c1, //
        };
        Rect roi = Rect.newPtToPt(1,  1, 3, 3); 
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer);
        // Perform
        RGBUtils.dumpFixedRGBString(dim, imgData, roi, out );
        // Post-check
        out.flush();
        String res = buffer.toString();
        Assert.assertEquals("   ___|    1        |    2        | \n"
                + "    1 | 255,255,255 | 255,255,255 | \n"
                + "    2 | 255,255,255 | 001,001,001 | \n", res);
    }
    
}

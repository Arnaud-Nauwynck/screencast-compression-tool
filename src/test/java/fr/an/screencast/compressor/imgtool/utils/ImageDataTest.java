package fr.an.screencast.compressor.imgtool.utils;

import org.junit.Test;

import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;

public class ImageDataTest {

    @Test
    public void testSetFillRect() {
        // Prepare
        Dim dim = new Dim(4, 3);
        ImageData sut = new ImageData(dim);
        // Perform
        sut.setFillRect(new Rect(new Pt(0, 0), new Pt(dim.width-1, dim.height-1)),  1);
        // Post-check
        ImageDataAssert.assertEquals(new int[] {
            1, 1, 1, 1, //
            1, 1, 1, 1, //
            1, 1, 1, 1
        }, sut);
        // Perform
        sut.setFillRect(new Rect(new Pt(1, 1), new Pt(2, 2)),  2);
        ImageDataAssert.assertEquals(new int[] {
            1, 1, 1, 1, //
            1, 2, 2, 1, //
            1, 2, 2, 1
        }, sut);
        // Post-check

    }
}

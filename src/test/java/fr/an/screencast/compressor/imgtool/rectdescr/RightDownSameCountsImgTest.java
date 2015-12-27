package fr.an.screencast.compressor.imgtool.rectdescr;

import org.junit.Test;

import fr.an.screencast.compressor.imgtool.utils.ImageDataAssert;
import fr.an.screencast.compressor.utils.Dim;

public class RightDownSameCountsImgTest {

    @Test
    public void testSetComputeFrom() {
        // Prepare
        Dim dim = new Dim(5, 4);
        RightDownSameCountsImg sut = new RightDownSameCountsImg(dim);
        final int[] src = new int[] {
            0, 0, 1, 1, 1, //
            0, 1, 1, 1, 2, //
            1, 2, 2, 3, 3, // 
            1, 1, 2, 3, 4  //
        };
        final int[] expectedRightSameCounts = new int[] {
            2, 1, 3, 2, 1, //
            1, 3, 2, 1, 1, //
            1, 2, 1, 2, 1, //
            2, 1, 1, 1, 1, //
        };
        final int[] expectedDownSameCounts = new int[] {
            2, 1, 2, 2, 1, //
            1, 1, 1, 1, 1, //
            2, 1, 2, 2, 1, //
            1, 1, 1, 1, 1, //
        };
        // Perform
        sut.setComputeFrom(src);
        // Post-check
        ImageDataAssert.assertEquals(expectedRightSameCounts, sut.getRightSameCounts(), dim);
        ImageDataAssert.assertEquals(expectedDownSameCounts, sut.getDownSameCounts(), dim);
    }
}

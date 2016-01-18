package fr.an.screencast.compressor.imgtool.rectdescr;

import org.junit.Test;

import fr.an.screencast.compressor.imgtool.utils.ImageDataAssert;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Rect;

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

    @Test
    public void testSetComputeFromUniformImg() {
        // Prepare
        Dim dim = new Dim(5, 4);
        RightDownSameCountsImg sut = new RightDownSameCountsImg(dim);
        // Perform
        sut.setComputeFromUniformImg();
        // Post-check
        final int[] expectedRightSameCounts = new int[] {
            5, 4, 3, 2, 1, //
            5, 4, 3, 2, 1, //
            5, 4, 3, 2, 1, //
            5, 4, 3, 2, 1, //
        };
        final int[] expectedDownSameCounts = new int[] {
            4, 4, 4, 4, 4, //
            3, 3, 3, 3, 3, //
            2, 2, 2, 2, 2, //
            1, 1, 1, 1, 1, //
        };
        ImageDataAssert.assertEquals(expectedRightSameCounts, sut.getRightSameCounts(), dim);
        ImageDataAssert.assertEquals(expectedDownSameCounts, sut.getDownSameCounts(), dim);
    }
    
    @Test
    public void testUpdateDiffCountsRect() {
        // Prepare
        Dim dim = new Dim(7, 5);
        RightDownSameCountsImg sut = new RightDownSameCountsImg(dim);
        sut.setComputeFromUniformImg();
        Rect rect = Rect.newPtToPt(2, 1, 4, 3);
        // Perform
        sut.updateDiffCountsRect(rect);
        // Post-check
        final int[] expectedRightSameCounts = new int[] {
            7, 6, 5, 4, 3, 2, 1, //
            2, 1, 2, 1, 3, 2, 1, //
            2, 1, 2, 1, 3, 2, 1, //
            7, 6, 5, 4, 3, 2, 1, //
            7, 6, 5, 4, 3, 2, 1, //
        };
        final int[] expectedDownSameCounts = new int[] {
            5, 5, 1, 1, 5, 5, 5, //
            4, 4, 2, 2, 4, 4, 4, //
            3, 3, 1, 1, 3, 3, 3, //
            2, 2, 2, 2, 2, 2, 2, //
            1, 1, 1, 1, 1, 1, 1, //
        };
        ImageDataAssert.assertEquals(expectedRightSameCounts, sut.getRightSameCounts(), dim);
        ImageDataAssert.assertEquals(expectedDownSameCounts, sut.getDownSameCounts(), dim);
    }
    
    @Test
    public void testUpdateDiffCountsSegment() {
        // Prepare
        Dim dim = new Dim(7, 5);
        RightDownSameCountsImg sut = new RightDownSameCountsImg(dim);
        sut.setComputeFromUniformImg();
        // Perform
        sut.updateDiffCountsSegment(2, 4, 1);
        // Post-check
        final int[] expectedRightSameCounts = new int[] {
            7, 6, 5, 4, 3, 2, 1, //
            2, 1, 2, 1, 3, 2, 1, //
            7, 6, 5, 4, 3, 2, 1, //
            7, 6, 5, 4, 3, 2, 1, //
            7, 6, 5, 4, 3, 2, 1, //
        };
        final int[] expectedDownSameCounts = new int[] {
            5, 5, 1, 1, 5, 5, 5, //
            4, 4, 1, 1, 4, 4, 4, //
            3, 3, 3, 3, 3, 3, 3, //
            2, 2, 2, 2, 2, 2, 2, //
            1, 1, 1, 1, 1, 1, 1, //
        };
        ImageDataAssert.assertEquals(expectedRightSameCounts, sut.getRightSameCounts(), dim);
        ImageDataAssert.assertEquals(expectedDownSameCounts, sut.getDownSameCounts(), dim);
    }
    
    
    
}

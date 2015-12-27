package fr.an.screencast.compressor.imgtool.rectdescr;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import fr.an.screencast.compressor.utils.Rect;

public class HorizontalSegmentToRectsFillerTest {

    @Test
    public void testScanLineAddSegment_simple() {
        // Prepare
        HorizontalSegmentToRectsFiller sut = new HorizontalSegmentToRectsFiller();
        // Perform
        int y = 0;
        sut.scanStartLine(y);
        //  0 1 2 3 4 5 6 7 8 9 10
        //  [.........(   [.(
        //  [.........(   [.(
        //  [.........(           
        sut.scanLineAddSegment(y, 0, 5);
        sut.scanLineAddSegment(y, 7, 8);
        sut.scanEndLine(y);

        y++;
        sut.scanStartLine(y);
        sut.scanLineAddSegment(y, 0, 5);
        sut.scanLineAddSegment(y, 7, 8);
        sut.scanEndLine(y);

        y++;
        sut.scanStartLine(y);
        sut.scanLineAddSegment(y, 0, 5);
        sut.scanEndLine(y);

        y++;
        sut.scanStartLine(y);
        sut.scanEndLine(y);

        sut.scanDone();

        // Post-check
        List<Rect> res = sut.getResultRects();
        Assert.assertEquals(2, res.size());
        Assert.assertEquals(Rect.newPtToPt(0, 0, 5, 3), res.get(0));
        Assert.assertEquals(Rect.newPtToPt(7, 0, 8, 2), res.get(1));
    }
    
    @Test
    public void testScanLineAddSegment_adjacentRects() {
        // Prepare
        HorizontalSegmentToRectsFiller sut = new HorizontalSegmentToRectsFiller();
        // Perform
        int y = 0;
        sut.scanStartLine(y);
        //  0 1 2 3 4 5 6 7 8 9 10
        //  [...|.....(   [.|...(
        //  [...|           [...(
        sut.scanLineAddSegment(y, 0, 2);
        sut.scanLineAddSegment(y, 2, 5);
        sut.scanLineAddSegment(y, 7, 8);
        sut.scanLineAddSegment(y, 8, 10);
        sut.scanEndLine(y);

        y++;
        sut.scanStartLine(y);
        sut.scanLineAddSegment(y, 0, 2);
        sut.scanLineAddSegment(y, 8, 10);
        sut.scanEndLine(y);

        y++;
        sut.scanStartLine(y);
        sut.scanEndLine(y);

        sut.scanDone();
        
        // Post-check
        List<Rect> res = sut.getResultRects();
        Assert.assertEquals(4, res.size());
        Assert.assertEquals(Rect.newPtToPt(0, 0, 2, 2), res.get(0));
        Assert.assertEquals(Rect.newPtToPt(2, 0, 5, 1), res.get(1));
        Assert.assertEquals(Rect.newPtToPt(7, 0, 8, 1), res.get(2));
        Assert.assertEquals(Rect.newPtToPt(8, 0, 10, 2), res.get(3));
    }

    
    @Test
    public void testScanLineAddSegment_splitChangeSeg() {
        // Prepare
        HorizontalSegmentToRectsFiller sut = new HorizontalSegmentToRectsFiller();
        // Perform
        //  0 1 2 3 4 5 6 7 8 9 10
        //      [.....(
        //    [...( [...(
        //      [.....(
        int y = 0;
        sut.scanStartLine(y);
        sut.scanLineAddSegment(y, 2, 5);
        sut.scanEndLine(y);

        y++;
        sut.scanStartLine(y);
        sut.scanLineAddSegment(y, 1, 3);
        sut.scanLineAddSegment(y, 4, 6);
        sut.scanEndLine(y);

        y++;
        sut.scanStartLine(y);
        sut.scanLineAddSegment(y, 2, 5);
        sut.scanEndLine(y);

        sut.scanDone();
        
        // Post-check
        List<Rect> res = sut.getResultRects();
        Assert.assertEquals(4, res.size());
        Assert.assertEquals(Rect.newPtToPt(2, 0, 5, 1), res.get(0));
        Assert.assertEquals(Rect.newPtToPt(1, 1, 3, 2), res.get(1));
        Assert.assertEquals(Rect.newPtToPt(4, 1, 6, 2), res.get(2));
        Assert.assertEquals(Rect.newPtToPt(2, 2, 5, 3), res.get(3));
    }

}

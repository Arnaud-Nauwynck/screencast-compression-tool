package fr.an.screencast.compressor.imgtool.rectdescr;

import java.util.ArrayList;
import java.util.List;

import fr.an.screencast.compressor.utils.Rect;

/**
 * scanner class in y,x order to build build rectangles on the fly
 * by filling their bottom segments [fromX,toX];y  
 *
 */
public final class HorizontalSegmentToRectsFiller {

    private List<Rect> resultRects = new ArrayList<Rect>();

    private List<Rect> prevLineOpenedRects = new ArrayList<Rect>();
    private List<Rect> currLineOpenedRects = new ArrayList<Rect>();

    private int currY;
    private int currX;
    private int prevLineCurrOpenedRectIndex = -1;
    private Rect prevLineCurrOpenedRect;
    
    // ------------------------------------------------------------------------

    public HorizontalSegmentToRectsFiller() {
    }

    // ------------------------------------------------------------------------

    public void scanStartLine(int y) {
        this.currY = y;
        this.currX = -1;
        this.prevLineCurrOpenedRectIndex = 0;
        this.prevLineCurrOpenedRect = (!prevLineOpenedRects.isEmpty())? prevLineOpenedRects.get(0) : null;
    }
    
    public void scanLineAddSegment(int y, int fromX, int toX) {
        if (y != currY) {
            scanEndLine(currY);
            y = currY;
            currX = 0;
            // throw new IllegalArgumentException("unsupported scanLineAddSegment() not in scan y,x order: expecting y:" + currY);
        } else {
            if (currX <= fromX) {
                currX = toX;
            } else {
                throw new IllegalArgumentException("unsupported scanLineAddSegment() not in scan y,x order: expecting x>" + currX);
            }
        }
        
        if (prevLineOpenedRects.isEmpty()) {
            // open first rect on line
            Rect rect = Rect.newPtToPt(fromX,  y, toX, y);
            resultRects.add(rect);
            currLineOpenedRects.add(rect);
        } else {
            //  |   |    |      |      |      |
            //  +...+    + .... +      +......+    previous line, curr Opened Rects
            //          /\
            //           |
            //         prevLineCurrOpenedRect

            while(prevLineCurrOpenedRect != null
                    && prevLineCurrOpenedRect.toX <= fromX) {
                //   |    |   |    |
                //   + .. +   +....+    previous line, curr Opened Rects
                //   /\
                //    |
                //  prevLineCurrOpenedRect
                //                   <  +.....+
                //                    fromX   toX
                // close previous rect
                prevLineCurrOpenedRect.toY = currY;
                // iterate on rects
                iterPrevLineNextRect();
            }

            assert prevLineCurrOpenedRect == null || prevLineCurrOpenedRect.toX > fromX;
            //        |        |      |    |
            //        + ...... +      +....+
            //                 \/
            //      fromX < r.toX 
            //      /\
            //       +.....+
            //     fromX   toX

            if (prevLineCurrOpenedRect == null || toX <= prevLineCurrOpenedRect.fromX) {
                // no rect, or rect not crossing segment (on right)
                // insert new rect
                Rect r = Rect.newPtToPt(fromX, y, toX, y);
                resultRects.add(r);
                currLineOpenedRects.add(r);
            } else {
                // rect crossing
                if (prevLineCurrOpenedRect.fromX == fromX && prevLineCurrOpenedRect.toX == toX) {
                    // same rect segment: => prologation of rect (still opened) 
                    //         |......|
                    //         +......+
                    currLineOpenedRects.add(prevLineCurrOpenedRect);
                    iterPrevLineNextRect();
                } else {
                    // crossing rect, but not same segment
                    //    |......|  or |.|     or  |....|  or  |...| 
                    //   +...+        +....+         +.+         +....+
                    // => close rect(and similar followings??), open new
                    while(prevLineCurrOpenedRect != null && prevLineCurrOpenedRect.fromX < toX) {
                        prevLineCurrOpenedRect.toY = currY;
                        iterPrevLineNextRect();
                    }
                    
                    Rect r = Rect.newPtToPt(fromX, y, toX, y);
                    resultRects.add(r);
                    currLineOpenedRects.add(r);
                }
            }
            
        }
    }

    private void iterPrevLineNextRect() {
        prevLineCurrOpenedRectIndex++;
        prevLineCurrOpenedRect = (prevLineCurrOpenedRectIndex < prevLineOpenedRects.size())? prevLineOpenedRects.get(prevLineCurrOpenedRectIndex) : null;
    }

    public void scanEndLine(int y) {
        // close remaining rects
        while(prevLineCurrOpenedRect != null) {
            prevLineCurrOpenedRect.toY = currY;
            iterPrevLineNextRect();
        }
        prevLineOpenedRects.clear();
        List<Rect> tmp = prevLineOpenedRects;
        this.prevLineOpenedRects = currLineOpenedRects;
        this.currLineOpenedRects = tmp;
        
        this.currY = y + 1;
        this.currX = -1;
    }

    public void scanDone() {
        // close all remaining rects
        for(Rect r : prevLineOpenedRects) { // ??
            r.toY = currY;
        }
        for(Rect r : currLineOpenedRects) {
            r.toY = currY;
        }
        this.prevLineCurrOpenedRectIndex = -1;
        this.prevLineCurrOpenedRect = null;
        this.currY = 0;
        this.prevLineOpenedRects.clear();
        this.currLineOpenedRects.clear();
    }
    
    // ------------------------------------------------------------------------
    
    public List<Rect> getResultRects() {
        return resultRects;
    }

    public List<Rect> getPrevLineOpenedRects() {
        return prevLineOpenedRects;
    }

    public Rect getPrevLineCurrOpenedRect() {
        return prevLineCurrOpenedRect;
    }

    
}

package fr.an.screencast.compressor.imgtool.rectdescr;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.mutable.MutableInt;

import fr.an.screencast.compressor.imgtool.glyph.GlyphIndexOrCode;
import fr.an.screencast.compressor.imgtool.glyph.GlyphMRUTable;
import fr.an.screencast.compressor.imgtool.glyph.GlyphMRUTable.GlyphMRUNode;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.BorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.ColumnsSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.FillRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.GlyphRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.HorizontalSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.LeftRightBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.LinesSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RectImgAboveRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RoundBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.TopBottomBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.VerticalSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.codec.RectImgDescrCodecConfig;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.IntsCRC32;
import fr.an.screencast.compressor.utils.Border;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.MutableDim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.QuadDirection;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.screencast.compressor.utils.Segment;

/**
 * helper method for detecting rectangular image descriptions
 *
 */
public final class RectImgDescrDetectorHelper {
    
    private static int DEFAULT_GLYPH_MAX_WIDTH = 50; // following glyph letters can be glued, example: "AVA", "//", "\\" 
    private static int DEFAULT_GLYPH_MAX_HEIGHT = 25;
    private static int DEFAULT_GLYPH_MAX_AREA = DEFAULT_GLYPH_MAX_WIDTH * DEFAULT_GLYPH_MAX_HEIGHT * 3 / 4;
    
    private final Dim dim;
    private int[] imgData;
    private RightDownSameCountsImg sameCountsImg;
    
    private GlyphMRUTable glyphMRUTable;

    private int maxAreaForGlyph = DEFAULT_GLYPH_MAX_AREA;
    private Dim maxDimForGlyph = new Dim(DEFAULT_GLYPH_MAX_WIDTH, DEFAULT_GLYPH_MAX_HEIGHT);

    
    // ------------------------------------------------------------------------
    
    public RectImgDescrDetectorHelper(Dim dim) {
        this.dim = dim;
        this.sameCountsImg = new RightDownSameCountsImg(dim);
        this.glyphMRUTable = new GlyphMRUTable(RectImgDescrCodecConfig.DEFAULT_GLYPHMRUTABLE_SIZE);
    }

    // ------------------------------------------------------------------------
    
    public void setImg(int[] imgData) {
        this.imgData = imgData;
        sameCountsImg.setComputeFrom(imgData);
    }
    
    public GlyphMRUTable getGlyphMRUTable() {
        return glyphMRUTable;
    }

    public Dim getMaxDimForGlyph() {
        return maxDimForGlyph;
    }


    public List<Rect> scanListLargestBorderRightThenDown(Rect rect, int retainMinRectW, int retainMinRectH) {
        List<Rect> res = new ArrayList<Rect>();
        
        final int W = dim.width;
        int x = rect.fromX;
        int idx = rect.fromY * W + rect.fromX;
        final int incrIdxY = W + rect.fromX - rect.toX;
        Pt pt = new Pt(rect.fromX, rect.fromY);
        // algorithm
        // scanning from y desc, then x left to right   
        // currOpenedBorders:  list of scanned  Rect currently "opened" : y < rect.y
        //      r0         r1         rk
        //      |   (     [   (       [   (
        //      [...(     [   (       [.. (
        //    
        // nextX: rec.toX to skip rect from r.fromX
        // x: 0 1 2 3 4 5 6 7 8 910 1 2 3 4 5 . 
        // => "." for unchanged: nextX[.]=.
        //    . 3 . . . . 8 . . . . . 14. . . .    
        List<Rect> currOpenedBorders = new ArrayList<Rect>();
        List<Rect> tmpBorders = new ArrayList<Rect>();
        int[] nextX = new int[rect.toX - rect.fromX];
        final int offset = - rect.fromX;
        for (int i = rect.fromX; i < rect.toX; i++) {
            nextX[offset+i] = i;
        }
        Rect maxWithinRect = new Rect(rect);
        
        for (int y = rect.fromY; y < rect.toY; y++,x = rect.fromX,idx+=incrIdxY) {
            pt.y = y;
            ImageRasterUtils.checkIdx(idx, x, y, W);
            
            // recompute nextX[x] for currently opened borders
            tmpBorders.clear();
            for(Rect r : currOpenedBorders) {
                if (y < r.toY) {
                    tmpBorders.add(r);
                    assert nextX[offset+r.fromX] == r.toX;
                } else {
                    nextX[offset+r.fromX] = r.fromX;
                }
            }
            List<Rect> swap = currOpenedBorders;
            currOpenedBorders = tmpBorders;
            tmpBorders = swap;
            tmpBorders.clear();
            
            for(x = rect.fromX; x < rect.toX; x++,idx++) {
                if (nextX[offset+x] != x) {
                    int prevX = x;
                    x = nextX[offset+x] - 1;
                    idx += x - prevX;
                    continue;
                }
                pt.x = x;
                // rect should not cross other currently opened rect => restrict maxWithinRect.toX
                maxWithinRect.toX = rect.toX;
                int endMaxToX = Math.min(rect.toX, x+sameCountsImg.getRightSameCount(y*W+x));
                for (int maxToX = x+1; maxToX < endMaxToX; maxToX++) {
                    if (nextX[offset+maxToX] != maxToX) {
                        maxWithinRect.toX = maxToX;
                        break;
                    }
                }

                Rect foundBorder = findLargestBorderRightThenDown(pt, maxWithinRect);
                if (foundBorder != null) {
                    if (foundBorder.getWidth() > retainMinRectW || foundBorder.getHeight() > retainMinRectH) {
                        res.add(foundBorder);
                    }
                    if (foundBorder.getHeight() > 1) {
                        currOpenedBorders.add(foundBorder);
                        nextX[offset+foundBorder.fromX] = foundBorder.toX;
                    } else {
                        nextX[offset+foundBorder.fromX] = foundBorder.fromX; 
                    }
                    int prevX = x;
                    x = foundBorder.toX - 1;
                    idx += x - prevX;
                }
            }
        }
        return res;
    }

    public LinesSplitRectImgDescr detectLineBreaksInScannedRightThenDownRects_noSplitColor(Rect rect, List<Rect> scannedRects) {
        List<Segment> splits = new ArrayList<Segment>();
        List<RectImgDescr> lines = new ArrayList<RectImgDescr>();
        int currFromY = scannedRects.get(0).fromY;
        int currToY = scannedRects.get(0).toY;
        List<Rect> currLineRects = new ArrayList<Rect>();
        for(Rect scannedRect : scannedRects) {
            if (scannedRect.fromY < currToY) {
                currFromY = Math.min(currFromY, scannedRect.fromY);
                currToY = Math.max(currToY, scannedRect.toY);
            } else { // if (scannedRect.fromY >= currToY)
                // line break
                Segment split = new Segment(currToY, currToY);
                splits.add(split);
                Rect lineRect = Rect.newPtToPt(rect.fromX, currFromY, rect.toX, currToY);
                RectImgAboveRectImgDescr lineElt = new RectImgAboveRectImgDescr(lineRect, null, 
                    currLineRects.toArray(new Rect[currLineRects.size()]));  
                lines.add(lineElt);
                currLineRects.clear();
                
                currFromY = scannedRect.fromY;
                currToY = scannedRect.toY;
            }
            currLineRects.add(scannedRect);
        }
        // flush current line
        if (!currLineRects.isEmpty()) {
            Rect lineRect = Rect.newPtToPt(rect.fromX, currFromY, rect.toX, currToY);
            RectImgDescr lineElt = createScannedRectsToImgDescr(lineRect, currLineRects, false);  
            lines.add(lineElt);
        }
        if (splits.isEmpty()) {
            return null;
        }
        return new LinesSplitRectImgDescr(rect, -1, splits, lines); 
    }
    
    public RectImgDescr createScannedRectsToImgDescr(Rect rect, List<Rect> ls, 
            boolean isScanRightThenDown) {
        if (ls != null && ls.size() == 1) {
            Rect r0 = ls.get(0);
            if (r0.equals(rect)) {
                // to inline in parent!
                return null;
            }
            FillRectImgDescr tmpres = detectExactFillRect(rect);
            if (tmpres != null) {
                return tmpres;
            }
        }
        if (isScanRightThenDown) {
            // detect if partition for consecutive line (LineSplit)
            // 
        } else {
            // detect if partition for consecutive column (ColumnSplit)
            
        }
        return new RectImgAboveRectImgDescr(rect, null, ls.toArray(new Rect[ls.size()]));  
    }

    public LinesSplitRectImgDescr detectLineSplitColorForScannedRects(Rect rect, List<Rect> rects) {
        int currToY = rect.fromY;
        for(Rect r : rects) {
            if (r.fromY == currToY) {
                currToY = r.toY;
            } else {
                return null;
            }
        }
        // determine uniform rects color => fill map color->{list of index of split}
        Map<Integer,List<Integer>> lsSplitIdxByColor = new HashMap<Integer,List<Integer>>();
        for(int i = 0; i < rects.size(); i++) {
            Integer color = detectExactFillColor(rect);
            if (color != null) {
                List<Integer> lsSplitIdx = lsSplitIdxByColor.get(color);
                if (lsSplitIdx == null) {
                    lsSplitIdx = new ArrayList<Integer>();
                    lsSplitIdxByColor.put(color, lsSplitIdx);
                }
                lsSplitIdx.add(i);
            }
        }
        // determine "optimal" split color: max splits count
        int splitColor = -1;
        List<Integer> lsSplitIdx = null;
        if (lsSplitIdxByColor.isEmpty()) {
            // no split color
        } else if (lsSplitIdxByColor.size() == 1) {
            // only 1 split color
            Map.Entry<Integer,List<Integer>> first = lsSplitIdxByColor.entrySet().iterator().next(); 
            splitColor = first.getKey();
            lsSplitIdx = first.getValue();
        } else {
            int maxCount = 0;
            for(Map.Entry<Integer,List<Integer>> e : lsSplitIdxByColor.entrySet()) {
                if (e.getValue().size() > maxCount) {
                    maxCount = e.getValue().size();
                    splitColor = e.getKey();
                    lsSplitIdx = e.getValue();
                }
            }
        }
        
        if (lsSplitIdx == null) {
            // 
        }
        // sublist by splits
        List<Segment> splitBorders = new ArrayList<Segment>();
        
        // LinesSplitRectImgDescr res = new LinesSplitRectImgDescr(rect, splitColor, splitBorders, );
        return null; //TODO
    }

    public ColumnsSplitRectImgDescr detectColumnSplitColorForScannedRects(Rect rect, List<Rect> ls) {
        return null; // TODO
    }

    public LinesSplitRectImgDescr detectLineBreaksInScannedRightThenDownRects(Rect rect, List<Rect> scannedRects) {
        LinesSplitRectImgDescr tmpres = detectLineBreaksInScannedRightThenDownRects_noSplitColor(rect, scannedRects);
        if (tmpres == null) {
            return null;
        }
        // TODO
        RectImgDescr[] lines = tmpres.getLines();
        for(RectImgDescr line : lines) {
            if (line instanceof RectImgAboveRectImgDescr) {
                
            }
        }
        return tmpres;
    }
    
    public List<Rect> pivotScannedRectsToDownThenRight(Rect rect, List<Rect> scannedRects) {
        final int colOffset = rect.getFromX();
        @SuppressWarnings("unchecked")
        List<Rect>[] col2list = new List[rect.getWidth()];
        for(Rect scannedRect : scannedRects) {
            int colIdx = scannedRect.fromX - colOffset;
            List<Rect> ls = col2list[colIdx];
            if (ls == null) {
                ls = new ArrayList<Rect>();
                col2list[colIdx] = ls;
            }
            ls.add(scannedRect);
        }
        // compute union (size then fill)
        int size = 0;
        for(List<Rect> ls : col2list) {
            if (ls != null) {
                size += ls.size();
            }
        }
        List<Rect> res = new ArrayList<Rect>(size);
        for(List<Rect> ls : col2list) {
            if (ls != null) {
                res.addAll(ls);
            }
        }
        return res;
    }
    
    public ColumnsSplitRectImgDescr detectColumnBreaksInScannedDownThenRightRects_noSplitColor(Rect rect, List<Rect> scannedRects) {
        List<Segment> splits = new ArrayList<Segment>();
        List<RectImgDescr> columns = new ArrayList<RectImgDescr>();
        int currFromX = scannedRects.get(0).fromX;
        int currToX = scannedRects.get(0).toX;
        List<Rect> currColumnRects = new ArrayList<Rect>();
        for(Rect scannedRect : scannedRects) {
            if (scannedRect.fromX < currToX) {
                currFromX = Math.min(currFromX, scannedRect.fromX);
                currToX = Math.max(currToX, scannedRect.toX);
            } else { // if (scannedRect.fromX >= currToX)
                // column break
                Segment split = new Segment(currToX, currToX);
                splits.add(split);
                Rect columnRect = Rect.newPtToPt(currFromX, rect.fromY, currToX, rect.toY);
                RectImgAboveRectImgDescr columnElt = new RectImgAboveRectImgDescr(columnRect, null, 
                    currColumnRects.toArray(new Rect[currColumnRects.size()]));  
                columns.add(columnElt);
                currColumnRects.clear();
                
                currFromX = scannedRect.fromX;
                currToX = scannedRect.toX;
            }
            currColumnRects.add(scannedRect);
        }
        // flush current column
        if (!currColumnRects.isEmpty()) {
            Rect columnRect = Rect.newPtToPt(currFromX, rect.fromY, currToX, rect.toY);
            RectImgDescr columnElt = createScannedRectsToImgDescr(columnRect, currColumnRects, true);  
            columns.add(columnElt);
        }
        if (splits.isEmpty()) {
            return null;
        }
        return new ColumnsSplitRectImgDescr(rect, -1, splits, columns); 
    }

    public ColumnsSplitRectImgDescr detectColumnBreaksInScannedDownThenRightRects(Rect rect, List<Rect> scannedRects) {
        ColumnsSplitRectImgDescr tmpres = detectColumnBreaksInScannedDownThenRightRects_noSplitColor(rect, scannedRects);
        if (tmpres == null) {
            return null;
        }
        // TODO
        return tmpres;
    }

    private FillRectImgDescr detectExactFillRectSameColor(Rect splitRect, MutableInt bgColor) {
        FillRectImgDescr fillRect = detectExactFillRect(splitRect);
        if (fillRect != null) {
            if (bgColor.intValue() == -1) {
                bgColor.setValue(fillRect.getColor());
            }
            if (fillRect.getColor() != bgColor.intValue()) {
                // not same split color => reduce to 0 split size!
                fillRect = null;
            }
        }
        return fillRect;
    }

    public Rect findLargestBorderRightThenDown(Pt pt, Rect maxWithinRect) {
        final int W = dim.width;
        final int idxUpLeft = pt.y * W + pt.x;
        ImageRasterUtils.checkIdx(idxUpLeft, pt.x, pt.y, W);
        final int maxToX = Math.min(maxWithinRect.toX, pt.x + sameCountsImg.getRightSameCount(idxUpLeft));
        final int maxToY0 = Math.min(maxWithinRect.toY, pt.y + sameCountsImg.getDownSameCount(idxUpLeft));
        if (maxToY0 > pt.y) {
            for (int toX = maxToX; toX >= pt.x; toX--) {
                Rect tmpres = findLargestBorderDown(idxUpLeft, pt, toX, maxToY0);
                if (tmpres != null) {
                    return tmpres;
                }
            }
        }
        return null;
    }
    
    public Rect findLargestBorderDown(int idxUpLeft, Pt pt, int toX, int maxToY0) {
        int idxUpRight = idxUpLeft + toX - pt.x - 1; 
        MutableDim tmpDim = new MutableDim(toX - pt.x, 0);
        final int maxToY = Math.min(maxToY0, pt.y + sameCountsImg.getDownSameCount(idxUpRight));
        tmpDim.height = maxToY - pt.y;
        for(int y = maxToY-1; y >= pt.y; y--,tmpDim.height--) {
            BorderRectImgDescr tmpres = detectBorder1AtUL(pt, tmpDim);
            if (tmpres != null) {
                return tmpres.getRect();
            }
        }
        return null;
    }

    public Integer detectExactFillColor(Rect rect) {
        final int W = dim.width;
        final int rectWidth = rect.getWidth();
        int idx = rect.fromY*W+rect.fromX;
        final int color = imgData[idx];
        int h = sameCountsImg.getDownSameCount(idx);
        if (h < rect.getHeight()) {
            return null;
        }
        for(int y  = rect.fromY; y < rect.toY; y++,idx+=W) {
            int w = sameCountsImg.getRightSameCount(idx);
            if (w < rectWidth) {
                return null;
            }
        }
        return color;
    }
    
    public FillRectImgDescr detectExactFillRect(Rect rect) {
        Integer color = detectExactFillColor(rect);
        if (color == null) {
            return null;
        }
        // OK found fill rect
        return new FillRectImgDescr(new Rect(rect), color);
    }
    
    public Rect detectDilateBorder(Rect rect, Dim maxDim, Rect maxWithinRect) {
        final int W = dim.width;
        int fromX = rect.fromX, fromY = rect.fromY, toX = rect.toX, toY = rect.toY;
        int idxUpLeft = rect.fromY * W + rect.fromX; 
        int idxUpRight = rect.fromY * W + rect.toX; 
        int idxDownLeft = rect.toY * W + rect.fromX; 
        int rectWidth = rect.getWidth();
        int rectHeight = rect.getHeight();
        boolean reeval = true, reevalLeft = true, reevalRight = true, reevalUp = true, reevalDown = true;
        int prev;
        
        for(QuadDirection dir = QuadDirection.RIGHT; reeval; dir = dir.nextClockwise()) {
            
            ImageRasterUtils.checkIdx(idxUpLeft, fromX, fromY, W);
            ImageRasterUtils.checkIdx(idxUpRight, toX, fromY, W);
            ImageRasterUtils.checkIdx(idxDownLeft, fromX, toY, W);
            if (fromX + rectWidth != toX) {
                throw new AssertionError();
            }
            if (fromY + rectHeight != toY) {
                throw new AssertionError();
            }
            
            switch(dir) {
            case RIGHT:
                if (! reevalRight) {
                    continue;
                }
                prev = fromX;
                while(toX < dim.width && sameCountsImg.getDownSameCount(idxUpRight) < rectHeight) {
                    toX++;
                    idxUpRight++;
                    rectWidth++;
                    if (rectWidth > maxDim.width) {
                        return null;
                    }
                    if (toX >= maxWithinRect.toX) {
                        return null;
                    }                    
                }
                reevalRight = false;
                if (prev != fromX) {
                    reeval = reevalUp = reevalDown = true;
                } else {
                    reeval = reevalUp || reevalDown || reevalLeft;
                }
                break;
            case UP:
                if (! reevalUp) {
                    continue;
                }
                prev = fromY;
                while(fromY >= 0 && sameCountsImg.getRightSameCount(idxUpLeft) < rectWidth) {
                    fromY--;
                    idxUpLeft-=W;
                    idxUpRight-=W;
                    rectHeight++;
                    if (rectHeight > maxDim.height) {
                        return null;
                    }
                    if (fromY < maxWithinRect.fromY) {
                        return null;
                    }                    
                }
                reevalUp = false; 
                if (prev != fromY) {
                    reeval = reevalLeft = reevalRight = true;
                } else {
                    reeval = reevalLeft || reevalRight || reevalDown;
                }
                break;
            case LEFT:
                if (! reevalLeft) {
                    continue;
                }
                prev = fromX;
                while(fromX >= 0 && sameCountsImg.getDownSameCount(idxUpLeft) < rectHeight) {
                    fromX--;
                    idxUpLeft--;
                    idxDownLeft--;
                    rectWidth++;
                    if (rectWidth > maxDim.width) {
                        return null;
                    }
                    if (fromX < maxWithinRect.fromX) {
                        return null;
                    }                    
                }
                reevalLeft = false;
                if (prev != fromX) {
                    reeval = reevalUp = reevalDown = true;
                } else {
                    reeval = reevalUp || reevalDown || reevalRight;
                }
                break;
            case DOWN:
                if (! reevalDown) {
                    continue;
                }
                prev = toY;
                while(toY < dim.height && sameCountsImg.getRightSameCount(idxDownLeft) < rectWidth) {
                    toY++;
                    idxDownLeft+=W;
                    rectHeight++;
                    if (rectHeight > maxDim.height) {
                        return null;
                    }
                    if (toY >= maxWithinRect.toY) {
                        return null;
                    }                    
                }
                reevalDown = false;
                if (prev != toY) {
                    reeval = reevalLeft = reevalRight = true;
                } else {
                    reeval = reevalLeft || reevalRight || reevalUp;
                }
                break;
            }
            
            if (reeval != (reevalLeft || reevalRight || reevalUp || reevalDown)) {
                throw new RuntimeException();
            }
        }

        // assert same color on border
        // int color = imgData[fromY * W + fromX];
        
        return Rect.newPtDim(fromX, fromY, rectWidth, rectHeight);
    }
    
    
    public BorderRectImgDescr detectBorder1AtUL(Pt pt, MutableDim rectDim) {
        //
        //    x,y --                       -- x+w
        //           \                    /
        //        pt0 +------------------+ pt1]
        //            |                  |
        //            |                  |
        //        pt2 +------------------+ pt3]

        final int W = dim.width;
        int idx = pt.y*W+pt.x;
        final int borderColor = imgData[idx];
        
        // Pt1
        final int w_01 = sameCountsImg.getRightSameCount(idx);
        if (w_01 < rectDim.width) {
            return null;
        }
        final int idxPt1 = idx + rectDim.width-1;
        
        // Pt2
        final int h_02 = sameCountsImg.getDownSameCount(idx);
        if (h_02 < rectDim.height) {
            return null;
        }
        final int idxPt2 = idx + (rectDim.height-1)*W; // -1 for pt above
                
        // Pt3
        final int h_13 = sameCountsImg.getDownSameCount(idxPt1);
        if (h_13 < rectDim.height) {
            return null;
        }
        final int idxPt3 = idxPt1 + (rectDim.height-1)*W; // -1 for pt above
        
        // check that diagram is symmetric:  down-then-right == right-then-down        
        final int w_23 = sameCountsImg.getRightSameCount(idxPt2);
        if (w_23 < rectDim.width) {
            return null;
        }
        final int checkIdxPt3 = idxPt2 + (rectDim.width-1); // -1 for pt left
        assert checkIdxPt3 == idxPt3;

        // OK, checked constraints.. got a round rect (borderThick 1)
        // should repeat recursively for inside rect to get borderThick=2, 3 ...
        Border border = new Border(1, 1, 1, 1); 
        
        Rect rect = Rect.newPtDim(pt, rectDim.width, rectDim.height);
        return new BorderRectImgDescr(rect, borderColor, border, null);
    }
    
    /**
     * given uniform border rect(outside rect), compute inner border sizes
     * <PRE>
     *       +-------------------+
     *       |        |          |
     *       |       \/          |
     *       |   +----------+    |
     *       |-->|          |<---|
     *       |   +----------+    |
     *       |       /\          |
     *       |        |          |
     *       +-------------------+
     * </PRE>
     * @return BorderRectImgDescr or FillRectImgDescr when fully uniform color 
     */
    public RectImgDescr innerBorderImgDescrAtBorder(Rect rect) {
        assert null != detectBorder1AtUL(rect.getFromPt(), new MutableDim(rect.getDim()));
        final int W = dim.width;
        final int idxLeftUp = rect.fromY*W + rect.fromX;
        final int idxRightUp = rect.fromY*W + rect.toX;
        final int borderColor = imgData[idxLeftUp];
        final int rectWidth = rect.getWidth(), rectHeight = rect.getHeight();
        assert sameCountsImg.getRightSameCount(idxLeftUp) >= rectWidth; 
        
        // scan left border (left to right)
        int idx = idxLeftUp;
        for (; idx < idxRightUp; idx++) {
            if (sameCountsImg.getDownSameCount(idx) < rectHeight) {
                break;
            }
        }
        if (idx == idxRightUp) {
            // uniform fill
            return new FillRectImgDescr(rect, borderColor);
        }
        final int borderLeft = idx - idxLeftUp;
        
        // scan right border (right to left)
        idx = idxRightUp - 1;
        for (; idx > idxLeftUp; idx--) {
            if (sameCountsImg.getDownSameCount(idx) < rectHeight) {
                idx++;
                break;
            }
        }
        final int borderRight = idxRightUp - idx;
        
        // scan top border (descending)
        final int idxLeftDown = idxLeftUp + rectHeight * W; 
        idx = idxLeftUp + W;
        int borderTop = 1;
        for(; idx < idxLeftDown; idx+=W,borderTop++) {
            if (sameCountsImg.getRightSameCount(idx) < rectWidth) {
                break;
            }
        }

        // scan bottom border (ascending)
        idx = idxLeftDown - W;
        int borderBottom = 1;
        for (; idx > idxLeftUp; idx-=W,borderBottom++) {
            if (sameCountsImg.getRightSameCount(idx) < rectWidth) {
                borderBottom--;
                break;
            }
        }
        
        Border border = new Border(borderLeft, borderRight, borderTop, borderBottom); 
        return new BorderRectImgDescr(rect, borderColor, border, null);
    }
    
    
    
    public RoundBorderRectImgDescr detectRoundBorderStartAtUL(Pt pt) {
        // TODO
        RoundBorderRectImgDescr res = _test_detectRoundBorderStartAtUL(pt);
        return res;
    }
    
    public RoundBorderRectImgDescr _test_detectRoundBorderStartAtUL(Pt pt) {
        final int W = dim.width, H = dim.height;
        final int idxFromPt = pt.y * W + pt.x; 
        int idx = idxFromPt;
        int topLeftCornerX = 0;
        for (; topLeftCornerX < W && sameCountsImg.getRightSameCount(idx) < 10;topLeftCornerX++,idx++) {            
        }
        if (topLeftCornerX == W) {
            return null;
        }
        final int bgColor = imgData[idx+topLeftCornerX];
        
        int topLeftCornerY = 0;
        for (; topLeftCornerY < H && (imgData[idx] != bgColor || sameCountsImg.getDownSameCount(idx) < 10);topLeftCornerY++,idx+=W) {            
        }
        if (topLeftCornerY == H) {
            return null;
        }
        MutableDim rectDim = new MutableDim(topLeftCornerX + sameCountsImg.getRightSameCount(idx) + topLeftCornerX,
                topLeftCornerY + sameCountsImg.getDownSameCount(idx) + topLeftCornerY);
        MutableDim topCornerDim = new MutableDim(topLeftCornerX, topLeftCornerY);
        MutableDim bottomCornerDim = new MutableDim(topLeftCornerX, topLeftCornerY);
        RoundBorderRectImgDescr res = detectRoundBorderStartAtULWithCorners(pt, rectDim, false, topCornerDim, bottomCornerDim, null);
        if (res != null) {
            return res;
        }

        bottomCornerDim.set(0, 0);
        res = detectRoundBorderStartAtULWithCorners(pt, rectDim, false, topCornerDim, bottomCornerDim, null);
        if (res != null) {
            return res;
        }

        return null;
    }
    
    public RoundBorderRectImgDescr detectRoundBorderStartAtULWithCorners(Pt pt, 
            MutableDim rectDim,
            boolean checkCornerColor, // <== problem with anti-aliasing!
            MutableDim topCornerDim, MutableDim bottomCornerDim,
            StringBuilder optReason) {
        //
        //    x,y --     x+rw               x+w-rw   -- x+w
        //           \   \/                   \/    /
        //         pt ....+-------------------+....  p3]
        //            ... [p1               p2( ...
        //            ..                         ..
        //            .                           .
        //         p4 +                           + p5]
        //            |                           |
        //            |                           |
        //         p6 +                           + p7]
        //            ..  p9                 p10 ..
        //         p8 ...+---------------------+... p11]
        
        final int W = dim.width;
        int x = pt.x, y = pt.y, idx = y*W+x;
        final int cornerBgColor = imgData[idx];
        final int borderColor = imgData[idx + rectDim.width/2];
        
        // apply checks in y,x scan order: Pt0, Pt1...Pt11
        // first line: pt, p1, pt2, pt3
        if (checkCornerColor) {
            final int w_01 = sameCountsImg.getRightSameCount(idx);
            if (topCornerDim.width != w_01) {
                return null;
            }
        }
        
        final int w_12 = sameCountsImg.getRightSameCount(idx+topCornerDim.width);
        final int idxPt2 = idx + rectDim.width - topCornerDim.width - topCornerDim.width;
        if (checkCornerColor && imgData[idxPt2] != cornerBgColor) {
            return null;
        }
        if (checkCornerColor) {
            final int w_23 = sameCountsImg.getRightSameCount(idxPt2);
            if (topCornerDim.width != w_23) {
                return null;
            }
        }
        final int idxPt3 = idx + rectDim.width - 1; // -1 for point at left of color change

        // second line: pt4, pt5
        if (checkCornerColor) {
            final int h_04 = sameCountsImg.getDownSameCount(idx);
            if (topCornerDim.height != h_04) {
                return null;
            }
        }
        final int idxPt4 = idx + topCornerDim.height*W;
        if (imgData[idxPt4] != borderColor) {
            return null;
        }
        final int cornerH_35 = sameCountsImg.getDownSameCount(idxPt3);
        if (topCornerDim.height != cornerH_35) {
            return null;
        }
        final int idxPt5 = idxPt3 + cornerH_35*W;
        if (imgData[idxPt5] != borderColor) {
            return null;
        }

        // line pt6, pt7
        final int h_46 = sameCountsImg.getDownSameCount(idxPt4);
        final int idxPt6 = idxPt4 + h_46*W;
        if (checkCornerColor && imgData[idxPt6] != cornerBgColor) {
            return null;
        }
        
        final int h_57 = sameCountsImg.getDownSameCount(idxPt5);
        final int idxPt7 = idxPt5 + h_57*W;
        if (imgData[idxPt7] != cornerBgColor) {
            return null;
        }
        if (h_46 != h_57) {
            return null;
        }
        
        // line pt8, pt9, pt10, pt11
        final int h_68 = sameCountsImg.getDownSameCount(idxPt6);
        if (h_68 > bottomCornerDim.height) {
            return null;
        }
        final int idxPt8 = idxPt6 + bottomCornerDim.height*W - W; // -W for point above color change
        if (checkCornerColor && imgData[idxPt8] != cornerBgColor) {
            return null;
        }
        
        int w_89 = sameCountsImg.getRightSameCount(idxPt8);
        if (w_89 != bottomCornerDim.width) {
            return null;
        }
        final int idxPt9 = idxPt8 + w_89;        
        if (imgData[idxPt9] != borderColor) {
            return null;
        }
        
        int w_910 = sameCountsImg.getRightSameCount(idxPt8);
        int idxPt10 = idxPt9 + w_910; 
        if (checkCornerColor && imgData[idxPt10] != cornerBgColor) {
            return null;
        }
        
        int w_1011 = sameCountsImg.getRightSameCount(idxPt10);
        if (w_1011 < bottomCornerDim.width) {
            return null;
        }
        int idxPt11 = idxPt10 + bottomCornerDim.width - 1;
        if (checkCornerColor && imgData[idxPt11] != cornerBgColor) {
            return null;
        }
        
        // check that diagram is symmetric:  down-then-right == right-then-down
        final int h_711 = sameCountsImg.getDownSameCount(idxPt7);
        if (h_711 > bottomCornerDim.height) {
            return null;
        }
        final int check_idxPt11 = idxPt7 + bottomCornerDim.height*W - W; // -W for point above color change

        if (check_idxPt11 != idxPt11) {
            return null;
        }
        
        // OK, checked constraints.. got a round rect (thick 1)
        // should repeat recursively for inside rect to get thick=2, 3 ...
        int borderThick = 1;
        
        final int width = topCornerDim.width + w_12 + topCornerDim.width;
            // also == bottomCornerDim.width + w_910 + bottomCornerDim.width;
        final int height = topCornerDim.height + h_46 + bottomCornerDim.height; 
        Rect rect = Rect.newPtDim(pt, width, height);
        return new RoundBorderRectImgDescr(rect, cornerBgColor, borderColor, borderThick, new Dim(topCornerDim), new Dim(bottomCornerDim), null);
    }
    

    public RectImgDescr detectVertSplit(Rect rect) {
        // scan vertical full lines, and sort by colors
        Map<Integer, List<Segment>> colorsToSplits = detectVerticalBorderSplits(rect);

        if (colorsToSplits.isEmpty()) {
            return null;
        }

        if (colorsToSplits.size() == 1) {
            Entry<Integer, List<Segment>> e = colorsToSplits.entrySet().iterator().next();
            int splitColor = e.getKey(); 
            List<Segment> splitBorders = colorsToSplits.values().iterator().next();
            if (splitBorders.size() == 1) {
                // only 1 split.. use simpler class instead of ColumnsSplitRectImgDescr
                Segment splitBorder = splitBorders.get(0);
                if (splitBorder.from == rect.fromX && splitBorder.to == rect.toX) {
                    return new FillRectImgDescr(rect, splitColor); // should not occur
                } else if (splitBorder.from == rect.fromX) {
                    int leftBorder = splitBorder.to - rect.fromX;
                    return new LeftRightBorderRectImgDescr(rect, splitColor, leftBorder, 0, null);
                } else if (splitBorder.to == rect.toX) {
                    int rightBorder = rect.toX - splitBorder.from;
                    return new LeftRightBorderRectImgDescr(rect, splitColor, 0, rightBorder, null);
                } else {
                    return new VerticalSplitRectImgDescr(rect, null, splitBorder, splitColor, null);
                }
            } else if (splitBorders.size() == 2 
                    && splitBorders.get(0).from == rect.fromX && splitBorders.get(1).to == rect.toX) {
                int leftBorder = splitBorders.get(0).to - rect.fromX;
                int rightBorder = rect.toX - splitBorders.get(1).from;
                return new LeftRightBorderRectImgDescr(rect, splitColor, leftBorder, rightBorder, null);
            }
        }
        
        // get color splits with highest split count (/ larger border?)  
        int maxCount = 0;
        int splitColor = 0;
        List<Segment> splitBorders = null;
        for(Map.Entry<Integer,List<Segment>> e : colorsToSplits.entrySet()) {
            List<Segment> ls = e.getValue();
            if (maxCount < ls.size()) {
                maxCount = ls.size();
                splitBorders = ls;
                splitColor = e.getKey();
            }
        }
        
        List<RectImgDescr> columns = null;
            // decompose columns with other colors vertical borders ...
            // TODO
        return new ColumnsSplitRectImgDescr(rect, splitColor, splitBorders, columns);
    }
    
    public Map<Integer, List<Segment>> detectVerticalBorderSplits(Rect rect) {
        Map<Integer,List<Segment>> colorsToSplits = new HashMap<Integer,List<Segment>>();
        final int rectH = rect.getHeight();
        int idx = rect.fromY * dim.width + rect.fromX;
        for(int x = rect.fromX; x < rect.toX; x++,idx++) {
            int lineH = sameCountsImg.getDownSameCount(idx);
            if (lineH >= rectH) {
                int color = imgData[idx];
                // find following lines with same height & color
                int fromX = x;
                int wSameColor = sameCountsImg.getRightSameCount(idx);
                if (wSameColor > 1) {
                    final int maxToX = Math.min(rect.toX, x+wSameColor); 
                    x++; idx++;
                    while(x < maxToX && sameCountsImg.getDownSameCount(idx) >= rectH) {
                        x++; idx++;
                    }
                } else {
                    x++; idx++;
                }
                List<Segment> splits = colorsToSplits.get(color);
                if (splits == null) {
                    splits = new ArrayList<Segment>();
                    colorsToSplits.put(color, splits);
                }
                splits.add(new Segment(fromX, x));
                x--; idx--;
            }
        }
        return colorsToSplits;
    }

    public RectImgDescr detectHorizontalSplit(Rect rect) {
        // scan horizontal full lines, and sort by colors
        Map<Integer, List<Segment>> colorsToSplits = detectHorizontalBorderSplits(rect);

        if (colorsToSplits.isEmpty()) {
            return null;
        }

        if (colorsToSplits.size() == 1) {
            Entry<Integer, List<Segment>> e = colorsToSplits.entrySet().iterator().next();
            int splitColor = e.getKey(); 
            List<Segment> splitBorders = e.getValue();
            if (splitBorders.size() == 1) {
                // only 1 split.. use simpler class instead of ColumnsSplitRectImgDescr
                Segment splitBorder = splitBorders.get(0);
                if (splitBorder.from == rect.fromY && splitBorder.to == rect.toY) {
                    return new FillRectImgDescr(rect, splitColor); // should not occur
                } else if (splitBorder.from == rect.fromY) {
                    int topBorder = splitBorder.to - splitBorder.from;
                    return new TopBottomBorderRectImgDescr(rect, splitColor, topBorder, 0, null);
                } else if (splitBorder.to == rect.toY) {
                    int bottomBorder = splitBorder.to - splitBorder.from;
                    return new TopBottomBorderRectImgDescr(rect, splitColor, 0, bottomBorder, null);
                } else {
                    return new HorizontalSplitRectImgDescr(rect, null, splitBorder, splitColor, null);
                }
            } else if (splitBorders.size() == 2 && 
                    splitBorders.get(0).from == rect.fromY && splitBorders.get(1).to == rect.toY) {
                int topBorder = splitBorders.get(0).to - rect.fromY;
                int bottomBorder = rect.toY - splitBorders.get(1).from;
                return new TopBottomBorderRectImgDescr(rect, splitColor, topBorder, bottomBorder, null);
            }
        }
        
        // get color splits with highest split count (/ larger border?)  
        int maxCount = 0;
        int splitColor = 0;
        List<Segment> splitBorders = null;
        for(Map.Entry<Integer,List<Segment>> e : colorsToSplits.entrySet()) {
            List<Segment> ls = e.getValue();
            if (maxCount < ls.size()) {
                maxCount = ls.size();
                splitBorders = ls;
                splitColor = e.getKey();
            }
        }
        
        List<RectImgDescr> rows = null; // new ArrayList<RectImgDescription>();
        if (colorsToSplits.size() > 1) {
            // decompose columns with other colors vertical borders ...
            // TODO
        }
        return new LinesSplitRectImgDescr(rect, splitColor, splitBorders, rows);
    }
    
    public Map<Integer, List<Segment>> detectHorizontalBorderSplits(Rect rect) {
        Map<Integer,List<Segment>> colorsToSplits = new HashMap<Integer,List<Segment>>();
        final int rectW = rect.getWidth();
        final int W = dim.getWidth();
        int idx = rect.fromY * dim.width + rect.fromX;
        for(int y = rect.fromY; y < rect.toY; y++,idx+=W) {
            int lineW = sameCountsImg.getRightSameCount(idx);
            if (lineW >= rectW) {
                int color = imgData[idx];
                // find following lines with same height & color
                int fromY = y;
                int hSameColor = sameCountsImg.getDownSameCount(idx);
                if (hSameColor > 1) {
                    final int maxToY = Math.min(rect.toY, y+hSameColor);
                    y++; idx+=W;
                    while(y < maxToY && sameCountsImg.getRightSameCount(idx) >= rectW) {
                        y++; idx+=W;
                    }
                } else {
                    y++; idx+=W;
                }
                List<Segment> splits = colorsToSplits.get(color);
                if (splits == null) {
                    splits = new ArrayList<Segment>();
                    colorsToSplits.put(color, splits);
                }
                splits.add(new Segment(fromY, y));
                y--; idx-=W;
            }
        }
        return colorsToSplits;
    }

    public GlyphRectImgDescr detectGlyph(Rect rect) {
        int crc = IntsCRC32.crc32ImgRect(dim, imgData, rect);
        GlyphMRUNode glyphNode = glyphMRUTable.findGlyphByCrc(rect.getDim(), crc);
        boolean isNew = (glyphNode == null);
        if (glyphNode == null) {
            glyphNode = glyphMRUTable.addGlyph(dim, imgData, rect, crc);
        } else {
            glyphMRUTable.incrUseCount(glyphNode);
        }
        GlyphIndexOrCode glyphIndexOrCode = glyphNode.getIndexOrCode();
        return new GlyphRectImgDescr(rect, crc, glyphNode.getData(), glyphMRUTable, glyphIndexOrCode, isNew);
    }

    public boolean allowDetectGlyphInRect(Dim dim) {
        return allowDetectGlyphInRectDim(dim.width, dim.height);
    }

    public boolean allowDetectGlyphInRect(Rect rect) {
        return allowDetectGlyphInRectDim(rect.getWidth(), rect.getHeight());
    }

    public boolean allowDetectGlyphInRectDim(int width, int height) {
        return width < maxDimForGlyph.width
                && height < maxDimForGlyph.height
                && (width*height) < maxAreaForGlyph
                ;
    }
    
}

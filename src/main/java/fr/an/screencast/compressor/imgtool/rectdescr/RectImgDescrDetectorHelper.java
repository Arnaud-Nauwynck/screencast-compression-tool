package fr.an.screencast.compressor.imgtool.rectdescr;

import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.BorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.FillRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RoundBorderRectImgDescr;
import fr.an.screencast.compressor.utils.Border;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.MutableDim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;

/**
 * helper method for detecting rectangular image descriptions
 *
 */
public final class RectImgDescrDetectorHelper {

    private final Dim dim;
    private int[] imgData;
    private RightDownSameCountsImg sameCountsImg;
    
    // ------------------------------------------------------------------------
    
    public RectImgDescrDetectorHelper(Dim dim) {
        this.dim = dim;
        this.sameCountsImg = new RightDownSameCountsImg(dim);
    }

    // ------------------------------------------------------------------------
    
    public void setImg(int[] imgData) {
        this.imgData = imgData;
        sameCountsImg.setComputeFrom(imgData);
    }

    
    public FillRectImgDescr detectExactFillRect(Rect rect) {
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
        // OK found fill rect
        return new FillRectImgDescr(new Rect(rect), color);
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
        final int idxPt1 = idx + rectDim.width;
        
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
    
    
    public RoundBorderRectImgDescr detectRoundBorderStartAtUL(Pt pt) {
        // TODO
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
}

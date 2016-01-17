package fr.an.screencast.compressor.imgtool.rectdescr.ast;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.an.screencast.compressor.imgtool.glyph.GlyphIndexOrCode;
import fr.an.screencast.compressor.imgtool.glyph.GlyphMRUTable;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.Border;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.screencast.compressor.utils.Segment;

/**
 * 
 */
public class RectImgDescriptionAST {

    /**
     * abstract root class of AST for describing a rectangular area of an image 
     */
    public static abstract class RectImgDescription implements Serializable {

        /** */
        private static final long serialVersionUID = 1L;

        protected RectImgDescription parent;
        
        protected Rect rect;
        
        public RectImgDescription() {
        }
        
        public RectImgDescription(Rect rect) {
            this.rect = rect;
        }
        
        public abstract void accept(RectImgDescrVisitor visitor);

        public abstract <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param);

        public Rect getRect() {
            return rect;
        }

        public static Rect[] arrayToRectArray(RectImgDescription[] src) {
            Rect[] res = new Rect[src.length];
            for(int i = 0; i < src.length; i++) {
                res[i] = src[i].getRect();
            }
            return res;
        }

        public int getWidth() {
            return rect.getWidth();
        }

        public int getHeight() {
            return rect.getHeight();
        }

        public Dim getDim() {
            return rect.getDim();
        }

        public abstract int getChildCount();
        public abstract RectImgDescription getChild(int child);
        public abstract int getExtraPartCount();
        public abstract Rect getExtraPartRect(int part);
        
        @Override
        public String toString() {
            return "RectImgDescr[rect=" + rect + "]";
        }
    }


    // ------------------------------------------------------------------------

    /**
     * 
     */
    public static class RootRectImgDescr extends RectImgDescription {

        /** */
        private static final long serialVersionUID = 1L;
        
        private Dim topLevelDim;
        
        private RectImgDescription target;
        
        public RootRectImgDescr(Dim dim, RectImgDescription target) {
            this.topLevelDim = dim;
            this.target = target;
        }

        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseRoot(this);
        }

        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseRoot(this, param);
        }

        public int getChildCount() {
            return 1;
        }
        public RectImgDescription getChild(int child) {
            assert child == 0;
            return target;
        }
        public int getExtraPartCount() {
            return 0;
        }
        public Rect getExtraPartRect(int part) {
            throw new IllegalStateException();
        }

        public Dim getTopLevelDim() {
            return topLevelDim;
        }

        public RectImgDescription getTarget() {
            return target;
        }

        public void setTarget(RectImgDescription target) {
            this.target = target;
        }

    }
    
    
    // ------------------------------------------------------------------------

    /**
     * Proxy design-pattern on RectImgDescription AST ... to use temporary mutable node during analysis
     */
    public static class AnalysisProxyRectImgDescr extends RectImgDescription {

        /** */
        private static final long serialVersionUID = 1L;
        
        private RectImgDescription target;
        
        public AnalysisProxyRectImgDescr(RectImgDescription target) {
            this.target = target;
        }

        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseAnalysisProxy(this);
        }

        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseAnalysisProxy(this, param);
        }

        public int getChildCount() {
            return 1;
        }
        public RectImgDescription getChild(int child) {
            assert child == 0;
            return target;
        }
        public int getExtraPartCount() {
            return 0;
        }
        public Rect getExtraPartRect(int part) {
            throw new IllegalStateException();
        }

        public RectImgDescription getTarget() {
            return target;
        }

        public void setTarget(RectImgDescription target) {
            this.target = target;
        }
        
    }
    
    
    // ------------------------------------------------------------------------

    public static class FillRectImgDescr extends RectImgDescription {

        /** */
        private static final long serialVersionUID = 1L;

        private int color;
        
        public FillRectImgDescr(Rect rect) {
            super(rect);
        }

        public FillRectImgDescr(Rect rect, int color) {
            super(rect);
            this.color = color;
        }

        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseFill(this);
        }

        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseFill(this, param);
        }

        public int getChildCount() {
            return 0;
        }
        public RectImgDescription getChild(int child) {
            throw new IllegalStateException();
        }
        public int getExtraPartCount() {
            return 0;
        }
        public Rect getExtraPartRect(int part) {
            throw new IllegalStateException();
        }
        
        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        @Override
        public String toString() {
            return "FillRectImgDescr [rect=" + rect + ", color=" + color + "]";
        }
        
    }

    // ------------------------------------------------------------------------

    /**
     * descr for rectangular img wrapped with corner(round?) border 
     * <PRE>
     * +-----------------------+
     * |0  |       1       | 2 |
     * |---+---------------+---|
     * |3|        inside     |4|
     * |--+-----------------+--|
     * |5 |      6          | 7|
     * +-----------------------+
     * </PRE>
     * 
     */
    public static class RoundBorderRectImgDescr extends RectImgDescription {

        /** */
        private static final long serialVersionUID = 1L;

        private int cornerBackgroundColor;
        private int borderColor;
        
        private int borderThick;
        
        private Dim topCornerDim;
        private Dim bottomCornerDim;
        
        private RectImgDescription inside;
        
        public RoundBorderRectImgDescr(Rect rect) {
            super(rect);
        }
        
        public RoundBorderRectImgDescr(Rect rect, int cornerBackgroundColor, int borderColor, int borderThick, Dim topCornerDim, Dim bottomCornerDim,
                RectImgDescription insideRect) {
            super(rect);
            this.cornerBackgroundColor = cornerBackgroundColor;
            this.borderColor = borderColor;
            this.borderThick = borderThick;
            this.topCornerDim = topCornerDim;
            this.bottomCornerDim = bottomCornerDim;
            this.inside = insideRect;
        }

        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseRoundBorder(this);
        }

        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseRoundBorder(this, param);
        }

        public int getChildCount() {
            return 1;
        }
        public RectImgDescription getChild(int child) {
            if (child != 0) throw new IllegalStateException();
            return inside;
        }
        public int getExtraPartCount() {
            return 8;
        }
        public Rect getExtraPartRect(int part) {
            switch(part) {
            case 0: return Rect.newPtToPt(rect.fromX, rect.fromY, 
                rect.fromX + topCornerDim.width, rect.fromY + topCornerDim.height);
            case 1: return Rect.newPtToPt(rect.fromX + topCornerDim.width, rect.fromY,
                rect.toX - topCornerDim.width, rect.fromY + topCornerDim.height);
            case 2: return Rect.newPtToPt(rect.toX - topCornerDim.width, rect.fromY,
                rect.toX, rect.fromY + topCornerDim.height);
            case 3: return Rect.newPtToPt(rect.fromX, rect.fromY + topCornerDim.height,
                rect.fromX + borderThick, rect.toY - bottomCornerDim.height);
            case 4: return Rect.newPtToPt(rect.fromX + borderThick, rect.fromY + topCornerDim.height,
                rect.toX - borderThick, rect.toY - bottomCornerDim.height);
            case 5: return Rect.newPtToPt(rect.fromX, rect.toY - bottomCornerDim.height,
                rect.fromX + bottomCornerDim.width, rect.toY);
            case 6: return Rect.newPtToPt(rect.fromX + bottomCornerDim.width, rect.toY - bottomCornerDim.height,
                rect.toX - bottomCornerDim.width, rect.toY);
            case 7: return Rect.newPtToPt(rect.toX - bottomCornerDim.width, rect.toY - bottomCornerDim.height,
                rect.toX, rect.toY);
            default: throw new IllegalStateException();
            }
        }
        
        public Rect getInsideRect() {
            return Rect.newPtToPt(rect.fromX + borderThick, rect.fromY + borderThick,
                rect.toX - borderThick, rect.toY - borderThick);
        }

        public int getCornerBackgroundColor() {
            return cornerBackgroundColor;
        }

        public void setCornerBackgroundColor(int p) {
            this.cornerBackgroundColor = p;
        }

        public int getBorderColor() {
            return borderColor;
        }

        public void setBorderColor(int p) {
            this.borderColor = p;
        }

        public int getBorderThick() {
            return borderThick;
        }

        public void setBorderThick(int p) {
            this.borderThick = p;
        }
        
        public Dim getTopCornerDim() {
            return topCornerDim;
        }

        public void setTopCornerDim(Dim topCornerDim) {
            this.topCornerDim = topCornerDim;
        }

        public Dim getBottomCornerDim() {
            return bottomCornerDim;
        }

        public void setBottomCornerDim(Dim bottomCornerDim) {
            this.bottomCornerDim = bottomCornerDim;
        }

        public RectImgDescription getInside() {
            return inside;
        }

        public void setInside(RectImgDescription p) {
            this.inside = p;
        }
        
    }
    
    // ------------------------------------------------------------------------

    /**
     * descr for rectangular img wrapped with round border 
     * <PRE>
     * +---------------------+
     * |       0             |
     * |--+---------------+--|
     * | 1|   inside      | 2|
     * |--+---------------+--|
     * |       3             |
     * +---------------------+
     * </PRE>
     * 
     */
    public static class BorderRectImgDescr extends RectImgDescription {

        /** */
        private static final long serialVersionUID = 1L;

        private int borderColor;
        private Border border;
        private RectImgDescription inside;
        
        public BorderRectImgDescr(Rect rect) {
            super(rect);
        }
        
        public BorderRectImgDescr(Rect rect, 
                int borderColor, Border border,
                RectImgDescription inside) {
            super(rect);
            this.borderColor = borderColor;
            this.border = border;
            this.inside = inside;
        }

        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseBorder(this);
        }

        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseBorder(this, param);
        }

        public int getChildCount() {
            return 1;
        }
        public RectImgDescription getChild(int child) {
            assert child == 0;
            return inside;
        }
        public int getExtraPartCount() {
            return 4;
        }
        public Rect getExtraPartRect(int part) {
            switch(part) {
            case 0: return Rect.newPtToPt(rect.fromX, rect.fromY, 
                rect.toX, rect.fromY + border.top);
            case 1: return Rect.newPtToPt(rect.fromX, rect.fromY + border.top,
                rect.toX + border.left, rect.toY - border.bottom);
            case 2: return Rect.newPtToPt(rect.toX - border.right, rect.fromY + border.top,
                rect.toX, rect.toY - border.bottom);
            case 3: return Rect.newPtToPt(rect.fromX, rect.toY - border.bottom,
                rect.toX, rect.toY);
            default: throw new IllegalStateException();
            }
        }

        public int getBorderColor() {
            return borderColor;
        }

        public void setBorderColor(int borderColor) {
            this.borderColor = borderColor;
        }

        public Border getBorder() {
            return border;
        }

        public void setBorder(Border p) {
            this.border = p;
        }

        public RectImgDescription getInside() {
            return inside;
        }

        public void setInside(RectImgDescription p) {
            this.inside = p;
        }
        
        public Rect getInsideRect() {
            return Rect.newPtToPt(rect.fromX + border.left, rect.fromY + border.top, 
                rect.toX - border.right, rect.toY - border.bottom);
        }
        
    }

    // ------------------------------------------------------------------------
    
    /** 
     * specialized BorderRectImgDescr, for Top&Bottom border only 
     * 
     * <PRE>
     * +---------------------+
     * |       0             |
     * |---------------------|
     * |      inside         |
     * |---------------------|
     * |       1             |
     * +---------------------+
     * </PRE>
     */ 
    public static class TopBottomBorderRectImgDescr extends RectImgDescription {

        /** */
        private static final long serialVersionUID = 1L;

        private int borderColor;
        private int topBorder;
        private int bottomBorder;
        private RectImgDescription inside;
        
        public TopBottomBorderRectImgDescr(Rect rect) {
            super(rect);
        }
        
        public TopBottomBorderRectImgDescr(Rect rect, int borderColor, int topBorder, int bottomBorder, RectImgDescription inside) {
            super(rect);
            int rectH = rect.getHeight();
            if ((topBorder == 0 && bottomBorder == 0) || (topBorder + bottomBorder) >= rectH) {
                throw new IllegalArgumentException();
            }
            this.borderColor = borderColor;
            this.topBorder = topBorder;
            this.bottomBorder = bottomBorder;
            this.inside = inside;
        }

        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseTopBottomBorder(this);
        }

        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseTopBottomBorder(this, param);
        }

        public int getChildCount() {
            return 1;
        }
        public RectImgDescription getChild(int child) {
            assert child == 0;
            return inside;
        }
        public int getExtraPartCount() {
            return 2;
        }
        public Rect getExtraPartRect(int part) {
            switch(part) {
            case 0: return Rect.newPtToPt(rect.fromX, rect.fromY, 
                rect.toX, rect.fromY + topBorder);
            case 1: return Rect.newPtToPt(rect.fromX, rect.toY - bottomBorder,
                rect.toX, rect.toY);
            default: throw new IllegalStateException();
            }
        }

        public int getBorderColor() {
            return borderColor;
        }

        public void setBorderColor(int borderColor) {
            this.borderColor = borderColor;
        }

        public int getTopBorder() {
            return topBorder;
        }

        public void setTopBorder(int topBorder) {
            this.topBorder = topBorder;
        }

        public int getBottomBorder() {
            return bottomBorder;
        }

        public void setBottomBorder(int bottomBorder) {
            this.bottomBorder = bottomBorder;
        }

        public RectImgDescription getInside() {
            return inside;
        }

        public void setInside(RectImgDescription p) {
            this.inside = p;
        }
        
        public Rect getInsideRect() {
            return Rect.newPtToPt(rect.fromX, rect.fromY + topBorder, 
                rect.toX, rect.toY - bottomBorder);
        }

        @Override
        public String toString() {
            return "TopBottomBorderRectImgDescr [rect=" + rect 
                    + ", border color=" + RGBUtils.toString(borderColor) 
                    + ", bottom=" + bottomBorder 
                    + ", top=" + topBorder 
                    + "]";
        }
        
    }

    // ------------------------------------------------------------------------
    
    /** specialized BorderRectImgDescr, for Left&Rightborder only 
     * 
     * <PRE>
     * +--+---------------+--+
     * |  |               |  |
     * | 0|   inside      | 1|
     * |  |               |  |
     * +--+---------------+--+
     * </PRE>
     */ 
    public static class LeftRightBorderRectImgDescr extends RectImgDescription {

        /** */
        private static final long serialVersionUID = 1L;

        private int borderColor;
        private int leftBorder;
        private int rightBorder;
        private RectImgDescription inside;
        
        public LeftRightBorderRectImgDescr(Rect rect) {
            super(rect);
        }
        
        public LeftRightBorderRectImgDescr(Rect rect, int borderColor, int leftBorder, int rightBorder, RectImgDescription inside) {
            super(rect);
            int rectW = rect.getWidth();
            if ((leftBorder == 0 && rightBorder == 0) || (leftBorder + rightBorder) >= rectW) {
                throw new IllegalArgumentException();
            }
            this.borderColor = borderColor;
            this.leftBorder = leftBorder;
            this.rightBorder = rightBorder;
            this.inside = inside;
        }

        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseLeftRightBorder(this);
        }

        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseLeftRightBorder(this, param);
        }

        public int getChildCount() {
            return 1;
        }
        public RectImgDescription getChild(int child) {
            assert child == 0;
            return inside;
        }
        public int getExtraPartCount() {
            return 2;
        }
        public Rect getExtraPartRect(int part) {
            switch(part) {
            case 0: return Rect.newPtToPt(rect.fromX, rect.fromY, 
                rect.fromX + leftBorder, rect.toY);
            case 1: return Rect.newPtToPt(rect.toX - rightBorder, rect.fromY,
                rect.toX, rect.toY);
            default: throw new IllegalStateException();
            }
        }

        public int getBorderColor() {
            return borderColor;
        }

        public void setBorderColor(int borderColor) {
            this.borderColor = borderColor;
        }
        
        public int getLeftBorder() {
            return leftBorder;
        }

        public void setLeftBorder(int leftBorder) {
            this.leftBorder = leftBorder;
        }

        public int getRightBorder() {
            return rightBorder;
        }

        public void setRightBorder(int rightBorder) {
            this.rightBorder = rightBorder;
        }

        public RectImgDescription getInside() {
            return inside;
        }

        public void setInside(RectImgDescription p) {
            this.inside = p;
        }
        
        public Rect getInsideRect() {
            return Rect.newPtToPt(rect.fromX + leftBorder, rect.fromY, 
                rect.toX - rightBorder, rect.toY);
        }

        @Override
        public String toString() {
            return "LeftRightBorderRectImgDescr [rect=" + rect 
                    + ", border color=" + RGBUtils.toString(borderColor) 
                    + ", left=" + leftBorder 
                    + ", right=" + rightBorder 
                    + "]";
        }
    }

    // ------------------------------------------------------------------------

    /**
     * <PRE>
     * +--------+-+---------+
     * |        | |         |
     * |  left  |0|  right  |
     * |        | |         |
     * +--------+-+---------+
     * </PRE>
     *
     */
    public static class VerticalSplitRectImgDescr extends RectImgDescription {
        
        /** */
        private static final long serialVersionUID = 1L;

        private RectImgDescription left;
        private Segment splitBorder;
        private int splitColor;
        private RectImgDescription right;
        
        public VerticalSplitRectImgDescr(Rect rect) {
            super(rect);
        }
        
        public VerticalSplitRectImgDescr(Rect rect, RectImgDescription left, Segment splitBorder, int splitColor, RectImgDescription right) {
            super(rect);
            this.left = left;
            this.splitBorder = splitBorder;
            this.splitColor = splitColor;
            this.right = right;
            if (splitBorder.to > rect.toX) {
                throw new IllegalArgumentException();
            }
        }

        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseVerticalSplit(this);
        }

        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseVerticalSplit(this, param);
        }

        public int getChildCount() {
            return 2;
        }
        public RectImgDescription getChild(int child) {
            assert 0 <= child && child < 2;
            return child == 0? left : right;
        }
        public int getExtraPartCount() {
            return 1;
        }
        public Rect getExtraPartRect(int part) {
            return Rect.newPtToPt(splitBorder.from, rect.fromY, 
                splitBorder.to, rect.toY);
        }

        public Rect getLeftRect() {
            return Rect.newPtToPt(rect.fromX, rect.fromY, splitBorder.from, rect.toY);
        }

        public Rect getRightRect() {
            return Rect.newPtToPt(splitBorder.to, rect.fromY, rect.toX, rect.toY);
        }

        public RectImgDescription getLeft() {
            return left;
        }

        public void setLeft(RectImgDescription left) {
            this.left = left;
        }
        
        public Segment getSplitBorder() {
            return splitBorder;
        }

        public void setSplitBorder(Segment splitBorder) {
            this.splitBorder = splitBorder;
        }

        public int getSplitColor() {
            return splitColor;
        }

        public void setSplitColor(int splitColor) {
            this.splitColor = splitColor;
        }

        public RectImgDescription getRight() {
            return right;
        }

        public void setRight(RectImgDescription right) {
            this.right = right;
        }
        
    }
    

    // ------------------------------------------------------------------------

    /**
     * <PRE>
     * +---------------------+
     * |        up           |
     * |                     |
     * +---------------------+
     * |         0           |
     * +---------------------+
     * |                     |
     * |        down         |
     * +---------------------+
     * </PRE>
     */
    public static class HorizontalSplitRectImgDescr extends RectImgDescription {

        /** */
        private static final long serialVersionUID = 1L;

        private RectImgDescription up;
        private int splitColor;
        private Segment splitBorder;
        private RectImgDescription down;
        
        public HorizontalSplitRectImgDescr(Rect rect) {
            super(rect);
        }
        
        public HorizontalSplitRectImgDescr(Rect rect, RectImgDescription up, Segment splitBorder, int splitColor, RectImgDescription down) {
            super(rect);
            this.up = up;
            this.splitBorder = splitBorder;
            this.splitColor = splitColor;
            this.down = down;
            if (splitBorder.to > rect.toY) {
                throw new IllegalArgumentException();
            }
        }

        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseHorizontalSplit(this);
        }

        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseHorizontalSplit(this, param);
        }

        public int getChildCount() {
            return 2;
        }
        public RectImgDescription getChild(int child) {
            assert 0 <= child && child < 2;
            return child == 0? up : down;
        }
        public int getExtraPartCount() {
            return 1;
        }
        public Rect getExtraPartRect(int part) {
            return Rect.newPtToPt(rect.fromX, splitBorder.from, 
                rect.toX, splitBorder.to);
        }

        public Rect getUpRect() {
            return Rect.newPtToPt(rect.fromX, rect.fromY, rect.toX, splitBorder.from);
        }

        public Rect getDownRect() {
            return Rect.newPtToPt(rect.fromX, splitBorder.to, rect.toX, rect.toY);
        }
        
        public RectImgDescription getUp() {
            return up;
        }

        public void setUp(RectImgDescription up) {
            this.up = up;
        }
        
        public Segment getSplitBorder() {
            return splitBorder;
        }

        public void setSplitBorder(Segment splitBorder) {
            this.splitBorder = splitBorder;
        }

        public int getSplitColor() {
            return splitColor;
        }

        public void setSplitColor(int splitColor) {
            this.splitColor = splitColor;
        }

        public RectImgDescription getDown() {
            return down;
        }

        public void setDown(RectImgDescription down) {
            this.down = down;
        }
        
    }
    
    // ------------------------------------------------------------------------

    /**
     * <PRE>
     * +---------------------+
     * |         0           |
     * +---------------------+
     * |        line[0]      |
     * |                     |
     * +---------------------+
     * |         1           |
     * +---------------------+
     * |        line[1]      |
     * +---------------------+
     * |         2           |
     * +---------------------+
     * |        ...          |
     * |                     |
     * |        line[n-1]    |
     * +---------------------+
     * |          n          |
     * +---------------------+
     * </PRE>
     */
    public static class LinesSplitRectImgDescr extends RectImgDescription {

        /** */
        private static final long serialVersionUID = 1L;

        private int backgroundColor;
        private Segment[] splitBorders;
        private RectImgDescription[] lines;
        
        public LinesSplitRectImgDescr(Rect rect) {
            super(rect);
        }
        
        public LinesSplitRectImgDescr(Rect rect, int backgroundColor, List<Segment> splitBorders, List<RectImgDescription> lines) {
            this(rect, backgroundColor, 
                (splitBorders != null)? splitBorders.toArray(new Segment[splitBorders.size()]) : null,
                (lines != null)? lines.toArray(new RectImgDescription[lines.size()]) : null);
        }
        
        public LinesSplitRectImgDescr(Rect rect, int backgroundColor, 
                Segment[] splitBorders,
                RectImgDescription[] lines) {
            super(rect);
            if (splitBorders != null && splitBorders.length == 0) {
                throw new IllegalArgumentException();
            }
            this.backgroundColor = backgroundColor;
            this.splitBorders = splitBorders;
            this.lines = lines;
        }
        
        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseLinesSplit(this);
        }

        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseLinesSplit(this, param);
        }

        public int getChildCount() {
            return lines.length;
        }
        public RectImgDescription getChild(int child) {
            assert 0 <= child && child < lines.length;
            return lines[child];
        }
        public int getExtraPartCount() {
            return splitBorders.length;
        }
        public Rect getExtraPartRect(int part) {
            assert 0 <= part && part < splitBorders.length;
            return Rect.newPtToPt(rect.fromX, splitBorders[part].from,  
                rect.toX, splitBorders[part].to);
        }

        public Rect[] getLineRects() {
            final Segment[] sb = splitBorders;
            int len = ((sb[0].from != rect.fromY)?1:0) + sb.length - 1 + ((sb[sb.length-1].to != rect.toY)?1:0);
            Rect[] res = new Rect[len];
            int lineIdx = 0;
            if (sb[0].from != rect.fromY) {
                res[lineIdx] = Rect.newPtToPt(rect.fromX, rect.fromY, rect.toX, sb[0].from);
                lineIdx++;
            }
            for(int i = 1; i < sb.length; i++,lineIdx++) {
                res[lineIdx] = Rect.newPtToPt(rect.fromX, sb[i-1].to, rect.toX, sb[i].from);
            }
            if (sb[sb.length-1].to != rect.toY) {
                res[lineIdx] = Rect.newPtToPt(rect.fromX, sb[sb.length-1].to, rect.toX, rect.toY);
            }
            return res;
        }

        public int getBackgroundColor() {
            return backgroundColor;
        }

        public void setBackgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        public Segment[] getSplitBorders() {
            return splitBorders;
        }

        public void setSplitBorders(Segment[] splitBorders) {
            this.splitBorders = splitBorders;
        }

        public RectImgDescription[] getLines() {
            return lines;
        }

        public void setLines(RectImgDescription[] lines) {
            this.lines = lines;
        }
        
    }
    

    // ------------------------------------------------------------------------

    /**
     * <PRE>
     * +--+------+-+---------+-+---------+--+
     * |  |      | |         | |         |  |
     * |0 |col[0]|1| col[1]  |2| col[n-1]|n |
     * |  |      | |         | |         |  | 
     * +--+------+-+---------+-+---------+--+
     * </PRE>
     *
     */
    public static class ColumnsSplitRectImgDescr extends RectImgDescription {

        /** */
        private static final long serialVersionUID = 1L;

        private int backgroundColor;
        private Segment[] splitBorders;
        private RectImgDescription[] columns;

        public ColumnsSplitRectImgDescr(Rect rect) {
            super(rect);
        }
        
        public ColumnsSplitRectImgDescr(Rect rect, int backgroundColor, List<Segment> splitBorders, 
                List<RectImgDescription> columns) {
            this(rect, backgroundColor,
                (splitBorders != null)? splitBorders.toArray(new Segment[splitBorders.size()]) : null,
                (columns != null)? columns.toArray(new RectImgDescription[columns.size()]) : null);
        }

        public ColumnsSplitRectImgDescr(Rect rect, int backgroundColor, 
                Segment[] splitBorders, RectImgDescription[] columns) {
            super(rect);
            this.backgroundColor = backgroundColor;
            this.splitBorders = splitBorders;
            this.columns = columns;
        }
        
        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseColumnsSplit(this);
        }

        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseColumnsSplit(this, param);
        }

        public int getChildCount() {
            return columns.length;
        }
        public RectImgDescription getChild(int child) {
            assert 0 <= child && child < columns.length;
            return columns[child];
        }
        public int getExtraPartCount() {
            return splitBorders.length;
        }
        public Rect getExtraPartRect(int part) {
            assert 0 <= part && part < splitBorders.length;
            return Rect.newPtToPt(splitBorders[part].from, rect.fromY,  
                splitBorders[part].to, rect.toY);
        }

        public Rect[] getColumnRects() {
            final Segment[] sb = splitBorders;
            int len = ((sb[0].from != rect.fromX)?1:0) + sb.length - 1 + ((sb[sb.length-1].to != rect.toX)?1:0);
            Rect[] res = new Rect[len];
            int idx = 0;
            if (sb[0].from != rect.fromX) {
                res[idx] = Rect.newPtToPt(rect.fromX, rect.fromY, sb[0].from, rect.toY);
                idx++;
            }
            for(int i = 1; i < sb.length; i++,idx++) {
                res[idx] = Rect.newPtToPt(sb[i-1].to, rect.fromY, sb[i].from, rect.toY);
            }
            if (sb[sb.length-1].to != rect.toX) {
                res[idx] = Rect.newPtToPt(sb[sb.length-1].to, rect.fromY, rect.toX, rect.toY);
            }
            return res;
        }

        public int getBackgroundColor() {
            return backgroundColor;
        }
        
        public void setBackgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
        }

        public Segment[] getSplitBorders() {
            return splitBorders;
        }

        public void setSplitBorders(Segment[] splitBorders) {
            this.splitBorders = splitBorders;
        }

        public RectImgDescription[] getColumns() {
            return columns;
        }
        public void setColumns(RectImgDescription[] columns) {
            this.columns = columns;
        }
        
        
    }

    // ------------------------------------------------------------------------

    /**
     * <PRE>
     * +--------------+
     * |              |
     * |  raw data    |
     * |              |
     * |  no repeat   |
     * |  size>20x20  |
     * +--------------+
     * </PRE>
     */
    public static class RawDataRectImgDescr extends RectImgDescription {
        
        /** */
        private static final long serialVersionUID = 1L;
        
        private int[] rawData;

        public RawDataRectImgDescr(Rect rect) {
            super(rect);
        }
        
        public RawDataRectImgDescr(Rect rect, int[] rawData) {
            super(rect);
            this.rawData = rawData;
        }
        
        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseRawData(this);
        }

        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseRawData(this, param);
        }

        public int getChildCount() {
            return 0;
        }
        public RectImgDescription getChild(int child) {
            throw new IllegalStateException();
        }
        public int getExtraPartCount() {
            return 0;
        }
        public Rect getExtraPartRect(int part) {
            throw new IllegalStateException();
        }

        public int[] getRawData() {
            return rawData;
        }

        public void setRawData(int[] rawData) {
            this.rawData = rawData;
        }
        
    }
    
    // ------------------------------------------------------------------------

    /**
     * <PRE>
     * +------------+
     * | glyph #123 | 
     * | (repeated) |
     * | size<~20x20|
     * +------------+
     * </PRE>
     */
    public static class GlyphRectImgDescr extends RectImgDescription {

        /** */
        private static final long serialVersionUID = 1L;

        private int crc;
        private int[] sharedData;
        
        @Deprecated
        private transient GlyphMRUTable glyphMRUTable;
        @Deprecated
        private transient GlyphIndexOrCode glyphIndexOrCode;
        @Deprecated
        private transient boolean isNewGlyph; // should be implicit with glyphMRUTable + indexOrCode... 
        
        public GlyphRectImgDescr(Rect rect) {
            super(rect);
        }
        
        public GlyphRectImgDescr(Rect rect, int crc, int[] sharedData,
                GlyphMRUTable glyphMRUTable, GlyphIndexOrCode glyphIndexOrCode, boolean isNewGlyph) {
            super(rect);
            this.crc = crc;
            this.sharedData = sharedData;
            
            this.glyphMRUTable = glyphMRUTable;
            this.glyphIndexOrCode = glyphIndexOrCode;
            this.isNewGlyph = isNewGlyph;
        }

        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseGlyph(this);
        }
        
        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseGlyph(this, param);
        }

        public int getChildCount() {
            return 0;
        }
        public RectImgDescription getChild(int child) {
            throw new IllegalStateException();
        }
        public int getExtraPartCount() {
            return 0;
        }
        public Rect getExtraPartRect(int part) {
            throw new IllegalStateException();
        }

        public int getCrc() {
            return crc;
        }

        public void setCrc(int crc) {
            this.crc = crc;
        }

        public int[] getSharedData() {
            return sharedData;
        }

        public void setSharedData(int[] p) {
            this.sharedData = p;
        }

        @Deprecated
        public GlyphMRUTable getGlyphMRUTable() {
            return glyphMRUTable;
        }

        @Deprecated
        public void setGlyphMRUTable(GlyphMRUTable glyphMRUTable) {
            this.glyphMRUTable = glyphMRUTable;
        }

        @Deprecated
        public boolean isNewGlyph() {
            return isNewGlyph;
        }

        @Deprecated
        public void setNewGlyph(boolean isNewGlyph) {
            this.isNewGlyph = isNewGlyph;
        }
        
        @Deprecated
        public GlyphIndexOrCode getGlyphIndexOrCode() {
            return glyphIndexOrCode;
        }

        @Deprecated
        public void setGlyphIndexOrCode(GlyphIndexOrCode p) {
            this.glyphIndexOrCode = p;
        }
        
    }
     
    // ------------------------------------------------------------------------

    public static class RectImgAboveRectImgDescr extends RectImgDescription {

        /** */
        private static final long serialVersionUID = 1L;

        private RectImgDescription underlying;
        private Rect[] aboveRects;
        private RectImgDescription[] aboves;

        public RectImgAboveRectImgDescr(Rect rect) {
            super(rect);
        }

        public RectImgAboveRectImgDescr(Rect rect, RectImgDescription underlying, 
                List<Rect> aboveRects) {
            this(rect, underlying, aboveRects.toArray(new Rect[aboveRects.size()]));
        }

        public RectImgAboveRectImgDescr(Rect rect, RectImgDescription underlying, 
                Rect[] aboveRects) {
            super(rect);
            this.underlying = underlying;
            this.aboveRects = aboveRects;
            // check nested rects
            if (aboveRects != null) {
                int idx = 0;
                for (Rect aboveRect : aboveRects) {
                    if (! rect.contains(aboveRect)) {
                        throw new IllegalArgumentException("aboveRect[" + idx + "]: " + aboveRect + " not contained in rect:" + rect);
                    }
                    idx++;
                }
            }
        }


        public RectImgAboveRectImgDescr(Rect rect, RectImgDescription underlying, 
                RectImgDescription[] aboves) {
            super(rect);
            this.underlying = underlying;
            this.aboveRects = (aboves != null)? arrayToRectArray(aboves) : null;
            this.aboves = aboves;
        }

        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseAbove(this);
        }
        
        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseAbove(this, param);
        }

        public int getChildCount() {
            return 1 + aboves.length;
        }
        public RectImgDescription getChild(int child) {
            if (child == 0) return underlying;
            else return aboves[child-1];
        }
        public int getExtraPartCount() {
            // TODO? partition rect without above rects?
            return 1;
        }
        public Rect getExtraPartRect(int part) {
            return rect;
        }

        public Rect[] getAboveRects() {
            return aboveRects;
        }

        public void setAboveRects(Rect[] aboveRects) {
            this.aboveRects = aboveRects;
            this.aboves = null;
        }

        public RectImgDescription getUnderlying() {
            return underlying;
        }

        public void setUnderlying(RectImgDescription underlyingRectImgDescr) {
            this.underlying = underlyingRectImgDescr;
        }

        public RectImgDescription[] getAboves() {
            return aboves;
        }

        public void setAboveRectImgDescrs(RectImgDescription[] aboveRectImgDescrs) {
            this.aboves = aboveRectImgDescrs;
            this.aboveRects = (aboveRectImgDescrs != null)? arrayToRectArray(aboveRectImgDescrs) : null;;
        }
        
    }
    
    // ------------------------------------------------------------------------

    public static abstract class NoiseFragment implements Serializable {

        /** */
        private static final long serialVersionUID = 1L;
        
        public abstract void accept(RectImgDescrVisitor visitor, NoiseAbovePartsRectImgDescr parent, int partIndex);

        public abstract <T, R> R accept(RectImgDescrVisitor2<T, R> visitor, NoiseAbovePartsRectImgDescr parent, int partIndex, T param);
        
    }
    
    public static class PtNoiseFragment extends NoiseFragment {

        /** */
        private static final long serialVersionUID = 1L;

        private int color;
        private int x, y;
        
        public PtNoiseFragment(int x, int y, int color) {
            this.x = x;
            this.y = y;
            this.color = color;
        }

        @Override
        public void accept(RectImgDescrVisitor visitor, NoiseAbovePartsRectImgDescr parent, int partIndex) {
            visitor.caseNoiseAboveParts_Pt(parent, partIndex, this);            
        }

        @Override
        public <T, R> R accept(RectImgDescrVisitor2<T, R> visitor, NoiseAbovePartsRectImgDescr parent, int partIndex, T param) {
            return visitor.caseNoiseAboveParts_Pt(parent, partIndex, this, param);
        }

        
        public int getColor() {
            return color;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
        
    }

    public static class SegmentNoiseFragment extends NoiseFragment {
        
        /** */
        private static final long serialVersionUID = 1L;
        
        private int color;
        private int fromX;
        private int toX;
        private int y;
        
        public SegmentNoiseFragment(int fromX, int toX, int y, int color) {
            this.fromX = fromX;
            this.toX = toX;
            this.y = y;
            this.color = color;
        }

        @Override
        public void accept(RectImgDescrVisitor visitor, NoiseAbovePartsRectImgDescr parent, int partIndex) {
            visitor.caseNoiseAboveParts_Segment(parent, partIndex, this);
        }

        @Override
        public <T, R> R accept(RectImgDescrVisitor2<T, R> visitor, NoiseAbovePartsRectImgDescr parent, int partIndex, T param) {
            return visitor.caseNoiseAboveParts_Segment(parent, partIndex, this, param);
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }

        public int getFromX() {
            return fromX;
        }

        public int getToX() {
            return toX;
        }

        public int getY() {
            return y;
        }
        
    }

    // TODO ... 
    public static class ConnexSegmentLinesNoiseFragment extends NoiseFragment {
        
        /** */
        private static final long serialVersionUID = 1L;
        
        private int color;
        private int fromY;
        private Segment[] lines;
        
        
        public ConnexSegmentLinesNoiseFragment(int fromY, Segment[] lines, int color) {
            this.fromY = fromY;
            this.lines = lines;
            this.color = color;
        }
        
        @Override
        public void accept(RectImgDescrVisitor visitor, NoiseAbovePartsRectImgDescr parent, int partIndex) {
            visitor.caseNoiseAboveParts_ConnexSegmentLines(parent, partIndex, this);            
        }

        @Override
        public <T, R> R accept(RectImgDescrVisitor2<T, R> visitor, NoiseAbovePartsRectImgDescr parent, int partIndex, T param) {
            return visitor.caseNoiseAboveParts_ConnexSegmentLines(parent, partIndex, this, param);
        }
        
        public int getColor() {
            return color;
        }
        public int getFromY() {
            return fromY;
        }
        public Segment[] getLines() {
            return lines;
        }
        
        
    }
    
    /** 
     * Description for any underlying element, with noise fragments above it
     * <PRE>
     * +---+----------+---+
     * |X  |   yyyyy  |   |
     * | 0 |  elt[0]  | 1 |
     * | XX| ZZ       |ZZZ|
     * +-- +----------+---+
     * </PRE>
     */ 
    public static class NoiseAbovePartsRectImgDescr extends RectImgDescription {

        /** */
        private static final long serialVersionUID = 1L;

        private RectImgDescription underlying;

        private NoiseFragment[][] noiseFragmentsAboveParts;

        public NoiseAbovePartsRectImgDescr(Rect rect) {
            super(rect);
        }

        public NoiseAbovePartsRectImgDescr(RectImgDescription underlying, NoiseFragment[][] noiseFragmentsAboveParts) {
            this.underlying = underlying;
            this.noiseFragmentsAboveParts = noiseFragmentsAboveParts;
        }

        @Override
        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseNoiseAboveParts(this);            
        }

        @Override
        public <T, R> R accept(RectImgDescrVisitor2<T, R> visitor, T param) {
            return visitor.caseNoiseAbove(this, param);
        }

        public int getChildCount() {
            return 1;
        }
        public RectImgDescription getChild(int child) {
            return underlying;
        }
        public int getExtraPartCount() {
            return (underlying != null)? underlying.getExtraPartCount() : 0;
        }
        public Rect getExtraPartRect(int part) {
            return underlying.getExtraPartRect(part);
        }
        
        public RectImgDescription getUnderlying() {
            return underlying;
        }

        public void setUnderlying(RectImgDescription  p) {
            this.underlying = p;
        }

        public NoiseFragment[][] getNoiseFragmentsAboveParts() {
            return noiseFragmentsAboveParts;
        }

        public void setNoiseFragmentsAboveParts(NoiseFragment[][] p) {
            this.noiseFragmentsAboveParts = p;
        }

        public Dim getUnderlyingExtraPartDim(int partIndex) {
            return underlying.getExtraPartRect(partIndex).getDim();
        }

        public int getUnderlyingExtraPartCount() {
            return underlying.getExtraPartCount();
        }

        public Rect getUnderlyingExtraPartRect(int partIndex) {
            return underlying.getExtraPartRect(partIndex);
        }
        
    }

    // ------------------------------------------------------------------------

    
    public static class OverrideAttributesProxyRectImgDescr extends RectImgDescription {

        /** */
        private static final long serialVersionUID = 1L;

        private RectImgDescription underlying;

        private Map<Object,Object> attributeOverrides = new HashMap<Object,Object>();
        
        public OverrideAttributesProxyRectImgDescr(Rect rect) {
            super(rect);
        }

        public OverrideAttributesProxyRectImgDescr(RectImgDescription underlying, Map<Object, Object> attributeOverrides) {
            this.underlying = underlying;
            this.attributeOverrides = attributeOverrides;
        }
        
        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseOverrideAttributesProxy(this);            
        }

        @Override
        public <T, R> R accept(RectImgDescrVisitor2<T, R> visitor, T param) {
            return visitor.caseOverrideAttributesProxy(this, param);
        }

        public int getChildCount() {
            return 1;
        }
        public RectImgDescription getChild(int child) {
            assert child == 0;
            return underlying;
        }
        public int getExtraPartCount() {
            return 0;
        }
        public Rect getExtraPartRect(int part) {
            throw new IllegalStateException();
        }

        public RectImgDescription getUnderlying() {
            return underlying;
        }
        
        public void setUnderlying(RectImgDescription underlying) {
            this.underlying = underlying;
        }

        public Map<Object, Object> getAttributeOverrides() {
            return attributeOverrides;
        }
        
        
    }
    
}

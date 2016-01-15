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
    
    /** specialized BorderRectImgDescr, for Top&Bottom border only */ 
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
    
    /** specialized BorderRectImgDescr, for Left&Rightborder only */ 
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

        public int[] getRawData() {
            return rawData;
        }

        public void setRawData(int[] rawData) {
            this.rawData = rawData;
        }
        
    }
    
    // ------------------------------------------------------------------------

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

        private RectImgDescription underlyingRectImgDescr;
        private Rect[] aboveRects;
        private RectImgDescription[] aboveRectImgDescrs;

        public RectImgAboveRectImgDescr(Rect rect) {
            super(rect);
        }

        public RectImgAboveRectImgDescr(Rect rect, RectImgDescription underlyingRectImgDescr, 
                List<Rect> aboveRects) {
            this(rect, underlyingRectImgDescr, aboveRects.toArray(new Rect[aboveRects.size()]));
        }

        public RectImgAboveRectImgDescr(Rect rect, RectImgDescription underlyingRectImgDescr, 
                Rect[] aboveRects) {
            super(rect);
            this.underlyingRectImgDescr = underlyingRectImgDescr;
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


        public RectImgAboveRectImgDescr(Rect rect, RectImgDescription underlyingRectImgDescr, 
                RectImgDescription[] aboveRectImgDescrs) {
            super(rect);
            this.underlyingRectImgDescr = underlyingRectImgDescr;
            this.aboveRects = (aboveRectImgDescrs != null)? arrayToRectArray(aboveRectImgDescrs) : null;
            this.aboveRectImgDescrs = aboveRectImgDescrs;
        }

        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseAbove(this);
        }
        
        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseAbove(this, param);
        }

        public Rect[] getAboveRects() {
            return aboveRects;
        }

        public void setAboveRects(Rect[] aboveRects) {
            this.aboveRects = aboveRects;
            this.aboveRectImgDescrs = null;
        }

        public RectImgDescription getUnderlying() {
            return underlyingRectImgDescr;
        }

        public void setUnderlying(RectImgDescription underlyingRectImgDescr) {
            this.underlyingRectImgDescr = underlyingRectImgDescr;
        }

        public RectImgDescription[] getAboves() {
            return aboveRectImgDescrs;
        }

        public void setAboveRectImgDescrs(RectImgDescription[] aboveRectImgDescrs) {
            this.aboveRectImgDescrs = aboveRectImgDescrs;
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

        public Dim getPartDim(int partIndex) {
            return rect.getDim(); // TODO NOTIMPLEMENTED YET .... restrict for partIndex !!
        }

        public int getPartCount() {
            return 0;
        }

        public Rect getPartRect(int partIndex) {
            return rect; // TODO NOT IMPlEMENTED YET ... restrict
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

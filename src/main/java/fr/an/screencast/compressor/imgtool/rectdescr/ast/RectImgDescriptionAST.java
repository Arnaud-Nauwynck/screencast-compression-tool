package fr.an.screencast.compressor.imgtool.rectdescr.ast;

import java.io.Serializable;
import java.util.List;

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
            visitor.caseAnalysisProxyRect(this);
        }

        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseAnalysisProxyRect(this, param);
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
            visitor.caseFillRect(this);
        }

        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseFillRect(this, param);
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
            visitor.caseRoundBorderDescr(this);
        }

        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseRoundBorderDescr(this, param);
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
            visitor.caseBorderDescr(this);
        }

        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseBorderDescr(this, param);
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
            visitor.caseTopBottomBorderDescr(this);
        }

        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseTopBottomBorderDescr(this, param);
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
            visitor.caseLeftRightBorderDescr(this);
        }

        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseLeftRightBorderDescr(this, param);
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
            visitor.caseVerticalSplitDescr(this);
        }

        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseVerticalSplitDescr(this, param);
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
            visitor.caseHorizontalSplitDescr(this);
        }

        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseHorizontalSplitDescr(this, param);
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
            visitor.caseLinesSplitDescr(this);
        }

        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseLinesSplitDescr(this, param);
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
            visitor.caseColumnsSplitDescr(this);
        }

        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseColumnsSplitDescr(this, param);
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
        
        private int[] rawData;

        public RawDataRectImgDescr(Rect rect) {
            super(rect);
        }
        
        public RawDataRectImgDescr(Rect rect, int[] rawData) {
            super(rect);
            this.rawData = rawData;
        }
        
        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseRawDataDescr(this);
        }

        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseRawDataDescr(this, param);
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
            visitor.caseGlyphDescr(this);
        }
        
        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseGlyphDescr(this, param);
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
            visitor.caseAboveDescr(this);
        }
        
        public <T,R> R accept(RectImgDescrVisitor2<T,R> visitor, T param) {
            return visitor.caseAboveDescr(this, param);
        }

        public Rect[] getAboveRects() {
            return aboveRects;
        }

        public void setAboveRects(Rect[] aboveRects) {
            this.aboveRects = aboveRects;
            this.aboveRectImgDescrs = null;
        }

        public RectImgDescription getUnderlyingRectImgDescr() {
            return underlyingRectImgDescr;
        }

        public void setUnderlyingRectImgDescr(RectImgDescription underlyingRectImgDescr) {
            this.underlyingRectImgDescr = underlyingRectImgDescr;
        }

        public RectImgDescription[] getAboveRectImgDescrs() {
            return aboveRectImgDescrs;
        }

        public void setAboveRectImgDescrs(RectImgDescription[] aboveRectImgDescrs) {
            this.aboveRectImgDescrs = aboveRectImgDescrs;
            this.aboveRects = (aboveRectImgDescrs != null)? arrayToRectArray(aboveRectImgDescrs) : null;;
        }
        
    }
    
}

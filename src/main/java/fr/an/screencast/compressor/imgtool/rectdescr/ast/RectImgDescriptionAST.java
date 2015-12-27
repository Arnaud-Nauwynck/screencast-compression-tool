package fr.an.screencast.compressor.imgtool.rectdescr.ast;

import fr.an.screencast.compressor.utils.Border;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Rect;

/**
 * 
 */
public class RectImgDescriptionAST {

    /**
     * abstract root class of AST for describing a rectangular area of an image 
     */
    public static abstract class RectImgDescription {

        private final Rect rect;
        
        public RectImgDescription(Rect rect) {
            this.rect = rect;
        }
        
        public abstract void accept(RectImgDescrVisitor visitor);
        
        @Override
        public String toString() {
            return "RectImgDescr[rect=" + rect + "]";
        }
    }

    
    // ------------------------------------------------------------------------

    public static class FillRectImgDescr extends RectImgDescription {

        private int color;
        
        public FillRectImgDescr(Rect rect, int color) {
            super(rect);
            this.color = color;
        }

        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseFillRect(this);
        }

        public int getColor() {
            return color;
        }

        public void setColor(int color) {
            this.color = color;
        }
    
        
    }

    // ------------------------------------------------------------------------

    public static class RoundBorderRectImgDescr extends RectImgDescription {
        
        private int cornerBackgroundColor;
        private int borderColor;
        
        private int borderThick;
        
        private Dim topCornerDim;
        private Dim bottomCornerDim;
        
        private RectImgDescription insideRect;
        
        public RoundBorderRectImgDescr(Rect rect, int cornerBackgroundColor, int borderColor, int borderThick, Dim topCornerDim, Dim bottomCornerDim,
                RectImgDescription insideRect) {
            super(rect);
            this.cornerBackgroundColor = cornerBackgroundColor;
            this.borderColor = borderColor;
            this.borderThick = borderThick;
            this.topCornerDim = topCornerDim;
            this.bottomCornerDim = bottomCornerDim;
            this.insideRect = insideRect;
        }

        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseRoundBorderDescr(this);
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

        public RectImgDescription getInsideRect() {
            return insideRect;
        }

        public void setInsideRect(RectImgDescription p) {
            this.insideRect = p;
        }
        
    }
    
    // ------------------------------------------------------------------------

    public static class BorderRectImgDescr extends RectImgDescription {
        
        private int borderColor;
        private Border border;
        private RectImgDescription insideRect;
        
        public BorderRectImgDescr(Rect rect, 
                int borderColor, Border border,
                RectImgDescription insideRect) {
            super(rect);
            this.borderColor = borderColor;
            this.border = border;
            this.insideRect = insideRect;
        }

        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseBorderDescr(this);
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

        public RectImgDescription getInsideRect() {
            return insideRect;
        }

        public void setInsideRect(RectImgDescription insideRect) {
            this.insideRect = insideRect;
        }
        
    }

    // ------------------------------------------------------------------------

    public static class VerticalSplitRectImgDescr extends RectImgDescription {
        
        private RectImgDescription left;
        private RectImgDescription right;
        
        public VerticalSplitRectImgDescr(Rect rect, RectImgDescription left, RectImgDescription right) {
            super(rect);
            this.left = left;
            this.right = right;
        }

        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseVerticalSplitDescr(this);
        }

        public RectImgDescription getLeft() {
            return left;
        }

        public void setLeft(RectImgDescription left) {
            this.left = left;
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
        private RectImgDescription up;
        private RectImgDescription down;
        
        public HorizontalSplitRectImgDescr(Rect rect, RectImgDescription up, RectImgDescription down) {
            super(rect);
            this.up = up;
            this.down = down;
        }

        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseHorizontalSplitDescr(this);
        }

        public RectImgDescription getUp() {
            return up;
        }

        public void setUp(RectImgDescription up) {
            this.up = up;
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

        private int backgroundColor;
        // private int interLinespace; // redundant with lines
        private RectImgDescription[] lines;
        
        public LinesSplitRectImgDescr(Rect rect, int backgroundColor, RectImgDescription[] lines) {
            super(rect);
            this.backgroundColor = backgroundColor;
            this.lines = lines;
        }
        
        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseLinesSplitDescr(this);
        }

        public int getBackgroundColor() {
            return backgroundColor;
        }

        public void setBackgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
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
        
        private int backgroundColor;
        // private int interspace; // redundant with columns
        private RectImgDescription[] columns;
        
        public ColumnsSplitRectImgDescr(Rect rect, int backgroundColor, RectImgDescription[] columns) {
            super(rect);
            this.backgroundColor = backgroundColor;
            this.columns = columns;
        }
        
        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseColumnsSplitDescr(this);
        }

        public int getBackgroundColor() {
            return backgroundColor;
        }
        public void setBackgroundColor(int backgroundColor) {
            this.backgroundColor = backgroundColor;
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

        public RawDataRectImgDescr(Rect rect, int[] rawData) {
            super(rect);
            this.rawData = rawData;
        }
        
        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseRawDataDescr(this);
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
        private int glyphId;

        public GlyphRectImgDescr(Rect rect, int glyphId) {
            super(rect);
            this.glyphId = glyphId;
        }

        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseGlyphDescr(this);
        }

        public int getGlyphId() {
            return glyphId;
        }

        public void setGlyphId(int glyphId) {
            this.glyphId = glyphId;
        }
        
    }
    
    // ------------------------------------------------------------------------

    public static class RectImgAboveRectImgDescr extends RectImgDescription {

        private RectImgDescription underlyingRectImgDescr;
        private RectImgDescription aboveRectImgDescr;
        
        public RectImgAboveRectImgDescr(Rect rect, RectImgDescription underlyingRectImgDescr, RectImgDescription aboveRectImgDescr) {
            super(rect);
            this.underlyingRectImgDescr = underlyingRectImgDescr;
            this.aboveRectImgDescr = aboveRectImgDescr;
        }

        public void accept(RectImgDescrVisitor visitor) {
            visitor.caseDescrAboveDescr(this);
        }

        public RectImgDescription getUnderlyingRectImgDescr() {
            return underlyingRectImgDescr;
        }

        public void setUnderlyingRectImgDescr(RectImgDescription underlyingRectImgDescr) {
            this.underlyingRectImgDescr = underlyingRectImgDescr;
        }

        public RectImgDescription getAboveRectImgDescr() {
            return aboveRectImgDescr;
        }

        public void setAboveRectImgDescr(RectImgDescription aboveRectImgDescr) {
            this.aboveRectImgDescr = aboveRectImgDescr;
        }
        
    }
    
}

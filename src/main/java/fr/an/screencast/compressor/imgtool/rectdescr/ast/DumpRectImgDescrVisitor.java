package fr.an.screencast.compressor.imgtool.rectdescr.ast;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import fr.an.screencast.compressor.imgtool.glyph.GlyphIndexOrCode;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.BorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.ColumnsSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.FillRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.GlyphRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.HorizontalSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.LeftRightBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.LinesSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RawDataRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RectImgAboveRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RectImgDescription;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RoundBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.TopBottomBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.VerticalSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.Border;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.screencast.compressor.utils.Segment;

/**
 * RectImgDescrVisitor implementation for recursive dumping RectImgDescr as indented plain text
 */
public class DumpRectImgDescrVisitor extends RectImgDescrVisitor {

    private PrintStream out;

    private int indentLevel;
    
    private Rect roi;
    
    // ------------------------------------------------------------------------

    public DumpRectImgDescrVisitor(PrintStream out, Rect roi) {
        this.out = out;
        this.roi = roi;
    }

    // ------------------------------------------------------------------------

    public static String dumpToString(RectImgDescription node) {
        return dumpToString(node, null);
    }
    
    public static String dumpToString(RectImgDescription node, Rect roi) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer);
        dumpTo(out, node, roi);
        out.flush();
        return buffer.toString();
    }

    public static void dumpTo(PrintStream out, RectImgDescription node) {
        dumpTo(out, node, null);
    }
    
    public static void dumpTo(PrintStream out, RectImgDescription node, Rect roi) {
        DumpRectImgDescrVisitor visitor = new DumpRectImgDescrVisitor(out, roi);
        node.accept(visitor);
    }

    protected void print(String text) {
        out.print(text);
    }
    
    protected void printlnIndent(String text) {
        printIndent();
        print(text);
        println();
    }

    protected void println() {
        out.println();
    }
    
    protected void printlnIndent(String name, RectImgDescription node) {
        if (node != null) {
            if (roi == null || node.getRect().isIntersect(roi)) {
                printlnIndent(name);
                incrIndent();
                node.accept(this);
                decrIndent();
            }
        }
    }

    private void printIndent(String text) {
        printIndent();
        print(text);
    }

    private void printIndent() {
        for(int i = 0; i < indentLevel; i++) {
            out.print("  ");
        }
    }
    
    protected void incrIndent() {
        indentLevel++;
    }
    protected void decrIndent() {
        indentLevel--;
    }
    
    
    @Override
    public void caseFillRect(FillRectImgDescr node) {
        Rect rect = node.getRect();
        int color = node.getColor();
        printlnIndent("Fill " + rect + " color:" + RGBUtils.toString(color));
    }

    @Override
    public void caseRoundBorderDescr(RoundBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int cornerBackgroundColor = node.getCornerBackgroundColor();
        final Dim topCornerDim = node.getTopCornerDim();
        final Dim bottomCornerDim = node.getBottomCornerDim();
        final int borderColor = node.getBorderColor();
        final int borderThick = node.getBorderThick();
        final RectImgDescription inside = node.getInside();

        printlnIndent("RoundBorder " + rect 
                + " borderThick:" + borderThick
                + " borderColor:" + RGBUtils.toString(borderColor)
                + " corner dim top: " + topCornerDim + " bottom:" + bottomCornerDim 
                + " cornerBgColor:" + RGBUtils.toString(cornerBackgroundColor)
                );
        printlnIndent("inside", inside);
    }

    @Override
    public void caseBorderDescr(BorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int borderColor = node.getBorderColor();
        final Border border = node.getBorder();
        final RectImgDescription inside = node.getInside();

        printlnIndent("Border " + rect 
            + " border:" + border
            + " borderColor:" + RGBUtils.toString(borderColor)
            );
        printlnIndent("inside", inside);
    }

    @Override
    public void caseTopBottomBorderDescr(TopBottomBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int borderColor = node.getBorderColor();
        final int topBorder = node.getTopBorder();
        final int bottomBorder = node.getBottomBorder();
        final RectImgDescription inside = node.getInside();

        printlnIndent("TopBottomBorder " + rect 
            + " border top:" + topBorder + " bottom:" + bottomBorder
            + " borderColor:" + RGBUtils.toString(borderColor)
            );
        printlnIndent("inside", inside);
    }

    @Override
    public void caseLeftRightBorderDescr(LeftRightBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int borderColor = node.getBorderColor();
        final int leftBorder = node.getLeftBorder();
        final int rightBorder = node.getRightBorder();
        final RectImgDescription inside = node.getInside();

        printlnIndent("LeftRightBorder " + rect 
            + " border left:" + leftBorder + " right:" + rightBorder
            + " borderColor:" + RGBUtils.toString(borderColor)
            );
        printlnIndent("inside", inside);
    }

    @Override
    public void caseVerticalSplitDescr(VerticalSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final RectImgDescription left = node.getLeft();
        final Segment splitBorder = node.getSplitBorder();
        final int splitColor = node.getSplitColor();
        final RectImgDescription right = node.getRight();

        printlnIndent("VerticalSplit " + rect 
            + " splitBorder:" + splitBorder + " color:" + RGBUtils.toString(splitColor));
        printlnIndent("left", left);
        printlnIndent("right", right);
    }

    @Override
    public void caseHorizontalSplitDescr(HorizontalSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final RectImgDescription down = node.getDown();
        final Segment splitBorder = node.getSplitBorder();
        final int splitColor = node.getSplitColor();
        final RectImgDescription up = node.getUp();

        printlnIndent("HorizontalSplit " + rect 
            + " splitBorder:" + splitBorder + " color:" + RGBUtils.toString(splitColor));
        printlnIndent("up", up);
        printlnIndent("down", down);
    }

    @Override
    public void caseLinesSplitDescr(LinesSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final int backgroundColor = node.getBackgroundColor();
        final Segment[] splitBorders = node.getSplitBorders();
        final RectImgDescription[] lines = node.getLines();

        printIndent();
        print("LinesSplit " + rect 
            + " backgroundColor:" + RGBUtils.toString(backgroundColor) + " splitBorders:");
        if (splitBorders != null) {
            for(Segment b : splitBorders) {
                print(b + ", ");
            }
        } else {
            print("null");
        }
        println();
        
        if (lines != null) {
            printlnIndent("lines:");
            int index = 0;
            for(RectImgDescription line : lines) {
                printlnIndent("line[" + index + "]", line);
                index++;
            }
        } else {
            // printlnIndent("lines: null");
        }
    }

    @Override
    public void caseColumnsSplitDescr(ColumnsSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final int backgroundColor = node.getBackgroundColor();
        final Segment[] splitBorders = node.getSplitBorders();
        final RectImgDescription[] columns = node.getColumns();
        
        printIndent("ColumnsSplit " + rect 
            + " backgroundColor:" + RGBUtils.toString(backgroundColor) + " splitBorders:");
        if (splitBorders != null) {
            for(Segment b : splitBorders) {
                print(b + ", ");
            }
        } else {
            print("null");
        }
        println();
        
        if (columns != null) {
            printlnIndent("columns:");
            int index = 0;
            for(RectImgDescription column : columns) {
                printlnIndent("col[" + index + "]", column);
                index++;
            }
        } else {
            // printlnIndent("lines: null");
        }

    }

    @Override
    public void caseRawDataDescr(RawDataRectImgDescr node) {
        final Rect rect = node.getRect();
        final int[] rawData = node.getRawData();
        printlnIndent("RawData " + rect + " => data.length:" + ((rawData != null)? rawData.length : -1));
    }

    @Override
    public void caseGlyphDescr(GlyphRectImgDescr node) {
        final Rect rect = node.getRect();
        final GlyphIndexOrCode glyphIndexOrCode = node.getGlyphIndexOrCode();
        printlnIndent("Glyph " + rect + " " + glyphIndexOrCode);
    }

    @Override
    public void caseDescrAboveDescr(RectImgAboveRectImgDescr node) {
        final Rect rect = node.getRect();
        final RectImgDescription underlying = node.getUnderlyingRectImgDescr();
        final RectImgDescription above = node.getAboveRectImgDescr();
        final Rect aboveRect = node.getAboveRect();
        printlnIndent("Above " + rect + ((above != null)? " aboveRect:" + aboveRect : ""));
        printlnIndent("underlying", underlying);
        printlnIndent("above", above);
    }

}

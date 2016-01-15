package fr.an.screencast.compressor.imgtool.rectdescr.ast.helper;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import fr.an.screencast.compressor.imgtool.glyph.GlyphIndexOrCode;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.AnalysisProxyRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.BorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.ColumnsSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.ConnexSegmentLinesNoiseFragment;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.FillRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.GlyphRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.HorizontalSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.LeftRightBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.LinesSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.NoiseAbovePartsRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.NoiseFragment;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.OverrideAttributesProxyRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.PtNoiseFragment;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RawDataRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RectImgAboveRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RectImgDescription;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RootRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RoundBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.SegmentNoiseFragment;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.TopBottomBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.VerticalSplitRectImgDescr;
import fr.an.screencast.compressor.utils.Border;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.screencast.compressor.utils.Segment;

/**
 * RectImgDescrVisitor implementation for recursive dumping RectImgDescr as indented plain text
 */
public class DumpRectImgDescrVisitor extends AbstractRectImgDescrROIVisitor {

    private PrintStream out;

    private int indentLevel;
    
    
    // ------------------------------------------------------------------------

    public DumpRectImgDescrVisitor(PrintStream out, Rect roi) {
        super(roi);
        this.out = out;
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
    
    protected String colorToString(int color) {
        return colorToString(color);
    }
    
    // ------------------------------------------------------------------------
    
    @Override
    public void caseRoot(RootRectImgDescr node) {
        node.getTarget().accept(this);
    }

    @Override
    public void caseFill(FillRectImgDescr node) {
        Rect rect = node.getRect();
        int color = node.getColor();
        printlnIndent("Fill " + rect + " color:" + colorToString(color));
    }

    @Override
    public void caseRoundBorder(RoundBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int cornerBackgroundColor = node.getCornerBackgroundColor();
        final Dim topCornerDim = node.getTopCornerDim();
        final Dim bottomCornerDim = node.getBottomCornerDim();
        final int borderColor = node.getBorderColor();
        final int borderThick = node.getBorderThick();
        final RectImgDescription inside = node.getInside();

        printlnIndent("RoundBorder " + rect 
                + " borderThick:" + borderThick
                + " borderColor:" + colorToString(borderColor)
                + " corner dim top: " + topCornerDim + " bottom:" + bottomCornerDim 
                + " cornerBgColor:" + colorToString(cornerBackgroundColor)
                );
        printlnIndent("inside", inside);
    }

    @Override
    public void caseBorder(BorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int borderColor = node.getBorderColor();
        final Border border = node.getBorder();
        final RectImgDescription inside = node.getInside();

        printlnIndent("Border " + rect 
            + " border:" + border
            + " borderColor:" + colorToString(borderColor)
            );
        printlnIndent("inside", inside);
    }

    @Override
    public void caseTopBottomBorder(TopBottomBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int borderColor = node.getBorderColor();
        final int topBorder = node.getTopBorder();
        final int bottomBorder = node.getBottomBorder();
        final RectImgDescription inside = node.getInside();

        printlnIndent("TopBottomBorder " + rect 
            + " border top:" + topBorder + " bottom:" + bottomBorder
            + " borderColor:" + colorToString(borderColor)
            );
        printlnIndent("inside", inside);
    }

    @Override
    public void caseLeftRightBorder(LeftRightBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int borderColor = node.getBorderColor();
        final int leftBorder = node.getLeftBorder();
        final int rightBorder = node.getRightBorder();
        final RectImgDescription inside = node.getInside();

        printlnIndent("LeftRightBorder " + rect 
            + " border left:" + leftBorder + " right:" + rightBorder
            + " borderColor:" + colorToString(borderColor)
            );
        printlnIndent("inside", inside);
    }

    @Override
    public void caseVerticalSplit(VerticalSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final RectImgDescription left = node.getLeft();
        final Segment splitBorder = node.getSplitBorder();
        final int splitColor = node.getSplitColor();
        final RectImgDescription right = node.getRight();

        printlnIndent("VerticalSplit " + rect 
            + " splitBorder:" + splitBorder + " color:" + colorToString(splitColor));
        printlnIndent("left", left);
        printlnIndent("right", right);
    }

    @Override
    public void caseHorizontalSplit(HorizontalSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final RectImgDescription down = node.getDown();
        final Segment splitBorder = node.getSplitBorder();
        final int splitColor = node.getSplitColor();
        final RectImgDescription up = node.getUp();

        printlnIndent("HorizontalSplit " + rect 
            + " splitBorder:" + splitBorder + " color:" + colorToString(splitColor));
        printlnIndent("up", up);
        printlnIndent("down", down);
    }

    @Override
    public void caseLinesSplit(LinesSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final int backgroundColor = node.getBackgroundColor();
        final Segment[] splitBorders = node.getSplitBorders();
        final RectImgDescription[] lines = node.getLines();

        printIndent();
        print("LinesSplit " + rect 
            + " backgroundColor:" + colorToString(backgroundColor) + " splitBorders:");
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
    public void caseColumnsSplit(ColumnsSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final int backgroundColor = node.getBackgroundColor();
        final Segment[] splitBorders = node.getSplitBorders();
        final RectImgDescription[] columns = node.getColumns();
        
        printIndent("ColumnsSplit " + rect 
            + " backgroundColor:" + colorToString(backgroundColor) + " splitBorders:");
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
    public void caseRawData(RawDataRectImgDescr node) {
        final Rect rect = node.getRect();
        final int[] rawData = node.getRawData();
        printlnIndent("RawData " + rect + " => data.length:" + ((rawData != null)? rawData.length : -1));
    }

    @Override
    public void caseGlyph(GlyphRectImgDescr node) {
        final Rect rect = node.getRect();
        final GlyphIndexOrCode glyphIndexOrCode = node.getGlyphIndexOrCode();
        printlnIndent("Glyph " + rect + " " + glyphIndexOrCode);
    }

    @Override
    public void caseAbove(RectImgAboveRectImgDescr node) {
        final RectImgDescription underlying = node.getUnderlying();
        final RectImgDescription[] aboves = node.getAboves();
        printlnIndent("underlying", underlying);
        final int aboveCount = (aboves != null)? aboves.length : 0;
        printlnIndent("aboveCount:" + aboveCount);
        for (int i= 0; i < aboveCount; i++) {
            RectImgDescription above = aboves[i];
            // final Rect aboveRect = above.getRect();
            // printlnIndent("Above " + rect + ((above != null)? " aboveRect:" + aboveRect : ""));
            printlnIndent("above", above);
        }
    }

    
    @Override
    public void caseNoiseAboveParts(NoiseAbovePartsRectImgDescr node) {
        printlnIndent("underlying", node.getUnderlying());
        NoiseFragment[][] noiseFragmentsAboveParts = node.getNoiseFragmentsAboveParts();
        if (noiseFragmentsAboveParts != null) {
            for (int part = 0; part < noiseFragmentsAboveParts.length; part++) {
                NoiseFragment[] frags = noiseFragmentsAboveParts[part];
                if (frags != null) {
                    printlnIndent("part["+ part + "] fragsCount:" + frags.length);
                    for(NoiseFragment frag : frags) {
                        incrIndent();
                        frag.accept(this, node, part);
                        decrIndent();
                    }
                }
            }
        }
    }


    @Override
    public void caseNoiseAboveParts_Pt(NoiseAbovePartsRectImgDescr parent, int partIndex, PtNoiseFragment node) {
        printlnIndent("pt:" + node.getX()+ "," + node.getY() + " : " + colorToString(node.getColor())); 
    }

    @Override
    public void caseNoiseAboveParts_Segment(NoiseAbovePartsRectImgDescr parent, int partIndex, SegmentNoiseFragment node) {
        printlnIndent("segment: [" + node.getFromX()+ "," + node.getToX() + "( "+ node.getY() + " : " + colorToString(node.getColor())); 
    }

    @Override
    public void caseNoiseAboveParts_ConnexSegmentLines(NoiseAbovePartsRectImgDescr parent, int partIndex, ConnexSegmentLinesNoiseFragment node) {
        Segment[] lines = node.getLines();
        printlnIndent("connexSegmentLines:" + colorToString(node.getColor()) + " y:" + node.getFromY() + " " + Segment.toString(lines));
    }

    @Override
    public void caseOverrideAttributesProxy(OverrideAttributesProxyRectImgDescr node) {
        printlnIndent("attributeOverrides:" + node.getAttributeOverrides());
        printlnIndent("underlying", node.getUnderlying());
    }
    
    @Override
    public void caseAnalysisProxy(AnalysisProxyRectImgDescr node) {
        RectImgDescription target = node.getTarget();
        printlnIndent("target", target);        
    }
    
}

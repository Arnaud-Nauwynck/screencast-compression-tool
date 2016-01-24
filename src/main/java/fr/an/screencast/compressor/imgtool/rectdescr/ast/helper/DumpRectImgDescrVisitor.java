package fr.an.screencast.compressor.imgtool.rectdescr.ast.helper;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import fr.an.screencast.compressor.imgtool.glyph.GlyphIndexOrCode;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.AnalysisProxyRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.BorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.ColumnsSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.ConnexSegmentLinesNoiseFragment;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.FillRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.GlyphRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.HorizontalSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.LeftRightBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.LinesSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.NoiseAbovePartsRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.NoiseFragment;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.OverrideAttributesProxyRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.PtNoiseFragment;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RawDataRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RectImgAboveRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RootRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RoundBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.SegmentNoiseFragment;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.TopBottomBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.VerticalSplitRectImgDescr;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
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

    public static String dumpToString(RectImgDescr node) {
        return dumpToString(node, null);
    }
    
    public static String dumpToString(RectImgDescr node, Rect roi) {
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer);
        dumpTo(out, node, roi);
        out.flush();
        return buffer.toString();
    }

    public static void dumpTo(PrintStream out, RectImgDescr node) {
        dumpTo(out, node, null);
    }
    
    public static void dumpTo(PrintStream out, RectImgDescr node, Rect roi) {
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
    
    protected void printlnIndent(String name, RectImgDescr node) {
        if (node != null) {
            if (roi == null || node.getRect().isIntersect(roi)) {
                if (maxLevel == -1 || currLevel < maxLevel) {
                    currLevel++;
                    
                    printlnIndent(name);
                    incrIndent();
                    
                    // recurse(node) ==> re-test isIntersect() + test malLevel..
                    // node.accept(this);
                    recurse(node);
                    
                    decrIndent();
                 
                    currLevel--;
                }
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
        return RGBUtils.toString(color);
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
        final RectImgDescr inside = node.getInside();

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
        final RectImgDescr inside = node.getInside();

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
        final RectImgDescr inside = node.getInside();

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
        final RectImgDescr inside = node.getInside();

        printlnIndent("LeftRightBorder " + rect 
            + " border left:" + leftBorder + " right:" + rightBorder
            + " borderColor:" + colorToString(borderColor)
            );
        printlnIndent("inside", inside);
    }

    @Override
    public void caseVerticalSplit(VerticalSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final RectImgDescr left = node.getLeft();
        final Segment splitBorder = node.getSplitBorder();
        final int splitColor = node.getSplitColor();
        final RectImgDescr right = node.getRight();

        printlnIndent("VerticalSplit " + rect 
            + " splitBorder:" + splitBorder + " color:" + colorToString(splitColor));
        printlnIndent("left", left);
        printlnIndent("right", right);
    }

    @Override
    public void caseHorizontalSplit(HorizontalSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final RectImgDescr down = node.getDown();
        final Segment splitBorder = node.getSplitBorder();
        final int splitColor = node.getSplitColor();
        final RectImgDescr up = node.getUp();

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
        final RectImgDescr[] lines = node.getLines();

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
            for(RectImgDescr line : lines) {
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
        final RectImgDescr[] columns = node.getColumns();
        
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
            for(RectImgDescr column : columns) {
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
        final RectImgDescr underlying = node.getUnderlying();
        final RectImgDescr[] aboves = node.getAboves();
        printlnIndent("underlying", underlying);
        final int aboveCount = (aboves != null)? aboves.length : 0;
        printlnIndent("aboveCount:" + aboveCount);
        for (int i= 0; i < aboveCount; i++) {
            RectImgDescr above = aboves[i];
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
        RectImgDescr target = node.getTarget();
        printlnIndent("target", target);        
    }
    
}

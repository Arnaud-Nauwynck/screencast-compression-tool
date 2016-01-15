package fr.an.screencast.compressor.imgtool.rectdescr.ast.helper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrVisitor;
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
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.utils.Border;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.screencast.compressor.utils.Segment;

/**
 * RectImgDescrVisitor implementation for recursive debug drawing decorations (borders, label, ..) on image 
 */
public class DebugDrawDecoratorRectImgDescrVisitor extends RectImgDescrVisitor {

    private BufferedImage img;
    private int W; // = img.getWidth();
    private int[] imgData; // = ImageRasterUtil.toInts(img); 
    private Graphics2D g2d; // = (Graphics2D) g2d.create();
    
    private BasicStroke debugFillStroke = new BasicStroke(2);
    private Color debugFillColor = Color.BLUE;
    
    private BasicStroke debugBorderStroke = new BasicStroke(2);
    private Color debugBorderColor = Color.BLUE;
//    private Color debugRoundBorderColor = Color.BLUE;
    
    private BasicStroke debugGlyphStroke = new BasicStroke(1);
    private Color debugGlyphColor = Color.GREEN;
    
    private BasicStroke debugRawDataStroke = new BasicStroke(3);
    private Color debugRawDataColor = Color.RED;
    
    private BasicStroke debugAboveStroke = new BasicStroke(3);
    private Color debugAboveColor = Color.RED;
    
    // ------------------------------------------------------------------------

    public DebugDrawDecoratorRectImgDescrVisitor(BufferedImage img) {
        this.img = img;
        this.W = img.getWidth();
        this.imgData = ImageRasterUtils.toInts(img);
        this.g2d = img.createGraphics();
    }

    // ------------------------------------------------------------------------
    
    public BufferedImage getImg() {
        return img;
    }

    protected void draw(RectImgDescription node) {
        if (node != null) {
            node.accept(this);
        }
    }
    
    protected void drawRect(Rect rect, Color color, BasicStroke stroke) {
        g2d.setStroke(stroke);
        g2d.setColor(color);
        
        int st2 = (int) stroke.getLineWidth()/2;
        g2d.drawRect(rect.fromX+st2, rect.fromY+st2, rect.getWidth()-2*st2, rect.getHeight()-2*st2);
    }

    private void drawRectDiagonales(final Rect rect) {
        g2d.drawLine(rect.fromX, rect.fromY, rect.toX, rect.toY);
        g2d.drawLine(rect.fromX, rect.toY, rect.toX, rect.fromY);
    }

    

    @Override
    public void caseRoot(RootRectImgDescr node) {
        draw(node.getTarget());
    }
    
    @Override
    public void caseFill(FillRectImgDescr node) {
        Rect rect = node.getRect();
        // int color = node.getColor();
        
        drawRect(rect, debugFillColor, debugFillStroke);
    }

    @Override
    public void caseRoundBorder(RoundBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        // final int cornerBackgroundColor = node.getCornerBackgroundColor();
//        final Dim topCornerDim = node.getTopCornerDim();
//        final Dim bottomCornerDim = node.getBottomCornerDim();
//        final int borderColor = node.getBorderColor();
        final int borderThick = node.getBorderThick();
        final RectImgDescription inside = node.getInside();
        
        drawRect(rect, debugBorderColor, debugBorderStroke);

        g2d.drawRect(rect.fromX+borderThick, rect.fromY+borderThick, rect.getWidth()-2*+borderThick, rect.getHeight()-2*+borderThick);
     
        draw(inside);
    }

    @Override
    public void caseBorder(BorderRectImgDescr node) {
        final Rect rect = node.getRect();
        // final int borderColor = node.getBorderColor();
        final Border border = node.getBorder();
        final RectImgDescription inside = node.getInside();
        if (border == null) {
            return;
        }
        g2d.setStroke(debugBorderStroke);
        g2d.setColor(debugBorderColor);
        // ?? g2d.setColor(debugFillRectColor);
        
        final int w = rect.getWidth(), h = rect.getHeight();
        g2d.drawRect(rect.fromX, rect.fromY, w, h);
        g2d.drawRect(rect.fromX+border.left, rect.fromY+border.top, w-border.left-border.right, h-border.top-border.bottom);

        draw(inside);
    }

    @Override
    public void caseTopBottomBorder(TopBottomBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int borderColor = node.getBorderColor();
        final int topBorder = node.getTopBorder();
        final int bottomBorder = node.getBottomBorder();
        final RectImgDescription inside = node.getInside();

        g2d.setColor(new Color(borderColor));
        g2d.setStroke(debugBorderStroke);

        final int rectW = rect.getWidth();
        if (topBorder > 0) {
            g2d.drawRect(rect.fromX, rect.fromY, rectW, topBorder);
        }
        if (bottomBorder > 0) {
            g2d.drawRect(rect.fromX, rect.toY - bottomBorder, rectW, bottomBorder);
        }
        
        draw(inside);
    }

    @Override
    public void caseLeftRightBorder(LeftRightBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int leftBorder = node.getLeftBorder();
        final int rightBorder = node.getRightBorder();
        final RectImgDescription inside = node.getInside();

        g2d.setStroke(debugBorderStroke);
        g2d.setColor(debugBorderColor);
        final int rectH = rect.getHeight();
        if (leftBorder > 0) {
            g2d.drawRect(rect.fromX, rect.fromY, leftBorder, rectH);
        }
        if (rightBorder > 0) {
            g2d.drawRect(rect.toX - rightBorder, rect.fromY, rightBorder, rectH);
        }
        
        draw(inside);
    }

    @Override
    public void caseVerticalSplit(VerticalSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final RectImgDescription left = node.getLeft();
        final Segment splitBorder = node.getSplitBorder();
        final RectImgDescription right = node.getRight();
        draw(left);
        if (splitBorder != null) {
            g2d.setStroke(debugBorderStroke);
            g2d.setColor(debugFillColor);
            g2d.drawRect(splitBorder.from, rect.fromY, splitBorder.to - splitBorder.from, rect.getHeight());
        }
        draw(right);
    }

    @Override
    public void caseHorizontalSplit(HorizontalSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final RectImgDescription down = node.getDown();
        final Segment splitBorder = node.getSplitBorder();
        final RectImgDescription up = node.getUp();
        draw(down);
        if (splitBorder != null) {
            g2d.setStroke(debugBorderStroke);
            g2d.setColor(debugFillColor);
            g2d.drawRect(rect.fromX, splitBorder.from, rect.getWidth(), splitBorder.to - splitBorder.from);
        }
        draw(up);
    }

    @Override
    public void caseLinesSplit(LinesSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final Segment[] splitBorders = node.getSplitBorders();
        final RectImgDescription[] lines = node.getLines();
        if (splitBorders != null) {
            g2d.setStroke(debugBorderStroke);
            g2d.setColor(debugFillColor);
            final int rectW = rect.getWidth();
            for(Segment b : splitBorders) {
                g2d.drawRect(rect.fromX, b.from, rectW, b.to - b.from);
            }
        }
        if (lines != null) {
            for(RectImgDescription line : lines) {
                draw(line);
            }
        }
    }

    @Override
    public void caseColumnsSplit(ColumnsSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final Segment[] splitBorders = node.getSplitBorders();
        final RectImgDescription[] columns = node.getColumns();
        if (splitBorders != null) {
            g2d.setStroke(debugBorderStroke);
            g2d.setColor(debugFillColor);
            final int rectH = rect.getHeight();
            for(Segment b : splitBorders) {
                g2d.drawRect(b.from, rect.fromY, b.to - b.from, rectH);
            }
        }
        if (columns != null) {
            for(RectImgDescription column : columns) {
                draw(column);
            }
        }
    }

    @Override
    public void caseRawData(RawDataRectImgDescr node) {
        final Rect rect = node.getRect();
        
        g2d.setStroke(debugRawDataStroke);
        g2d.setColor(debugRawDataColor);
        g2d.drawRect(rect.fromX, rect.fromY, rect.getWidth(), rect.getHeight());
        // draw diagonal lines
        drawRectDiagonales(rect);
    }

    @Override
    public void caseGlyph(GlyphRectImgDescr node) {
        final Rect rect = node.getRect();
        
        g2d.setStroke(debugGlyphStroke);
        g2d.setColor(debugGlyphColor);
        g2d.drawRect(rect.fromX, rect.fromY, rect.getWidth(), rect.getHeight());
    }

    @Override
    public void caseAbove(RectImgAboveRectImgDescr node) {
        final RectImgDescription underlying = node.getUnderlying();
        final RectImgDescription[] aboves = node.getAboves();
        draw(underlying);
        if (aboves != null) {
            int aboveCount = (aboves != null)? aboves.length : 0;
            for (int i = 0; i < aboveCount; i++) {
                draw(aboves[i]);
                drawRect(aboves[i].getRect(), debugAboveColor, debugAboveStroke);
            }
        }
    }

    @Override
    public void caseAnalysisProxy(AnalysisProxyRectImgDescr node) {
        RectImgDescription target = node.getTarget();
        draw(target);
    }

    @Override
    public void caseNoiseAboveParts(NoiseAbovePartsRectImgDescr node) {
        RectImgDescription underlying = node.getUnderlying();
        draw(underlying);
        
        NoiseFragment[][] noiseFragmentsAboveParts = node.getNoiseFragmentsAboveParts();
        if (noiseFragmentsAboveParts != null) {
            for (int part = 0; part < noiseFragmentsAboveParts.length; part++) {
                NoiseFragment[] frags = noiseFragmentsAboveParts[part];
                if (frags != null) {
                    for(NoiseFragment frag : frags) {
                        frag.accept(this, node, part);
                    }
                }
            }
        }
    }
    
    @Override
    public void caseNoiseAboveParts_Pt(NoiseAbovePartsRectImgDescr parent, int partIndex, PtNoiseFragment node) {
        final int x = node.getX(), y = node.getY(), color = node.getColor();
        int idx = y * W + x; 
        imgData[idx] = color;
    }

    @Override
    public void caseNoiseAboveParts_Segment(NoiseAbovePartsRectImgDescr parent, int partIndex, SegmentNoiseFragment node) {
        final int fromX = node.getFromX(), toX = node.getToX(), y = node.getY(), color = node.getColor();
        final int toIdx = y * W + toX;  
        for(int idx = y * W + fromX; idx < toIdx; idx++) {
            imgData[idx] = color;
        }
    }

    @Override
    public void caseNoiseAboveParts_ConnexSegmentLines(NoiseAbovePartsRectImgDescr parent, int partIndex, ConnexSegmentLinesNoiseFragment node) {
        final int fromY = node.getFromY(), color = node.getColor();
        final Segment[] lines = node.getLines();
        if (lines == null) return;
        final int linesLen = lines.length; 
        for(int lineI = 0, idxY = fromY * W; lineI < linesLen; lineI++,idxY+=W) { 
            int fromIdx = idxY + lines[lineI].from;
            int toIdx = idxY + lines[lineI].to;
            for (int idx = fromIdx; idx < toIdx; idx++) {
                imgData[idx] = color; 
            }
        }
    }

    @Override
    public void caseOverrideAttributesProxy(OverrideAttributesProxyRectImgDescr node) {
        draw(node.getUnderlying());
    }

}

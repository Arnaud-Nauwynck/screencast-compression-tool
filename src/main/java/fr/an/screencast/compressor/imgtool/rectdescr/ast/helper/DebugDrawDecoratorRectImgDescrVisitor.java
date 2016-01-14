package fr.an.screencast.compressor.imgtool.rectdescr.ast.helper;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrVisitor;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.AnalysisProxyRectImgDescr;
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
import fr.an.screencast.compressor.utils.Border;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.screencast.compressor.utils.Segment;

/**
 * RectImgDescrVisitor implementation for recursive debug drawing decorations (borders, label, ..) on image 
 */
public class DebugDrawDecoratorRectImgDescrVisitor extends RectImgDescrVisitor {

    private BufferedImage img;
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
    public void caseFillRect(FillRectImgDescr node) {
        Rect rect = node.getRect();
        // int color = node.getColor();
        
        drawRect(rect, debugFillColor, debugFillStroke);
    }

    @Override
    public void caseRoundBorderDescr(RoundBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        // final int cornerBackgroundColor = node.getCornerBackgroundColor();
//        final Dim topCornerDim = node.getTopCornerDim();
//        final Dim bottomCornerDim = node.getBottomCornerDim();
//        final int borderColor = node.getBorderColor();
        final int borderThick = node.getBorderThick();
        final RectImgDescription inside = node.getInside();
        
        drawRect(rect, debugBorderColor, debugBorderStroke);

        g2d.drawRect(rect.fromX+borderThick, rect.fromY+borderThick, rect.getWidth()-2*+borderThick, rect.getHeight()-2*+borderThick);
     
        if (inside != null) {
            inside.accept(this);
        }
    }

    @Override
    public void caseBorderDescr(BorderRectImgDescr node) {
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
    public void caseTopBottomBorderDescr(TopBottomBorderRectImgDescr node) {
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
    public void caseLeftRightBorderDescr(LeftRightBorderRectImgDescr node) {
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
    public void caseVerticalSplitDescr(VerticalSplitRectImgDescr node) {
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
    public void caseHorizontalSplitDescr(HorizontalSplitRectImgDescr node) {
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
    public void caseLinesSplitDescr(LinesSplitRectImgDescr node) {
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
    public void caseColumnsSplitDescr(ColumnsSplitRectImgDescr node) {
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
    public void caseRawDataDescr(RawDataRectImgDescr node) {
        final Rect rect = node.getRect();
        
        g2d.setStroke(debugRawDataStroke);
        g2d.setColor(debugRawDataColor);
        g2d.drawRect(rect.fromX, rect.fromY, rect.getWidth(), rect.getHeight());
        // draw diagonal lines
        drawRectDiagonales(rect);
    }

    @Override
    public void caseGlyphDescr(GlyphRectImgDescr node) {
        final Rect rect = node.getRect();
        
        g2d.setStroke(debugGlyphStroke);
        g2d.setColor(debugGlyphColor);
        g2d.drawRect(rect.fromX, rect.fromY, rect.getWidth(), rect.getHeight());
    }

    @Override
    public void caseAboveDescr(RectImgAboveRectImgDescr node) {
        final RectImgDescription underlying = node.getUnderlyingRectImgDescr();
        final RectImgDescription[] aboves = node.getAboveRectImgDescrs();
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
    public void caseAnalysisProxyRect(AnalysisProxyRectImgDescr node) {
        RectImgDescription target = node.getTarget();
        draw(target);
    }
    
}

package fr.an.screencast.compressor.imgtool.rectdescr.ast;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import fr.an.screencast.compressor.imgtool.glyph.GlyphIndexOrCode;
import fr.an.screencast.compressor.imgtool.glyph.GlyphMRUTable;
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
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.utils.Border;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.screencast.compressor.utils.Segment;

/**
 * RectImgDescrVisitor implementation for recursive drawing of RectImgDescr
 */
public class DrawRectImgDescrVisitor extends RectImgDescrVisitor {

    private BufferedImage img;

    private Graphics2D g2d; // = (Graphics2D) g2d.create();
    private Dim imgDim; // = new Dim(img.getWidth(), img.getHeight());
    private int[] imgData; // = ImageRasterUtils.toInts(img);

    private GlyphMRUTable glyphMRUTable;
    
    // ------------------------------------------------------------------------

    public DrawRectImgDescrVisitor(BufferedImage img, GlyphMRUTable glyphMRUTable) {
        this.img = img;
        this.g2d = img.createGraphics();
        this.imgDim = new Dim(img.getWidth(), img.getHeight());
        this.imgData = ImageRasterUtils.toInts(img);
        this.glyphMRUTable = glyphMRUTable;
    }

    // ------------------------------------------------------------------------
    
    public BufferedImage getImg() {
        return img;
    }

    public GlyphMRUTable getGlyphMRUTable() {
        return glyphMRUTable;
    }
    
    @Override
    public void caseFillRect(FillRectImgDescr node) {
        Rect rect = node.getRect();
        int color = node.getColor();
        
        g2d.setColor(new Color(color));
        g2d.fillRect(rect.fromX, rect.fromY, rect.getWidth(), rect.getHeight());
    }

    @Override
    public void caseRoundBorderDescr(RoundBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int cornerBackgroundColor = node.getCornerBackgroundColor();
        final Dim topCornerDim = node.getTopCornerDim();
        final Dim bottomCornerDim = node.getBottomCornerDim();
        final int borderColor = node.getBorderColor();
        final int borderThick = node.getBorderThick();
        final RectImgDescription insideRect = node.getInside();
        
        g2d.setColor(new Color(cornerBackgroundColor));
        final int tcW = topCornerDim.width;
        final int tcH = topCornerDim.height;
        if (topCornerDim != null && topCornerDim.isNotEmpty()) {
            g2d.fillRect(rect.fromX, rect.fromY, tcW, tcH);
            g2d.fillRect(rect.toX - tcW, rect.fromY, tcW, tcH);
        }
        final int bcW = bottomCornerDim.width;
        final int bcH = bottomCornerDim.height;
        if (bottomCornerDim != null && bottomCornerDim.isNotEmpty()) {
            g2d.fillRect(rect.fromX, rect.toY - bcH, bcW, bcH);
            g2d.fillRect(rect.toX - bcW, rect.toY - bcH, bcW, bcH);
        }
        if (borderThick > 0) {
            int rectW = rect.getWidth();
            int rectH = rect.getHeight();
            g2d.setColor(new Color(borderColor));
            // top border
            g2d.drawRect(rect.fromX + tcW, rect.fromY, rectW - tcW - tcW, borderThick);
            // left border
            g2d.drawRect(rect.fromX, rect.fromY + tcH, borderThick, rectH - tcH - bcH);
            // right border
            g2d.drawRect(rect.toX - borderThick, rect.fromY + tcH, borderThick, rectH - tcH - bcH);
            // bottom border
            g2d.drawRect(rect.fromX + bcW, rect.toY - borderThick, rectW - bcW - bcW, borderThick);
        }
     
        if (insideRect != null) {
            insideRect.accept(this);
        }
    }

    @Override
    public void caseBorderDescr(BorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int borderColor = node.getBorderColor();
        final Border border = node.getBorder();
        final RectImgDescription insideRect = node.getInside();
        if (border == null) {
            return;
        }
        g2d.setColor(new Color(borderColor));
        final int rectW = rect.getWidth();
        final int rectHeightMid = rect.getHeight() - border.top - border.bottom ;
        if (border.top > 0) {
            g2d.fillRect(rect.fromX, rect.fromY, rectW, border.top);
        }
        if (border.left > 0) {
            g2d.fillRect(rect.fromX, rect.fromY + border.top, border.left, rectHeightMid);
        }
        if (border.right > 0) {
            g2d.fillRect(rect.toX - border.right, rect.fromY + border.top, border.right, rectHeightMid);
        }
        if (border.bottom > 0) {
            g2d.fillRect(rect.fromX, rect.toY - border.bottom, rectW, border.bottom);
        }
        
        if (insideRect != null) {
            insideRect.accept(this);
        }
    }

    @Override
    public void caseTopBottomBorderDescr(TopBottomBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int borderColor = node.getBorderColor();
        final int topBorder = node.getTopBorder();
        final int bottomBorder = node.getBottomBorder();
        final RectImgDescription insideRect = node.getInside();

        g2d.setColor(new Color(borderColor));
        final int rectW = rect.getWidth();
        if (topBorder > 0) {
            g2d.fillRect(rect.fromX, rect.fromY, rectW, topBorder);
        }
        if (bottomBorder > 0) {
            g2d.fillRect(rect.fromX, rect.toY - bottomBorder, rectW, bottomBorder);
        }
        
        if (insideRect != null) {
            insideRect.accept(this);
        }
    }

    @Override
    public void caseLeftRightBorderDescr(LeftRightBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int borderColor = node.getBorderColor();
        final int leftBorder = node.getLeftBorder();
        final int rightBorder = node.getRightBorder();
        final RectImgDescription insideRect = node.getInside();
        g2d.setColor(new Color(borderColor));
        final int rectH = rect.getHeight();
        if (leftBorder > 0) {
            g2d.fillRect(rect.fromX, rect.fromY, leftBorder, rectH);
        }
        if (rightBorder > 0) {
            g2d.fillRect(rect.toX - rightBorder, rect.fromY, rightBorder, rectH);
        }
        
        if (insideRect != null) {
            insideRect.accept(this);
        }
    }

    @Override
    public void caseVerticalSplitDescr(VerticalSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final RectImgDescription left = node.getLeft();
        final Segment splitBorder = node.getSplitBorder();
        final int splitColor = node.getSplitColor();
        final RectImgDescription right = node.getRight();
        if (left != null) {
            left.accept(this);
        }
        if (splitBorder != null) {
            g2d.setColor(new Color(splitColor));
            g2d.fillRect(splitBorder.from, rect.fromY, splitBorder.to - splitBorder.from, rect.getHeight());
        }
        if (right != null) {
            right.accept(this);
        }
    }

    @Override
    public void caseHorizontalSplitDescr(HorizontalSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final RectImgDescription down = node.getDown();
        final Segment splitBorder = node.getSplitBorder();
        final int splitColor = node.getSplitColor();
        final RectImgDescription up = node.getUp();
        if (down != null) {
            down.accept(this);
        }
        if (splitBorder != null) {
            g2d.setColor(new Color(splitColor));
            g2d.fillRect(rect.fromX, splitBorder.from, rect.getWidth(), splitBorder.to - splitBorder.from);
        }
        if (up != null) {
            up.accept(this);
        }
    }

    @Override
    public void caseLinesSplitDescr(LinesSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final int backgroundColor = node.getBackgroundColor();
        final Segment[] splitBorders = node.getSplitBorders();
        final RectImgDescription[] lines = node.getLines();
        if (splitBorders != null) {
            g2d.setColor(new Color(backgroundColor));
            final int rectW = rect.getWidth();
            for(Segment b : splitBorders) {
                g2d.fillRect(rect.fromX, b.from, rectW, b.to - b.from);
            }
        }
        if (lines != null) {
            for(RectImgDescription line : lines) {
                line.accept(this);
            }
        }
    }

    @Override
    public void caseColumnsSplitDescr(ColumnsSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final int backgroundColor = node.getBackgroundColor();
        final Segment[] splitBorders = node.getSplitBorders();
        final RectImgDescription[] columns = node.getColumns();
        if (splitBorders != null) {
            g2d.setColor(new Color(backgroundColor));
            final int rectH = rect.getHeight();
            for(Segment b : splitBorders) {
                g2d.fillRect(b.from, rect.fromY, b.to - b.from, rectH);
            }
        }
        if (columns != null) {
            for(RectImgDescription column : columns) {
                column.accept(this);
            }
        }
    }

    @Override
    public void caseRawDataDescr(RawDataRectImgDescr node) {
        final Rect rect = node.getRect();
        final int[] rawData = node.getRawData();
        ImageRasterUtils.drawRectImg(imgDim, imgData, rect, rawData);
    }

    @Override
    public void caseGlyphDescr(GlyphRectImgDescr node) {
        final Rect rect = node.getRect();
        final GlyphIndexOrCode glyphIndexOrCode = node.getGlyphIndexOrCode();
        glyphMRUTable.drawGlyphFindByIndexOrCode(glyphIndexOrCode, imgDim, imgData, rect);
    }

    @Override
    public void caseDescrAboveDescr(RectImgAboveRectImgDescr node) {
        final RectImgDescription underlying = node.getUnderlyingRectImgDescr();
        final RectImgDescription[] aboves = node.getAboveRectImgDescrs();
        if (underlying != null) {
            underlying.accept(this);
        }
        if (aboves != null) {
            int aboveCount = (aboves != null)? aboves.length : 0;
            for (int i = 0; i < aboveCount; i++) {
                aboves[i].accept(this);
            }
        }
    }

}

package fr.an.screencast.compressor.imgtool.rectdescr.ast.helper;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrVisitor;
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
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.utils.Border;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.screencast.compressor.utils.Segment;

/**
 * RectImgDescrVisitor implementation for recursive drawing of RectImgDescr
 */
public class DrawRectImgDescrVisitor extends RectImgDescrVisitor {

    private BufferedImage img;

    private Graphics2D g2d; // = (Graphics2D) g2d.create();
    private Dim imgDim; // = new Dim(img.getWidth(), img.getHeight());
    private int W; // = img.getWidth();
    private int[] imgData; // = ImageRasterUtils.toInts(img);
    
    // ------------------------------------------------------------------------

    public DrawRectImgDescrVisitor(BufferedImage img) {
        this.img = img;
        this.g2d = img.createGraphics();
        this.imgDim = new Dim(img.getWidth(), img.getHeight());
        this.W = img.getWidth();
        this.imgData = ImageRasterUtils.toInts(img);
    }

    // ------------------------------------------------------------------------
    
    public BufferedImage getImg() {
        return img;
    }

    protected void draw(RectImgDescr node) {
        if (node != null) {
            node.accept(this);
        }
    }

    @Override
    public void caseRoot(RootRectImgDescr node) {
        draw(node.getTarget());
    }

    @Override
    public void caseFill(FillRectImgDescr node) {
        Rect rect = node.getRect();
        int color = node.getColor();
        
        g2d.setColor(new Color(color));
        g2d.fillRect(rect.fromX, rect.fromY, rect.getWidth(), rect.getHeight());
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
     
        draw(inside);
    }

    @Override
    public void caseBorder(BorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int borderColor = node.getBorderColor();
        final Border border = node.getBorder();
        final RectImgDescr inside = node.getInside();
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
        
        draw(inside);
    }

    @Override
    public void caseTopBottomBorder(TopBottomBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int borderColor = node.getBorderColor();
        final int topBorder = node.getTopBorder();
        final int bottomBorder = node.getBottomBorder();
        final RectImgDescr inside = node.getInside();

        g2d.setColor(new Color(borderColor));
        final int rectW = rect.getWidth();
        if (topBorder > 0) {
            g2d.fillRect(rect.fromX, rect.fromY, rectW, topBorder);
        }
        if (bottomBorder > 0) {
            g2d.fillRect(rect.fromX, rect.toY - bottomBorder, rectW, bottomBorder);
        }
        
        draw(inside);
    }

    @Override
    public void caseLeftRightBorder(LeftRightBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int borderColor = node.getBorderColor();
        final int leftBorder = node.getLeftBorder();
        final int rightBorder = node.getRightBorder();
        final RectImgDescr inside = node.getInside();

        g2d.setColor(new Color(borderColor));
        final int rectH = rect.getHeight();
        if (leftBorder > 0) {
            g2d.fillRect(rect.fromX, rect.fromY, leftBorder, rectH);
        }
        if (rightBorder > 0) {
            g2d.fillRect(rect.toX - rightBorder, rect.fromY, rightBorder, rectH);
        }
        
        draw(inside);
    }

    @Override
    public void caseVerticalSplit(VerticalSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final RectImgDescr left = node.getLeft();
        final Segment splitBorder = node.getSplitBorder();
        final int splitColor = node.getSplitColor();
        final RectImgDescr right = node.getRight();

        draw(left);
        if (splitBorder != null) {
            g2d.setColor(new Color(splitColor));
            g2d.fillRect(splitBorder.from, rect.fromY, splitBorder.to - splitBorder.from, rect.getHeight());
        }
        draw(right);
    }

    @Override
    public void caseHorizontalSplit(HorizontalSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final RectImgDescr down = node.getDown();
        final Segment splitBorder = node.getSplitBorder();
        final int splitColor = node.getSplitColor();
        final RectImgDescr up = node.getUp();
        
        draw(down);
        if (splitBorder != null) {
            g2d.setColor(new Color(splitColor));
            g2d.fillRect(rect.fromX, splitBorder.from, rect.getWidth(), splitBorder.to - splitBorder.from);
        }
        draw(up);
    }

    @Override
    public void caseLinesSplit(LinesSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final int backgroundColor = node.getBackgroundColor();
        final Segment[] splitBorders = node.getSplitBorders();
        final RectImgDescr[] lines = node.getLines();
        
        if (splitBorders != null) {
            g2d.setColor(new Color(backgroundColor));
            final int rectW = rect.getWidth();
            for(Segment b : splitBorders) {
                g2d.fillRect(rect.fromX, b.from, rectW, b.to - b.from);
            }
        }
        if (lines != null) {
            for(RectImgDescr line : lines) {
                draw(line);
            }
        }
    }

    @Override
    public void caseColumnsSplit(ColumnsSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final int backgroundColor = node.getBackgroundColor();
        final Segment[] splitBorders = node.getSplitBorders();
        final RectImgDescr[] columns = node.getColumns();

        if (splitBorders != null) {
            g2d.setColor(new Color(backgroundColor));
            final int rectH = rect.getHeight();
            for(Segment b : splitBorders) {
                g2d.fillRect(b.from, rect.fromY, b.to - b.from, rectH);
            }
        }
        if (columns != null) {
            for(RectImgDescr column : columns) {
                draw(column);
            }
        }
    }

    @Override
    public void caseRawData(RawDataRectImgDescr node) {
        final Rect rect = node.getRect();
        final int[] rawData = node.getRawData();
        ImageRasterUtils.drawRectImg(imgDim, imgData, rect, rawData);
    }

    @Override
    public void caseGlyph(GlyphRectImgDescr node) {
        final Rect rect = node.getRect();
        final int[] glyphData = node.getSharedData();
        if (glyphData == null) {
            // should not occur!
            return;
        }
//        final GlyphIndexOrCode glyphIndexOrCode = node.getGlyphIndexOrCode();
//        glyphMRUTable.drawGlyphFindByIndexOrCode(glyphIndexOrCode, imgDim, imgData, rect);

        Dim glyphDim = rect.getDim();
        Rect glyphROI = Rect.newDim(glyphDim); 
        Pt rectFromPt = rect.getFromPt();
        ImageRasterUtils.drawRectImg(imgDim, imgData, rectFromPt, glyphDim, glyphData, glyphROI);        

    }

    @Override
    public void caseAbove(RectImgAboveRectImgDescr node) {
        final RectImgDescr underlying = node.getUnderlying();
        final RectImgDescr[] aboves = node.getAboves();
        draw(underlying);
        if (aboves != null) {
            int aboveCount = (aboves != null)? aboves.length : 0;
            for (int i = 0; i < aboveCount; i++) {
                draw(aboves[i]);
            }
        }
    }

    @Override
    public void caseOverrideAttributesProxy(OverrideAttributesProxyRectImgDescr node) {
        draw(node.getUnderlying());
    }

    @Override
    public void caseNoiseAboveParts(NoiseAbovePartsRectImgDescr node) {
        draw(node.getUnderlying());
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
    public void caseAnalysisProxy(AnalysisProxyRectImgDescr node) {
        final RectImgDescr target = node.getTarget();
        draw(target);
    }
    
}

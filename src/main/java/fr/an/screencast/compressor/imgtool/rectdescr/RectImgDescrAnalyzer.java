package fr.an.screencast.compressor.imgtool.rectdescr;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.compressor.imgtool.glyph.GlyphMRUTable;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrVisitor;
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
import fr.an.screencast.compressor.imgtool.utils.ImageData;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.MutableDim;
import fr.an.screencast.compressor.utils.Rect;

public class RectImgDescrAnalyzer {
    
    private static final Logger LOG = LoggerFactory.getLogger(RectImgDescrAnalyzer.class);
    
    private final Dim dim;
    private RectImgDescrDetectorHelper helper;
    
    private int[] imgData;
    
    private RectImgDescrAnalyzerVisitor recursiveAnalyzer = new RectImgDescrAnalyzerVisitor(); 
    
    // ------------------------------------------------------------------------
    
    public RectImgDescrAnalyzer(Dim dim) {
        this.dim = dim;
        this.helper = new RectImgDescrDetectorHelper(dim);
    }
    
    // ------------------------------------------------------------------------

    
    public void setImg(int[] imgData) {
        this.imgData = imgData;
        this.helper.setImg(imgData);
    }

    public int[] getImgData() {
        return imgData;
    }

    public ImageData getImageData() {
        return new ImageData(dim, imgData);
    }

    public Dim getDim() {
        return dim;
    }
    
    public GlyphMRUTable getGlyphMRUTable() {
        return helper.getGlyphMRUTable();
    }
    
    public RectImgDescription analyze(Rect rect) {
        RectImgDescription res;
        
        res = helper.detectExactFillRect(rect);
        if (res != null) {
            return res;
        }
        
        MutableDim tmpDim = new MutableDim(rect.getWidth(), rect.getHeight());
        res  = helper.detectBorder1AtUL(rect.getFromPt(), tmpDim);
        if (res != null) {
            res.accept(recursiveAnalyzer);
            return res;
        }
        
        res = helper.detectRoundBorderStartAtUL(rect.getFromPt());
        if (res != null) {
            res.accept(recursiveAnalyzer);
            return res;
        }
        
        MutableDim topCornerDim = new MutableDim();
        MutableDim bottomCornerDim = new MutableDim();
        StringBuilder optReason = new StringBuilder();
        // checkCornerColor=false  .. problem with anti-aliasing!
        res = helper.detectRoundBorderStartAtULWithCorners(rect.getFromPt(), tmpDim, false, topCornerDim, bottomCornerDim, optReason);
        if (res != null) {
            res.accept(recursiveAnalyzer);
            return res;
        }
        
        if (helper.allowDetectGlyphInRect(rect)) {
            res = helper.detectGlyph(rect);
            if (res != null) {
                return res;
            }
        }
        
        res = helper.detectVertSplit(rect);
        if (res != null) {
            res.accept(recursiveAnalyzer);
            return res;
        }
        
        res = helper.detectHorizontalSplit(rect);
        if (res != null) {
            res.accept(recursiveAnalyzer);
            return res;
        }

        // nothing found => use RawData !
        int[] rawData = ImageRasterUtils.getCopyData(dim, imgData, rect);
        res = new RawDataRectImgDescr(rect, rawData);
        
        boolean debug = false;
        if (debug) {
            BufferedImage img = new BufferedImage(rect.getWidth(), rect.getHeight(), BufferedImage.TYPE_INT_RGB);
            ImageRasterUtils.copyData(img, rawData);
            File outputFile = new File("src/test/imgs/img-" + rect.toStringPtDim() + ".png");
            try {
                ImageIO.write(img, "png", outputFile);
            } catch (IOException ex) {
                LOG.warn("Failed write file:" + outputFile, ex);
            }
        }
        
        return res;
    }
    
    
    /**
     * Vistior for recursive analysis of RectImgDescr AST
     */
    public class RectImgDescrAnalyzerVisitor extends RectImgDescrVisitor {

        // ------------------------------------------------------------------------

        public RectImgDescrAnalyzerVisitor() {
        }
        
        // ------------------------------------------------------------------------
        
        @Override
        public void caseFillRect(FillRectImgDescr node) {
            // do nothing
        }

        @Override
        public void caseRoundBorderDescr(RoundBorderRectImgDescr node) {
            RectImgDescription inside = node.getInside();
            if (inside == null) {
                Rect insideRect = node.getInsideRect();
                inside = analyze(insideRect);
                node.setInside(inside);
            }
            if (inside != null) {
                inside.accept(this);
            }
        }

        @Override
        public void caseBorderDescr(BorderRectImgDescr node) {
            RectImgDescription inside = node.getInside();
            if (inside == null) {
                Rect insideRect = node.getInsideRect();
                inside = analyze(insideRect);
                node.setInside(inside);
            }
            if (inside != null) {
                inside.accept(this);
            }
        }

        @Override
        public void caseTopBottomBorderDescr(TopBottomBorderRectImgDescr node) {
            RectImgDescription inside = node.getInside();
            if (inside == null) {
                Rect insideRect = node.getInsideRect();
                inside = analyze(insideRect);
                node.setInside(inside);
            }
            if (inside != null) {
                inside.accept(this);
            }
        }

        @Override
        public void caseLeftRightBorderDescr(LeftRightBorderRectImgDescr node) {
            RectImgDescription inside = node.getInside();
            if (inside == null) {
                Rect insideRect = node.getInsideRect();
                inside = analyze(insideRect);
                node.setInside(inside);
            }
            if (inside != null) {
                inside.accept(this);
            }
        }

        
        @Override
        public void caseVerticalSplitDescr(VerticalSplitRectImgDescr node) {
            RectImgDescription left = node.getLeft();
            RectImgDescription right = node.getRight();
            if (left == null) {
                Rect leftRect = node.getLeftRect();
                left = analyze(leftRect);
                node.setLeft(left);
            }
            if (right == null) {
                Rect rightRect = node.getRightRect();
                right = analyze(rightRect);
                node.setRight(right);
            }
            if (left != null) {
                left.accept(this);
            }
            if (right != null) {
                right.accept(this);
            }
        }

        @Override
        public void caseHorizontalSplitDescr(HorizontalSplitRectImgDescr node) {
            RectImgDescription down = node.getDown();
            RectImgDescription up = node.getUp();
            if (up == null) {
                Rect upRect = node.getUpRect();
                up = analyze(upRect);
                node.setUp(up);
            }
            if (down == null) {
                Rect downRect = node.getDownRect();
                down = analyze(downRect);
                node.setDown(down);
            }
            if (down != null) {
                down.accept(this);
            }
            if (up != null) {
                up.accept(this);
            }
        }

        @Override
        public void caseLinesSplitDescr(LinesSplitRectImgDescr node) {
            RectImgDescription[] lines = node.getLines();
            if (lines == null || lines.length == 0) {
                Rect[] lineRects = node.getLineRects();
                lines = new RectImgDescription[lineRects.length];
                for(int i = 0; i < lines.length; i++) {
                    lines[i] = analyze(lineRects[i]);
                }
                node.setLines(lines);
            }
            if (lines != null) {
                for(RectImgDescription line : lines) {
                    line.accept(this);
                }
            }
        }

        @Override
        public void caseColumnsSplitDescr(ColumnsSplitRectImgDescr node) {
            RectImgDescription[] columns = node.getColumns();
            if (columns == null || columns.length == 0) {
                Rect[] columnRects = node.getColumnRects();
                columns = new RectImgDescription[columnRects.length];
                for(int i = 0; i < columns.length; i++) {
                    columns[i] = analyze(columnRects[i]);
                }
                node.setColumns(columns);
            }
            if (columns != null) {
                for(RectImgDescription column : columns) {
                    column.accept(this);
                }
            }
        }

        @Override
        public void caseRawDataDescr(RawDataRectImgDescr node) {
            // do nothing?        
        }

        @Override
        public void caseGlyphDescr(GlyphRectImgDescr node) {
            // do nothing
        }

        @Override
        public void caseDescrAboveDescr(RectImgAboveRectImgDescr node) {
            RectImgDescription underlying = node.getUnderlyingRectImgDescr();
            RectImgDescription above = node.getAboveRectImgDescr();
            if (above == null) {
                Rect aboveRect = node.getAboveRect();
                above = analyze(aboveRect);
                node.setAboveRectImgDescr(above);
            }
            if (underlying != null) {
                underlying.accept(this);
            }
            if (above != null) {
                above.accept(this);
            }
        }

    }

    
}

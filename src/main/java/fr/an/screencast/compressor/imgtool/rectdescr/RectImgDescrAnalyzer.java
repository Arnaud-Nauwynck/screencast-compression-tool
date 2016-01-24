package fr.an.screencast.compressor.imgtool.rectdescr;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.compressor.imgtool.glyph.GlyphMRUTable;
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
import fr.an.screencast.compressor.imgtool.utils.ImageData;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.MutableDim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;

public class RectImgDescrAnalyzer {
    
    private static final Logger LOG = LoggerFactory.getLogger(RectImgDescrAnalyzer.class);
    
    private final Dim dim;
    private RectImgDescrDetectorHelper helper;
    
    private int[] imgData;
    
    private RectImgDescrAnalyzerVisitor recursiveAnalyzer = new RectImgDescrAnalyzerVisitor(); 
    
    private static final boolean DEBUG_USE_SCAN_RECTS = 
            true;
//            false; //TODO
    
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
    
    public RectImgDescr analyze(Rect rect) {
        RectImgDescr res = detect(rect);
        if (res != null) {
            res.accept(recursiveAnalyzer);
        }
        return res;
    }
    
    public RectImgDescr detect(Rect rect) {
        RectImgDescr res;
        
        res = helper.detectExactFillRect(rect);
        if (res != null) {
            return res;
        }
        
        Pt rectFromPt = rect.getFromPt();
        MutableDim tmpDim = new MutableDim(rect.getWidth(), rect.getHeight());
        res  = helper.detectBorder1AtUL(rectFromPt, tmpDim);
        if (res != null) {
            return res;
        }
        
        res = helper.detectRoundBorderStartAtUL(rectFromPt); // TODO within rect!!
        if (res != null) {
            return res;
        }
        
        MutableDim topCornerDim = new MutableDim();
        MutableDim bottomCornerDim = new MutableDim();
        StringBuilder optReason = new StringBuilder();
        // checkCornerColor=false  .. problem with anti-aliasing!
        res = helper.detectRoundBorderStartAtULWithCorners(rectFromPt, tmpDim, false, topCornerDim, bottomCornerDim, optReason);
        if (res != null) {
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
            return res;
        }
        
        res = helper.detectHorizontalSplit(rect);
        if (res != null) {
            return res;
        }

        // do more exhaustive computation... scan all max uniforms borders in rect
        if (DEBUG_USE_SCAN_RECTS) {
            List<Rect> scannedBorderRects = helper.scanListLargestBorderRightThenDown(rect, 0, 0);
            int sumArea = Rect.sumArea(scannedBorderRects);
            if (sumArea != rect.getArea()) {
                LOG.warn("missing rects area in scan: expecting " + rect.getArea() + ", got " + sumArea + " for " + rect);
            }
            if (scannedBorderRects != null && !scannedBorderRects.isEmpty() 
                    ) {
                res = helper.detectLineBreaksInScannedRightThenDownRects(rect, scannedBorderRects);
                if (res != null) {
                    return res;
                }
        
                List<Rect> pivotScannedBorderRects = helper.pivotScannedRectsToDownThenRight(rect, scannedBorderRects);
                res = helper.detectColumnBreaksInScannedDownThenRightRects(rect, pivotScannedBorderRects);
                if (res != null) {
                    return res;
                }

//                if (scannedBorderRects.size() < (rect.getArea() / 8)) {  // else many small borders...useless!
//                    res = helper.createScannedRectsToImgDescr(rect, scannedBorderRects, true);
//                    if (res != null) {
//                        return res;
//                    }
//                }
            }
        }
        
        // nothing found => use RawData !
        int[] rawData = ImageRasterUtils.getCopyData(dim, imgData, rect);
        res = new RawDataRectImgDescr(rect, rawData);
        
        return res;
    }
    
    
    
    
    
    /**
     * Visitor for recursive analysis of RectImgDescr AST
     */
    public class RectImgDescrAnalyzerVisitor extends RectImgDescrVisitor {

        // ------------------------------------------------------------------------

        public RectImgDescrAnalyzerVisitor() {
        }
        
        // ------------------------------------------------------------------------
        
        protected void recurseAnalyse(RectImgDescr node) {
            if (node != null) {
                node.accept(this);
            }
        }
        
        @Override
        public void caseRoot(RootRectImgDescr node) {
            recurseAnalyse(node.getTarget());
        }

        @Override
        public void caseFill(FillRectImgDescr node) {
            // do nothing
        }

        @Override
        public void caseRoundBorder(RoundBorderRectImgDescr node) {
            RectImgDescr inside = node.getInside();
            if (inside == null) {
                Rect insideRect = node.getInsideRect();
                inside = detect(insideRect);
                node.setInside(inside);
            }
            recurseAnalyse(inside);
        }

        @Override
        public void caseBorder(BorderRectImgDescr node) {
            RectImgDescr inside = node.getInside();
            if (inside == null) {
                Rect insideRect = node.getInsideRect();
                inside = detect(insideRect);
                node.setInside(inside);
            }
            recurseAnalyse(inside);
        }

        @Override
        public void caseTopBottomBorder(TopBottomBorderRectImgDescr node) {
            RectImgDescr inside = node.getInside();
            if (inside == null) {
                Rect insideRect = node.getInsideRect();
                inside = detect(insideRect);
                node.setInside(inside);
            }
            recurseAnalyse(inside);
        }

        @Override
        public void caseLeftRightBorder(LeftRightBorderRectImgDescr node) {
            RectImgDescr inside = node.getInside();
            if (inside == null) {
                Rect insideRect = node.getInsideRect();
                inside = detect(insideRect);
                node.setInside(inside);
            }
            recurseAnalyse(inside);
        }

        
        @Override
        public void caseVerticalSplit(VerticalSplitRectImgDescr node) {
            RectImgDescr left = node.getLeft();
            RectImgDescr right = node.getRight();
            if (left == null) {
                Rect leftRect = node.getLeftRect();
                left = detect(leftRect);
                node.setLeft(left);
            }
            if (right == null) {
                Rect rightRect = node.getRightRect();
                right = detect(rightRect);
                node.setRight(right);
            }
            recurseAnalyse(left);
            recurseAnalyse(right);
        }

        @Override
        public void caseHorizontalSplit(HorizontalSplitRectImgDescr node) {
            RectImgDescr down = node.getDown();
            RectImgDescr up = node.getUp();
            if (up == null) {
                Rect upRect = node.getUpRect();
                up = detect(upRect);
                node.setUp(up);
            }
            if (down == null) {
                Rect downRect = node.getDownRect();
                down = detect(downRect);
                node.setDown(down);
            }
            recurseAnalyse(down);
            recurseAnalyse(up);
        }

        @Override
        public void caseLinesSplit(LinesSplitRectImgDescr node) {
            RectImgDescr[] lines = node.getLines();
            if (lines == null || lines.length == 0) {
                Rect[] lineRects = node.getLineRects();
                lines = new RectImgDescr[lineRects.length];
                for(int i = 0; i < lines.length; i++) {
                    lines[i] = detect(lineRects[i]);
                }
                node.setLines(lines);
            }
            if (lines != null) {
                for(RectImgDescr line : lines) {
                    recurseAnalyse(line);
                }
            }
        }

        @Override
        public void caseColumnsSplit(ColumnsSplitRectImgDescr node) {
            RectImgDescr[] columns = node.getColumns();
            if (columns == null || columns.length == 0) {
                Rect[] columnRects = node.getColumnRects();
                columns = new RectImgDescr[columnRects.length];
                for(int i = 0; i < columns.length; i++) {
                    columns[i] = detect(columnRects[i]);
                }
                node.setColumns(columns);
            }
            if (columns != null) {
                for(RectImgDescr column : columns) {
                    recurseAnalyse(column);
                }
            }
        }

        @Override
        public void caseRawData(RawDataRectImgDescr node) {
            // do nothing?        
        }

        @Override
        public void caseGlyph(GlyphRectImgDescr node) {
            // do nothing
        }

        @Override
        public void caseAbove(RectImgAboveRectImgDescr node) {
            RectImgDescr underlying = node.getUnderlying();
            RectImgDescr[] aboves = node.getAboves();
            if (aboves == null) {
                Rect[] aboveRects = node.getAboveRects();
                int abovesCount = aboveRects != null? aboveRects.length : 0;
                aboves = new RectImgDescr[abovesCount]; 
                for(int i = 0; i < abovesCount; i++) {
                    aboves[i] = detect(aboveRects[i]);
                }
                node.setAboveRectImgDescrs(aboves);
            }
            recurseAnalyse(underlying);
            if (aboves != null) {
                int abovesCount = aboves != null? aboves.length : 0;
                for(int i = 0; i < abovesCount; i++) {
                    recurseAnalyse(aboves[i]);
                }
            }
        }
        
        @Override
        public void caseNoiseAboveParts(NoiseAbovePartsRectImgDescr node) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void caseNoiseAboveParts_Pt(NoiseAbovePartsRectImgDescr parent, int partIndex, PtNoiseFragment node) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void caseNoiseAboveParts_Segment(NoiseAbovePartsRectImgDescr parent, int partIndex, SegmentNoiseFragment node) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void caseNoiseAboveParts_ConnexSegmentLines(NoiseAbovePartsRectImgDescr parent, int partIndex, ConnexSegmentLinesNoiseFragment node) {
            // TODO Auto-generated method stub
            
        }

        @Override
        public void caseOverrideAttributesProxy(OverrideAttributesProxyRectImgDescr node) {
            recurseAnalyse(node.getUnderlying());
        }

        @Override
        public void caseAnalysisProxy(AnalysisProxyRectImgDescr node) {
            recurseAnalyse(node.getTarget());
        }
        
    }

    
}

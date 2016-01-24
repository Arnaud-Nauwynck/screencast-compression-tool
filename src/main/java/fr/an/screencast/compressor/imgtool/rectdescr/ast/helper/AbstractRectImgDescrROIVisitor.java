package fr.an.screencast.compressor.imgtool.rectdescr.ast.helper;

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
import fr.an.screencast.compressor.utils.Rect;

/**
 * abstract RectImgDescrVisitor implementation for recursive traversal of RectImgDescr AST 
 * using ROI="Region Of Interest" filtering selection
 */
public abstract class AbstractRectImgDescrROIVisitor extends RectImgDescrVisitor {

    protected Rect roi;
    
    protected int currLevel;
    protected int maxLevel = -1;
    
    // ------------------------------------------------------------------------

    public AbstractRectImgDescrROIVisitor(Rect roi) {
        this.roi = roi;
    }

    // ------------------------------------------------------------------------

    public void setMaxLevel(int maxLevel) {
        this.maxLevel = maxLevel;
    }
    
    
    protected void recurse(RectImgDescr node) {
        if (node != null) {
            if (roi == null || node.getRect().isIntersect(roi)) {
                if (maxLevel == -1 || currLevel < maxLevel) {
                    currLevel++;
                    node.accept(this);
                    currLevel--;
                }
            }
        }
    }
    
    protected void recurse(RectImgDescr[] nodes) {
        if (nodes != null && nodes.length != 0) {
            for(RectImgDescr node : nodes) {
                recurse(node);
            }
        }
    }
    
    @Override
    public void caseRoot(RootRectImgDescr node) {
        recurse(node.getTarget());
    }
    
    @Override
    public void caseFill(FillRectImgDescr node) {
    }

    @Override
    public void caseRoundBorder(RoundBorderRectImgDescr node) {
        final RectImgDescr inside = node.getInside();
        recurse(inside);
    }

    @Override
    public void caseBorder(BorderRectImgDescr node) {
        final RectImgDescr inside = node.getInside();
        recurse(inside);
    }

    @Override
    public void caseTopBottomBorder(TopBottomBorderRectImgDescr node) {
        final RectImgDescr inside = node.getInside();
        recurse(inside);
    }

    @Override
    public void caseLeftRightBorder(LeftRightBorderRectImgDescr node) {
        final RectImgDescr inside = node.getInside();
        recurse(inside);
    }

    @Override
    public void caseVerticalSplit(VerticalSplitRectImgDescr node) {
        final RectImgDescr left = node.getLeft();
        final RectImgDescr right = node.getRight();
        recurse(left);
        recurse(right);
    }

    @Override
    public void caseHorizontalSplit(HorizontalSplitRectImgDescr node) {
        final RectImgDescr down = node.getDown();
        final RectImgDescr up = node.getUp();
        recurse(up);
        recurse(down);
    }

    @Override
    public void caseLinesSplit(LinesSplitRectImgDescr node) {
        final RectImgDescr[] lines = node.getLines();
        recurse(lines);
    }

    @Override
    public void caseColumnsSplit(ColumnsSplitRectImgDescr node) {
        final RectImgDescr[] columns = node.getColumns();
        recurse(columns);
    }

    @Override
    public void caseRawData(RawDataRectImgDescr node) {
    }

    @Override
    public void caseGlyph(GlyphRectImgDescr node) {
    }

    @Override
    public void caseAbove(RectImgAboveRectImgDescr node) {
        final RectImgDescr underlying = node.getUnderlying();
        final RectImgDescr[] aboves = node.getAboves();
        recurse(underlying);
        recurse(aboves);
    }

    @Override
    public void caseAnalysisProxy(AnalysisProxyRectImgDescr node) {
        RectImgDescr target = node.getTarget();
        recurse(target);        
    }

    @Override
    public void caseNoiseAboveParts(NoiseAbovePartsRectImgDescr node) {
        recurse(node.getUnderlying());
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
        // do nothing
    }

    @Override
    public void caseNoiseAboveParts_Segment(NoiseAbovePartsRectImgDescr parent, int partIndex, SegmentNoiseFragment node) {
        // do nothing
    }

    @Override
    public void caseNoiseAboveParts_ConnexSegmentLines(NoiseAbovePartsRectImgDescr parent, int partIndex, ConnexSegmentLinesNoiseFragment node) {
        // do nothing
    }

    @Override
    public void caseOverrideAttributesProxy(OverrideAttributesProxyRectImgDescr node) {
        recurse(node.getUnderlying());
    }
    
}

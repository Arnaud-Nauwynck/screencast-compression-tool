package fr.an.screencast.compressor.imgtool.rectdescr.ast.helper;

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
import fr.an.screencast.compressor.utils.Rect;

/**
 * abstract RectImgDescrVisitor implementation for recursive traversal of RectImgDescr AST
 */
public class AbstractRectImgDescrROIVisitor extends RectImgDescrVisitor {

   protected Rect roi;
    
    // ------------------------------------------------------------------------

    public AbstractRectImgDescrROIVisitor(Rect roi) {
        this.roi = roi;
    }

    // ------------------------------------------------------------------------

    protected void recurse(RectImgDescription node) {
        if (node != null) {
            if (roi == null || node.getRect().isIntersect(roi)) {
                node.accept(this);
            }
        }
    }
    
    protected void recurse(RectImgDescription[] nodes) {
        if (nodes != null && nodes.length != 0) {
            for(RectImgDescription node : nodes) {
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
        final RectImgDescription inside = node.getInside();
        recurse(inside);
    }

    @Override
    public void caseBorder(BorderRectImgDescr node) {
        final RectImgDescription inside = node.getInside();
        recurse(inside);
    }

    @Override
    public void caseTopBottomBorder(TopBottomBorderRectImgDescr node) {
        final RectImgDescription inside = node.getInside();
        recurse(inside);
    }

    @Override
    public void caseLeftRightBorder(LeftRightBorderRectImgDescr node) {
        final RectImgDescription inside = node.getInside();
        recurse(inside);
    }

    @Override
    public void caseVerticalSplit(VerticalSplitRectImgDescr node) {
        final RectImgDescription left = node.getLeft();
        final RectImgDescription right = node.getRight();
        recurse(left);
        recurse(right);
    }

    @Override
    public void caseHorizontalSplit(HorizontalSplitRectImgDescr node) {
        final RectImgDescription down = node.getDown();
        final RectImgDescription up = node.getUp();
        recurse(up);
        recurse(down);
    }

    @Override
    public void caseLinesSplit(LinesSplitRectImgDescr node) {
        final RectImgDescription[] lines = node.getLines();
        recurse(lines);
    }

    @Override
    public void caseColumnsSplit(ColumnsSplitRectImgDescr node) {
        final RectImgDescription[] columns = node.getColumns();
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
        final RectImgDescription underlying = node.getUnderlying();
        final RectImgDescription[] aboves = node.getAboves();
        recurse(underlying);
        recurse(aboves);
    }

    @Override
    public void caseAnalysisProxy(AnalysisProxyRectImgDescr node) {
        RectImgDescription target = node.getTarget();
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

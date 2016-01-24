package fr.an.screencast.compressor.imgtool.rectdescr.ast.helper;

import java.util.ArrayList;
import java.util.List;

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
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrVisitor;
import fr.an.screencast.compressor.utils.Rect;

/**
 * RectImgDescrVisitor implementation for recursive delimiting RectImgDescr path enclosing given rect roi 
 */
public class ROIToDescrPathRectImgDescrVisitor extends RectImgDescrVisitor {

    private List<RectImgDescr> path = new ArrayList<RectImgDescr>();
    
    private Rect roi;
    
    // ------------------------------------------------------------------------

    public ROIToDescrPathRectImgDescrVisitor(Rect roi) {
        this.roi = roi;
    }

    public static List<RectImgDescr> roiToPath(RectImgDescr node, Rect roi) {
        ROIToDescrPathRectImgDescrVisitor visitor = new ROIToDescrPathRectImgDescrVisitor(roi);
        if (node != null) {
            node.accept(visitor);
        }
        return visitor.getPath();
    }
    
    // ------------------------------------------------------------------------

    public List<RectImgDescr> getPath() {
        return path;
    }
    
    protected void recurse(RectImgDescr node) {
        if (node != null) {
            if (roi == null || node.getRect().contains(roi)) {
                path.add(node);
                node.accept(this);
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
        RectImgDescr inside = node.getInside();
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
        final RectImgDescr right = node.getRight();
        recurse(right);
    }

    @Override
    public void caseHorizontalSplit(HorizontalSplitRectImgDescr node) {
        final RectImgDescr down = node.getDown();
        final RectImgDescr up = node.getUp();
        recurse(down);
        recurse(up);
    }

    @Override
    public void caseLinesSplit(LinesSplitRectImgDescr node) {
        final RectImgDescr[] lines = node.getLines();
        if (lines != null) {
            for(RectImgDescr line : lines) {
                recurse(line);
            }
        }
    }

    @Override
    public void caseColumnsSplit(ColumnsSplitRectImgDescr node) {
        final RectImgDescr[] columns = node.getColumns();
        if (columns != null) {
            for(RectImgDescr column : columns) {
                recurse(column);
            }
        }
    }

    @Override
    public void caseRawData(RawDataRectImgDescr node) {
    }

    @Override
    public void caseGlyph(GlyphRectImgDescr node) {
    }

    @Override
    public void caseAbove(RectImgAboveRectImgDescr node) {
        // ignore ... final RectImgDescr underlying = node.getUnderlying();
        // recurse(underlying);
        final RectImgDescr[] aboves = node.getAboves();
        if (aboves != null) {
            int aboveCount = (aboves != null)? aboves.length : 0;
            for (int i = 0; i < aboveCount; i++) {
                recurse(aboves[i]);
            }
        }
    }

    @Override
    public void caseAnalysisProxy(AnalysisProxyRectImgDescr node) {
        RectImgDescr target = node.getTarget();
        recurse(target);
    }

    @Override
    public void caseNoiseAboveParts(NoiseAbovePartsRectImgDescr node) {
        // ignore ... RectImgDescr underlying = node.getUnderlying();
        // recurse(underlying);
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
        // TODO?
    }

    @Override
    public void caseNoiseAboveParts_Segment(NoiseAbovePartsRectImgDescr parent, int partIndex, SegmentNoiseFragment node) {
        // TODO?
    }

    @Override
    public void caseNoiseAboveParts_ConnexSegmentLines(NoiseAbovePartsRectImgDescr parent, int partIndex, ConnexSegmentLinesNoiseFragment node) {
        // TODO?
    }

    @Override
    public void caseOverrideAttributesProxy(OverrideAttributesProxyRectImgDescr node) {
        recurse(node.getUnderlying());
    }

}

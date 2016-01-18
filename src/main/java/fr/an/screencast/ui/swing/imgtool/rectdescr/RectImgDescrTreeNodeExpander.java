package fr.an.screencast.ui.swing.imgtool.rectdescr;

import java.util.ArrayList;
import java.util.List;

import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrVisitor2;
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
import fr.an.screencast.ui.swing.imgtool.rectdescr.RectImgDescrTreeNodeData.NodeRectImgDescrTreeNodeData;

/**
 * builder for javafx.scene.control.TreeItem view for corresponding RectImgDescription model
 *
 */
public class RectImgDescrTreeNodeExpander extends RectImgDescrVisitor2<RectImgDescrTreeNode,Void> {

    // ------------------------------------------------------------------------

    public RectImgDescrTreeNodeExpander() {
    }

    // ------------------------------------------------------------------------

    protected RectImgDescrTreeNode recurseBuild(String displayName, RectImgDescr node) {
        if (node == null) {
            return null;
        }
        RectImgDescrTreeNode res = new RectImgDescrTreeNode(
            new NodeRectImgDescrTreeNodeData(displayName, node));
        node.accept(this, res);
        return res;
    }
    
    protected RectImgDescrTreeNode recurseBuildChild(RectImgDescrTreeNode parent, String displayName, RectImgDescr child) {
        RectImgDescrTreeNode res = null;
        if (child != null) {
            res = recurseBuild(displayName, child);
            parent.add(res);
        }
        return res;
    }

    protected List<RectImgDescrTreeNode> recurseBuildChildLs(RectImgDescrTreeNode parent, String displayName, RectImgDescr[] childLs) {
        List<RectImgDescrTreeNode> res = new ArrayList<RectImgDescrTreeNode>();
        if (childLs != null) {
            int i = 0;
            for(RectImgDescr child : childLs) {
                RectImgDescrTreeNode resElt = recurseBuild(displayName + "[" + i + "]", child);
                res.add(resElt);
                parent.add(resElt);
                i++;
            }
        }
        return res;
    }

    // ------------------------------------------------------------------------
    
    @Override
    public Void caseRoot(RootRectImgDescr node, RectImgDescrTreeNode view) {
        recurseBuildChild(view, "target", node.getTarget());
        return null;
    }

    @Override
    public Void caseFill(FillRectImgDescr node, RectImgDescrTreeNode view) {
        return null;
    }

    @Override
    public Void caseRoundBorder(RoundBorderRectImgDescr node, RectImgDescrTreeNode view) {
        recurseBuildChild(view, "inside", node.getInside());
        return null;
    }

    @Override
    public Void caseBorder(BorderRectImgDescr node, RectImgDescrTreeNode view) {
        recurseBuildChild(view, "inside", node.getInside());
        return null;
    }

    @Override
    public Void caseTopBottomBorder(TopBottomBorderRectImgDescr node, RectImgDescrTreeNode view) {
        recurseBuildChild(view, "inside", node.getInside());
        return null;
    }

    @Override
    public Void caseLeftRightBorder(LeftRightBorderRectImgDescr node, RectImgDescrTreeNode view) {
        recurseBuildChild(view, "inside", node.getInside());
        return null;
    }

    @Override
    public Void caseVerticalSplit(VerticalSplitRectImgDescr node, RectImgDescrTreeNode view) {
        recurseBuildChild(view, "left", node.getLeft());
        recurseBuildChild(view, "right", node.getRight());
        return null;
    }

    @Override
    public Void caseHorizontalSplit(HorizontalSplitRectImgDescr node, RectImgDescrTreeNode view) {
        recurseBuildChild(view, "up", node.getUp());
        recurseBuildChild(view, "down", node.getDown());
        return null;
    }

    @Override
    public Void caseLinesSplit(LinesSplitRectImgDescr node, RectImgDescrTreeNode view) {
        recurseBuildChildLs(view, "lines", node.getLines());
        return null;
    }

    @Override
    public Void caseColumnsSplit(ColumnsSplitRectImgDescr node, RectImgDescrTreeNode view) {
        recurseBuildChildLs(view, "columns", node.getColumns());
        return null;
    }

    @Override
    public Void caseRawData(RawDataRectImgDescr node, RectImgDescrTreeNode view) {
        return null;
    }

    @Override
    public Void caseGlyph(GlyphRectImgDescr node, RectImgDescrTreeNode view) {
        return null;
    }

    @Override
    public Void caseAbove(RectImgAboveRectImgDescr node, RectImgDescrTreeNode view) {
        recurseBuildChild(view, "underlying", node.getUnderlying());
        recurseBuildChildLs(view, "aboves", node.getAboves());
        return null;
    }

    @Override
    public Void caseNoiseAbove(NoiseAbovePartsRectImgDescr node, RectImgDescrTreeNode view) {
        recurseBuildChild(view, "underlying", node.getUnderlying());
        return null;
    }

    @Override
    public Void caseNoiseAboveParts_Pt(NoiseAbovePartsRectImgDescr parent, int partIndex, PtNoiseFragment node,
            RectImgDescrTreeNode view) {
        return null;
    }

    @Override
    public Void caseNoiseAboveParts_Segment(NoiseAbovePartsRectImgDescr parent, int partIndex, SegmentNoiseFragment node,
            RectImgDescrTreeNode view) {
        return null;
    }

    @Override
    public Void caseNoiseAboveParts_ConnexSegmentLines(NoiseAbovePartsRectImgDescr parent, int partIndex,
            ConnexSegmentLinesNoiseFragment node, RectImgDescrTreeNode view) {
        return null;
    }

    @Override
    public Void caseOverrideAttributesProxy(OverrideAttributesProxyRectImgDescr node, RectImgDescrTreeNode view) {
        recurseBuildChild(view, "underlying", node.getUnderlying());
        return null;
    }

    @Override
    public Void caseAnalysisProxy(AnalysisProxyRectImgDescr node, RectImgDescrTreeNode view) {
        recurseBuildChild(view, "target", node.getTarget());
        return null;
    }

}

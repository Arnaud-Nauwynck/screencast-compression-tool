package fr.an.screencast.ui.jfx.imgtool.rectdescr;

import java.util.ArrayList;
import java.util.List;

import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrVisitor2;
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
import fr.an.screencast.ui.jfx.imgtool.rectdescr.RectImgDescrTreeItemData.NodeRectImgDescrTreeItemData;

/**
 * builder for javafx.scene.control.TreeItem view for corresponding RectImgDescription model
 *
 */
@SuppressWarnings("restriction")
public class RectImgDescrTreeItemExpander extends RectImgDescrVisitor2<RectImgDescrTreeItem,Void> {

    // ------------------------------------------------------------------------

    public RectImgDescrTreeItemExpander() {
    }

    // ------------------------------------------------------------------------

    protected RectImgDescrTreeItem recurseBuild(String displayName, RectImgDescription node) {
        if (node == null) {
            return null;
        }
        RectImgDescrTreeItem res = new RectImgDescrTreeItem(
            new NodeRectImgDescrTreeItemData<RectImgDescription>(displayName, node));
        node.accept(this, res);
        return res;
    }
    
    protected RectImgDescrTreeItem recurseBuildChild(RectImgDescrTreeItem parent, String displayName, RectImgDescription child) {
        RectImgDescrTreeItem res = null;
        if (child != null) {
            res = recurseBuild(displayName, child);
            parent.getChildren().add(res);
        }
        return res;
    }

    protected List<RectImgDescrTreeItem> recurseBuildChildLs(RectImgDescrTreeItem parent, String displayName, RectImgDescription[] childLs) {
        List<RectImgDescrTreeItem> res = new ArrayList<RectImgDescrTreeItem>();
        if (childLs != null) {
            int i = 0;
            for(RectImgDescription child : childLs) {
                RectImgDescrTreeItem resElt = recurseBuild(displayName + "[" + i + "]", child);
                res.add(resElt);
                parent.getChildren().add(resElt);
                i++;
            }
        }
        return res;
    }

    // ------------------------------------------------------------------------
    
    @Override
    public Void caseRoot(RootRectImgDescr node, RectImgDescrTreeItem view) {
        recurseBuildChild(view, "target", node.getTarget());
        return null;
    }

    @Override
    public Void caseFill(FillRectImgDescr node, RectImgDescrTreeItem view) {
        return null;
    }

    @Override
    public Void caseRoundBorder(RoundBorderRectImgDescr node, RectImgDescrTreeItem view) {
        recurseBuildChild(view, "inside", node.getInside());
        return null;
    }

    @Override
    public Void caseBorder(BorderRectImgDescr node, RectImgDescrTreeItem view) {
        recurseBuildChild(view, "inside", node.getInside());
        return null;
    }

    @Override
    public Void caseTopBottomBorder(TopBottomBorderRectImgDescr node, RectImgDescrTreeItem view) {
        recurseBuildChild(view, "inside", node.getInside());
        return null;
    }

    @Override
    public Void caseLeftRightBorder(LeftRightBorderRectImgDescr node, RectImgDescrTreeItem view) {
        recurseBuildChild(view, "inside", node.getInside());
        return null;
    }

    @Override
    public Void caseVerticalSplit(VerticalSplitRectImgDescr node, RectImgDescrTreeItem view) {
        recurseBuildChild(view, "left", node.getLeft());
        recurseBuildChild(view, "right", node.getRight());
        return null;
    }

    @Override
    public Void caseHorizontalSplit(HorizontalSplitRectImgDescr node, RectImgDescrTreeItem view) {
        recurseBuildChild(view, "up", node.getUp());
        recurseBuildChild(view, "down", node.getDown());
        return null;
    }

    @Override
    public Void caseLinesSplit(LinesSplitRectImgDescr node, RectImgDescrTreeItem view) {
        recurseBuildChildLs(view, "lines", node.getLines());
        return null;
    }

    @Override
    public Void caseColumnsSplit(ColumnsSplitRectImgDescr node, RectImgDescrTreeItem view) {
        recurseBuildChildLs(view, "columns", node.getColumns());
        return null;
    }

    @Override
    public Void caseRawData(RawDataRectImgDescr node, RectImgDescrTreeItem view) {
        return null;
    }

    @Override
    public Void caseGlyph(GlyphRectImgDescr node, RectImgDescrTreeItem view) {
        return null;
    }

    @Override
    public Void caseAbove(RectImgAboveRectImgDescr node, RectImgDescrTreeItem view) {
        recurseBuildChild(view, "underlying", node.getUnderlying());
        recurseBuildChildLs(view, "aboves", node.getAboves());
        return null;
    }

    @Override
    public Void caseNoiseAbove(NoiseAbovePartsRectImgDescr node, RectImgDescrTreeItem view) {
        recurseBuildChild(view, "underlying", node.getUnderlying());
        return null;
    }

    @Override
    public Void caseNoiseAboveParts_Pt(NoiseAbovePartsRectImgDescr parent, int partIndex, PtNoiseFragment node,
            RectImgDescrTreeItem view) {
        return null;
    }

    @Override
    public Void caseNoiseAboveParts_Segment(NoiseAbovePartsRectImgDescr parent, int partIndex, SegmentNoiseFragment node,
            RectImgDescrTreeItem view) {
        return null;
    }

    @Override
    public Void caseNoiseAboveParts_ConnexSegmentLines(NoiseAbovePartsRectImgDescr parent, int partIndex,
            ConnexSegmentLinesNoiseFragment node, RectImgDescrTreeItem view) {
        return null;
    }

    @Override
    public Void caseOverrideAttributesProxy(OverrideAttributesProxyRectImgDescr node, RectImgDescrTreeItem view) {
        recurseBuildChild(view, "underlying", node.getUnderlying());
        return null;
    }

    @Override
    public Void caseAnalysisProxy(AnalysisProxyRectImgDescr node, RectImgDescrTreeItem view) {
        recurseBuildChild(view, "target", node.getTarget());
        return null;
    }

}

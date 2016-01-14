package fr.an.screencast.compressor.imgtool.rectdescr.ast.helper;

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
import fr.an.screencast.compressor.utils.Rect;

/**
 * abstract RectImgDescrVisitor implementation for recursive traversal of RectImgDescr AST
 */
public class AbstractRectImgDescrROIVisitor extends RectImgDescrVisitor {

   private Rect roi;
    
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
    public void caseFillRect(FillRectImgDescr node) {
    }

    @Override
    public void caseRoundBorderDescr(RoundBorderRectImgDescr node) {
        final RectImgDescription inside = node.getInside();
        recurse(inside);
    }

    @Override
    public void caseBorderDescr(BorderRectImgDescr node) {
        final RectImgDescription inside = node.getInside();
        recurse(inside);
    }

    @Override
    public void caseTopBottomBorderDescr(TopBottomBorderRectImgDescr node) {
        final RectImgDescription inside = node.getInside();
        recurse(inside);
    }

    @Override
    public void caseLeftRightBorderDescr(LeftRightBorderRectImgDescr node) {
        final RectImgDescription inside = node.getInside();
        recurse(inside);
    }

    @Override
    public void caseVerticalSplitDescr(VerticalSplitRectImgDescr node) {
        final RectImgDescription left = node.getLeft();
        final RectImgDescription right = node.getRight();
        recurse(left);
        recurse(right);
    }

    @Override
    public void caseHorizontalSplitDescr(HorizontalSplitRectImgDescr node) {
        final RectImgDescription down = node.getDown();
        final RectImgDescription up = node.getUp();
        recurse(up);
        recurse(down);
    }

    @Override
    public void caseLinesSplitDescr(LinesSplitRectImgDescr node) {
        final RectImgDescription[] lines = node.getLines();
        recurse(lines);
    }

    @Override
    public void caseColumnsSplitDescr(ColumnsSplitRectImgDescr node) {
        final RectImgDescription[] columns = node.getColumns();
        recurse(columns);
    }

    @Override
    public void caseRawDataDescr(RawDataRectImgDescr node) {
    }

    @Override
    public void caseGlyphDescr(GlyphRectImgDescr node) {
    }

    @Override
    public void caseAboveDescr(RectImgAboveRectImgDescr node) {
        final RectImgDescription underlying = node.getUnderlyingRectImgDescr();
        final RectImgDescription[] aboves = node.getAboveRectImgDescrs();
        recurse(underlying);
        recurse(aboves);
    }

    @Override
    public void caseAnalysisProxyRect(AnalysisProxyRectImgDescr node) {
        RectImgDescription target = node.getTarget();
        recurse(target);        
    }
    
}

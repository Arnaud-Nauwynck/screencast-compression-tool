package fr.an.screencast.compressor.imgtool.rectdescr.ast;

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
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RootRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RoundBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.SegmentNoiseFragment;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.TopBottomBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.VerticalSplitRectImgDescr;

public abstract class RectImgDescrVisitor {

    public abstract void caseRoot(RootRectImgDescr node);

    public abstract void caseFill(FillRectImgDescr node);
    public abstract void caseRoundBorder(RoundBorderRectImgDescr node);
    public abstract void caseBorder(BorderRectImgDescr node);
    public abstract void caseTopBottomBorder(TopBottomBorderRectImgDescr node);
    public abstract void caseLeftRightBorder(LeftRightBorderRectImgDescr node);
    public abstract void caseVerticalSplit(VerticalSplitRectImgDescr node);
    public abstract void caseHorizontalSplit(HorizontalSplitRectImgDescr node);
    public abstract void caseLinesSplit(LinesSplitRectImgDescr node);
    public abstract void caseColumnsSplit(ColumnsSplitRectImgDescr node);
    public abstract void caseRawData(RawDataRectImgDescr node);
    public abstract void caseGlyph(GlyphRectImgDescr node);
    public abstract void caseAbove(RectImgAboveRectImgDescr node);
    public abstract void caseNoiseAboveParts(NoiseAbovePartsRectImgDescr node);
    public abstract void caseOverrideAttributesProxy(OverrideAttributesProxyRectImgDescr node);
    public abstract void caseNoiseAboveParts_Pt(NoiseAbovePartsRectImgDescr parent, int partIndex, PtNoiseFragment node);
    public abstract void caseNoiseAboveParts_Segment(NoiseAbovePartsRectImgDescr parent, int partIndex, SegmentNoiseFragment node);
    public abstract void caseNoiseAboveParts_ConnexSegmentLines(NoiseAbovePartsRectImgDescr parent, int partIndex, ConnexSegmentLinesNoiseFragment node);

    public abstract void caseAnalysisProxy(AnalysisProxyRectImgDescr node);


}

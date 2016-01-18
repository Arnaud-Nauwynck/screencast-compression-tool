package fr.an.screencast.compressor.imgtool.rectdescr.ast;

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
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RootRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RoundBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.SegmentNoiseFragment;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.TopBottomBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.VerticalSplitRectImgDescr;

public abstract class RectImgDescrVisitor2<T,R> {

    public abstract R caseRoot(RootRectImgDescr node, T param);

    public abstract R caseFill(FillRectImgDescr node, T param);
    public abstract R caseRoundBorder(RoundBorderRectImgDescr node, T param);
    public abstract R caseBorder(BorderRectImgDescr node, T param);
    public abstract R caseTopBottomBorder(TopBottomBorderRectImgDescr node, T param);
    public abstract R caseLeftRightBorder(LeftRightBorderRectImgDescr node, T param);
    public abstract R caseVerticalSplit(VerticalSplitRectImgDescr node, T param);
    public abstract R caseHorizontalSplit(HorizontalSplitRectImgDescr node, T param);
    public abstract R caseLinesSplit(LinesSplitRectImgDescr node, T param);
    public abstract R caseColumnsSplit(ColumnsSplitRectImgDescr node, T param);
    public abstract R caseRawData(RawDataRectImgDescr node, T param);
    public abstract R caseGlyph(GlyphRectImgDescr node, T param);
    public abstract R caseAbove(RectImgAboveRectImgDescr node, T param);
    public abstract R caseNoiseAbove(NoiseAbovePartsRectImgDescr node, T param);
    public abstract R caseNoiseAboveParts_Pt(NoiseAbovePartsRectImgDescr parent, int partIndex, PtNoiseFragment node, T param);
    public abstract R caseNoiseAboveParts_Segment(NoiseAbovePartsRectImgDescr parent, int partIndex, SegmentNoiseFragment node, T param);
    public abstract R caseNoiseAboveParts_ConnexSegmentLines(NoiseAbovePartsRectImgDescr parent, int partIndex, ConnexSegmentLinesNoiseFragment node, T param);
    public abstract R caseOverrideAttributesProxy(OverrideAttributesProxyRectImgDescr node, T param);

    public abstract R caseAnalysisProxy(AnalysisProxyRectImgDescr node, T param);

}

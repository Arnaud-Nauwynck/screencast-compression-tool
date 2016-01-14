package fr.an.screencast.compressor.imgtool.rectdescr.ast;

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
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.RoundBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.TopBottomBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescriptionAST.VerticalSplitRectImgDescr;

public abstract class RectImgDescrVisitor2<T,R> {

    public abstract R caseFillRect(FillRectImgDescr node, T param);
    public abstract R caseRoundBorderDescr(RoundBorderRectImgDescr node, T param);
    public abstract R caseBorderDescr(BorderRectImgDescr node, T param);
    public abstract R caseTopBottomBorderDescr(TopBottomBorderRectImgDescr node, T param);
    public abstract R caseLeftRightBorderDescr(LeftRightBorderRectImgDescr node, T param);
    public abstract R caseVerticalSplitDescr(VerticalSplitRectImgDescr node, T param);
    public abstract R caseHorizontalSplitDescr(HorizontalSplitRectImgDescr node, T param);
    public abstract R caseLinesSplitDescr(LinesSplitRectImgDescr node, T param);
    public abstract R caseColumnsSplitDescr(ColumnsSplitRectImgDescr node, T param);
    public abstract R caseRawDataDescr(RawDataRectImgDescr node, T param);
    public abstract R caseGlyphDescr(GlyphRectImgDescr node, T param);
    public abstract R caseAboveDescr(RectImgAboveRectImgDescr node, T param);

    public abstract R caseAnalysisProxyRect(AnalysisProxyRectImgDescr node, T param);

}

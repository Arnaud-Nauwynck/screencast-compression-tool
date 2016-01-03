package fr.an.screencast.compressor.imgtool.rectdescr.ast;

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

public abstract class RectImgDescrVisitor {

    public abstract void caseFillRect(FillRectImgDescr node);
    public abstract void caseRoundBorderDescr(RoundBorderRectImgDescr node);
    public abstract void caseBorderDescr(BorderRectImgDescr node);
    public abstract void caseTopBottomBorderDescr(TopBottomBorderRectImgDescr node);
    public abstract void caseLeftRightBorderDescr(LeftRightBorderRectImgDescr node);
    public abstract void caseVerticalSplitDescr(VerticalSplitRectImgDescr node);
    public abstract void caseHorizontalSplitDescr(HorizontalSplitRectImgDescr node);
    public abstract void caseLinesSplitDescr(LinesSplitRectImgDescr node);
    public abstract void caseColumnsSplitDescr(ColumnsSplitRectImgDescr node);
    public abstract void caseRawDataDescr(RawDataRectImgDescr node);
    public abstract void caseGlyphDescr(GlyphRectImgDescr node);
    public abstract void caseDescrAboveDescr(RectImgAboveRectImgDescr node);
     
}

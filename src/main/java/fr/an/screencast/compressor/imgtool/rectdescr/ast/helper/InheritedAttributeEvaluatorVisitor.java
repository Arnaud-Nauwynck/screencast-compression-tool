package fr.an.screencast.compressor.imgtool.rectdescr.ast.helper;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrVisitor2;
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

/**
 * recursive evaluator of synthetised attribute on RectImgDescr AST
 * 
 * @param <TVal>
 * @param <TInheritedVal>
 */
public class InheritedAttributeEvaluatorVisitor<TVal,TInheritedVal> extends RectImgDescrVisitor2<TInheritedVal,TInheritedVal> {

    private Supplier<TVal> baseValFactory;
    private BiConsumer<RectImgDescription,TVal> baseValEvaluator;
    // private BiConsumer<RectImgDescription,TVal> baseAttributeUpdater;
    
    private Supplier<TInheritedVal> valFactory;
    private BiFunction<TInheritedVal,TVal,TInheritedVal> mapFunc;
    private BiFunction<TInheritedVal,TInheritedVal,TInheritedVal> reduceFunc;
    
    private BiConsumer<RectImgDescription,TInheritedVal> attributeUpdater;
    
    // ------------------------------------------------------------------------
    
    public InheritedAttributeEvaluatorVisitor(
            Supplier<TVal> baseValFactory,
            BiConsumer<RectImgDescription,TVal> baseValEvaluator,
            Supplier<TInheritedVal> valFactory, 
            BiFunction<TInheritedVal, TVal, TInheritedVal> mapFunc,
            BiFunction<TInheritedVal, TInheritedVal, TInheritedVal> reduceFunc,
            BiConsumer<RectImgDescription,TInheritedVal> attributeUpdater) {
        this.baseValFactory = baseValFactory;
        this.baseValEvaluator = baseValEvaluator;
        this.valFactory = valFactory;
        this.mapFunc = mapFunc;
        this.reduceFunc = reduceFunc;
        this.attributeUpdater = attributeUpdater;
    }
    
    // ------------------------------------------------------------------------

    protected TInheritedVal evalRecursive(RectImgDescription node) {
        TInheritedVal res = valFactory.get();
        TVal base = evalBase(node);
        // (optional) store base attribute on node?
        // if (baseAttributeUpdater != null) { baseAttributeUpdater.apply(node, base); }
        res = mapFunc.apply(res, base);
        // recursive merge with child synthetised results
        res = node.accept(this, res);
        // (optional) store synth attribute on node
        if (attributeUpdater != null) {
            attributeUpdater.accept(node, res);
        }
        return res;
    }

    protected TVal evalBase(RectImgDescription node) {
        TVal res = baseValFactory.get();
        if (node != null) {
            baseValEvaluator.accept(node, res);
        }
        return res;
    }
    
    protected TInheritedVal recurseMerge(TInheritedVal synth, RectImgDescription child) {
        TInheritedVal res = synth;
        if (child != null) {
            TInheritedVal childRes = evalRecursive(child);
            res = reduceFunc.apply(res, childRes);
        }
        return res;
    }

    protected TInheritedVal recurseMerge(TInheritedVal synth, RectImgDescription[] childLs) {
        TInheritedVal res = synth;
        if (childLs != null) {
            for(RectImgDescription child : childLs) {
                if (child != null) {
                    TInheritedVal childRes = evalRecursive(child);
                    res = reduceFunc.apply(res, childRes);
                }
            }
        }
        return res;
    }

    @Override
    public TInheritedVal caseFillRect(FillRectImgDescr node, TInheritedVal input) {
        return input;
    }

    @Override
    public TInheritedVal caseRoundBorderDescr(RoundBorderRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescription inside = node.getInside();
        res = recurseMerge(res, inside);
        return res;
    }

    @Override
    public TInheritedVal caseBorderDescr(BorderRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescription inside = node.getInside();
        res = recurseMerge(res, inside);
        return res;
    }

    @Override
    public TInheritedVal caseTopBottomBorderDescr(TopBottomBorderRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescription inside = node.getInside();
        res = recurseMerge(res, inside);
        return res;
    }

    @Override
    public TInheritedVal caseLeftRightBorderDescr(LeftRightBorderRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescription inside = node.getInside();
        res = recurseMerge(res, inside);
        return res;
    }

    @Override
    public TInheritedVal caseVerticalSplitDescr(VerticalSplitRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescription left = node.getLeft();
        RectImgDescription right = node.getRight();
        res = recurseMerge(res, left);
        res = recurseMerge(res, right);
        return res;
    }

    @Override
    public TInheritedVal caseHorizontalSplitDescr(HorizontalSplitRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescription left = node.getUp();
        RectImgDescription right = node.getDown();
        res = recurseMerge(res, left);
        res = recurseMerge(res, right);
        return res;
    }

    @Override
    public TInheritedVal caseLinesSplitDescr(LinesSplitRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescription[] lines = node.getLines();
        res = recurseMerge(res, lines);
        return res;
    }

    @Override
    public TInheritedVal caseColumnsSplitDescr(ColumnsSplitRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescription[] cols = node.getColumns();
        res = recurseMerge(res, cols);
        return res;
    }

    @Override
    public TInheritedVal caseRawDataDescr(RawDataRectImgDescr node, TInheritedVal input) {
        return input;
    }

    @Override
    public TInheritedVal caseGlyphDescr(GlyphRectImgDescr node, TInheritedVal input) {
        return input;
    }

    @Override
    public TInheritedVal caseAboveDescr(RectImgAboveRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescription underlying = node.getUnderlyingRectImgDescr();
        RectImgDescription[] aboves = node.getAboveRectImgDescrs();
        res = recurseMerge(res, underlying);
        res = recurseMerge(res, aboves);
        return res;
    }

    @Override
    public TInheritedVal caseAnalysisProxyRect(AnalysisProxyRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescription target= node.getTarget();
        res = recurseMerge(res, target);
        return res;
    }

}

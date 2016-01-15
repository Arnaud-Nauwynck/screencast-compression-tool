package fr.an.screencast.compressor.imgtool.rectdescr.ast.helper;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

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

/**
 * recursive evaluator of synthetised attribute on RectImgDescr AST
 * 
 * @param <TVal>
 * @param <TInheritedVal>
 */
public class InheritedAttributeEvaluatorVisitor<TVal,TInheritedVal> extends RectImgDescrVisitor2<TInheritedVal,TInheritedVal> {

    @FunctionalInterface
    public static interface LightweightNoiseFragAdder<TInheritedVal> {
        public TInheritedVal apply(NoiseAbovePartsRectImgDescr parent, int partIndex, NoiseFragment node, TInheritedVal input);
    }

    private Supplier<TVal> baseValFactory;
    private BiConsumer<RectImgDescription,TVal> baseValEvaluator;
    private LightweightNoiseFragAdder<TInheritedVal> lightweightNoiseFragAdder;
    // private BiConsumer<RectImgDescription,TVal> baseAttributeUpdater;
    
    private Supplier<TInheritedVal> valFactory;
    private BiFunction<TInheritedVal,TVal,TInheritedVal> mapFunc;
    private BiFunction<TInheritedVal,TInheritedVal,TInheritedVal> reduceFunc;
    
    private BiConsumer<RectImgDescription,TInheritedVal> attributeUpdater;
    
    // ------------------------------------------------------------------------
    
    public InheritedAttributeEvaluatorVisitor(
            Supplier<TVal> baseValFactory,
            BiConsumer<RectImgDescription,TVal> baseValEvaluator,
            LightweightNoiseFragAdder<TInheritedVal> lightweightNoiseFragAdder,
            Supplier<TInheritedVal> valFactory, 
            BiFunction<TInheritedVal, TVal, TInheritedVal> mapFunc,
            BiFunction<TInheritedVal, TInheritedVal, TInheritedVal> reduceFunc,
            BiConsumer<RectImgDescription,TInheritedVal> attributeUpdater) {
        this.baseValFactory = baseValFactory;
        this.baseValEvaluator = baseValEvaluator;
        this.lightweightNoiseFragAdder = lightweightNoiseFragAdder;
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

    // implements Visitor
    // ------------------------------------------------------------------------
    
    @Override
    public TInheritedVal caseRoot(RootRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescription target = node.getTarget();
        res = recurseMerge(res, target);
        return res;
    }
    
    @Override
    public TInheritedVal caseFill(FillRectImgDescr node, TInheritedVal input) {
        return input;
    }

    @Override
    public TInheritedVal caseRoundBorder(RoundBorderRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescription inside = node.getInside();
        res = recurseMerge(res, inside);
        return res;
    }

    @Override
    public TInheritedVal caseBorder(BorderRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescription inside = node.getInside();
        res = recurseMerge(res, inside);
        return res;
    }

    @Override
    public TInheritedVal caseTopBottomBorder(TopBottomBorderRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescription inside = node.getInside();
        res = recurseMerge(res, inside);
        return res;
    }

    @Override
    public TInheritedVal caseLeftRightBorder(LeftRightBorderRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescription inside = node.getInside();
        res = recurseMerge(res, inside);
        return res;
    }

    @Override
    public TInheritedVal caseVerticalSplit(VerticalSplitRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescription left = node.getLeft();
        RectImgDescription right = node.getRight();
        res = recurseMerge(res, left);
        res = recurseMerge(res, right);
        return res;
    }

    @Override
    public TInheritedVal caseHorizontalSplit(HorizontalSplitRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescription left = node.getUp();
        RectImgDescription right = node.getDown();
        res = recurseMerge(res, left);
        res = recurseMerge(res, right);
        return res;
    }

    @Override
    public TInheritedVal caseLinesSplit(LinesSplitRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescription[] lines = node.getLines();
        res = recurseMerge(res, lines);
        return res;
    }

    @Override
    public TInheritedVal caseColumnsSplit(ColumnsSplitRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescription[] cols = node.getColumns();
        res = recurseMerge(res, cols);
        return res;
    }

    @Override
    public TInheritedVal caseRawData(RawDataRectImgDescr node, TInheritedVal input) {
        return input;
    }

    @Override
    public TInheritedVal caseGlyph(GlyphRectImgDescr node, TInheritedVal input) {
        return input;
    }

    @Override
    public TInheritedVal caseAbove(RectImgAboveRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescription underlying = node.getUnderlying();
        RectImgDescription[] aboves = node.getAboves();
        res = recurseMerge(res, underlying);
        res = recurseMerge(res, aboves);
        return res;
    }

    
    @Override
    public TInheritedVal caseNoiseAbove(NoiseAbovePartsRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescription underlying = node.getUnderlying();
        res = recurseMerge(res, underlying);
        NoiseFragment[][] noiseFragmentsAboveParts = node.getNoiseFragmentsAboveParts();
        if (noiseFragmentsAboveParts != null) {
            for (int part = 0; part < noiseFragmentsAboveParts.length; part++) {
                NoiseFragment[] frags = noiseFragmentsAboveParts[part];
                if (frags != null) {
                    for(NoiseFragment frag : frags) {
                        res = frag.accept(this, node, part, res);
                    }
                }
            }
        }
        return res;
    }

    @Override
    public TInheritedVal caseNoiseAboveParts_Pt(NoiseAbovePartsRectImgDescr parent, int partIndex, PtNoiseFragment node, TInheritedVal input) {
        TInheritedVal res = lightweightNoiseFragAdder.apply(parent, partIndex, node, input);
        return res;
    }

    @Override
    public TInheritedVal caseNoiseAboveParts_Segment(NoiseAbovePartsRectImgDescr parent, int partIndex, SegmentNoiseFragment node, TInheritedVal input) {
        TInheritedVal res = lightweightNoiseFragAdder.apply(parent, partIndex, node, input);
        return res;
    }

    @Override
    public TInheritedVal caseNoiseAboveParts_ConnexSegmentLines(NoiseAbovePartsRectImgDescr parent, int partIndex,
            ConnexSegmentLinesNoiseFragment node, TInheritedVal input) {
        TInheritedVal res = lightweightNoiseFragAdder.apply(parent, partIndex, node, input);
        return res;
    }

    @Override
    public TInheritedVal caseOverrideAttributesProxy(OverrideAttributesProxyRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescription underlying = node.getUnderlying();
        res = recurseMerge(res, underlying);
        return res;
    }

    @Override
    public TInheritedVal caseAnalysisProxy(AnalysisProxyRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescription target= node.getTarget();
        res = recurseMerge(res, target);
        return res;
    }

}

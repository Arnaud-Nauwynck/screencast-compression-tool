package fr.an.screencast.compressor.imgtool.rectdescr.ast.helper;

import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

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

/**
 * recursive evaluator of synthetised attribute on RectImgDescr AST
 * 
 * @param <TVal>
 * @param <TInheritedVal>
 */
public class SynthetisedAttributeEvaluatorVisitor<TVal,TInheritedVal> extends RectImgDescrVisitor2<TInheritedVal,TInheritedVal> {

    @FunctionalInterface
    public static interface LightweightNoiseFragAdder<TInheritedVal> {
        public TInheritedVal apply(NoiseAbovePartsRectImgDescr parent, int partIndex, NoiseFragment node, TInheritedVal input);
    }

    private Supplier<TVal> baseValFactory;
    private BiConsumer<RectImgDescr,TVal> nodeValEvaluator;
    private LightweightNoiseFragAdder<TInheritedVal> lightweightNodeFragAdder;
    // private BiConsumer<RectImgDescription,TVal> baseAttributeUpdater;
    
    private Supplier<TInheritedVal> valFactory;
    private BiFunction<TInheritedVal,TVal,TInheritedVal> mapFunc;
    private BiFunction<TInheritedVal,TInheritedVal,TInheritedVal> reduceFunc;
    
    private BiConsumer<RectImgDescr,TInheritedVal> attributeUpdater;
    
    // ------------------------------------------------------------------------
    
    public SynthetisedAttributeEvaluatorVisitor(
            Supplier<TVal> baseValFactory,
            BiConsumer<RectImgDescr,TVal> nodeValEvaluator,
            LightweightNoiseFragAdder<TInheritedVal> lightweightNodeFragAdder,
            Supplier<TInheritedVal> valFactory,
            BiFunction<TInheritedVal, TVal, TInheritedVal> mapFunc,
            BiFunction<TInheritedVal, TInheritedVal, TInheritedVal> reduceFunc,
            BiConsumer<RectImgDescr,TInheritedVal> attributeUpdater) {
        this.baseValFactory = baseValFactory;
        this.nodeValEvaluator = nodeValEvaluator;
        this.lightweightNodeFragAdder = lightweightNodeFragAdder;
        this.valFactory = valFactory;
        this.mapFunc = mapFunc;
        this.reduceFunc = reduceFunc;
        this.attributeUpdater = attributeUpdater;
    }
    
    // ------------------------------------------------------------------------

    protected TInheritedVal evalRecursive(RectImgDescr node) {
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

    protected TVal evalBase(RectImgDescr node) {
        TVal res = baseValFactory.get();
        if (node != null) {
            nodeValEvaluator.accept(node, res);
        }
        return res;
    }
    
    protected TInheritedVal recurseMerge(TInheritedVal synth, RectImgDescr child) {
        TInheritedVal res = synth;
        if (child != null) {
            TInheritedVal childRes = evalRecursive(child);
            res = reduceFunc.apply(res, childRes);
        }
        return res;
    }

    protected TInheritedVal recurseMerge(TInheritedVal synth, RectImgDescr[] childLs) {
        TInheritedVal res = synth;
        if (childLs != null) {
            for(RectImgDescr child : childLs) {
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
        RectImgDescr target = node.getTarget();
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
        RectImgDescr inside = node.getInside();
        res = recurseMerge(res, inside);
        return res;
    }

    @Override
    public TInheritedVal caseBorder(BorderRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescr inside = node.getInside();
        res = recurseMerge(res, inside);
        return res;
    }

    @Override
    public TInheritedVal caseTopBottomBorder(TopBottomBorderRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescr inside = node.getInside();
        res = recurseMerge(res, inside);
        return res;
    }

    @Override
    public TInheritedVal caseLeftRightBorder(LeftRightBorderRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescr inside = node.getInside();
        res = recurseMerge(res, inside);
        return res;
    }

    @Override
    public TInheritedVal caseVerticalSplit(VerticalSplitRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescr left = node.getLeft();
        RectImgDescr right = node.getRight();
        res = recurseMerge(res, left);
        res = recurseMerge(res, right);
        return res;
    }

    @Override
    public TInheritedVal caseHorizontalSplit(HorizontalSplitRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescr left = node.getUp();
        RectImgDescr right = node.getDown();
        res = recurseMerge(res, left);
        res = recurseMerge(res, right);
        return res;
    }

    @Override
    public TInheritedVal caseLinesSplit(LinesSplitRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescr[] lines = node.getLines();
        res = recurseMerge(res, lines);
        return res;
    }

    @Override
    public TInheritedVal caseColumnsSplit(ColumnsSplitRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescr[] cols = node.getColumns();
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
        RectImgDescr underlying = node.getUnderlying();
        RectImgDescr[] aboves = node.getAboves();
        res = recurseMerge(res, underlying);
        res = recurseMerge(res, aboves);
        return res;
    }

    
    @Override
    public TInheritedVal caseNoiseAbove(NoiseAbovePartsRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescr underlying = node.getUnderlying();
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
        TInheritedVal res = lightweightNodeFragAdder.apply(parent, partIndex, node, input);
        return res;
    }

    @Override
    public TInheritedVal caseNoiseAboveParts_Segment(NoiseAbovePartsRectImgDescr parent, int partIndex, SegmentNoiseFragment node, TInheritedVal input) {
        TInheritedVal res = lightweightNodeFragAdder.apply(parent, partIndex, node, input);
        return res;
    }

    @Override
    public TInheritedVal caseNoiseAboveParts_ConnexSegmentLines(NoiseAbovePartsRectImgDescr parent, int partIndex,
            ConnexSegmentLinesNoiseFragment node, TInheritedVal input) {
        TInheritedVal res = lightweightNodeFragAdder.apply(parent, partIndex, node, input);
        return res;
    }

    @Override
    public TInheritedVal caseOverrideAttributesProxy(OverrideAttributesProxyRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescr underlying = node.getUnderlying();
        res = recurseMerge(res, underlying);
        return res;
    }

    @Override
    public TInheritedVal caseAnalysisProxy(AnalysisProxyRectImgDescr node, TInheritedVal input) {
        TInheritedVal res = input;
        RectImgDescr target= node.getTarget();
        res = recurseMerge(res, target);
        return res;
    }

}

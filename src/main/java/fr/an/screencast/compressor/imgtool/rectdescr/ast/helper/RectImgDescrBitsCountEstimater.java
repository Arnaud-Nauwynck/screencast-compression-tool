package fr.an.screencast.compressor.imgtool.rectdescr.ast.helper;

import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Supplier;

import fr.an.bitwise4j.encoder.structio.Pow2Utils;
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
import fr.an.screencast.compressor.utils.Rect;
import fr.an.screencast.compressor.utils.Segment;

public class RectImgDescrBitsCountEstimater {

    public static class BitsCount {
        private String key;
        private int count;
        private int estimatedBitsCount;
        
        public BitsCount(String key) {
            this.key = key;
        }

        public void incr(int incrEstimatedBitsCount) {
            count++;
            estimatedBitsCount += incrEstimatedBitsCount;
        }

        public void incr(BitsCount src) {
            count += src.count;
            estimatedBitsCount += src.estimatedBitsCount;
        }

        public int getCount() {
            return count;
        }

        public int getEstimatedBitsCount() {
            return estimatedBitsCount;
        }

        @Override
        public String toString() {
            return "count:" + count 
                    + ", estimated Bits=" + estimatedBitsCount 
                    + ((estimatedBitsCount>1024)? " (" + (estimatedBitsCount/1024) + " KB)" : "");
        }
        
        
    }
    
    /**
     * estimated bits counter with synthetised sum (per child class categories) 
     */
    public static class SynthetisedBitsCount {
        
        private BitsCount sum = new BitsCount(null);
        private Map<String,BitsCount> sumPerClass = new TreeMap<String,BitsCount>();
        
        public SynthetisedBitsCount incr(BitsCount childStats) {
            sum.incr(childStats);
            String key = childStats.key;
            BitsCount statsPerKey = sumPerClass.get(key);
            if (statsPerKey == null) {
                statsPerKey = new BitsCount(key);
                sumPerClass.put(key, statsPerKey);
            }
            statsPerKey.incr(childStats);
            return this;
        }

        public SynthetisedBitsCount incr(SynthetisedBitsCount src) {
            sum.incr(src.sum);
            for(Map.Entry<String,BitsCount> e : src.sumPerClass.entrySet()) {
                String key = e.getKey();
                BitsCount incrPerKey = e.getValue();
                
                BitsCount statsPerKey = sumPerClass.get(key);
                if (statsPerKey == null) {
                    statsPerKey = new BitsCount(key);
                    sumPerClass.put(key, statsPerKey);
                }
                statsPerKey.incr(incrPerKey);
            }
            return this;
        }

        public BitsCount getSum() {
            return sum;
        }

        public Map<String, BitsCount> getSumPerClass() {
            return sumPerClass;
        }

        public String toStringFilter(int thresholdBitsCount) {
            StringBuilder sb = new StringBuilder();
            sb.append("Total: " + sum + "\n");
            for (Map.Entry<String, BitsCount> e : sumPerClass.entrySet()) {
                if (e.getValue().estimatedBitsCount >= thresholdBitsCount) {
                    sb.append(" per " + e.getKey() + ": " + e.getValue() + "\n");
                }
            }
            return sb.toString();
        }
        
        @Override
        public String toString() {
            return toStringFilter(0);
        }

    }
    
    // ------------------------------------------------------------------------
    
    protected static String pixelSmallMediumLarge(int val) {
        if (val <= 1) return "p";
        else if (val <= 5) return "s";
        else if (val <= 20) return "m";
        else if (val <= 50) return "l";
        else return "L";
    }
    
    protected static String nodeToKey(RectImgDescription node) {
        int w = node.getWidth(), h = node.getHeight();
        String res = node.getClass().getSimpleName() + "-" + pixelSmallMediumLarge(w) + "x" + pixelSmallMediumLarge(h);
        return res; 
    }
    
    public static void evalBaseStatsBitsCount(RectImgDescription node, BitsCount res) {
        node.accept(StatsBitsCountBaseEstimater.INSTANCE, res);
    }
    
    public static SynthetisedBitsCount recursiveEvalSynthetisedStatsBitsCount(RectImgDescription rootNode, Map<RectImgDescription,SynthetisedBitsCount> results) {
        if (results == null) {
            results = new HashMap<RectImgDescription,SynthetisedBitsCount>();
        }
        final Map<RectImgDescription,SynthetisedBitsCount> resAttributes = results;
        
        Supplier<BitsCount> baseValFactory = () -> new BitsCount(null);
        BiConsumer<RectImgDescription,BitsCount> baseValEvaluator = 
                (n,r) -> { r.key = nodeToKey(n); n.accept(StatsBitsCountBaseEstimater.INSTANCE, r); };
        // BiConsumer<RectImgDescription,TVal> baseAttributeUpdater;
        
        Supplier<SynthetisedBitsCount> valFactory = SynthetisedBitsCount::new;
        BiFunction<SynthetisedBitsCount,BitsCount,SynthetisedBitsCount> mapFunc =
                (synth,val) -> synth.incr(val);
        BiFunction<SynthetisedBitsCount,SynthetisedBitsCount,SynthetisedBitsCount> reduceFunc =
                (s1,s2) -> s1.incr(s2); 
        
        BiConsumer<RectImgDescription,SynthetisedBitsCount> attributeUpdater =
                (n,s) -> { resAttributes.put(n, s); };
        
        InheritedAttributeEvaluatorVisitor<BitsCount,SynthetisedBitsCount> recursiveMergeEval = 
                new InheritedAttributeEvaluatorVisitor<BitsCount,SynthetisedBitsCount>(
                        baseValFactory, baseValEvaluator, valFactory, 
                        mapFunc, reduceFunc,
                        attributeUpdater
                        );
        
        SynthetisedBitsCount res = new SynthetisedBitsCount();

        // ** do recursive eval **
        rootNode.accept(recursiveMergeEval, res);
        
        results.put(rootNode, res);
        return res;
    }
    
    // ------------------------------------------------------------------------

    /**
     * estimater of bits count for RectImgDescription base object only, not recursive
     */
    private static class StatsBitsCountBaseEstimater extends RectImgDescrVisitor2<BitsCount,Void> {

        private static final StatsBitsCountBaseEstimater INSTANCE = new StatsBitsCountBaseEstimater();
        private static final int COLOR_BITS = 3*8;
        private static final int ESTIM_BORDER_LEN_BITS = 4;
        
        // ------------------------------------------------------------------------

        public StatsBitsCountBaseEstimater() {
        }

        // ------------------------------------------------------------------------

        protected static int estimSumBitsForPoints(RectImgDescription node, int xCount, int yCount) {
            Rect rect = node.getRect();
            int res = 0;
            if (xCount > 0) {
                int xBitsCount = Pow2Utils.valueToUpperLog2(rect.getWidth());
                res += xCount * xBitsCount;
                // for sorted order points => better encoding is by divide&conquer...
                // = xBitsCount + (xBitsCount-1) + (xBitsCount-2) + .. + (xBitsCount-xCount)
                // = xCount * xBitsCount - xCount*(xCount-1)/2;
            }
            if (yCount > 0) {
                int yBitsCount = Pow2Utils.valueToUpperLog2(rect.getHeight());
                res += yCount * yBitsCount;
                // for sorted order points => 
            }
            return res;
        }
        
        @Override
        public Void caseFillRect(FillRectImgDescr node, BitsCount stats) {
            stats.incr(COLOR_BITS);
            return null;
        }

        @Override
        public Void caseRoundBorderDescr(RoundBorderRectImgDescr node, BitsCount stats) {
            int bitsCount = 2*COLOR_BITS // borderColor, cornerBackgroundColor
                    + 4 * ESTIM_BORDER_LEN_BITS // TopCornerDim, BottomCornerDim
                    + ESTIM_BORDER_LEN_BITS; // BorderThick
            stats.incr(bitsCount);
            return null;
        }

        @Override
        public Void caseBorderDescr(BorderRectImgDescr node, BitsCount stats) {
            int bitsCount = COLOR_BITS + 4 * ESTIM_BORDER_LEN_BITS;
            stats.incr(bitsCount);
            return null;
        }

        @Override
        public Void caseTopBottomBorderDescr(TopBottomBorderRectImgDescr node, BitsCount stats) {
            int bitsCount = COLOR_BITS + 2 * ESTIM_BORDER_LEN_BITS;
            stats.incr(bitsCount);
            return null;
        }

        @Override
        public Void caseLeftRightBorderDescr(LeftRightBorderRectImgDescr node, BitsCount stats) {
            int bitsCount = COLOR_BITS + 2 * ESTIM_BORDER_LEN_BITS;
            stats.incr(bitsCount);
            return null;
        }

        @Override
        public Void caseVerticalSplitDescr(VerticalSplitRectImgDescr node, BitsCount stats) {
            int bitsCount = COLOR_BITS // splitColor
                    + estimSumBitsForPoints(node, 2, 0); //  SplitBorder
            stats.incr(bitsCount);
            return null;
        }

        @Override
        public Void caseHorizontalSplitDescr(HorizontalSplitRectImgDescr node, BitsCount stats) {
            int bitsCount = COLOR_BITS // splitColor
                    + estimSumBitsForPoints(node, 0, 2); //  SplitBorder
            stats.incr(bitsCount);
            return null;
        }

        @Override
        public Void caseLinesSplitDescr(LinesSplitRectImgDescr node, BitsCount stats) {
            Segment[] splitBorders = node.getSplitBorders();
            int splitBordersCount = splitBorders != null? splitBorders.length : 0; 
            int bitsCount = COLOR_BITS // splitColor
                    + estimSumBitsForPoints(node, 0, splitBordersCount); //  SplitBorders
            stats.incr(bitsCount);
            return null;
        }

        @Override
        public Void caseColumnsSplitDescr(ColumnsSplitRectImgDescr node, BitsCount stats) {
            Segment[] splitBorders = node.getSplitBorders();
            int splitBordersCount = splitBorders != null? splitBorders.length : 0; 
            int bitsCount = COLOR_BITS // splitColor
                    + estimSumBitsForPoints(node, splitBordersCount, 0); //  SplitBorders
            stats.incr(bitsCount);
            return null;
        }

        @Override
        public Void caseRawDataDescr(RawDataRectImgDescr node, BitsCount stats) {
            int bitsCount = node.getRect().getArea() * COLOR_BITS;
            bitsCount -= (bitsCount >>> 3); // -1/8  estim basic gzip compression
            stats.incr(bitsCount);
            return null;
        }

        @Override
        public Void caseGlyphDescr(GlyphRectImgDescr node, BitsCount stats) {
            int bitsCount = node.getRect().getArea() * COLOR_BITS;
            bitsCount -= (bitsCount >>> 3); // -1/8  estim basic gzip compression
            stats.incr(bitsCount);
            return null;
        }

        @Override
        public Void caseAboveDescr(RectImgAboveRectImgDescr node, BitsCount stats) {
            final Rect[] aboveRects = node.getAboveRects();
            int aboveRectsCount = aboveRects != null? aboveRects.length : 0; 
            int bitsCount = estimSumBitsForPoints(node, aboveRectsCount, aboveRectsCount);
            stats.incr(bitsCount);
            return null;
        }

        @Override
        public Void caseAnalysisProxyRect(AnalysisProxyRectImgDescr node, BitsCount stats) {
            return null;
        }

    }
    
}

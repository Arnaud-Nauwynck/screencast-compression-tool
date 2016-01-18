package fr.an.screencast.compressor.imgtool.rectdescr.ast.codec;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.bitwise4j.encoder.huffman.HuffmanTable;
import fr.an.bitwise4j.encoder.structio.StructDataInput;
import fr.an.bitwise4j.encoder.structio.IStreamMultiplexerSupport.StreamPopper;
import fr.an.screencast.compressor.imgtool.glyph.GlyphIndexOrCode;
import fr.an.screencast.compressor.imgtool.glyph.GlyphMRUTable;
import fr.an.screencast.compressor.imgtool.glyph.GlyphMRUTable.GlyphMRUNode;
import fr.an.screencast.compressor.imgtool.rectdescr.ExternalFormatRawDataHelper;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrVisitor;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST;
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
import fr.an.screencast.compressor.imgtool.rectdescr.ast.helper.DumpRectImgDescrVisitor;
import fr.an.screencast.compressor.imgtool.utils.IntsCRC32;
import fr.an.screencast.compressor.utils.Border;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.screencast.compressor.utils.Segment;

/**
 * RectImgDescrVisitor implementation for recursive decoding RectImgDescr as bitstream
 */
public class BitStreamInputRectImgDescrVisitor extends RectImgDescrVisitor {
    
    private static final Logger LOG = LoggerFactory.getLogger(BitStreamInputRectImgDescrVisitor.class);

    private static final boolean DEBUG_MARK = BitStreamOutputRectImgDescrVisitor.DEBUG_MARK;
    
    private RectImgDescrCodecConfig codecConfig;
    
    private StructDataInput in;

    private HuffmanTable<Class<? extends RectImgDescr>> huffmanTableRectImgDescriptionClass;
    private GlyphMRUTable glyphMRUTable;
    // TODO ... private Map<String,HuffmanTable<Integer>> field2huffmanTableColor;

    private ExternalFormatRawDataHelper externalFormatHelper = new ExternalFormatRawDataHelper();

    private List<Rect> currRectStack = new ArrayList<Rect>();
    
    // ------------------------------------------------------------------------

    public BitStreamInputRectImgDescrVisitor(RectImgDescrCodecConfig codecConfig, StructDataInput in) {
        this.codecConfig = codecConfig;
        this.in = in;
        this.huffmanTableRectImgDescriptionClass = codecConfig.createHuffmanTableForClass2Frequency();
        this.glyphMRUTable = codecConfig.createGlyphMRUTable();
    }

    // ------------------------------------------------------------------------
    
    public RectImgDescrCodecConfig getCodecConfig() {
        return codecConfig;
    }
    
    public static RectImgDescr readTopLevelFrom(RectImgDescrCodecConfig codecConfig, StructDataInput in) {
        BitStreamInputRectImgDescrVisitor visitor = new BitStreamInputRectImgDescrVisitor(codecConfig, in);
        return visitor.readTopLevel();
    }

    public RectImgDescr readTopLevel() {
        if (codecConfig.isDebugAddBeginEndMarker()) {
            debugReadCheckMarker("RectImgDecr{{{");
        }
        
        Rect rect = new Rect();
        rect.fromX = in.readUInt0ElseMax(Short.MAX_VALUE);
        rect.toX = rect.fromX + in.readUIntLt2048ElseMax(Short.MAX_VALUE);
        rect.fromY = in.readUInt0ElseMax(Short.MAX_VALUE);
        rect.toY = rect.fromY + in.readUIntLt2048ElseMax(Short.MAX_VALUE);
        pushRect(rect);

        // RectImgDescription res = doRead();
        Class<? extends RectImgDescr> nodeClass = in.readDecodeHuffmanCode(huffmanTableRectImgDescriptionClass);
        // introspection code equivalent to <code>switch(nodeClass.getName()) { case XX: return new XX(rect);... }</code>
        RectImgDescr node = newInstance(nodeClass, rect);
        try {
            doReadNode(node);
        } catch(Exception ex) {
            LOG.error("Failed to read img descr", ex);
            String dumpText = DumpRectImgDescrVisitor.dumpToString(node);
            LOG.info("partially read img descr:\n" + dumpText);
            throw new RuntimeException("Failed", ex);
        }
        
        popRect();
        
        if (codecConfig.isDebugAddBeginEndMarker()) {
            debugReadCheckMarker("}}}RectImgDecr");
        }
        return node;
    }

    protected RectImgDescr doRead() {
        // TOOPTIM: if small rect => use different HuffmanTable for "mostly" glyph 
        Class<? extends RectImgDescr> nodeClass = in.readDecodeHuffmanCode(huffmanTableRectImgDescriptionClass);
        Rect rect = getCurrRect();
        // introspection code equivalent to <code>switch(nodeClass.getName()) { case XX: return new XX(rect);... }</code>
        RectImgDescr node = newInstance(nodeClass, rect);
        
        doReadNode(node);

        return node;
    }
    
    protected void doReadNode(RectImgDescr node) {
        if (DEBUG_MARK && codecConfig.isDebugAddMarkers()) {
            debugReadCheckMarker("DEBUG_MARK node: {" + node.getClass().getSimpleName() + " " + node.getRect());
        }
        
        node.accept(this);

        if (DEBUG_MARK && codecConfig.isDebugAddMarkers()) {
            debugReadCheckMarker("DEBUG_MARK } node:" + node.getClass().getSimpleName() + " " + node.getRect());
        }
    }

    private void debugReadCheckMarker(String expectedMarker) {
        String checkDebugMark = in.readUTF();
        if (!checkDebugMark.equals(expectedMarker)) {
            throw new RuntimeException("expecting readUTF marker '" + expectedMarker + "' .. got '" + checkDebugMark + "'");
        };
    }
    
    private static RectImgDescr newInstance(Class<? extends RectImgDescr> clss, Rect rect) {
        RectImgDescr res;
        Constructor<?> ctor = class2ctors.get(clss);
        try {
            res = (RectImgDescr) ctor.newInstance(rect);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException("Failed", e);
        }
        return res;
    }
    
    private static Map<Class<? extends RectImgDescr>, Constructor<?>> class2ctors = resolveClassCtors();
    
    private static Map<Class<? extends RectImgDescr>, Constructor<?>> resolveClassCtors() {
        if (class2ctors != null) {
            return class2ctors;
        }
        Map<Class<? extends RectImgDescr>,Constructor<?>> res = new HashMap<>();
        for(Class<? extends RectImgDescr> clss : RectImgDescrCodecConfig.defaultClass2FrequencyMap().keySet()) {
            Constructor<? extends RectImgDescr> ctor;
            try {
                ctor = clss.getDeclaredConstructor(Rect.class);
            } catch (NoSuchMethodException | SecurityException e) {
                throw new RuntimeException("Failed to resolve ctor for " + clss, e);
            }
            res.put(clss, ctor);
        }
        class2ctors = res;
        return class2ctors;
    }

    
    protected RectImgDescr readWithRect(Rect rect) {
        RectImgDescr node;
        if (in.readBit()) {
            pushRect(rect);
            
            node = doRead();
            
            popRect();
        } else {
            node = null;
        }
        return node;
    }
    
    private int readColor(String colorField) {
        // TODO field2huffmanTableColor.get(colorField);
        return in.readInt();
    }
    
    protected void pushRect(Rect p) {
        currRectStack.add(p);
    }

    protected void popRect() {
        currRectStack.remove(currRectStack.size() - 1);
    }
    
    private Rect getCurrRect() {
        return currRectStack.get(currRectStack.size() - 1);
    }

    protected Rect readCurrNestedRect() {
        Rect curr = getCurrRect(); 
        int fromX = in.readIntMinMax(curr.fromX, curr.toX);
        int toX = in.readIntMinMax(fromX, curr.toX + 1);
        int fromY = in.readIntMinMax(curr.fromY, curr.toY);
        int toY = in.readIntMinMax(fromY, curr.toY + 1);
        return Rect.newPtToPt(fromX,  fromY, toX, toY);
    }

    // implements Visitor
    // ------------------------------------------------------------------------
    
    @Override
    public void caseRoot(RootRectImgDescr node) {
        // TODO cf readTopLevel..
        Rect rect = node.getRect();
        RectImgDescr target = readWithRect(rect);
        node.setTarget(target);
    }

    @Override
    public void caseFill(FillRectImgDescr node) {
        int color = readColor("fill");
        node.setColor(color);
    }
    
    @Override
    public void caseRoundBorder(RoundBorderRectImgDescr node) {
        Rect rect = node.getRect();

        node.setBorderColor(readColor("fill"));
        node.setCornerBackgroundColor(readColor("cornerBg"));
        // TODO?
        int maxBorder = Math.min(rect.getWidth()/2,  rect.getHeight()/2);
        int borderThick = in.readIntMinMax(1, maxBorder);
        node.setBorderThick(borderThick);
        int topCornerDimWidth = in.readIntMinMax(0, maxBorder);
        int topCornerDimHeight = in.readIntMinMax(0, maxBorder);
        node.setTopCornerDim(new Dim(topCornerDimWidth, topCornerDimHeight));
        int bottomCornerDimWidth = in.readIntMinMax(0, maxBorder);
        int bottomCornerDimHeight = in.readIntMinMax(0, maxBorder);
        node.setBottomCornerDim(new Dim(bottomCornerDimWidth, bottomCornerDimHeight));

        Rect insideRect = node.getInsideRect();
        RectImgDescr inside = readWithRect(insideRect);
        node.setInside(inside);
    }

    @Override
    public void caseBorder(BorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int W = rect.getWidth(), H = rect.getHeight();
        int borderTop = in.readIntMinMax(0, H-1);
        int borderBottom = in.readIntMinMax(0, H-borderTop);
        int borderLeft = in.readIntMinMax(0, W-1);
        int borderRight = in.readIntMinMax(0, W-borderLeft);
        node.setBorder(new Border(borderLeft, borderRight, borderTop, borderBottom));
        node.setBorderColor(readColor("border"));
        
        Rect insideRect = node.getInsideRect();
        RectImgDescr inside = readWithRect(insideRect);
        node.setInside(inside);
    }

    @Override
    public void caseTopBottomBorder(TopBottomBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int H = rect.getHeight();
        int borderTop = in.readIntMinMax(0, H-1);
        int borderBottom = in.readIntMinMax(0, H-borderTop);
        node.setTopBorder(borderTop);
        node.setBottomBorder(borderBottom);
        node.setBorderColor(readColor("border"));        

        Rect insideRect = node.getInsideRect();
        RectImgDescr inside = readWithRect(insideRect);
        node.setInside(inside);
    }

    @Override
    public void caseLeftRightBorder(LeftRightBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int W = rect.getWidth();
        int borderLeft = in.readIntMinMax(0, W-1);
        int borderRight = in.readIntMinMax(0, W-borderLeft);
        node.setLeftBorder(borderLeft);
        node.setRightBorder(borderRight);        
        node.setBorderColor(readColor("border"));
        
        Rect insideRect = node.getInsideRect();
        RectImgDescr inside = readWithRect(insideRect);
        node.setInside(inside);
    }

    @Override
    public void caseVerticalSplit(VerticalSplitRectImgDescr node) {
        final Rect rect = node.getRect();

        int splitBorderFrom = in.readIntMinMax(rect.fromX, rect.toX);
        int splitBorderTo = in.readIntMinMax(splitBorderFrom+1, rect.toX);
        node.setSplitBorder(new Segment(splitBorderFrom, splitBorderTo));
        node.setSplitColor(readColor("split")); // may use "border" ?

        final Rect leftRect = node.getLeftRect();
        final Rect rightRect = node.getRightRect();
        RectImgDescr left = readWithRect(leftRect);
        node.setLeft(left);
        RectImgDescr right = readWithRect(rightRect);
        node.setRight(right);
    }

    @Override
    public void caseHorizontalSplit(HorizontalSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        int splitBorderFrom = in.readIntMinMax(rect.fromY, rect.toY);
        int splitBorderTo = in.readIntMinMax(splitBorderFrom+1, rect.toY);
        node.setSplitBorder(new Segment(splitBorderFrom, splitBorderTo));
        node.setSplitColor(readColor("split")); // may use "border" ?
        
        final Rect upRect = node.getUpRect();
        final Rect downRect = node.getDownRect();
        RectImgDescr up = readWithRect(upRect);
        node.setUp(up);
        RectImgDescr down = readWithRect(downRect);
        node.setDown(down);
    }

    @Override
    public void caseLinesSplit(LinesSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        if (!in.readBit()) {
            return;
        }
        node.setBackgroundColor(readColor("background"));
        Segment[] splitBorders = readSegmentsOrderedMinMax(rect.fromY, rect.toY);
        node.setSplitBorders(splitBorders);

        Rect[] lineRects = node.getLineRects();
        RectImgDescr[] lines = new RectImgDescr[lineRects.length];
        for(int i = 0; i < lineRects.length; i++) {
            // TODO ... optim encode child when expected TopBottomBorderRectImgDescr with same background color.. 
            lines[i] = readWithRect(lineRects[i]);
        }
        node.setLines(lines);
    }

    // TODO move as utility method
    private Segment[] readSegmentsOrderedMinMax(int min, final int max) {
        int len = in.readUIntLtMinElseMax(32, max/2);
        Segment[] res = new Segment[len];
        int prev = min;
        int remainSplitCount = len - 1;
        int maxInc = max + 1;
        for(int i = 0; i < len; i++) {
            int bFrom = in.readIntMinMax(prev, maxInc-remainSplitCount);
            int bTo = in.readIntMinMax(bFrom, maxInc-remainSplitCount);
            remainSplitCount--;
            prev = bTo;
            res[i] = new Segment(bFrom, bTo);
        }
        return res;
    }

    @Override
    public void caseColumnsSplit(ColumnsSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        if (!in.readBit()) {
            return;
        }
        node.setBackgroundColor(readColor("background"));
        Segment[] splitBorders = readSegmentsOrderedMinMax(rect.fromX, rect.toX);
        node.setSplitBorders(splitBorders);

        Rect[] columnRects = node.getColumnRects();
        RectImgDescr[] columns = new RectImgDescr[columnRects.length];
        for(int i = 0; i < columnRects.length; i++) {
            // TODO ... optim encode child when expected TopBottomBorderRectImgDescr with same background color.. 
            columns[i] = readWithRect(columnRects[i]);
        }
        node.setColumns(columns);
    }

    @Override
    public void caseRawData(RawDataRectImgDescr node) {
        Dim dim = node.getDim();
        int[] rawData;
        try (StreamPopper topPop = in.pushSetCurrStream("rawData")) {
            rawData = externalFormatHelper.readRGBData(in, dim);
        }
        node.setRawData(rawData);
    }


    @Override
    public void caseGlyph(GlyphRectImgDescr node) {
        GlyphIndexOrCode glyphIndexOrCode;
        int crc;
        int[] glyphData;
        boolean isNew = in.readBit();
        if (isNew) {
            // int youngIndex = glyphIndexOrCode.getYoungIndex();
            // implicit .. in.readIntMinMax();
            int youngIndex = 1 + glyphMRUTable.getYoungGlyphIndexCount();
            glyphIndexOrCode = new GlyphIndexOrCode(youngIndex, null);
            
            Dim glyphDim = getCurrRect().getDim();
            try (StreamPopper topPop = in.pushSetCurrStream("glyph")) {
                glyphData = externalFormatHelper.readRGBData(in, glyphDim);
            }
            
            GlyphMRUNode glyphNode = glyphMRUTable.findGlyphByIndexOrCode(glyphIndexOrCode);
            if (glyphNode == null) {
                crc = IntsCRC32.crc32(glyphData);
                glyphMRUTable.addGlyph(glyphDim, glyphData, Rect.newDim(glyphDim), crc);
            } else {
                throw new IllegalStateException("should not occur: glyph index " + youngIndex + " already found in glyph table");
            }
        } else {
            GlyphMRUNode glyphNode = glyphMRUTable.readDecodeReuseGlyphIndexOrCode(in);
            glyphIndexOrCode = glyphNode.getIndexOrCode();
            crc = glyphNode.getCrc();
            glyphData = glyphNode.getData();
        }
        node.setCrc(crc);
        node.setSharedData(glyphData);
        node.setGlyphIndexOrCode(glyphIndexOrCode);
    }

    @Override
    public void caseAbove(RectImgAboveRectImgDescr node) {
        final Rect rect = node.getRect();

        RectImgDescr underlying = readWithRect(rect);
        node.setUnderlying(underlying);

        int aboveCount = in.readIntMinMax(0, rect.getArea()+1);
        RectImgDescr[] aboves = new RectImgDescr[aboveCount]; 
        for (int i = 0; i < aboveCount; i++) {
            Rect aboveRect = readCurrNestedRect();
            
            pushRect(aboveRect);
            aboves[i] = readWithRect(aboveRect);
            popRect();
        }
        node.setAboveRectImgDescrs(aboves);
    }
    
    @Override
    public void caseNoiseAboveParts(NoiseAbovePartsRectImgDescr node) {
        final Rect rect = node.getRect();
        RectImgDescr underlying = readWithRect(rect);
        node.setUnderlying(underlying);
        int maxFragCount = rect.getArea(); // TODO 
        int partCount = node.getUnderlyingExtraPartCount();
        NoiseFragment[][] noiseFragmentsAboveParts = new NoiseFragment[partCount][];
        for (int partIndex = 0; partIndex < partCount; partIndex++) {
            Rect fragRect = node.getUnderlyingExtraPartRect(partIndex);
            int fragCount = in.readIntMinMax(0, maxFragCount);
            NoiseFragment[] frags = new NoiseFragment[fragCount];
            for(int fragI = 0; fragI < fragCount; fragI++) {
                int type = in.readIntMinMax(0, 3); // 0:Pt, 1:Seg, 2:ConnexLines
                NoiseFragment frag;
                switch(type) {
                case 0: {
                    int x = in.readIntMinMax(fragRect.fromX, fragRect.toX);
                    int y = in.readIntMinMax(fragRect.fromY, fragRect.toY);
                    int color = readColor("noise-pt");
                    frag = new PtNoiseFragment(x, y, color);
                } break;
                case 1: { 
                    int fromX = in.readIntMinMax(fragRect.fromX, fragRect.toX);
                    int toX = in.readIntMinMax(fromX + 1, fragRect.toX);
                    int y = in.readIntMinMax(fragRect.fromY, fragRect.toY);
                    int color = readColor("noise-seg");
                    frag = new SegmentNoiseFragment(fromX, toX, y, color);
                } break;
                case 2: { 
                    int fromY = in.readIntMinMax(fragRect.fromY, fragRect.toY);
                    int linesCount = in.readIntMinMax(fromY, fragRect.toY) - fromY;
                    Segment[] lines = new Segment[linesCount];
                    for(int lineI = 0; lineI < linesCount; lineI++) {
                        int fromX = in.readIntMinMax(fragRect.fromX, fragRect.toX);
                        int toX = in.readIntMinMax(fromX + 1, fragRect.toX);
                        lines[lineI] = new Segment(fromX, toX);
                    }
                    int color = readColor("noise-connexe");
                    frag = new ConnexSegmentLinesNoiseFragment(fromY, lines, color);
                } break;
                default:
                    throw new IllegalStateException();
                }
                frags[fragI] = frag; //TODO NOT IMPLEMENTED YET
            }
            noiseFragmentsAboveParts[partIndex] = frags;
        }
        node.setNoiseFragmentsAboveParts(noiseFragmentsAboveParts);
    }

    @Override
    public void caseNoiseAboveParts_Pt(NoiseAbovePartsRectImgDescr parent, int partIndex, PtNoiseFragment node) {
        // no used
    }

    @Override
    public void caseNoiseAboveParts_Segment(NoiseAbovePartsRectImgDescr parent, int partIndex, SegmentNoiseFragment node) {
        // no used
    }

    @Override
    public void caseNoiseAboveParts_ConnexSegmentLines(NoiseAbovePartsRectImgDescr parent, int partIndex, ConnexSegmentLinesNoiseFragment node) {
        // no used
    }

    @Override
    public void caseOverrideAttributesProxy(OverrideAttributesProxyRectImgDescr node) {
        Rect rect = node.getRect();
        RectImgDescr underlying = readWithRect(rect);
        node.setUnderlying(underlying);
        Map<Object, Object> attributeOverrides = node.getAttributeOverrides();
        // TODO NOT IMPLEMENTED read attributes!
        
    }

    @Override
    public void caseAnalysisProxy(AnalysisProxyRectImgDescr node) {
        Rect rect = node.getRect();
        RectImgDescr target = readWithRect(rect);
        node.setTarget(target);
    }
    
}

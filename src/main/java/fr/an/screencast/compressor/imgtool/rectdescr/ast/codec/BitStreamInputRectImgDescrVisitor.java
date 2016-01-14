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

    private HuffmanTable<Class<? extends RectImgDescription>> huffmanTableRectImgDescriptionClass;
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
    
    public static RectImgDescription readTopLevelFrom(RectImgDescrCodecConfig codecConfig, StructDataInput in) {
        BitStreamInputRectImgDescrVisitor visitor = new BitStreamInputRectImgDescrVisitor(codecConfig, in);
        return visitor.readTopLevel();
    }

    public RectImgDescription readTopLevel() {
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
        Class<? extends RectImgDescription> nodeClass = in.readDecodeHuffmanCode(huffmanTableRectImgDescriptionClass);
        // introspection code equivalent to <code>switch(nodeClass.getName()) { case XX: return new XX(rect);... }</code>
        RectImgDescription node = newInstance(nodeClass, rect);
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

    protected RectImgDescription doRead() {
        // TOOPTIM: if small rect => use different HuffmanTable for "mostly" glyph 
        Class<? extends RectImgDescription> nodeClass = in.readDecodeHuffmanCode(huffmanTableRectImgDescriptionClass);
        Rect rect = getCurrRect();
        // introspection code equivalent to <code>switch(nodeClass.getName()) { case XX: return new XX(rect);... }</code>
        RectImgDescription node = newInstance(nodeClass, rect);
        
        doReadNode(node);

        return node;
    }
    
    protected void doReadNode(RectImgDescription node) {
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
    
    private static RectImgDescription newInstance(Class<? extends RectImgDescription> clss, Rect rect) {
        RectImgDescription res;
        Constructor<?> ctor = class2ctors.get(clss);
        try {
            res = (RectImgDescription) ctor.newInstance(rect);
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
            throw new RuntimeException("Failed", e);
        }
        return res;
    }
    
    private static Map<Class<? extends RectImgDescription>, Constructor<?>> class2ctors = resolveClassCtors();
    
    private static Map<Class<? extends RectImgDescription>, Constructor<?>> resolveClassCtors() {
        if (class2ctors != null) {
            return class2ctors;
        }
        Map<Class<? extends RectImgDescription>,Constructor<?>> res = new HashMap<>();
        for(Class<? extends RectImgDescription> clss : RectImgDescrCodecConfig.defaultClass2FrequencyMap().keySet()) {
            Constructor<? extends RectImgDescription> ctor;
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

    
    protected RectImgDescription readWithRect(Rect rect) {
        RectImgDescription node;
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

    @Override
    public void caseFillRect(FillRectImgDescr node) {
        int color = readColor("fill");
        node.setColor(color);
    }

    @Override
    public void caseRoundBorderDescr(RoundBorderRectImgDescr node) {
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
        RectImgDescription inside = readWithRect(insideRect);
        node.setInside(inside);
    }

    @Override
    public void caseBorderDescr(BorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int W = rect.getWidth(), H = rect.getHeight();
        int borderTop = in.readIntMinMax(0, H-1);
        int borderBottom = in.readIntMinMax(0, H-borderTop);
        int borderLeft = in.readIntMinMax(0, W-1);
        int borderRight = in.readIntMinMax(0, W-borderLeft);
        node.setBorder(new Border(borderLeft, borderRight, borderTop, borderBottom));
        node.setBorderColor(readColor("border"));
        
        Rect insideRect = node.getInsideRect();
        RectImgDescription inside = readWithRect(insideRect);
        node.setInside(inside);
    }

    @Override
    public void caseTopBottomBorderDescr(TopBottomBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int H = rect.getHeight();
        int borderTop = in.readIntMinMax(0, H-1);
        int borderBottom = in.readIntMinMax(0, H-borderTop);
        node.setTopBorder(borderTop);
        node.setBottomBorder(borderBottom);
        node.setBorderColor(readColor("border"));        

        Rect insideRect = node.getInsideRect();
        RectImgDescription inside = readWithRect(insideRect);
        node.setInside(inside);
    }

    @Override
    public void caseLeftRightBorderDescr(LeftRightBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int W = rect.getWidth();
        int borderLeft = in.readIntMinMax(0, W-1);
        int borderRight = in.readIntMinMax(0, W-borderLeft);
        node.setLeftBorder(borderLeft);
        node.setRightBorder(borderRight);        
        node.setBorderColor(readColor("border"));
        
        Rect insideRect = node.getInsideRect();
        RectImgDescription inside = readWithRect(insideRect);
        node.setInside(inside);
    }

    @Override
    public void caseVerticalSplitDescr(VerticalSplitRectImgDescr node) {
        final Rect rect = node.getRect();

        int splitBorderFrom = in.readIntMinMax(rect.fromX, rect.toX);
        int splitBorderTo = in.readIntMinMax(splitBorderFrom+1, rect.toX);
        node.setSplitBorder(new Segment(splitBorderFrom, splitBorderTo));
        node.setSplitColor(readColor("split")); // may use "border" ?

        final Rect leftRect = node.getLeftRect();
        final Rect rightRect = node.getRightRect();
        RectImgDescription left = readWithRect(leftRect);
        node.setLeft(left);
        RectImgDescription right = readWithRect(rightRect);
        node.setRight(right);
    }

    @Override
    public void caseHorizontalSplitDescr(HorizontalSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        int splitBorderFrom = in.readIntMinMax(rect.fromY, rect.toY);
        int splitBorderTo = in.readIntMinMax(splitBorderFrom+1, rect.toY);
        node.setSplitBorder(new Segment(splitBorderFrom, splitBorderTo));
        node.setSplitColor(readColor("split")); // may use "border" ?
        
        final Rect upRect = node.getUpRect();
        final Rect downRect = node.getDownRect();
        RectImgDescription up = readWithRect(upRect);
        node.setUp(up);
        RectImgDescription down = readWithRect(downRect);
        node.setDown(down);
    }

    @Override
    public void caseLinesSplitDescr(LinesSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        if (!in.readBit()) {
            return;
        }
        node.setBackgroundColor(readColor("background"));
        Segment[] splitBorders = readSegmentsOrderedMinMax(rect.fromY, rect.toY);
        node.setSplitBorders(splitBorders);

        Rect[] lineRects = node.getLineRects();
        RectImgDescription[] lines = new RectImgDescription[lineRects.length];
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
    public void caseColumnsSplitDescr(ColumnsSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        if (!in.readBit()) {
            return;
        }
        node.setBackgroundColor(readColor("background"));
        Segment[] splitBorders = readSegmentsOrderedMinMax(rect.fromX, rect.toX);
        node.setSplitBorders(splitBorders);

        Rect[] columnRects = node.getColumnRects();
        RectImgDescription[] columns = new RectImgDescription[columnRects.length];
        for(int i = 0; i < columnRects.length; i++) {
            // TODO ... optim encode child when expected TopBottomBorderRectImgDescr with same background color.. 
            columns[i] = readWithRect(columnRects[i]);
        }
        node.setColumns(columns);
    }

    @Override
    public void caseRawDataDescr(RawDataRectImgDescr node) {
        Dim dim = node.getDim();
        int[] rawData;
        try (StreamPopper topPop = in.pushSetCurrStream("rawData")) {
            rawData = externalFormatHelper.readRGBData(in, dim);
        }
        node.setRawData(rawData);
    }


    @Override
    public void caseGlyphDescr(GlyphRectImgDescr node) {
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
    public void caseAboveDescr(RectImgAboveRectImgDescr node) {
        final Rect rect = node.getRect();

        RectImgDescription underlying = readWithRect(rect);
        node.setUnderlyingRectImgDescr(underlying);

        int aboveCount = in.readIntMinMax(0, rect.getArea()+1);
        RectImgDescription[] aboves = new RectImgDescription[aboveCount]; 
        for (int i = 0; i < aboveCount; i++) {
            Rect aboveRect = readCurrNestedRect();
            
            pushRect(aboveRect);
            aboves[i] = readWithRect(aboveRect);
            popRect();
        }
        node.setAboveRectImgDescrs(aboves);
    }

    @Override
    public void caseAnalysisProxyRect(AnalysisProxyRectImgDescr node) {
        Rect rect = node.getRect();
        RectImgDescription target = readWithRect(rect);
        node.setTarget(target);
    }
    
}

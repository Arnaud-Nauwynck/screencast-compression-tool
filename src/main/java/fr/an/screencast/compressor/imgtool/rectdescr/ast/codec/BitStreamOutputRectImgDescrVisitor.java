package fr.an.screencast.compressor.imgtool.rectdescr.ast.codec;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.an.bitwise4j.encoder.huffman.HuffmanTable;
import fr.an.bitwise4j.encoder.structio.BitStreamStructDataOutput;
import fr.an.bitwise4j.encoder.structio.IStreamMultiplexerSupport.StreamPopper;
import fr.an.bitwise4j.encoder.structio.StructDataOutput;
import fr.an.screencast.compressor.imgtool.glyph.GlyphIndexOrCode;
import fr.an.screencast.compressor.imgtool.glyph.GlyphMRUTable;
import fr.an.screencast.compressor.imgtool.glyph.GlyphMRUTable.GlyphMRUNode;
import fr.an.screencast.compressor.imgtool.rectdescr.ExternalFormatRawDataHelper;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrVisitor;
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
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RootRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RoundBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.SegmentNoiseFragment;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.TopBottomBorderRectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.VerticalSplitRectImgDescr;
import fr.an.screencast.compressor.utils.Border;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.screencast.compressor.utils.Segment;

/**
 * RectImgDescrVisitor implementation for recursive encoding RectImgDescr as bitstream
 */
public class BitStreamOutputRectImgDescrVisitor extends RectImgDescrVisitor {

    /*pp*/ static final boolean DEBUG_MARK = false;
    
    private RectImgDescrCodecConfig codecConfig;
    
    private StructDataOutput out;

    private HuffmanTable<Class<? extends RectImgDescr>> huffmanTableRectImgDescriptionClass;
    private GlyphMRUTable glyphMRUTable;
    // TODO ... private Map<String,HuffmanTable<Integer>> field2huffmanTableColor;

    private ExternalFormatRawDataHelper externalFormatHelper = new ExternalFormatRawDataHelper();
    
    private List<Rect> currRectStack = new ArrayList<Rect>();
    
    // ------------------------------------------------------------------------

    public BitStreamOutputRectImgDescrVisitor(RectImgDescrCodecConfig codecConfig, StructDataOutput out) {
        this.codecConfig = codecConfig;
        this.out = out;
        this.huffmanTableRectImgDescriptionClass = codecConfig.createHuffmanTableForClass2Frequency();
        this.glyphMRUTable = codecConfig.createGlyphMRUTable();
    }

    // ------------------------------------------------------------------------
    
    public RectImgDescrCodecConfig getCodecConfig() {
        return codecConfig;
    }
    
    public static void writeTopLevelTo(RectImgDescrCodecConfig codecConfig, BitStreamStructDataOutput out, 
            RectImgDescr node) {
        BitStreamOutputRectImgDescrVisitor visitor = new BitStreamOutputRectImgDescrVisitor(codecConfig, out);
        visitor.writeTopLevel(node);
    }

    public void writeTopLevel(RectImgDescr node) {
        Rect rect = node.getRect();

        if (codecConfig.isDebugAddBeginEndMarker()) {
            out.writeUTF("RectImgDecr{{{");
        }
        
        out.writeUInt0ElseMax(Short.MAX_VALUE, rect.fromX);
        out.writeUIntLt2048ElseMax(Short.MAX_VALUE, rect.toX - rect.fromX);
        out.writeUInt0ElseMax(Short.MAX_VALUE, rect.fromY);
        out.writeUIntLt2048ElseMax(Short.MAX_VALUE, rect.toY - rect.fromY);
        pushRect(rect);
        doWrite(node);
        popRect();

        if (codecConfig.isDebugAddBeginEndMarker()) {
            out.writeUTF("}}}RectImgDecr");
        }
    }

    protected void doWrite(RectImgDescr node) {
        // TOOPTIM: if small rect => use different HuffmanTable for "mostly" glyph 
        Class<? extends RectImgDescr> nodeClass = node.getClass();
        huffmanTableRectImgDescriptionClass.writeEncodeSymbol(out, nodeClass);
        
        if (DEBUG_MARK && codecConfig.isDebugAddMarkers()) {
            out.writeUTF("DEBUG_MARK node: {" + nodeClass.getSimpleName() + " " + node.getRect());
        }
        
        node.accept(this);

        if (DEBUG_MARK && codecConfig.isDebugAddMarkers()) {
            out.writeUTF("DEBUG_MARK } node:" + nodeClass.getSimpleName() + " " + node.getRect());
        }
    }

    protected void writeCheckRect(RectImgDescr node, Rect rect) {
        out.writeBit(node != null);
        if (node != null) {
            Rect nodeRect = node.getRect();
            if (! nodeRect.equals(rect)) {
                throw new IllegalStateException();
            }
            pushRect(rect);
            
            doWrite(node);
            
            popRect();
        }        
    }
    
    private void writeColor(String colorField, int color) {
        // TODO field2huffmanTableColor.get(colorField);
        out.writeInt(color);
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

    protected void writeCurrNestedRect(Rect rect) {
        Rect curr = getCurrRect(); 
        out.writeIntMinMax(curr.fromX, curr.toX, rect.fromX);
        out.writeIntMinMax(rect.fromX, curr.toX+1, rect.toX);
        out.writeIntMinMax(curr.fromY, curr.toY, rect.fromY);
        out.writeIntMinMax(rect.fromY, curr.toY+1, rect.toY);
    }

    // implements Visitor
    // ------------------------------------------------------------------------
    
    @Override
    public void caseRoot(RootRectImgDescr node) {
        final Rect rect = node.getRect();
        writeCheckRect(node.getTarget(), rect);
    }
    
    @Override
    public void caseFill(FillRectImgDescr node) {
        // Rect rect = node.getRect();
        int color = node.getColor();
        writeColor("fill", color);
    }


    @Override
    public void caseRoundBorder(RoundBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int cornerBackgroundColor = node.getCornerBackgroundColor();
        final Dim topCornerDim = node.getTopCornerDim();
        final Dim bottomCornerDim = node.getBottomCornerDim();
        final int borderColor = node.getBorderColor();
        final int borderThick = node.getBorderThick();
        final RectImgDescr inside = node.getInside();
        final Rect insideRect = node.getInsideRect();

        writeColor("fill", borderColor);
        writeColor("cornerBg", cornerBackgroundColor);
        // TODO?
        int maxBorder = Math.min(rect.getWidth()/2,  rect.getHeight()/2);
        out.writeIntMinMax(1, maxBorder, borderThick);
        out.writeIntMinMax(0, maxBorder, topCornerDim.width);
        out.writeIntMinMax(0, maxBorder, topCornerDim.height);
        out.writeIntMinMax(0, maxBorder, bottomCornerDim.width);
        out.writeIntMinMax(0, maxBorder, bottomCornerDim.height);

        writeCheckRect(inside, insideRect);
    }

    @Override
    public void caseBorder(BorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int borderColor = node.getBorderColor();
        final Border border = node.getBorder();
        final RectImgDescr inside = node.getInside();
        final Rect insideRect = node.getInsideRect();

        final int W = rect.getWidth(), H = rect.getHeight();
        out.writeIntMinMax(0, H-1, border.top);
        out.writeIntMinMax(0, H-border.top, border.bottom);
        out.writeIntMinMax(0, W-1, border.left);
        out.writeIntMinMax(0, W-border.left, border.right);
        writeColor("border", borderColor);
        
        writeCheckRect(inside, insideRect);
    }

    @Override
    public void caseTopBottomBorder(TopBottomBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int borderColor = node.getBorderColor();
        final int topBorder = node.getTopBorder();
        final int bottomBorder = node.getBottomBorder();
        final RectImgDescr inside = node.getInside();
        final Rect insideRect = node.getInsideRect();
        
        final int H = rect.getHeight();
        out.writeIntMinMax(0, H-1, topBorder);
        out.writeIntMinMax(0, H-topBorder, bottomBorder);
        writeColor("border", borderColor);
        
        writeCheckRect(inside, insideRect);
    }

    @Override
    public void caseLeftRightBorder(LeftRightBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int borderColor = node.getBorderColor();
        final int leftBorder = node.getLeftBorder();
        final int rightBorder = node.getRightBorder();
        final RectImgDescr inside = node.getInside();
        final Rect insideRect = node.getInsideRect();
        
        final int W = rect.getWidth();
        out.writeIntMinMax(0, W-1, leftBorder);
        out.writeIntMinMax(0, W-leftBorder, rightBorder);
        writeColor("border", borderColor);
        
        writeCheckRect(inside, insideRect);
    }

    @Override
    public void caseVerticalSplit(VerticalSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final RectImgDescr left = node.getLeft();
        final Rect leftRect = node.getLeftRect();
        final Segment splitBorder = node.getSplitBorder();
        final int splitColor = node.getSplitColor();
        final RectImgDescr right = node.getRight();
        final Rect rightRect = node.getRightRect();

        out.writeIntMinMax(rect.fromX, rect.toX, splitBorder.from);
        out.writeIntMinMax(splitBorder.from+1, rect.toX, splitBorder.to);
        writeColor("split", splitColor); // may use "border" ?

        writeCheckRect(left, leftRect);
        writeCheckRect(right, rightRect);
    }

    @Override
    public void caseHorizontalSplit(HorizontalSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final RectImgDescr down = node.getDown();
        final Rect downRect = node.getDownRect();
        final Segment splitBorder = node.getSplitBorder();
        final int splitColor = node.getSplitColor();
        final RectImgDescr up = node.getUp();
        final Rect upRect = node.getUpRect();

        out.writeIntMinMax(rect.fromY, rect.toY, splitBorder.from);
        out.writeIntMinMax(splitBorder.from+1, rect.toY, splitBorder.to);
        writeColor("split", splitColor); // may use "border" ?

        writeCheckRect(up, upRect);
        writeCheckRect(down, downRect);
    }

    @Override
    public void caseLinesSplit(LinesSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final int backgroundColor = node.getBackgroundColor();
        final Segment[] splitBorders = node.getSplitBorders();
        final RectImgDescr[] lines = node.getLines();

        if (splitBorders == null) {
            out.writeBit(false);
            return;
        }
        out.writeBit(true);
        
        writeColor("background", backgroundColor);
        writeSegmentsOrderedMinMax(rect.fromY, rect.toY, splitBorders);

        for(RectImgDescr line : lines) {
            Rect lineRect = line.getRect();
            // TODO ... optim encode child when expected TopBottomBorderRectImgDescr with same background color.. 
            writeCheckRect(line, lineRect);
        }
    }

    // TODO move as utility method + use divide&conquer (mid pivot => recursive encode left points, right points)
    private void writeSegmentsOrderedMinMax(int min, final int max, final Segment[] segments) {
        out.writeUIntLtMinElseMax(32, max/2, segments.length);
        int prev = min;
        int remainSplitCount = segments.length - 1;
        int maxInc = max + 1;
        for(Segment b : segments) {
            out.writeIntMinMax(prev, maxInc-remainSplitCount, b.from); // , b.from); // TODO TOCHECK
            out.writeIntMinMax(b.from, maxInc-remainSplitCount, b.to); // -remainSplitCount, b.to);
            remainSplitCount--;
            prev = b.to;
        }
    }

    @Override
    public void caseColumnsSplit(ColumnsSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final int backgroundColor = node.getBackgroundColor();
        final Segment[] splitBorders = node.getSplitBorders();
        final RectImgDescr[] columns = node.getColumns();
        
        if (splitBorders == null) {
            out.writeBit(false);
            return;
        }
        out.writeBit(true);
        
        writeColor("background", backgroundColor);
        writeSegmentsOrderedMinMax(rect.fromX, rect.toX, splitBorders);

        for(RectImgDescr column : columns) {
            Rect lineRect = column.getRect();
            // TODO ... optim encode child when expected LeftRightBorderRectImgDescr with same background color.. 
            writeCheckRect(column, lineRect);
        }
    }

    @Override
    public void caseRawData(RawDataRectImgDescr node) {
        Dim dim = node.getDim();
        final int[] rawData = node.getRawData();
        try (StreamPopper topPop = out.pushSetCurrStream("rawData")) {
            externalFormatHelper.writeRGBData(out, dim, rawData);
        }
    }

    @Override
    public void caseGlyph(GlyphRectImgDescr node) {
        // final GlyphIndexOrCode glyphIndexOrCode = node.getGlyphIndexOrCode();
        // final boolean isNew = node.isNewGlyph();
        final Dim glyphDim = node.getRect().getDim();
        final int crc = node.getCrc();
        
        // re-resolve using encoder MRUTable  (not analyser MRUTable!)
        GlyphMRUNode glyphNode = glyphMRUTable.findGlyphByCrc(glyphDim, crc);
        // GlyphIndexOrCode glyphIndexOrCode = glyphNode.getIndexOrCode();
        final boolean isNew = glyphNode == null;
        
        out.writeBit(isNew);
        if (isNew) {
            int[] glyphData = node.getSharedData();
            // int crc = IntsCRC32.crc32(glyphData);

            // int youngIndex = glyphIndexOrCode.getYoungIndex();
            // GlyphMRUNode glyphNode = glyphMRUTable.findGlyphByCrc(glyphDim, crc);
            if (glyphNode == null) {
                glyphNode = glyphMRUTable.addGlyph(glyphDim, glyphData, Rect.newDim(glyphDim), crc);
            } else {
                // should not occur??
                glyphMRUTable.incrUseCount(glyphNode);
            }
            
            // int youngIndex = glyphIndexOrCode.getYoungIndex();
            // implicit .. out.writeInt(1 + glyphMRUTable.getYoungGlyphIndexCount());

            try (StreamPopper topPop = out.pushSetCurrStream("glyph")) {
                externalFormatHelper.writeRGBData(out, glyphDim, glyphData);
            }
        } else {
            GlyphIndexOrCode glyphIndexOrCode = glyphNode.getIndexOrCode();
            glyphMRUTable.writeEncodeReuseGlyphIndexOrCode(out, glyphIndexOrCode);
        }
    }

    @Override
    public void caseAbove(RectImgAboveRectImgDescr node) {
        final Rect rect = node.getRect();
        final RectImgDescr underlying = node.getUnderlying();
        final RectImgDescr[] aboves = node.getAboves();
        // final Rect aboveRect = node.getAboveRect();

        writeCheckRect(underlying, rect);
        final int aboveCount = (aboves != null)? aboves.length : 0;
        out.writeIntMinMax(0, rect.getArea()+1, aboveCount);
        for (int i = 0; i < aboveCount; i++) {
            RectImgDescr above = aboves[i];
            Rect aboveRect =  above.getRect();
            writeCurrNestedRect(aboveRect);
            pushRect(aboveRect);
            writeCheckRect(above, aboveRect);
            popRect();
        }
    }

    
    @Override
    public void caseNoiseAboveParts(NoiseAbovePartsRectImgDescr node) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void caseNoiseAboveParts_Pt(NoiseAbovePartsRectImgDescr parent, int partIndex, PtNoiseFragment node) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void caseNoiseAboveParts_Segment(NoiseAbovePartsRectImgDescr parent, int partIndex, SegmentNoiseFragment node) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void caseNoiseAboveParts_ConnexSegmentLines(NoiseAbovePartsRectImgDescr parent, int partIndex, ConnexSegmentLinesNoiseFragment node) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void caseOverrideAttributesProxy(OverrideAttributesProxyRectImgDescr node) {
        Rect rect = node.getRect();
        writeCheckRect(node.getUnderlying(), rect);

        Map<Object, Object> attributeOverrides = node.getAttributeOverrides();
        // TODO NOT IMPLEMENTED encode override attributes
    }


    @Override
    public void caseAnalysisProxy(AnalysisProxyRectImgDescr node) {
        Rect rect = node.getRect();
        writeCheckRect(node.getTarget(), rect);
    }
    
}

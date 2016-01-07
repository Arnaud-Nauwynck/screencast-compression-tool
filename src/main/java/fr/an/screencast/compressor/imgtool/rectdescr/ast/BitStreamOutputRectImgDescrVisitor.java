package fr.an.screencast.compressor.imgtool.rectdescr.ast;

import java.util.ArrayList;
import java.util.List;

import fr.an.screencast.compressor.imgtool.glyph.GlyphIndexOrCode;
import fr.an.screencast.compressor.imgtool.glyph.GlyphMRUTable;
import fr.an.screencast.compressor.imgtool.glyph.GlyphMRUTable.GlyphMRUNode;
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
import fr.an.screencast.compressor.imgtool.utils.IntsCRC32;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.Border;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.screencast.compressor.utils.Segment;
import fr.an.util.encoder.huffman.HuffmanTable;
import fr.an.util.encoder.structio.BitStreamStructDataOutput;
import fr.an.util.encoder.structio.StructDataOutput;

/**
 * RectImgDescrVisitor implementation for recursive encoding RectImgDescr as bitstream
 */
public class BitStreamOutputRectImgDescrVisitor extends RectImgDescrVisitor {

    /*pp*/ static final boolean DEBUG_MARK = false;
    
    private RectImgDescrCodecConfig codecConfig;
    
    private StructDataOutput out;

    private HuffmanTable<Class<? extends RectImgDescription>> huffmanTableRectImgDescriptionClass;
    private GlyphMRUTable glyphMRUTable;
    // TODO ... private Map<String,HuffmanTable<Integer>> field2huffmanTableColor;

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
            RectImgDescription node) {
        BitStreamOutputRectImgDescrVisitor visitor = new BitStreamOutputRectImgDescrVisitor(codecConfig, out);
        visitor.writeTopLevel(node);
    }

    public void writeTopLevel(RectImgDescription node) {
        Rect rect = node.getRect();
        out.writeUInt0ElseMax(Short.MAX_VALUE, rect.fromX);
        out.writeUIntLt2048ElseMax(Short.MAX_VALUE, rect.toX - rect.fromX);
        out.writeUInt0ElseMax(Short.MAX_VALUE, rect.fromY);
        out.writeUIntLt2048ElseMax(Short.MAX_VALUE, rect.toY - rect.fromY);
        pushRect(rect);
        doWrite(node);
        popRect();
    }

    protected void doWrite(RectImgDescription node) {
        // TOOPTIM: if small rect => use different HuffmanTable for "mostly" glyph 
        Class<? extends RectImgDescription> nodeClass = node.getClass();
        huffmanTableRectImgDescriptionClass.writeEncodeSymbol(out, nodeClass);
        
        if (DEBUG_MARK && codecConfig.isDebugAddMarkers()) {
            out.writeUTF("DEBUG_MARK node: {" + nodeClass.getSimpleName() + " " + node.getRect());
        }
        
        node.accept(this);

        if (DEBUG_MARK && codecConfig.isDebugAddMarkers()) {
            out.writeUTF("DEBUG_MARK } node:" + nodeClass.getSimpleName() + " " + node.getRect());
        }
    }

    protected void writeCheckRect(RectImgDescription node, Rect rect) {
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
        out.writeIntMinMax(rect.fromX, curr.toX, rect.toX);
        out.writeIntMinMax(curr.fromY, curr.toY, rect.fromY);
        out.writeIntMinMax(rect.fromY, curr.toY, rect.toY);
    }

    @Override
    public void caseFillRect(FillRectImgDescr node) {
        // Rect rect = node.getRect();
        int color = node.getColor();
        writeColor("fill", color);
    }

    @Override
    public void caseRoundBorderDescr(RoundBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int cornerBackgroundColor = node.getCornerBackgroundColor();
        final Dim topCornerDim = node.getTopCornerDim();
        final Dim bottomCornerDim = node.getBottomCornerDim();
        final int borderColor = node.getBorderColor();
        final int borderThick = node.getBorderThick();
        final RectImgDescription inside = node.getInside();
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
    public void caseBorderDescr(BorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int borderColor = node.getBorderColor();
        final Border border = node.getBorder();
        final RectImgDescription inside = node.getInside();
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
    public void caseTopBottomBorderDescr(TopBottomBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int borderColor = node.getBorderColor();
        final int topBorder = node.getTopBorder();
        final int bottomBorder = node.getBottomBorder();
        final RectImgDescription inside = node.getInside();
        final Rect insideRect = node.getInsideRect();
        
        final int H = rect.getHeight();
        out.writeIntMinMax(0, H-1, topBorder);
        out.writeIntMinMax(0, H-topBorder, bottomBorder);
        writeColor("border", borderColor);
        
        writeCheckRect(inside, insideRect);
    }

    @Override
    public void caseLeftRightBorderDescr(LeftRightBorderRectImgDescr node) {
        final Rect rect = node.getRect();
        final int borderColor = node.getBorderColor();
        final int leftBorder = node.getLeftBorder();
        final int rightBorder = node.getRightBorder();
        final RectImgDescription inside = node.getInside();
        final Rect insideRect = node.getInsideRect();
        
        final int W = rect.getWidth();
        out.writeIntMinMax(0, W-1, leftBorder);
        out.writeIntMinMax(0, W-leftBorder, rightBorder);
        writeColor("border", borderColor);
        
        writeCheckRect(inside, insideRect);
    }

    @Override
    public void caseVerticalSplitDescr(VerticalSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final RectImgDescription left = node.getLeft();
        final Rect leftRect = node.getLeftRect();
        final Segment splitBorder = node.getSplitBorder();
        final int splitColor = node.getSplitColor();
        final RectImgDescription right = node.getRight();
        final Rect rightRect = node.getRightRect();

        out.writeIntMinMax(rect.fromX, rect.toX, splitBorder.from);
        out.writeIntMinMax(splitBorder.from+1, rect.toX, splitBorder.to);
        writeColor("split", splitColor); // may use "border" ?

        writeCheckRect(left, leftRect);
        writeCheckRect(right, rightRect);
    }

    @Override
    public void caseHorizontalSplitDescr(HorizontalSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final RectImgDescription down = node.getDown();
        final Rect downRect = node.getDownRect();
        final Segment splitBorder = node.getSplitBorder();
        final int splitColor = node.getSplitColor();
        final RectImgDescription up = node.getUp();
        final Rect upRect = node.getUpRect();

        out.writeIntMinMax(rect.fromY, rect.toY, splitBorder.from);
        out.writeIntMinMax(splitBorder.from+1, rect.toY, splitBorder.to);
        writeColor("split", splitColor); // may use "border" ?

        writeCheckRect(up, upRect);
        writeCheckRect(down, downRect);
    }

    @Override
    public void caseLinesSplitDescr(LinesSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final int backgroundColor = node.getBackgroundColor();
        final Segment[] splitBorders = node.getSplitBorders();
        final RectImgDescription[] lines = node.getLines();

        if (splitBorders == null) {
            out.writeBit(false);
            return;
        }
        out.writeBit(true);
        
        writeColor("background", backgroundColor);
        writeSegmentsOrderedMinMax(rect.fromY, rect.toY, splitBorders);

        for(RectImgDescription line : lines) {
            Rect lineRect = line.getRect();
            // TODO ... optim encode child when expected TopBottomBorderRectImgDescr with same background color.. 
            writeCheckRect(line, lineRect);
        }
    }

    // TODO move as utility method
    private void writeSegmentsOrderedMinMax(int min, final int max, final Segment[] segments) {
        out.writeUIntLtMinElseMax(32, max/2, segments.length);
        int prev = min;
        int remainSplitCount = segments.length;
        for(Segment b : segments) {
            out.writeIntMinMax(prev, max-2*remainSplitCount, b.from);
            remainSplitCount--;
            out.writeIntMinMax(b.from, max-2*remainSplitCount, b.to);
            prev = b.to;
        }
    }

    @Override
    public void caseColumnsSplitDescr(ColumnsSplitRectImgDescr node) {
        final Rect rect = node.getRect();
        final int backgroundColor = node.getBackgroundColor();
        final Segment[] splitBorders = node.getSplitBorders();
        final RectImgDescription[] columns = node.getColumns();
        
        if (splitBorders == null) {
            out.writeBit(false);
            return;
        }
        out.writeBit(true);
        
        writeColor("background", backgroundColor);
        writeSegmentsOrderedMinMax(rect.fromX, rect.toX, splitBorders);

        for(RectImgDescription column : columns) {
            Rect lineRect = column.getRect();
            // TODO ... optim encode child when expected LeftRightBorderRectImgDescr with same background color.. 
            writeCheckRect(column, lineRect);
        }
    }

    @Override
    public void caseRawDataDescr(RawDataRectImgDescr node) {
        final int[] rawData = node.getRawData();
        
        // encode as GZip .. write encoded len + bytes
        byte[] gzipBytes = RGBUtils.intRGBsToGzipBytes(rawData);
        
        out.writeIntMinMax(0, rawData.length, gzipBytes.length);
        out.writeBytes(gzipBytes, gzipBytes.length);
    }

    @Override
    public void caseGlyphDescr(GlyphRectImgDescr node) {
        final GlyphIndexOrCode glyphIndexOrCode = node.getGlyphIndexOrCode();
        final boolean isNew = node.isNewGlyph();
        
        out.writeBit(isNew);
        if (isNew) {
            Dim glyphDim = node.getRect().getDim();
            int[] glyphData = node.getNewGlyphData();
            int crc = IntsCRC32.crc32(glyphData);

            GlyphMRUNode glyphNode = glyphMRUTable.findGlyphByCrc(glyphDim, crc);
            if (glyphNode == null) {
                glyphNode = glyphMRUTable.addGlyph(glyphDim, glyphData, Rect.newDim(glyphDim), crc);
            } else {
                // should not occur??
                glyphMRUTable.incrUseCount(glyphNode);
            }
            
            // int youngIndex = glyphIndexOrCode.getYoungIndex();
            // implicit .. out.writeInt(1 + glyphMRUTable.getYoungGlyphIndexCount());
            
            // encode as GZip .. write encoded len + bytes
            byte[] gzipBytes = RGBUtils.intRGBsToGzipBytes(glyphData);
            
            out.writeIntMinMax(0, glyphData.length, gzipBytes.length);
            out.writeBytes(gzipBytes, gzipBytes.length);
        } else {
            glyphMRUTable.writeEncodeReuseGlyphIndexOrCode(out, glyphIndexOrCode);
        }
    }

    @Override
    public void caseDescrAboveDescr(RectImgAboveRectImgDescr node) {
        final Rect rect = node.getRect();
        final RectImgDescription underlying = node.getUnderlyingRectImgDescr();
        final RectImgDescription above = node.getAboveRectImgDescr();
        final Rect aboveRect = node.getAboveRect();

        writeCheckRect(underlying, rect);

        writeCurrNestedRect(aboveRect);
        pushRect(aboveRect);
        writeCheckRect(above, aboveRect);
        popRect();
    }

}

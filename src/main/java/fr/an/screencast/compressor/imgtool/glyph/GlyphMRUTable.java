package fr.an.screencast.compressor.imgtool.glyph;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.compressor.imgtool.glyph.GlyphMRUTable.GlyphMRUNode;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;

/**
 * a MRU (Most-Recently-Used) table for glyphs
 *
 */
public class GlyphMRUTable {

    private static final boolean EXPLICIT_COMPARE_DATA = false;
    
    private static class GlyphKey {
        final Dim dim;
        final int[] data;
        final int crc;
        
        public GlyphKey(Dim dim, int crc, int[] data) {
            this.dim = dim;
            this.crc = crc;
            this.data = data;
        }

        @Override
        public int hashCode() {
            return crc;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            GlyphKey other = (GlyphKey) obj;
            if (crc != other.crc)
                return false;
            if (dim == null) {
                if (other.dim != null)
                    return false;
            } else if (!dim.equals(other.dim))
                return false;
            
//            // explicit compare  (in case of crc collision?)
//            if (data != null && other.data!= null && EXPLICIT_COMPARE_DATA ! Arrays.equals(data, other.data)) {
//                return false; // should not occur...
//            }
            
            return true;
        }
        
    }
    
    public static class GlyphMRUNode {
        private final GlyphKey key;
        private final int id;

        // not final ... will change when reindexing huffman codes
        private GlyphIndexOrCode indexOrCode;
        
        private int useCount;
        /**
         * counter to decrement each time any element is purged from cache, and increment on cache hit of this element
         * => time delay decreasing priority...
         */
        private int priorityKeep;
        
        public GlyphMRUNode(GlyphKey key, int id, GlyphIndexOrCode indexOrCode) {
            this.key = key;
            this.id = id;
            this.indexOrCode = indexOrCode;
        }
        
        public Dim getDim() {
            return key.dim;
        }
        
        public int[] getData() { 
            return key.data; 
        }

        public GlyphIndexOrCode getIndexOrCode() {
            return indexOrCode;
        }
        
    }
    
    
    private static final Logger LOG = LoggerFactory.getLogger(GlyphMRUTable.class);
    
    private int maxSize;
    private Map<GlyphKey,GlyphMRUNode> glyphByCrcKey = new HashMap<GlyphKey,GlyphMRUNode>();
    private Map<GlyphIndexOrCode,GlyphMRUNode> glyphByIndexOrCode = new HashMap<GlyphIndexOrCode,GlyphMRUNode>();
    
    private int youngGlyphIndexCount;
    private int globalGlyphIdCount;
    
    // ------------------------------------------------------------------------

    public GlyphMRUTable(int maxSize) {
        this.maxSize = maxSize;
    }

    // ------------------------------------------------------------------------
    
    public GlyphMRUNode findGlyphByIndexOrCode(GlyphIndexOrCode key) {
        return glyphByIndexOrCode.get(key);
    }
    
    public GlyphMRUNode findGlyphByCrc(Dim dim, int crc) {
        GlyphKey key = new GlyphKey(dim, crc, null);
        GlyphMRUNode glyph = glyphByCrcKey.get(key);
        return glyph;
    }
        
    public GlyphMRUNode addGlyph(Dim dim, int[] img, Rect rect, int crc) {
        int[] data = new int[rect.getArea()];
        ImageRasterUtils.drawRectImg(rect.getDim(), data, new Pt(0, 0), dim, img, rect);
        // assert crc == IntsCRC32.crc32(data, 0, data.length);
        
        GlyphKey crcKey = new GlyphKey(dim, crc, data);
        GlyphMRUNode glyph = glyphByCrcKey.get(crcKey);
        if (glyph == null) {
            GlyphIndexOrCode indexOrCode = new GlyphIndexOrCode(youngGlyphIndexCount++, null);
            glyph = new GlyphMRUNode(crcKey, globalGlyphIdCount++, indexOrCode);
            glyph.priorityKeep = data.length >>> 4;

            if (glyphByCrcKey.size() + 1 > maxSize) {
                removeLeastUsedGlyph();
            }
            glyphByCrcKey.put(crcKey, glyph);
            glyphByIndexOrCode.put(indexOrCode, glyph);

        }
        // increment used counter
        glyph.useCount++;
        glyph.priorityKeep++;
        
        return glyph; 
    }

    private void removeLeastUsedGlyph() {
        if (glyphByCrcKey.isEmpty()) return;
        int minPriority = Integer.MAX_VALUE;
        GlyphMRUNode foundMin = null;
        for(GlyphMRUNode n : glyphByCrcKey.values()) {
            n.priorityKeep--;
            if (n.priorityKeep < minPriority) {
                minPriority = n.priorityKeep;
                foundMin = n;
            }
        }
        glyphByCrcKey.remove(foundMin.key);
        glyphByIndexOrCode.remove(foundMin.indexOrCode);
        // TODO ...may re-assign youngIndex (to keep small int numbers ...)
        // TODO ... may compute HuffmanTable and re-assign huffman codes to glyphs 
    }

    public void drawGlyphFindByIndexOrCode(GlyphIndexOrCode glyphIndexOrCode, Dim destDim, int[] destData, Rect rect) {
        GlyphMRUNode glyphNode = findGlyphByIndexOrCode(glyphIndexOrCode);
        if (glyphNode == null) {
            LOG.warn("glyph not found by index/code:" + glyphIndexOrCode + " ... IGNORE, can not draw!");
            return;
        }

        final int[] glyphData = glyphNode.getData();
        Dim glyphDim = glyphNode.getDim();
        Dim rectDim = rect.getDim();
        if (!glyphDim.equals(rectDim)) {
            LOG.warn("glyph id:" + glyphNode + " dim:" + glyphDim + " expected rect dim:" + rectDim + "... IGNORE, can not draw!");
            return;
        }
        Rect glyphROI = Rect.newDim(glyphDim); 
        Pt rectFromPt = rect.getFromPt();
        ImageRasterUtils.drawRectImg(destDim, destData, rectFromPt, glyphDim, glyphData, glyphROI);        
    }
    
}

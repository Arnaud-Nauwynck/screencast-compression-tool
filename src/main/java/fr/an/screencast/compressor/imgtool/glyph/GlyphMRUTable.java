package fr.an.screencast.compressor.imgtool.glyph;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import fr.an.screencast.compressor.imgtool.utils.IntsCRC32;
import fr.an.screencast.compressor.utils.Dim;

/**
 * a MRU (Most-Recently-Used) table for glyphs
 *
 */
public class GlyphMRUTable {

    private static class GlyphKey {
        final Dim dim;
        final int[] data;
        final int crc;
        
        public GlyphKey(Dim dim, int[] data) {
            this.dim = dim;
            this.data = data;
            this.crc = IntsCRC32.crc32(data, 0, data.length);
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
            
            // explicit compare  (in case of crc collision?)
            if (! Arrays.equals(data, other.data)) {
                return false; // should not occur...
            }
            
            return true;
        }
        
    }
    
    public static class GlyphMRUNode {
        private final GlyphKey key;
        private final int glyphId;
        
        private int useCount;
        /**
         * counter to decrement each time any element is purged from cache, and increment on cache hit of this element
         * => time delay decreasing priority...
         */
        private int priorityKeep;
        
        public GlyphMRUNode(GlyphKey key, int glyphId) {
            this.key = key;
            this.glyphId = glyphId;
        }
        
        public Dim getDim() { return key.dim; }
        public int[] getData() { return key.data; }
    }
    
    private int maxSize;
    private Map<GlyphKey,GlyphMRUNode> glyphByCrc = new HashMap<GlyphKey,GlyphMRUNode>();
    private Map<Integer,GlyphMRUNode> glyphById = new HashMap<Integer,GlyphMRUNode>();
    
    private int maxGlyphId;
    
    // ------------------------------------------------------------------------

    public GlyphMRUTable(int maxSize) {
        this.maxSize = maxSize;
    }

    // ------------------------------------------------------------------------
    
    public GlyphMRUNode findGlyphById(int glyphId) {
        return glyphById.get(glyphId);
    }
    
    public int findOrAddGlyph(Dim dim, int[] data) {
        GlyphKey key = new GlyphKey(dim, data);
        GlyphMRUNode glyph = glyphByCrc.get(key);
        if (glyph == null) {
            glyph = new GlyphMRUNode(key, maxGlyphId++);
            glyph.priorityKeep = data.length >>> 4;

            if (glyphByCrc.size() + 1 > maxSize) {
                removeLeastUsedGlyph();
            }
            glyphByCrc.put(key, glyph);
            glyphById.put(glyph.glyphId, glyph);

        }
        // increment used counter
        glyph.useCount++;
        glyph.priorityKeep++;
        
        return glyph.glyphId; 
    }

    private void removeLeastUsedGlyph() {
        if (glyphByCrc.isEmpty()) return;
        int minPriority = Integer.MAX_VALUE;
        GlyphMRUNode foundMin = null;
        for(GlyphMRUNode n : glyphByCrc.values()) {
            n.priorityKeep--;
            if (n.priorityKeep < minPriority) {
                minPriority = n.priorityKeep;
                foundMin = n;
            }
        }
        glyphByCrc.remove(foundMin.key);
        glyphById.remove(foundMin.glyphId);
        // TODO ...may re-assign glyphIds (to keep small int numbers ...)
        // TODO ... may compute HuffmanTable and re-assign huffman codes to glyphs 
    }
    
}

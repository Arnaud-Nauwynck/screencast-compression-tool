package fr.an.screencast.compressor.imgtool.glyph;

import java.io.Serializable;

import fr.an.util.encoder.huffman.HuffmanBitsCode;

public final class GlyphIndexOrCode implements Serializable {
    
    /** */
    private static final long serialVersionUID = 1L;
    
    private final int youngIndex;
    private final HuffmanBitsCode oldHuffmanCode;
    
    public GlyphIndexOrCode(int youngIndex, HuffmanBitsCode oldHuffmanCode) {
        this.youngIndex = youngIndex;
        this.oldHuffmanCode = oldHuffmanCode;
    }
    
    public boolean isYoung() {
        return youngIndex >= 0;
    }
    
    public int getYoungIndex() {
        return youngIndex;
    }

    public HuffmanBitsCode getOldHuffmanCode() {
        return oldHuffmanCode;
    }


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((oldHuffmanCode == null) ? 0 : oldHuffmanCode.hashCode());
        result = prime * result + youngIndex;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        GlyphIndexOrCode other = (GlyphIndexOrCode) obj;
        if (oldHuffmanCode == null) {
            if (other.oldHuffmanCode != null)
                return false;
        } else if (!oldHuffmanCode.equals(other.oldHuffmanCode))
            return false;
        if (youngIndex != other.youngIndex)
            return false;
        return true;
    }

    @Override
    public String toString() {
        return (youngIndex >= 0)? "tmpIdx:" + youngIndex : "code:" + oldHuffmanCode;
    }
    
}
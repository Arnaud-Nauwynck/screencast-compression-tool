package fr.an.screencast.compressor.utils;

import java.io.Serializable;

import fr.an.screencast.compressor.imgtool.utils.RGBUtils;

public final class ColorSegment implements ISegment, Serializable {

    /** */
    private static final long serialVersionUID = 1L;
    
    public final int from;
    public final int to;
    
    public int color;
    
    // ------------------------------------------------------------------------

    public ColorSegment(ISegment src, int color) {
        this(src.getFrom(), src.getTo(), color);
    }
    
    public ColorSegment(int from, int to, int color) {
        this.from = from;
        this.to = to;
        this.color = color;
    }
    
    // ------------------------------------------------------------------------

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }
    
    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return from + "-" + to + ":" + RGBUtils.toString(color);
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + from;
        result = prime * result + to;
        result = prime * result + color;
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
        ColorSegment other = (ColorSegment) obj;
        if (from != other.from)
            return false;
        if (to != other.to)
            return false;
        if (color != other.color)
            return false;
        return true;
    }

    public boolean eq(ColorSegment other) {
        return from == other.from && to == other.to && color == other.color;
    }
    
}

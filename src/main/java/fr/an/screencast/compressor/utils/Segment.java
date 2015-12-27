package fr.an.screencast.compressor.utils;

import java.io.Serializable;

public final class Segment implements ISegment, Serializable {

    /** */
    private static final long serialVersionUID = 1L;
    
    public final int from;
    public final int to;
    
    // ------------------------------------------------------------------------

    public Segment(MutableSegment src) {
        this(src.from, src.to);
    }
    
    public Segment(int from, int to) {
        this.from = from;
        this.to = to;
    }
    
    // ------------------------------------------------------------------------

    public int getFrom() {
        return from;
    }

    public int getTo() {
        return to;
    }

    @Override
    public String toString() {
        return from + "-" + to;
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + from;
        result = prime * result + to;
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
        Segment other = (Segment) obj;
        if (from != other.from)
            return false;
        if (to != other.to)
            return false;
        return true;
    }

    public boolean eq(Segment other) {
        return from == other.from && to == other.to;
    }
    
}

package fr.an.screencast.compressor.utils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

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
        return "[" + from + "-" + to + "(";
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

    /**
     * @param text string to parse, expecting format "(\d+-\d+, )*" 
     * @return
     */
    public static List<Segment> parseSegmentList(String text) {
        List<Segment> res = new ArrayList<Segment>();
        for(StringTokenizer tokenizer = new StringTokenizer(text, "[(, "); tokenizer.hasMoreTokens(); ) {
            String token = tokenizer.nextToken();
            String[] split = token.split("-");
            int from = Integer.parseInt(split[0].trim());
            int to = Integer.parseInt(split[1].trim());
            res.add(new Segment(from, to));
        }
        return res;
    }
    
}

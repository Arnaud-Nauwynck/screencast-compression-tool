package fr.an.screencast.compressor.utils;

import java.io.Serializable;

public final class Border implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;

    private final int left;
    private final int right;
    private final int top;
    private final int bottom;

    // ------------------------------------------------------------------------

    public Border(MutableBorder src) {
        this(src.left, src.right, src.top, src.bottom);
    }
    
    public Border(int left, int right, int top, int bottom) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    // ------------------------------------------------------------------------
    
    public int getLeft() {
        return left;
    }

    public int getRight() {
        return right;
    }

    public int getTop() {
        return top;
    }

    public int getBottom() {
        return bottom;
    }

    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return "Border [left=" + left + ", right=" + right + ", top=" + top + ", bottom=" + bottom + "]";
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + bottom;
        result = prime * result + left;
        result = prime * result + right;
        result = prime * result + top;
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
        Border other = (Border) obj;
        if (bottom != other.bottom)
            return false;
        if (left != other.left)
            return false;
        if (right != other.right)
            return false;
        if (top != other.top)
            return false;
        return true;
    }
    
}

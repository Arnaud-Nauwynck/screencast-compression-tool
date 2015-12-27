package fr.an.screencast.compressor.utils;

import java.io.Serializable;

public final class MutableBorder implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;

    public int left;
    public int right;
    public int top;
    public int bottom;
    
    // ------------------------------------------------------------------------
    
    public MutableBorder(int left, int right, int top, int bottom) {
        this.left = left;
        this.right = right;
        this.top = top;
        this.bottom = bottom;
    }

    // ------------------------------------------------------------------------
    
    public int getLeft() {
        return left;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public int getRight() {
        return right;
    }

    public void setRight(int right) {
        this.right = right;
    }

    public int getTop() {
        return top;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public int getBottom() {
        return bottom;
    }

    public void setBottom(int bottom) {
        this.bottom = bottom;
    }

    // ------------------------------------------------------------------------
    
    @Override
    public String toString() {
        return "MutableBorder [left=" + left + ", right=" + right + ", top=" + top + ", bottom=" + bottom + "]";
    }
    
}

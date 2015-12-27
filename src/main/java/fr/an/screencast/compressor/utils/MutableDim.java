package fr.an.screencast.compressor.utils;

import java.awt.Point;
import java.io.Serializable;

public final class MutableDim implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;
    
    public int width;
    public int height;
    
    // ------------------------------------------------------------------------

    public MutableDim() {
    }
    
    public MutableDim(int width, int height) {
        this.width = width;
        this.height = height;
    }

    // ------------------------------------------------------------------------

    
    public Point toAwtPoint() {
        return new java.awt.Point(width, height);
    }
    
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getArea() {
        return width * height;
    }

    @Override
    public String toString() {
        return width + "x" + height;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + height;
        result = prime * result + width;
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
        MutableDim other = (MutableDim) obj;
        return eq(other);
    }
    
    public boolean eq(MutableDim other) {
        return width == other.width && height == other.height;
    }
    
}

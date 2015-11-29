package fr.an.screencast.compressor.utils;

import java.awt.Point;

public final class Dim {

    public final int width;
    public final int height;
    
    // ------------------------------------------------------------------------

    public Dim(int width, int height) {
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

    public int getHeight() {
        return height;
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
        Dim other = (Dim) obj;
        return eq(other);
    }
    
    public boolean eq(Dim other) {
        return width == other.width && height == other.height;
    }
    
}

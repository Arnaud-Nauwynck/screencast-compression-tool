package fr.an.screencast.compressor.utils;

import java.awt.Graphics;
import java.io.Serializable;

public class Rect implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;

    public int fromX;
    public int toX; // exclusive
    public int fromY; 
    public int toY; // exclusive
    
    // ------------------------------------------------------------------------

    public Rect() {
    }

    public Rect(Rect src) {
        this(src.fromX, src.fromY, src.toX, src.toY);
    }

    protected Rect(int fromX, int fromY, int toX, int toY) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
    }

    public static Rect newPtToPt(Pt from, Pt to) {
        return new Rect(from.x, from.y, to.x, to.y);
    }

    public static Rect newPtToPt(int fromX, int fromY, int toX, int toY) {
        return new Rect(fromX, fromY, toX, toY);
    }

    public static Rect newPtDim(Pt from, Dim dim) {
        return new Rect(from.x, from.y, from.x + dim.width, from.y + dim.height);
    }

    public static Rect newPtDim(Pt from, int width, int height) {
        return new Rect(from.x, from.y, from.x + width, from.y + height);
    }

    // ------------------------------------------------------------------------
    
    public void toAWTRectangle(java.awt.Rectangle dest) {
        dest.x = fromX;
        dest.y = fromY;
        dest.width = getWidth();
        dest.height = getHeight();        
    }
    
    public java.awt.Rectangle toAWTRectangle() {
        java.awt.Rectangle res = new java.awt.Rectangle();
        toAWTRectangle(res);
        return res;
    }

    public int getWidth() {
        return toX - fromX;
    }

    public int getHeight() {
        return toY - fromY;
    }

    public int getArea() {
        return getWidth() * getHeight();
    }

    public boolean isNotEmpty() {
        return toX > fromX && toY > fromY;
    }

    // ------------------------------------------------------------------------
    
    public int getFromX() {
        return fromX;
    }

    public void setFromX(int fromX) {
        this.fromX = fromX;
    }

    public int getToX() {
        return toX;
    }

    public void setToX(int toX) {
        this.toX = toX;
    }

    public int getFromY() {
        return fromY;
    }

    public void setFromY(int fromY) {
        this.fromY = fromY;
    }

    public int getToY() {
        return toY;
    }

    public void setToY(int toY) {
        this.toY = toY;
    }

    public void setPtToPt(int fromX, int fromY, int toX, int toY) {
        this.fromX = fromX;
        this.fromY = fromY;
        this.toX = toX;
        this.toY = toY;
    }

    public void graphicsFillRect(Graphics g2d) {
        g2d.fillRect(fromX, fromY, getWidth(), getHeight());        
    }

    public void graphicsDrawRect(Graphics g2d) {
        g2d.drawRect(fromX, fromY, getWidth(), getHeight());        
    }

    public void graphicsDrawRectErode(Graphics g2d, int stroke) {
        g2d.drawRect(fromX+stroke, fromY+stroke, getWidth()-2*stroke, getHeight()-2*stroke);        
    }

    public void graphicsDrawRectDilate(Graphics g2d, int stroke) {
        g2d.drawRect(fromX-stroke, fromY-stroke, getWidth()+2*stroke, getHeight()+2*stroke);        
    }

    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return "[" + fromX + "," + toX + "]x[" + fromY + "," + toY + "]";
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + fromX;
        result = prime * result + fromY;
        result = prime * result + toX;
        result = prime * result + toY;
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
        Rect other = (Rect) obj;
        if (fromX != other.fromX)
            return false;
        if (fromY != other.fromY)
            return false;
        if (toX != other.toX)
            return false;
        if (toY != other.toY)
            return false;
        return true;
    }
    
}

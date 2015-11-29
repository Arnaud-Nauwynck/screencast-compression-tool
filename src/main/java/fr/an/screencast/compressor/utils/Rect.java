package fr.an.screencast.compressor.utils;

public class Rect {

    public int fromX;
    public int toX; // inclusive
    public int fromY; 
    public int toY; // inclusive
    
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

    public Rect(Pt from, Pt to) {
        this(from.x, from.y, to.x, to.y);
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
        return toX - fromX + 1;
    }

    public int getHeight() {
        return toY - fromY + 1;
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

    // ------------------------------------------------------------------------

    
}

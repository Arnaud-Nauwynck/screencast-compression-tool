package fr.an.screencast.compressor.utils;

import java.io.Serializable;

public final class Pt implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;

    public int x;
    public int y;
    
    // ------------------------------------------------------------------------

    public Pt() {
    }

    public Pt(Pt src) {
        this(src.x, src.y);
    }

    public Pt(int x, int y) {
        this.x = x;
        this.y = y;
    }

    // ------------------------------------------------------------------------

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public void set(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void set(Pt pt) {
        this.x = pt.x;
        this.y = pt.y;
    }

    public boolean setNextHorizontalScan(Dim dim) {
        x++;
        if (x >= dim.width) {
            x = 0;
            y++;
            if (y >= dim.height) {
                return false; // finished .. reached end of image
            }
        }
        return true;
    }

    // ------------------------------------------------------------------------

    @Override
    public String toString() {
        return x + "," + y;
    }

    public static String polyLineToString(Pt[] pts) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < pts.length; i++) {
            sb.append(pts[i]);
            if (i + 1 < pts.length) {
                sb.append("->");
            }
        }
        return sb.toString();
    }
}

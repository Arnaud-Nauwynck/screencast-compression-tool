package fr.an.screencast.compressor.imgtool.search;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;

import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;

public class ColorsConnexComponent {

    private Map<Integer, HashSet<Pt>> color2pts;

    // private Map<Integer, Rect> color2enclosingRect;

    private Rect enclosingRect;

    // ------------------------------------------------------------------------
    
    public ColorsConnexComponent(Map<Integer, HashSet<Pt>> color2pts) {
        this.color2pts = color2pts;
        this.enclosingRect = null;
    }

    // ------------------------------------------------------------------------
    
    public Map<Integer, HashSet<Pt>> getColor2pts() {
        return color2pts;
    }

    public Rect getEnclosingRect() {
        if (enclosingRect == null) {
            Rect res = new Rect(); // empty
            for(Collection<Pt> pts : color2pts.values()) {
                res.setDilateToContain(pts);
            }
            enclosingRect = res;
        }
        return enclosingRect;
    }

}

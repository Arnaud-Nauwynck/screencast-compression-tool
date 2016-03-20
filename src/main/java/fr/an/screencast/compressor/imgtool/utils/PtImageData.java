package fr.an.screencast.compressor.imgtool.utils;

import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;

public class PtImageData {

    private Pt pt;
    private ImageData imageData;
    
    // ------------------------------------------------------------------------
    
    public PtImageData(Pt pt, ImageData imageData) {
        this.pt = pt;
        this.imageData = imageData;
    }

    // ------------------------------------------------------------------------
    
    public Pt getPt() {
        return pt;
    }

    public void setPt(Pt pt) {
        this.pt = pt;
    }

    public ImageData getImageData() {
        return imageData;
    }

    public void setImageData(ImageData imageData) {
        this.imageData = imageData;
    }

    public Rect getRect() {
        return Rect.newPtDim(pt, imageData.getDim());
    }
    
    // ------------------------------------------------------------------------
    
    @Override
    public String toString() {
        return "PtImageData[pt=" + pt + ((imageData != null)? ", dim=" + imageData.getDim() : "") + "]";
    }
    
}

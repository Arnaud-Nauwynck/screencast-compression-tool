package fr.an.screencast.compressor.imgtool.utils;

import fr.an.screencast.compressor.utils.Dim;

public class ImageData {

    protected final Dim dim;
    protected int[] data;
    
    // ------------------------------------------------------------------------
    
    public ImageData(Dim dim, int[] data) {
        this.dim = dim;
        this.data = data;
    }

    public ImageData(Dim dim) {
        this(dim, new int[dim.width * dim.height]);
    }
    

    // ------------------------------------------------------------------------

    public Dim getDim() {
        return dim;
    }
    
    public int[] getData() {
        return data;
    }
    
    public int getAt(int index) {
        return data[index];
    }
    
    public int safeGetAt(int x, int y) {
        if (x < 0 || x >= dim.width || y < 0 || y >= dim.height) return 0;
        else return data[dim.width * y + x];
    }

    public void setCopyData(int[] src) {
        System.arraycopy(src,  0, data, 0, data.length);
    }
    
}

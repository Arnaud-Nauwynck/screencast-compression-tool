package fr.an.screencast.compressor.imgtool.utils;

import java.io.Serializable;

import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Rect;

public class ImageData implements Serializable {

    /** */
    private static final long serialVersionUID = 1L;
    
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

    public int index(int x, int y) {
        return dim.width * y + x;
    }
    
    public void setCopyData(int[] src) {
        System.arraycopy(src,  0, data, 0, data.length);
    }

    public void setCopyData(ImageData src) {
        setCopyData(src.data);
    }

    public void setFillRect(Rect rect, int value) {
        final int width = dim.width;
        final int[] data = this.data;
        for(int y = rect.fromY,idx=index(rect.fromX,rect.fromY); y < rect.toY; y++) {
            int idx_fromX = idx;
            for (int x = rect.fromX; x < rect.toX; x++,idx++) {
                data[idx] = value;
            }
            idx = idx_fromX + width;
        }
    }
    
    public void set(RasterImageFunction src) {
        final int width = dim.width, height = dim.height;
        final int[] data = this.data;
        for(int y = 0,idx=0; y < height; y++) {
            for (int x = 0; x < width; x++,idx++) {
                data[idx] = src.eval(x, y, idx);
            }
        }
    }

    
    
    public void checkEquals(final int[] actualData) {
        for(int y = 0, idx_xy = 0; y < dim.height; y++) {
            for(int x = 0; x < dim.width; x++,idx_xy++) {
                int actualValue = actualData[idx_xy];
                int expectedValue = data[idx_xy];
                if (actualValue != expectedValue) {
                    throw new RuntimeException("expected [" + x + "][" + y + "] : " + expectedValue + ", got " + actualValue);
                }
            }
        }
    }

    public void checkEquals(ImageData actual) {
        checkEquals(actual.getData());
    }
}

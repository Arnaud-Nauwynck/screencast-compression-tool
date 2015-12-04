package fr.an.screencast.compressor.imgtool.utils;

// .. not an interface! @FunctionalInterface
public abstract class RasterImageFunction {

    // private final Dim dim;
    
    public abstract int eval(int x, int y, int index_xy);
    
}

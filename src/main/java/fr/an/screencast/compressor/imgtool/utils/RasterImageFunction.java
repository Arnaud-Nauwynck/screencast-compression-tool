package fr.an.screencast.compressor.imgtool.utils;

import fr.an.screencast.compressor.utils.Dim;

public abstract class RasterImageFunction {

    // private final Dim dim;
    
    public abstract int eval(int x, int y, int index_xy);
    
}

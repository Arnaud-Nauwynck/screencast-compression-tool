package fr.an.screencast.compressor.imgtool.utils;

import fr.an.screencast.compressor.utils.Dim;

/**
 * Factory utility class standards RasterImageFunction 
 *
 */
public final class RasterImageFunctions {

    private RasterImageFunctions() {
    }
    
    public static abstract class AbstractRasterImageFunctions extends RasterImageFunction {
        protected Dim dim;
        protected int[] data;
        
        public AbstractRasterImageFunctions(Dim dim, int[] data) {
            this.dim = dim;
            this.data = data;
        }
        
    }
    
    public static RasterImageFunction of(Dim dim, final int[] data) {
        return new AbstractRasterImageFunctions(dim, data) {
            @Override
            public int eval(int x, int y, int index_xy) {
                return data[index_xy];
            }
        };
    }

    public static RasterImageFunction of(final ImageData data) {
        return of(data.getDim(), data.getData());
    }
    
    
    public static RasterImageFunction binaryDiff(Dim dim, final int[] compareData1, final int[] compareData2) {
        return new RasterImageFunction() {
            @Override
            public int eval(int x, int y, int index_xy) {
                return compareData1[index_xy] == compareData2[index_xy]? 0 : 1;
            }
        };
    }
}

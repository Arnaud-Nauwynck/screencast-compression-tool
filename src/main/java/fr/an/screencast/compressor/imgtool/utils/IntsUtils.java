package fr.an.screencast.compressor.imgtool.utils;

public final class IntsUtils {

    
    public static int[] insert(int[] src, int size, int pos, int value) {
        int[] res;
        int len = size-pos;
        if (size + 1 < src.length) {
            res = src;
            System.arraycopy(src, pos, res, pos+1, len);
        } else {
            res = new int[size + 10];
            System.arraycopy(src, 0, res, 0, pos);
            System.arraycopy(src, pos, res, pos+1, len);
        }
        res[pos] = value;
        return res;
    }
    
}

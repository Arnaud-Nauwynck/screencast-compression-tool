package fr.an.screencast.compressor.imgtool.utils;

public final class FastModuloUtils {

    public static int incrModulo(int value, int modulo) {
        int res = value + 1;
        if (res == modulo) {
            res = 0;
        }
        return res;
    }


    public static int minusModulo(int val, int minus, int modulo) {
        int res = val - minus;
        if (res < 0) {
            res = modulo-1;
        }
        return res;
    }
    
}

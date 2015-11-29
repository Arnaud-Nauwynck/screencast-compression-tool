package fr.an.screencast.compressor.imgtool.utils;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import fr.an.screencast.compressor.utils.Dim;

public class ImageRasterUtils {

    public static int[] toInts(BufferedImage img) {
        return ((DataBufferInt) img.getRaster().getDataBuffer()).getData();
    }
    
    
}

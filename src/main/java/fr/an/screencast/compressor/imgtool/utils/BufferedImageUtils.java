package fr.an.screencast.compressor.imgtool.utils;

import java.awt.image.BufferedImage;

public final class BufferedImageUtils {

    /* private to force all static*/
    private BufferedImageUtils() {}
    

    public static BufferedImage copyOrReallocImage(BufferedImage cache, BufferedImage source) {
        int height = source.getHeight();
        int width = source.getWidth();
        if (cache == null || cache.getWidth() != width || cache.getHeight() != height || cache.getType() != source.getType()) {
            cache = new BufferedImage(width, height, source.getType());
        }
        cache.getRaster().setRect(source.getRaster());
        return cache;
    }
}

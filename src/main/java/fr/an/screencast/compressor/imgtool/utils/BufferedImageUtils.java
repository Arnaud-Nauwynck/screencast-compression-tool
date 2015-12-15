package fr.an.screencast.compressor.imgtool.utils;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

public final class BufferedImageUtils {

    /* private to force all static */
    private BufferedImageUtils() {
    }

    public static BufferedImage copyOrReallocImage(BufferedImage cache, BufferedImage source) {
        int height = source.getHeight();
        int width = source.getWidth();
        if (cache == null || cache.getWidth() != width || cache.getHeight() != height || cache.getType() != source.getType()) {
            cache = new BufferedImage(width, height, source.getType());
        }
        cache.getRaster().setRect(source.getRaster());
        return cache;
    }

    /**
     * convert sourceImage to destImage
     * 
     * @param destImage
     *            destination to copy image, or re-allocated one if null
     * @param source
     * @param targetType
     * @return
     */
    public static BufferedImage convertToType(BufferedImage destImage, BufferedImage source, int targetType) {
        BufferedImage res;
        if (source.getType() == targetType) {
            res = source;
        } else {
            if (destImage == null || !sameSize(destImage, source) || destImage.getType() != targetType) {
                destImage = new BufferedImage(source.getWidth(), source.getHeight(), targetType);
            }
            res = destImage;
            Graphics2D g2d = res.createGraphics();
            g2d.drawImage(source, 0, 0, null);
        }
        return res;
    }

    public static boolean sameSize(BufferedImage i1, BufferedImage i2) {
        return i1.getWidth() == i2.getWidth() && i1.getHeight() == i2.getHeight();
    }

    public static boolean compatible(BufferedImage i1, BufferedImage i2) {
        return i1.getWidth() == i2.getWidth() && i1.getHeight() == i2.getHeight() && i1.getType() == i2.getType();
    }

}
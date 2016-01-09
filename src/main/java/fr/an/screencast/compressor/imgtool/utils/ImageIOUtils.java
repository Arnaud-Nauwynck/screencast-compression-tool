package fr.an.screencast.compressor.imgtool.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import fr.an.screencast.compressor.utils.Dim;
import fr.an.util.bits.RuntimeIOException;

public class ImageIOUtils {

    public static byte[] writeToBytes(Dim dim, int[] imgData, String formatName) {
        BufferedImage img = BufferedImageUtils.copyImage(dim, imgData);
        return writeToBytes(img, formatName);
    }

    public static byte[] writeToBytes(BufferedImage img, String formatName) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream(); 
        writeTo(bout, img, formatName);
        return bout.toByteArray();
    }

    public static void writeTo(OutputStream dest, BufferedImage img, String formatName) {
        try {
            ImageIO.write(img, formatName, dest);
        } catch (IOException e) {
            throw new RuntimeIOException("Failed", e);
        }
    }

    public static BufferedImage read(BufferedImage img, String formatName, InputStream in) {
        BufferedImage tmpImg;
        try {
            tmpImg = ImageIO.read(in);
        } catch (IOException e) {
            throw new RuntimeIOException("Failed", e);
        }
        return BufferedImageUtils.convertToType(img, tmpImg, BufferedImage.TYPE_INT_RGB);
    }

}

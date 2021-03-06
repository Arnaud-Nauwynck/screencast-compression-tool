package fr.an.screencast.compressor.imgtool.utils;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import fr.an.bitwise4j.util.RuntimeIOException;
import fr.an.screencast.compressor.utils.Dim;

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

    public static void writeTo(File dest, Dim dim, int[] imgData, String formatName) {
        BufferedImage img = BufferedImageUtils.copyImage(dim, imgData);
        writeTo(dest, img, formatName);
    }

    public static void writeTo(File dest, ImageData imgData, String formatName) {
        BufferedImage img = BufferedImageUtils.copyImage(imgData); // INT_RGB !
        writeTo(dest, img, formatName);
    }

    public static void writeRGBATo(File dest, ImageData imgData, String formatName) {
        Dim dim = imgData.getDim();
        BufferedImage img = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_ARGB); 
        ImageRasterUtils.copyData(img, imgData.getData());
        writeTo(dest, img, formatName);
    }

    public static void writeTo(File dest, BufferedImage img, String formatName) {
        try (BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(dest))) {
            writeTo(out, img, formatName);
        } catch (IOException e) {
            throw new RuntimeIOException("Failed to write to file '" + dest + "'", e);
        }
    }
    
    public static BufferedImage read(BufferedImage img, InputStream in) {
        BufferedImage tmpImg;
        try {
            tmpImg = ImageIO.read(in);
        } catch (IOException e) {
            throw new RuntimeIOException("Failed", e);
        }
        return BufferedImageUtils.convertToType(img, tmpImg, BufferedImage.TYPE_INT_RGB);
    }

    public static BufferedImage read(File in) {
        return read(null, in);
    }
    
    public static BufferedImage read(BufferedImage img, File in) {
        BufferedImage tmpImg;
        try {
            tmpImg = ImageIO.read(in);
        } catch (IOException e) {
            throw new RuntimeIOException("Failed", e);
        }
        return BufferedImageUtils.convertToType(img, tmpImg, BufferedImage.TYPE_INT_RGB);
    }

    public static ImageData readImageData(File in) {
        BufferedImage tmpres = read(in);
        return BufferedImageUtils.copyToImageData(tmpres);
    }

    public static ImageData readRGBAImageData(File in) {
        BufferedImage tmpres = readRGBA(in);
        return BufferedImageUtils.copyToImageData(tmpres);
    }

    public static BufferedImage readRGBA(File in) {
        return readRGBA(null, in);
    }

    public static BufferedImage readRGBA(BufferedImage img, File in) {
        BufferedImage tmpImg;
        try {
            tmpImg = ImageIO.read(in);
        } catch (IOException e) {
            throw new RuntimeIOException("Failed", e);
        }
        return BufferedImageUtils.convertToType(img, tmpImg, BufferedImage.TYPE_INT_ARGB);
    }
    
}

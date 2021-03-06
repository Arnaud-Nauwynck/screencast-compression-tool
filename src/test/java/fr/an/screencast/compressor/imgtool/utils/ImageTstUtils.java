package fr.an.screencast.compressor.imgtool.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import fr.an.screencast.compressor.imgtool.color.ColorBitsReducer;

public class ImageTstUtils {


    public static final String FILENAME_screen_eclipse_1920x1080 = "screen-eclipse-1920x1080.png";
    public static BufferedImage getImageTest_1920x1080() {
        return loadTestImg(FILENAME_screen_eclipse_1920x1080);
    }

    public static final String FILENAME_screen_eclipse_1920x1080_color7bits = "screen-eclipse-1920x1080-color7bits.png";
    public static BufferedImage getImageTest_1920x1080_color7bits() {
        return loadTestImg(FILENAME_screen_eclipse_1920x1080_color7bits);
    }

    public static final String FILENAME_screen_eclipse_1920x1080_color6bits = "screen-eclipse-1920x1080-color6bits.png";
    public static BufferedImage getImageTest_1920x1080_color6bits() {
        return loadTestImg(FILENAME_screen_eclipse_1920x1080_color6bits);
    }

    public static BufferedImage loadTestImg(String testFilename) {
        return loadImg(new File("src/test/imgs/" + testFilename));
    }
    
    public static BufferedImage loadImg(File file) {
        try {
            BufferedImage srcImg = ImageIO.read(file);
            BufferedImage img = BufferedImageUtils.convertToType(null, srcImg, BufferedImage.TYPE_INT_RGB);
            return img;
        } catch(IOException ex) {
            throw new RuntimeException("Failed to read file " + file, ex);
        }
    }

    public static void saveImg(BufferedImage img, File file) {
        try {
            ImageIO.write(img, "png", file);
        } catch(IOException ex) {
            throw new RuntimeException("Failed to write png file " + file, ex);
        }
    }

    public static void loadReduceLeastSignificantBitsAndSave(File inputFile, File outputFile, int reduceBitCount) throws IOException {
        BufferedImage img = loadImg(inputFile);

        int[] imgData = ImageRasterUtils.toInts(img);
        // clear least significant bits!!! (color 0;0;0 ~= 0;0;1 !!)
        ColorBitsReducer.maskLeastSignificantBits(imgData, reduceBitCount);
        
        ImageIO.write(img, "png", outputFile);
    }
    
}

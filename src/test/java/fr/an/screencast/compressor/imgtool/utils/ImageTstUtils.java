package fr.an.screencast.compressor.imgtool.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import fr.an.screencast.compressor.imgtool.color.ColorBitsReducer;

public class ImageTstUtils {


    public static BufferedImage getImageTest_1920x1080() {
        return loadImg(new File("src/test/imgs/screen-eclipse-1920x1080.png"));
    }

    public static BufferedImage getImageTest_1920x1080_color7bits() {
        return loadImg(new File("src/test/imgs/screen-eclipse-1920x1080-color7bits.png"));
    }


    public static BufferedImage getImageTest_1920x1080_color6bits() {
        return loadImg(new File("src/test/imgs/screen-eclipse-1920x1080-color6bits.png"));
    }


    public static BufferedImage loadImg(File file) {
        try {
            BufferedImage srcImg = ImageIO.read(file);
            BufferedImage img = BufferedImageUtils.convertToType(null, srcImg, BufferedImage.TYPE_INT_RGB);
            return img;
        } catch(IOException ex) {
            throw new RuntimeException("Failed", ex);
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

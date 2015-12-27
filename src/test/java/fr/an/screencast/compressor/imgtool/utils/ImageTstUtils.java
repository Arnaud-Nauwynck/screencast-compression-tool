package fr.an.screencast.compressor.imgtool.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class ImageTstUtils {


    public static BufferedImage getImageTest_1920x1080() {
        try {
            BufferedImage srcImg = ImageIO.read(new File("src/test/imgs/screen-eclipse-1920x1080.png"));
            BufferedImage img = BufferedImageUtils.convertToType(null, srcImg, BufferedImage.TYPE_INT_RGB);
            return img;
        } catch(IOException ex) {
            throw new RuntimeException("Failed", ex);
        }
    }
}

package fr.an.screencast.ui.swing;

import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.JFrame;

import fr.an.screencast.compressor.imgtool.utils.BufferedImageUtils;
import fr.an.screencast.compressor.imgtool.utils.ImageData;
import fr.an.screencast.ui.swing.internal.ImageCanvas;

public class ImageViewUtils {

    public static JFrame openImageFrame(ImageData imgData) {
        BufferedImage img = BufferedImageUtils.copyImage(imgData);
        return openImageFrame(img);
    }
    
    public static JFrame openImageFrame(BufferedImage img) {
        return JFrameUtils.openFrame("", () -> {
            ImageCanvas imageCanvas = new ImageCanvas();
            imageCanvas.setImage(img);
            imageCanvas.setPreferredSize(new Dimension(img.getWidth(), img.getHeight()));
            return imageCanvas;
        });
    }
    
}

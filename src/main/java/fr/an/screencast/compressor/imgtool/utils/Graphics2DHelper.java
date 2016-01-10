package fr.an.screencast.compressor.imgtool.utils;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;

import fr.an.screencast.compressor.utils.Rect;

/**
 * helper "extensions" class for java.awt.Graphics2D
 */
public class Graphics2DHelper {

    private BufferedImage img;
    private Graphics2D g2d;
    
    // ------------------------------------------------------------------------

    public Graphics2DHelper(BufferedImage img) {
        this.img = img;
        this.g2d = img.createGraphics();
    }
    
    // ------------------------------------------------------------------------

    public void drawRect(Rect rect) {
        g2d.drawRect(rect.fromX, rect.fromY, rect.getWidth(), rect.getHeight());
    }

    public void setColorStroke(Color color, int stroke) {
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(stroke));
    }
    
}

package fr.an.screencast.ui.swing.internal;

import java.awt.Graphics;
import java.awt.Image;

import javax.swing.JComponent;

public class ImageCanvas extends JComponent {

    /** */
    private static final long serialVersionUID = 1L;
    
    private Image image;

    // ------------------------------------------------------------------------

    public ImageCanvas() {
    }

    // ------------------------------------------------------------------------

    public void setImage(Image image) {
        this.image = image;
        repaint();
    }

    public void paint(Graphics g) {
        if (image != null) {
            int w = this.getWidth();
            int h = this.getHeight();
            g.drawImage(image, 0, 0, w, h, this);
        }
    }
}
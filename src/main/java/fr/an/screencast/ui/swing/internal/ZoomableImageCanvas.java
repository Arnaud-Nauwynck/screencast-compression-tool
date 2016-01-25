package fr.an.screencast.ui.swing.internal;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JPanel;

import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;

public class ZoomableImageCanvas {

    private JPanel panel;
    
    private BufferedImage image;
    private Dim imageDim;
    
    private Rect imageRect;

    private double zoom = 1.0;
    
    private boolean editZoomMode = false;
    
    
    // ------------------------------------------------------------------------
    
    public ZoomableImageCanvas() {
        imageRect = Rect.newPtDim(0, 0, 1, 1);
        panel = new JPanel() {
            private static final long serialVersionUID = 1L;
            @Override 
            public void paintComponent(Graphics g) {
                onPaintComponent(g);
            }
        };
        panel.setOpaque(false);
        MouseAdapter mouseListener = new MouseAdapter() {
            private double previousX;
            private double previousY;
            @Override
            public void mousePressed(MouseEvent e) {
                if (editZoomMode || (0 != (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK))) {
                    previousX = e.getX();
                    previousY = e.getY();
                }
            }
            @Override
            public void mouseDragged(MouseEvent e){
                if (editZoomMode || (0 != (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK))) {
                    double newX = e.getX() - previousX;
                    double newY = e.getY() - previousY;
        
                    previousX += newX;
                    previousY += newY;
        
                    imageRect.fromX += newX;
                    imageRect.fromY += newY;
                    updateImageRectDim();
                     
                    panel.repaint();
                }
            }
            @Override
            public void mouseWheelMoved(MouseWheelEvent e) {
                if (editZoomMode || (0 != (e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK))) {
                    if(e.getScrollType() == MouseWheelEvent.WHEEL_UNIT_SCROLL) {
                        incrementZoom(.1 * -(double)e.getWheelRotation());
                    }
                }
            }
        };
        panel.addMouseListener(mouseListener);
        panel.addMouseMotionListener(mouseListener);
         
    }

    // ------------------------------------------------------------------------

    public JComponent getJComponent() {
        return panel;
    }
    
    protected void updateImageRectDim() {
        imageRect.toX = imageRect.fromX + (int) (image.getWidth() * zoom); 
        imageRect.toY = imageRect.fromY + (int) (image.getHeight() * zoom); 
    }

    public void setImage(BufferedImage image) {
        this.image = image;
        this.imageDim = new Dim(image.getWidth(), image.getHeight());
        updateImageRectDim();
        panel.repaint();
    }
    
    public BufferedImage getImage() {
        return image;
    }
    
    public Dim getImageDim() {
        return imageDim;
    }

    public boolean isEditZoomMode() {
        return editZoomMode;
    }

    public void setEditZoomMode(boolean editZoomMode) {
        this.editZoomMode = editZoomMode;
    }

    public void incrementZoom(double amount) {
        zoom += amount;
        zoom = Math.max(0.00001, zoom);
        updateImageRectDim();
        panel.repaint();
    }
     
    protected void onPaintComponent(Graphics g) {
        Graphics2D g2d = (Graphics2D)g.create();

        g2d.drawImage(image, imageRect.fromX, imageRect.fromY, imageRect.getWidth(), imageRect.getHeight(), null);
        
        g2d.dispose();
    }

    public Pt viewToModelPt(MouseEvent e) {
        return viewToModelPt(e.getX(), e.getY());
    }
    
    public Pt viewToModelPt(int x, int y) {
        int imgX = (x - imageRect.fromX) * image.getWidth() / imageRect.getWidth();
        int imgY = (y - imageRect.fromY) * image.getHeight() / imageRect.getHeight();
        return new Pt(imgX, imgY);
    }

}
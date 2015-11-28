package fr.an.screencast.compressor.ui;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public class DeltaImageAnalysisPanel {

    private JPanel panel;
    private ImageCanvas prevImgCanvas;
    private ImageCanvas imgCanvas;
    private ImageCanvas diffImgCanvas;
    private ImageCanvas deltaImgCanvas;
    
    // ------------------------------------------------------------------------

    public DeltaImageAnalysisPanel() {
        panel = new JPanel(new GridLayout(2, 2));
        int dispImgWidth = 1900 / 5;
        int dispImgHeight= 1080 / 5;
        Dimension imgDisplaySize = new Dimension(dispImgWidth, dispImgHeight);

        prevImgCanvas = new ImageCanvas();
        prevImgCanvas.setPreferredSize(imgDisplaySize);
        
        imgCanvas = new ImageCanvas();
        imgCanvas.setPreferredSize(imgDisplaySize);
        
        diffImgCanvas = new ImageCanvas();
        diffImgCanvas.setPreferredSize(imgDisplaySize);
        
        deltaImgCanvas = new ImageCanvas();
        deltaImgCanvas.setPreferredSize(imgDisplaySize);
        
        panel.add(prevImgCanvas);
        panel.add(imgCanvas);
        panel.add(diffImgCanvas);
        panel.add(deltaImgCanvas);
    }

    // ------------------------------------------------------------------------

    public JComponent getJComponent() {
        return panel;
    }
    
    public void setImages(Image prevImage, Image image, Image diffImage, Image deltaImage) {
        prevImgCanvas.setImage(prevImage);
        imgCanvas.setImage(image);
        diffImgCanvas.setImage(diffImage);
        deltaImgCanvas.setImage(deltaImage);
    }

    public void asyncSetImages(final Image prevImageRGB, final Image imageRGB, final Image diffImage, final Image deltaImage) {
        SwingUtilities.invokeLater(() -> setImages(prevImageRGB, imageRGB, diffImage, deltaImage));
    }
}

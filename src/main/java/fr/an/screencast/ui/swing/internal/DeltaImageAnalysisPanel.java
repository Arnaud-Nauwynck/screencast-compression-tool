package fr.an.screencast.ui.swing.internal;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import fr.an.screencast.compressor.imgtool.utils.BufferedImageUtils;

public class DeltaImageAnalysisPanel {

    private JPanel panel;
    private ImageCanvas prevImgCanvas;
    private ImageCanvas imgCanvas;
    private ImageCanvas diffImgCanvas;
    private ImageCanvas deltaImgCanvas;

    private BufferedImage copyImg;
    private BufferedImage copyPrevImg;
    private BufferedImage copyDiffImg;
    private BufferedImage copyDeltaImg;

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

    public void asyncSetImages(final BufferedImage prevImageRGB, final BufferedImage imageRGB, 
            final BufferedImage diffImage, final BufferedImage deltaImage) {
        // bug when calling invokeLater ... BufferedImage may be modified from thread!
        // using invokeAndWait() is NOT enough 
        // ... because underlying ImageCanvas call swing repaint(), which is also asynchronous (RepaintManager setDirty region ...) 
        // => need copy data ...  
        copyImg = safeCopyImage(copyImg, prevImageRGB);
        copyPrevImg = safeCopyImage(copyPrevImg, imageRGB);
        copyDiffImg = safeCopyImage(copyDiffImg, diffImage);
        copyDeltaImg = safeCopyImage(copyDeltaImg, deltaImage);
        
        SwingUtilities.invokeLater(() -> setImages(copyPrevImg, copyImg, copyDiffImg, copyDeltaImg));
    }
    
    private static boolean SLOW_SAFE_COPY = true;

    private static BufferedImage safeCopyImage(BufferedImage cache, BufferedImage source) {
        if (! SLOW_SAFE_COPY) return source;
        return BufferedImageUtils.copyOrReallocImage(cache, source);
    }
    
}

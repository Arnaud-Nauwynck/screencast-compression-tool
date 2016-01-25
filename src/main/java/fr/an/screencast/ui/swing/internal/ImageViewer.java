package fr.an.screencast.ui.swing.internal;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JToolBar;

import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.ui.swing.JButtonUtils;

public class ImageViewer {

    private JPanel mainPanel;
    
    private JToolBar toolbar;
    private JLabel otherLabel;
    
    private ZoomableImageCanvas imageCanvas;
    
    // ------------------------------------------------------------------------

    public ImageViewer() {
        mainPanel = new JPanel(new BorderLayout());
        toolbar = new JToolBar();
        imageCanvas = new ZoomableImageCanvas();
        
        mainPanel.add(toolbar, BorderLayout.NORTH);
        mainPanel.add(imageCanvas.getJComponent(), BorderLayout.CENTER);
        
        toolbar.add(JButtonUtils.snew("+", e -> imageCanvas.incrementZoom(0.1)));
        toolbar.add(JButtonUtils.snew("-", e -> imageCanvas.incrementZoom(-0.1)));
        JCheckBox editModeCB = new JCheckBox("move");
        editModeCB.setSelected(imageCanvas.isEditZoomMode());
        editModeCB.addActionListener(e -> {
            boolean newSelected = editModeCB.isSelected();
            imageCanvas.setEditZoomMode(newSelected);
        });
        toolbar.add(editModeCB);
        
        JLabel mousePointLabel = new JLabel("Pt()");
        toolbar.add(mousePointLabel);

        otherLabel = new JLabel();
        toolbar.add(otherLabel);

        MouseAdapter mouseListener = new MouseAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                updatePixelLabel(e);
            }
            @Override
            public void mouseDragged(MouseEvent e) {
                updatePixelLabel(e);
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                updatePixelLabel(e);
            }
            private void updatePixelLabel(MouseEvent e) {
                Pt pt = imageCanvas.viewToModelPt(e);
                String pixel = "";
                Dim imgDim = imageCanvas.getImageDim();
                if (pt.x >= 0 && pt.x < imgDim.width && pt.y >= 0 && pt.y < imgDim.height) {
                    int rgb = imageCanvas.getImage().getRGB(pt.x, pt.y);
                    pixel = " " + RGBUtils.toString(rgb);
                }
                String text = "Pt(" + pt.x + ", " + pt.y + ") " + pixel;
                mousePointLabel.setText(text);
            }
        };
        imageCanvas.getJComponent().addMouseListener(mouseListener);
        imageCanvas.getJComponent().addMouseMotionListener(mouseListener);
    }

    // ------------------------------------------------------------------------
    
    public JComponent getComponent() {
        return mainPanel;
    }

    public JComponent getImageComponent() {
        return imageCanvas.getJComponent();
    }

    public void setImage(BufferedImage image) {
        imageCanvas.setImage(image);   
    }
    
    public Pt viewToModelPt(int x, int y) {
        return imageCanvas.viewToModelPt(x, y);
    }

    public void setOtherLabelText(String text) {
        otherLabel.setText(text);
    }
    
}

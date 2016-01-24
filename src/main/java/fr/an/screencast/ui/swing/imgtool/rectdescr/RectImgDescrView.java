package fr.an.screencast.ui.swing.imgtool.rectdescr;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JSplitPane;

import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.helper.ROIToDescrPathRectImgDescrVisitor;
import fr.an.screencast.compressor.imgtool.utils.BufferedImageUtils;
import fr.an.screencast.compressor.imgtool.utils.Graphics2DHelper;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.screencast.ui.swing.internal.ImageCanvas;

public class RectImgDescrView {

    private JSplitPane mainPanel;
    private RectImgDescrJTree leftTree;
    private ImageCanvas imageCanvas;
    
    private BufferedImage origImg;
    private BufferedImage img;
    
    // ------------------------------------------------------------------------

    public RectImgDescrView(BufferedImage srcImg, RectImgDescr model) {
        createUI();
        // leftTree.getComponent().setPreferredSize(new Dim);
        imageCanvas.setPreferredSize(new Dimension(srcImg.getWidth()/2, srcImg.getHeight()/2));
        origImg = BufferedImageUtils.copyImage(srcImg);
        img = BufferedImageUtils.copyImage(srcImg);
        setImage(img);
        setRectImgDescrModel(model);
    }

    private void createUI() {
        mainPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        leftTree = new RectImgDescrJTree();
        imageCanvas = new ImageCanvas();
        mainPanel.add(leftTree.getComponent());
        mainPanel.add(imageCanvas);
        mainPanel.setDividerLocation(0.2);
        
        leftTree.addPropertyChangeListener(RectImgDescrJTree.PROP_selectedRectDescrPath, evt -> {
            // evt.getNewValue();
            RectImgDescr[] selectedRectDescrPath = leftTree.getSelectedRectDescrPath();
            BufferedImageUtils.copyImage(img, origImg);
            if (selectedRectDescrPath != null) {
                Graphics2DHelper g2d = new Graphics2DHelper(img);
                g2d.setColorStroke(Color.ORANGE, 2);
                for(int i = 0; i < selectedRectDescrPath.length; i++) {
                    RectImgDescr rectDescr = selectedRectDescrPath[i];
                    Rect rect = rectDescr.getRect();
                    if (i+1==selectedRectDescrPath.length) {
                        g2d.setColorStroke(Color.RED, 4);
                    }
                    g2d.drawRectOut(rect);
                }
            }
            imageCanvas.setImage(img);
        });
        
        
        imageCanvas.addMouseListener(new MouseAdapter() {
            Pt selectionFromPt;

            @Override
            public void mousePressed(MouseEvent e) {
                this.selectionFromPt = viewToModelPt(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                Pt selectionToPt = viewToModelPt(e);
                Rect roi = Rect.newSortPtToPt(selectionFromPt, selectionToPt);
                roiToTreePathSelection(roi);
                
                selectionFromPt = null;
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                if (selectionFromPt != null) {
                    Pt selectionToPt = viewToModelPt(e);
                    Rect roi = Rect.newSortPtToPt(selectionFromPt, selectionToPt);
                    roiToTreePathSelection(roi);
                }
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                Pt pt = viewToModelPt(e);
                Rect roi = Rect.newPtDim(pt, new Dim(1, 1));
                roiToTreePathSelection(roi);
            }
            private Pt viewToModelPt(MouseEvent e) {
                return viewToModelPt(e.getX(), e.getY()); 
            }
            private Pt viewToModelPt(int x, int y) {
//                int offsetX = imageCanvas.getX();
//                int offsetY = imageCanvas.getY();
                int offsetX = 0, offsetY = 0;
                int canvasW = imageCanvas.getWidth();
                int canvasH = imageCanvas.getHeight();
                int imgX = (x - offsetX) * img.getWidth() / canvasW;
                int imgY = (y - offsetY) * img.getHeight() / canvasH;
                return new Pt(imgX, imgY);
            }
            private void roiToTreePathSelection(Rect roi) {
                RectImgDescr model = leftTree.getModel();
                List<RectImgDescr> path = ROIToDescrPathRectImgDescrVisitor.roiToPath(model, roi);
                leftTree.setSelectedPath(path);
            }
        });
    }

    // ------------------------------------------------------------------------

    public JComponent getJComponent() {
        return mainPanel;
    }
    
    public void setRectImgDescrModel(RectImgDescr model) {
        leftTree.setModel(model);
    }
    
    public void setImage(BufferedImage img) {
        imageCanvas.setImage(img);
    }
    
    
}

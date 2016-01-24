package fr.an.screencast.ui.swing.imgtool.rectdescr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.compressor.imgtool.rectdescr.RectImgDescrAnalyzer;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.helper.DumpRectImgDescrVisitor;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.helper.ROIToDescrPathRectImgDescrVisitor;
import fr.an.screencast.compressor.imgtool.utils.BufferedImageUtils;
import fr.an.screencast.compressor.imgtool.utils.Graphics2DHelper;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.screencast.ui.swing.internal.ImageCanvas;

public class RectImgDescrView {

    private static final Logger LOG = LoggerFactory.getLogger(RectImgDescrView.class);
    
    private JPanel mainPanel;
    private JPanel menuPanel;
    private JSplitPane splitImgDetailPanel;
    private JSplitPane splitTreeViewImgPanel;
    private RectImgDescrJTree leftTree;
    private ImageCanvas imageCanvas;
    private JPanel bottomDetailsPanel;
    private JTextPane detailsTextPane;
    
    private BufferedImage origImg;
    private BufferedImage img;
    private RectImgDescrAnalyzer analyzer;
    
    private RectImgDescr currSelectedRectDescr;
    
    // ------------------------------------------------------------------------

    public RectImgDescrView(BufferedImage srcImg, RectImgDescr model) {
        createUI();
        // leftTree.getComponent().setPreferredSize(new Dim);
        imageCanvas.setPreferredSize(new Dimension(srcImg.getWidth()/2, srcImg.getHeight()/2));
        origImg = BufferedImageUtils.copyImage(srcImg);
        img = BufferedImageUtils.copyImage(srcImg);
        setImage(img);
        setRectImgDescrModel(model);
        Dim dim = new Dim(srcImg.getWidth(), srcImg.getHeight());
        analyzer = new RectImgDescrAnalyzer(dim);
        analyzer.setImg(ImageRasterUtils.toInts(origImg));
    }

    private void createUI() {
        mainPanel = new JPanel(new BorderLayout());
        menuPanel = new JPanel(new FlowLayout());
        splitImgDetailPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitTreeViewImgPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        leftTree = new RectImgDescrJTree();
        imageCanvas = new ImageCanvas();
        bottomDetailsPanel = new JPanel(new GridLayout(1, 2));
        detailsTextPane = new JTextPane();
        JScrollPane detailScrollPane = new JScrollPane(detailsTextPane);
        detailScrollPane.setPreferredSize(new Dimension(200, 80));
        bottomDetailsPanel.add(detailScrollPane);
        
        mainPanel.add(menuPanel, BorderLayout.NORTH);
        mainPanel.add(splitImgDetailPanel, BorderLayout.CENTER);
        
        splitImgDetailPanel.add(splitTreeViewImgPanel);
        splitImgDetailPanel.add(bottomDetailsPanel);
        splitTreeViewImgPanel.setDividerLocation(0.8);
        
        splitTreeViewImgPanel.add(leftTree.getComponent());
        splitTreeViewImgPanel.add(imageCanvas);
        splitTreeViewImgPanel.setDividerLocation(0.2);
        
        JButton buttonDump = new JButton("Dump");
        buttonDump.addActionListener(e -> {
            if (currSelectedRectDescr != null) {
                dumpTextDetail(currSelectedRectDescr);
            }
        });
        menuPanel.add(buttonDump);
        
        JButton reevalDetectButton = new JButton("Detect");
        reevalDetectButton.addActionListener(e -> {
            if (currSelectedRectDescr != null) {
                Rect rect = currSelectedRectDescr.getRect();
                analyzeRect(rect);
            }
        });
        menuPanel.add(reevalDetectButton);
        
        leftTree.addPropertyChangeListener(RectImgDescrJTree.PROP_selectedRectDescrPath, evt -> {
            RectImgDescr[] selectedRectDescrPath = leftTree.getSelectedRectDescrPath();
            onSelectedTreePath_drawRectsImg(selectedRectDescrPath);
            
            currSelectedRectDescr = selectedRectDescrPath.length > 0? selectedRectDescrPath[selectedRectDescrPath.length-1] : null;
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
    

    private void onSelectedTreePath_drawRectsImg(RectImgDescr[] selectedRectDescrPath) {
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
    }

    private void dumpTextDetail(RectImgDescr rectDescr) {
        if (rectDescr == null) {
            return;
        }
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer);
        out.println();
        out.println("dump:");
        DumpRectImgDescrVisitor dumpVisitor = new DumpRectImgDescrVisitor(out, null);
        dumpVisitor.setMaxLevel(1);
        rectDescr.accept(dumpVisitor);
        
        out.flush();
        String text = buffer.toString();
        appendTextTo(detailsTextPane, text);
    }

    private void appendTextTo(JTextPane textPane, String text) {
        Document doc = textPane.getDocument();
        try {
            doc.insertString(doc.getLength(), text, null);
        } catch (BadLocationException e) {
            LOG.warn("Failed to write text", e);
        }
    }
    

    private void analyzeRect(Rect rect) {
        appendTextTo(detailsTextPane, "analyze rect =>\n");

        RectImgDescr res = analyzer.detect(rect);
        
        dumpTextDetail(res);
    }

}

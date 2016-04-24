package fr.an.screencast.ui.swing.imgtool.rectdescr;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.InputEvent;
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
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.screencast.ui.swing.internal.ImageViewer;

public class RectImgDescrView {

    private static final Logger LOG = LoggerFactory.getLogger(RectImgDescrView.class);
    
    private JPanel mainPanel;
    private JPanel menuPanel;
    private JSplitPane splitImgDetailPanel;
    private JSplitPane splitTreeViewImgPanel;
    private RectImgDescrJTree leftTree;
    private ImageViewer imageViewer;
    private JPanel bottomDetailsPanel;
    private JTextPane detailsTextPane;
    
    private BufferedImage origImg;
    private Dim dim;
    private BufferedImage img;
    private RectImgDescrAnalyzer analyzer;
    
    private RectImgDescr currSelectedRectDescr;
    private RectImgDescr[] currSelectedRectDescrPath;
    private Rect currSelRect;
    
    // ------------------------------------------------------------------------

    public RectImgDescrView(BufferedImage srcImg, RectImgDescr model) {
        createUI();
        // leftTree.getComponent().setPreferredSize(new Dim);
        imageViewer.getComponent().setPreferredSize(new Dimension(srcImg.getWidth()/2, srcImg.getHeight()/2));
        origImg = BufferedImageUtils.copyImage(srcImg);
        dim = new Dim(origImg.getWidth(), origImg.getHeight());
        img = BufferedImageUtils.copyImage(srcImg);
        setImage(img);
        setRectImgDescrModel(model);
        analyzer = new RectImgDescrAnalyzer(dim);
        analyzer.setImg(ImageRasterUtils.toInts(origImg));
    }

    private void createUI() {
        mainPanel = new JPanel(new BorderLayout());
        menuPanel = new JPanel(new FlowLayout());
        splitImgDetailPanel = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitTreeViewImgPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        leftTree = new RectImgDescrJTree();
        imageViewer = new ImageViewer();
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
        splitTreeViewImgPanel.add(imageViewer.getComponent());
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
                analyzeRect(currSelectedRectDescr.getRect());
            }
        });
        menuPanel.add(reevalDetectButton);
        
        JButton dumpRGBButton = new JButton("DumpRGB");
        dumpRGBButton.addActionListener(e -> {
            if (currSelRect != null) {
                dumpRGBRect(currSelRect);
            }
        });
        menuPanel.add(dumpRGBButton);
        
        leftTree.addPropertyChangeListener(RectImgDescrJTree.PROP_selectedRectDescrPath, evt -> {
            currSelectedRectDescrPath = leftTree.getSelectedRectDescrPath();
            currSelectedRectDescr = currSelectedRectDescrPath.length > 0? currSelectedRectDescrPath[currSelectedRectDescrPath.length-1] : null;

            onRepaint();
        });
        
        
        MouseAdapter mouseListener = new MouseAdapter() {
            Pt selectionFromPt;

            @Override
            public void mousePressed(MouseEvent e) {
                if (0 == (e.getModifiers() & InputEvent.SHIFT_MASK)) {
                    return;
                }
                this.selectionFromPt = viewToModelPt(e);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (selectionFromPt == null) {
                    return;
                }
                Pt selectionToPt = viewToModelPt(e);
                Rect roi = Rect.newSortPtToPt(selectionFromPt, selectionToPt);

                if (0 != (e.getModifiers() & InputEvent.SHIFT_MASK)) {
                    currSelRect = roi;
                } else {
                    roiToTreePathSelection(roi);
                }
                
                selectionFromPt = null;
                
                imageViewer.setOtherLabelText(" sel rect: " + currSelRect + 
                    ((currSelectedRectDescr != null)? " descr: " + currSelectedRectDescr.getRect() : ""));
                onRepaint();
            }
            @Override
            public void mouseMoved(MouseEvent e) {
                if (0 == (e.getModifiers() & InputEvent.SHIFT_MASK)) {
                    return;
                }
                if (selectionFromPt != null) {
                    Pt selectionToPt = viewToModelPt(e);
                    Rect roi = Rect.newSortPtToPt(selectionFromPt, selectionToPt);
                    roiToTreePathSelection(roi);
                }
            }
            @Override
            public void mouseClicked(MouseEvent e) {
                if (0 != (e.getModifiers() & InputEvent.SHIFT_MASK)) {
                    return;
                }
                Pt pt = viewToModelPt(e);
                Rect roi = Rect.newPtDim(pt, new Dim(1, 1));
                roiToTreePathSelection(roi);
            }
            private Pt viewToModelPt(MouseEvent e) {
                return imageViewer.viewToModelPt(e.getX(), e.getY()); 
            }
            
            private void roiToTreePathSelection(Rect roi) {
                RectImgDescr model = leftTree.getModel();
                List<RectImgDescr> path = ROIToDescrPathRectImgDescrVisitor.roiToPath(model, roi);
                leftTree.setSelectedPath(path);
            }
        };
        imageViewer.getImageComponent().addMouseListener(mouseListener);
        imageViewer.getImageComponent().addMouseMotionListener(mouseListener);
    }


    // ------------------------------------------------------------------------

    public JComponent getJComponent() {
        return mainPanel;
    }
    
    public void setRectImgDescrModel(RectImgDescr model) {
        leftTree.setModel(model);
    }
    
    public void setImage(BufferedImage img) {
        imageViewer.setImage(img);
    }
    

    private void onRepaint() {
        BufferedImageUtils.copyImage(img, origImg);
        Graphics2DHelper g2d = new Graphics2DHelper(img);
        
        if (currSelectedRectDescrPath != null) {
            g2d.setColorStroke(Color.ORANGE, 2);
            for(int i = 0; i < currSelectedRectDescrPath.length; i++) {
                RectImgDescr rectDescr = currSelectedRectDescrPath[i];
                Rect rect = rectDescr.getRect();
                if (i+1==currSelectedRectDescrPath.length) {
                    g2d.setColorStroke(Color.RED, 4);
                }
                g2d.drawRectOut(rect);
            }
        }
        
        if (currSelRect != null) {
            g2d.setColorStroke(Color.BLACK, 1);
            g2d.drawRectOut(currSelRect);
        }
        
        // imageViewer.setImage(img); // useless??
        imageViewer.getComponent().repaint();
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
        
        out.println("\n");
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

    private void dumpRGBRect(Rect rect) {
        appendTextTo(detailsTextPane, "dump RGB rect => " + rect + "\n");

        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        PrintStream out = new PrintStream(buffer);
        
        RGBUtils.dumpFixedRGBString(dim, ImageRasterUtils.toInts(origImg), rect, out);
        out.flush();
        
        appendTextTo(detailsTextPane, buffer.toString());
    }

}

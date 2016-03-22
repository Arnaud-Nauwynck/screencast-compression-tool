package fr.an.screencast.ui.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import fr.an.screencast.compressor.imgtool.ocr.OCRInteractivePrompter;
import fr.an.screencast.compressor.imgtool.ocr.settings.OCRGlyphConnexeComponent;
import fr.an.screencast.compressor.imgtool.ocr.settings.OCRGlyphDescr;
import fr.an.screencast.compressor.imgtool.ocr.settings.OCRSettings;
import fr.an.screencast.compressor.imgtool.ocr.settings.OCRSettingsIOUtils;
import fr.an.screencast.compressor.imgtool.utils.BufferedImageUtils;
import fr.an.screencast.compressor.imgtool.utils.Graphics2DHelper;
import fr.an.screencast.compressor.imgtool.utils.ImageIOUtils;
import fr.an.screencast.compressor.imgtool.utils.PtImageData;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.ui.swing.internal.ImageCanvas;

public class OCRInteractivePrompterView implements OCRInteractivePrompter {
    
    private ScreenshotRecorderView screenshotRecorderView;
    
    private JDialog modalDialog;
    private JPanel mainPanel;
    private ImageCanvas imageCanvas;
    
    private JTextField glyphDisplayNameField;
    private JTextField glyphResultTextField;
    
    private JButton okButton;
    private JButton uncompleteGlyphButton;
    private JButton ignoreButton;
    
    private File currentOCRSettingsFile;
    private OCRSettings currentOCRSettings;
    private BufferedImage currentScreenshotImg;
    private BufferedImage renderImg;
    private PtImageData currConnexeComp;
    
    private OCRGlyphDescr currGlyphDescr;
    private Pt currGlyphOrigin;
    
    // ------------------------------------------------------------------------
    
    public OCRInteractivePrompterView(ScreenshotRecorderView screenshotRecorderView) {
        this.screenshotRecorderView = screenshotRecorderView;
        
        mainPanel = new JPanel(new BorderLayout());
        
        imageCanvas = new ImageCanvas();
        imageCanvas.setPreferredSize(new Dimension(100, 100));
        mainPanel.add(imageCanvas, BorderLayout.CENTER);
        
        JPanel answerPanel = new JPanel(new GridBagLayout());
        mainPanel.add(answerPanel, BorderLayout.SOUTH);
        
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0; 
        gbcLabel.gridy = 0;
        gbcLabel.gridwidth = 1;
        gbcLabel.gridheight = 1;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbcLabel.insets = new Insets(5, 5, 5, 5);

        GridBagConstraints gbcField = new GridBagConstraints();
        gbcField.gridx = 1; 
        gbcField.gridy = 0;
        gbcField.gridwidth = 1;
        gbcField.gridheight = 1;
        gbcField.anchor = GridBagConstraints.CENTER;
        gbcField.fill = GridBagConstraints.BOTH;
        gbcField.insets = new Insets(5, 5, 5, 5);
        
        answerPanel.add(new JLabel("Glyph display name"), gbcLabel);
        glyphDisplayNameField = new JTextField();
        answerPanel.add(glyphDisplayNameField, gbcField);
        gbcLabel.gridy++;
        gbcField.gridy++;
        
        answerPanel.add(new JLabel("Glyph result text"), gbcLabel);
        glyphResultTextField = new JTextField();
        answerPanel.add(glyphResultTextField, gbcField);
        gbcLabel.gridy++;
        gbcField.gridy++;
        
        okButton = new JButton("OK");
        okButton.addActionListener(e -> onOKAction());
        answerPanel.add(okButton, gbcField);
        gbcLabel.gridy++;
        gbcField.gridy++;
        
        uncompleteGlyphButton = new JButton("Uncomplete-Glyph");
        uncompleteGlyphButton.addActionListener(e -> onUncompleteGlyphAction());
        answerPanel.add(uncompleteGlyphButton, gbcField);
        gbcLabel.gridy++;
        gbcField.gridy++;
        
        ignoreButton = new JButton("Ignore");
        ignoreButton.addActionListener(e -> onIgnoreGlyphAction());
        answerPanel.add(ignoreButton, gbcField);
        gbcLabel.gridy++;
        gbcField.gridy++;
    }

    // ------------------------------------------------------------------------
    
    private void onOKAction() {
        addCurrConnexComp();
        
        currentOCRSettings.addGlyphDescr(currGlyphDescr);
        currGlyphDescr = null;
        currConnexeComp = null;
        
        glyphDisplayNameField.setText("");
        glyphResultTextField.setText("");
        
        OCRSettingsIOUtils.writeOCRSettings(currentOCRSettingsFile, currentOCRSettings);

        modalDialog.setVisible(false);
    }

    private void onUncompleteGlyphAction() {
        addCurrConnexComp();
        
        modalDialog.setVisible(false);
    }

    protected void addCurrConnexComp() {
        if (currGlyphDescr == null) {
            String glyphDisplayName = glyphDisplayNameField.getText();
            String glyphResultText = glyphResultTextField.getText();
            
            currGlyphDescr = new OCRGlyphDescr(currentOCRSettings, glyphDisplayName, glyphResultText);
            currGlyphOrigin = currConnexeComp.getPt();
        }
        
        Pt connexCompOffset = currConnexeComp.getPt().newMinus(currGlyphOrigin);
        
        int currConnexCompIndex = currGlyphDescr.getConnexComponents().size();
        String connexCompFilename = currGlyphDescr.getGlyphDisplayName() + ((currConnexCompIndex != 0)? "-" + currConnexCompIndex : "") + ".png";
        
        File connexCompFile = (currentOCRSettings.getBaseDir() != null)? 
                new File(currentOCRSettings.getBaseDir(), connexCompFilename)
                : new File(connexCompFilename);
        ImageIOUtils.writeRGBATo(connexCompFile, currConnexeComp.getImageData(), "png");
                
        OCRGlyphConnexeComponent glyphConnexComp = new OCRGlyphConnexeComponent(currGlyphDescr, connexCompOffset, connexCompFilename);
        currGlyphDescr.addConnexComponent(glyphConnexComp);
    }
    
    private void onIgnoreGlyphAction() {
        modalDialog.setVisible(false);
    }

    
    
    @Override
    public void startScreenshotOCR(File ocrSettingsFile, OCRSettings ocrSettings, BufferedImage screenshotImg) {
        currentOCRSettingsFile = ocrSettingsFile;
        currentOCRSettings = ocrSettings;
        currentScreenshotImg = screenshotImg;

        this.renderImg = BufferedImageUtils.copyImage(currentScreenshotImg);
        imageCanvas.setImage(renderImg);
        imageCanvas.setPreferredSize(new Dimension(renderImg.getWidth(), renderImg.getHeight()));
        
        imageCanvas.invalidate();
        
        if (modalDialog == null) {
            Frame owner = (screenshotRecorderView != null)? (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, screenshotRecorderView.getJComponent()) : null;
            modalDialog = new JDialog(owner, "OCR: prompt for unrecognized Glyph connexe component", true);
            modalDialog.getContentPane().add(mainPanel);
        }
        modalDialog.invalidate();
        Dimension preferredSize = modalDialog.getPreferredSize();
        modalDialog.setSize(preferredSize);
    }

    @Override
    public void finishScreenshotOCR() {
        currentOCRSettings = null;
        currentScreenshotImg = null;
        currConnexeComp = null;
        if (modalDialog != null && modalDialog.isVisible()) {
            modalDialog.setVisible(false);
        }
    }

    @Override
    public void promptForGlyphConnexeComp(PtImageData connexeComp) {
        this.currConnexeComp = connexeComp;
        this.renderImg = BufferedImageUtils.copyImage(currentScreenshotImg);
        Graphics2DHelper g2d = new Graphics2DHelper(renderImg);
        g2d.setColorStroke(Color.RED, 2);
        g2d.drawRect(connexeComp.getRect().newDilate(2));

        imageCanvas.setImage(renderImg);
        
        modalDialog.setVisible(true);
        glyphDisplayNameField.requestFocus();        
    }
    
    
}

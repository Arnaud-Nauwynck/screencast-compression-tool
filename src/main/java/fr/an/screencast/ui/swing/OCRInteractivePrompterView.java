package fr.an.screencast.ui.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import fr.an.screencast.compressor.imgtool.ocr.OCRInteractivePrompter;
import fr.an.screencast.compressor.imgtool.ocr.settings.OCRSettings;
import fr.an.screencast.compressor.imgtool.utils.PtImageData;
import fr.an.screencast.ui.swing.internal.ImageCanvas;

public class OCRInteractivePrompterView implements OCRInteractivePrompter {
    
    private JDialog modalDialog;
    private JPanel mainPanel;
    private ImageCanvas imageCanvas;
    
    private JTextField glyphDisplayNameField;
    private JTextField glyphResultText;
    
    private JButton okButton;
    private JButton uncompleteGlyphButton;
    private JButton ignoreButton;
    
    // ------------------------------------------------------------------------
    
    public OCRInteractivePrompterView(Frame owner) {
        modalDialog = new JDialog(owner, "OCR: prompt for unrecognized Glyph connexe component", true);
        
        mainPanel = new JPanel(new BorderLayout());
        modalDialog.getContentPane().add(mainPanel);
        
        imageCanvas = new ImageCanvas();
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
        glyphResultText = new JTextField();
        answerPanel.add(glyphResultText, gbcField);
        gbcLabel.gridy++;
        gbcField.gridy++;
        
        okButton = new JButton("OK");
        okButton.addActionListener(e -> onOKAction());
        answerPanel.add(okButton, gbcField);
        gbcLabel.gridy++;
        gbcField.gridy++;
        
        uncompleteGlyphButton = new JButton("Uncomplete-Glyph");
        answerPanel.add(uncompleteGlyphButton, gbcField);
        gbcLabel.gridy++;
        gbcField.gridy++;
        
        ignoreButton = new JButton("Ignore");
        answerPanel.add(ignoreButton, gbcField);
        gbcLabel.gridy++;
        gbcField.gridy++;
    }

    // ------------------------------------------------------------------------
    
    private void onOKAction() {
        // TODO ..
        
        modalDialog.setVisible(false);
    }

    @Override
    public void promptForGlyphConnexeComp(OCRSettings ocrSettings, BufferedImage img, PtImageData connexeComp) {
        imageCanvas.setImage(img);
        imageCanvas.setSize(new Dimension(img.getWidth(), img.getHeight()));
        
        modalDialog.pack();
        modalDialog.setVisible(true);
    }
    
    
}

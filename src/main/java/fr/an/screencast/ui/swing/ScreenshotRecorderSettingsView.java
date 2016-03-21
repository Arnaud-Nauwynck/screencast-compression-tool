package fr.an.screencast.ui.swing;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import fr.an.screencast.recorder.ScreenshotRecorder;
import fr.an.screencast.ui.swing.internal.TransparentFrameScreenArea;

public class ScreenshotRecorderSettingsView {
    
    private ScreenshotRecorder model;
    
    private ScreenshotRecorderView screenshotRecorderView;
    
    private JPanel panel;
    
    private JButton newSessionButton;
    private JLabel text;

    private JPanel paramsPanel;
    private JTextField rectangleAreaField;
    private JButton revealRectButton;
    
    private JLabel baseFilenameLabel;
    private JTextField baseFilenameField;

    private JLabel outputDirLabel;
    private JTextField outputDirField;


    private JCheckBox enableOCRCheckbox;
    private JPanel ocrParamsPanel;
    private JLabel ocrSettingsLabel;
    private JTextField ocrSettingsField;

    private OCRInteractivePrompterView ocrInteractivePrompterView;

    // ------------------------------------------------------------------------

    public ScreenshotRecorderSettingsView(ScreenshotRecorder model) {
        this.model = model;
        panel = new JPanel(new BorderLayout());

        buildParamsPanel();

        panel.add(paramsPanel, BorderLayout.CENTER);
        
        newSessionButton = new JButton("Start Screenshots");
        newSessionButton.setActionCommand("new");
        newSessionButton.addActionListener(e -> onNewSessionAction());
        
        panel.add(newSessionButton, BorderLayout.SOUTH);

        modelToView();
    }

    private void buildParamsPanel() {
        paramsPanel = new JPanel(new GridBagLayout());
        
        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0; 
        gbcLabel.gridy = 0;
        gbcLabel.gridwidth = 1;
        gbcLabel.gridheight = 1;
        gbcLabel.weightx = 1.0;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbcLabel.fill = GridBagConstraints.NONE;
        gbcLabel.insets = new Insets(5, 5, 5, 5);

        GridBagConstraints gbcField = new GridBagConstraints();
        gbcField.gridx = 1; 
        gbcField.gridy = 0;
        gbcField.gridwidth = 1;
        gbcField.gridheight = 1;
        gbcField.anchor = GridBagConstraints.WEST;
        gbcField.fill = GridBagConstraints.HORIZONTAL;
        gbcField.weightx = 1.0;
        gbcField.insets = new Insets(5, 5, 5, 5);

        revealRectButton = new JButton("reveal area");
        revealRectButton.addActionListener(e -> onReavealAreaAction());
        paramsPanel.add(revealRectButton, gbcLabel);

        rectangleAreaField = new JTextField();
        paramsPanel.add(rectangleAreaField, gbcField);
        
        
        gbcLabel.gridy++;
        gbcField.gridy++;

        baseFilenameLabel = new JLabel("fileName");
        paramsPanel.add(baseFilenameLabel, gbcLabel);
        baseFilenameField = new JTextField();
        paramsPanel.add(baseFilenameField, gbcField);

        gbcLabel.gridy++;
        gbcField.gridy++;
        
        outputDirLabel = new JLabel("output dir");
        paramsPanel.add(outputDirLabel, gbcLabel);
        outputDirField = new JTextField();
        paramsPanel.add(outputDirField, gbcField);

        gbcLabel.gridy++;
        gbcField.gridy++;
        
        text = new JLabel("Ready to record");
        paramsPanel.add(text, gbcField);

        gbcField.gridx = 0;
        gbcField.gridwidth = 2;
        
        gbcField.gridx = 1;
        gbcField.gridwidth = 1;
        
        gbcLabel.gridy++;
        gbcField.gridy++;

        enableOCRCheckbox = new JCheckBox("enable OCR");
        
        
        enableOCRCheckbox.setSelected(true);
        enableOCRCheckbox.addActionListener((e) -> { 
            boolean vis = enableOCRCheckbox.isSelected();
            model.setEnableOCR(vis);
            ocrParamsPanel.setVisible(vis);
        });
        paramsPanel.add(enableOCRCheckbox, gbcLabel);
        gbcLabel.gridy++;
        gbcField.gridy++;

        ocrParamsPanel = new JPanel(new GridBagLayout());
        gbcLabel.gridwidth = 2;
        paramsPanel.add(ocrParamsPanel, gbcLabel);
        gbcLabel.gridwidth = 1;

        gbcLabel.gridy = 0;
        gbcField.gridy = 0;        
        ocrSettingsLabel = new JLabel("OCR settings");
        ocrParamsPanel.add(ocrSettingsLabel, gbcLabel);
        ocrSettingsField = new JTextField(model.getOcrSettingsFilename());
        ocrParamsPanel.add(ocrSettingsField, gbcField);

        gbcLabel.gridy++;
        gbcField.gridy++;
    }


    public void dispose() {
        if (model != null) {
            // model
        }
    }

    // ------------------------------------------------------------------------

    public JPanel getJComponent() {
        return panel;
    }

    
    
    protected TransparentFrameScreenArea recordAreaFrame;
    
    private void onReavealAreaAction() {
        if (recordAreaFrame == null) {
            recordAreaFrame = new TransparentFrameScreenArea();
            viewToModel();
            recordAreaFrame.setBounds(model.getRecordArea());
            recordAreaFrame.setVisible(true);
            recordAreaFrame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    updateModelRecordArea();
                }

                @Override
                public void componentMoved(ComponentEvent e) {
                    updateModelRecordArea();
                }

                protected void updateModelRecordArea() {
                    Rectangle r = recordAreaFrame.getBounds();
                    model.setRecordArea(r);
                    modelToView();
                }
            });
        } else {
            model.setRecordArea(recordAreaFrame.getBounds());
            recordAreaFrame.setVisible(false);
            recordAreaFrame.dispose();
            recordAreaFrame = null;
        }
    }

    private void onNewSessionAction() {
        viewToModel();
        boolean sessionActive = model.isActiveSession();
        if (!sessionActive) {
            Frame screenshotRecorderFrame = screenshotRecorderView != null? (Frame) SwingUtilities.getAncestorOfClass(Frame.class, screenshotRecorderView.getJComponent()) : null;
            if (screenshotRecorderFrame == null) {
                return;
            }
            
            boolean isEnableOCR = enableOCRCheckbox.isSelected();
            model.setEnableOCR(isEnableOCR);

            File outputDir = model.getOutputDir();
            if (outputDir == null) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.showOpenDialog(panel);
                outputDir = fileChooser.getSelectedFile();
                model.setOutputDir(outputDir);
            }
            String baseFileName = baseFilenameField.getText();
                        
            if (isEnableOCR) {
                model.setOcrSettingsFilename(ocrSettingsField.getText());
                String ocrFileName = baseFileName.replace("-$i", "").replace(".png", ".txt");
                model.setOcrResultFilename(ocrFileName);
            }
            
            boolean valid = outputDir != null; 

            if (valid && screenshotRecorderFrame != null) {
                Frame thisFrame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, panel);
                if (thisFrame != null) {
                    thisFrame.setVisible(false);
                }
                screenshotRecorderFrame.setVisible(true);
                
                model.setActiveSession(true);
                screenshotRecorderView.model2view_updateActive();
            }
        }
    }
    
    private void modelToView() {
        
        Rectangle r = model.getRecordArea();
        String rectText = "" + r.x + "," + r.y + "," + r.width + "," + r.height;
        rectangleAreaField.setText(rectText);

        File outputDir = model.getOutputDir();
        outputDirField.setText((outputDir != null)? outputDir.getPath() : "");
        
        baseFilenameField.setText(model.getBaseFilename());
    }
    
    private void viewToModel() {
        String[] rectCoords = rectangleAreaField.getText().split(",");
        Rectangle r = new Rectangle();
        r.x = Integer.parseInt(rectCoords[0]);
        r.y = Integer.parseInt(rectCoords[1]);
        r.width = Integer.parseInt(rectCoords[2]);
        r.height = Integer.parseInt(rectCoords[3]);
        model.setRecordArea(r);

        String outputDirText = outputDirField.getText();
        File outputDir = (outputDirText != null && !outputDirText.isEmpty())? new File(outputDirText) : null;
        model.setOutputDir(outputDir);
    }

    public void setScreenshotRecorderView(ScreenshotRecorderView screenshotRecorderView) {
        this.screenshotRecorderView = screenshotRecorderView;
    }

    
}

package fr.an.screencast.ui.swing;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import fr.an.screencast.recorder.ScreenshotRecorder;
import fr.an.screencast.ui.swing.internal.TransparentFrameScreenArea;

public class ScreenshotRecorderView {
    
    private ScreenshotRecorder model;
    
    private JPanel panel;
    
    private JButton newSessionButton;
    private JLabel text;
    private boolean sessionActive;

    private JPanel paramsPanel;
    private JTextField rectangleAreaField;
    private JButton revealRectButton;
    
    private JLabel baseFilenameLabel;
    private JTextField baseFilenameField;

    private JLabel outputDirLabel;
    private JTextField outputDirField;


    private JCheckBox enableOCRCheckbox;
    private JLabel ocrSettingsLabel;
    private JTextField ocrSettingsField;
    private OCRInteractivePrompterView ocrInteractivePrompterView;

    
    private JButton takeSnapshotButton;

    // ------------------------------------------------------------------------

    public static void main(String[] args) {
        try {
            ScreenshotRecorder model = new ScreenshotRecorder();
            Rectangle recordArea = model.getRecordArea();
            recordArea.width = recordArea.width / 2; 
            
            // temporary for debug
            recordArea.x = 1410; recordArea.y = 987; recordArea.width = 125; recordArea.height = 59;
            
            model.setRecordArea(recordArea);
            
            ScreenshotRecorderView app = new ScreenshotRecorderView(model);
            app.parseArgs(args);
            app.run();

            // System.out.println("Finished .. exiting");
        } catch (Exception ex) {
            System.err.println("Failed .. exiting");
        }
    }

    public void parseArgs(String[] args) {
        if (args.length >= 1) {
            if (args[0].equals("-white_cursor"))
                model.setUseWhiteCursor(true);
            else {
                System.out.println("Usage: java -cp .. " + ScreenshotRecorderView.class.getName() + " [OPTION]...");
                System.out.println("Start the screenhost recorder.");
                System.out.println("Options:   ");
                System.out.println("   -white_cursor   record with white cursor");
                System.exit(0);
            }
        }
    }

    public void run() {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame();
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    ScreenshotRecorderView.this.dispose();
                    frame.dispose();
                }
            });

            frame.getContentPane().add(getJComponent());
            frame.pack();
            frame.setVisible(true);
        });
    }

    public ScreenshotRecorderView(ScreenshotRecorder model) {
        this.model = model;
        panel = new JPanel(new BorderLayout());

        newSessionButton = new JButton("New Session");
        newSessionButton.setActionCommand("new");
        newSessionButton.addActionListener(e -> onNewSessionAction());

        buildParamsPanel();

        takeSnapshotButton = new JButton("Take Snapshot");
        takeSnapshotButton.setEnabled(false);
        takeSnapshotButton.setActionCommand("takeSnapshot");
        takeSnapshotButton.addActionListener(e -> onTakeSnapshotAction());

        
        panel.add(newSessionButton, BorderLayout.NORTH);
        panel.add(paramsPanel, BorderLayout.CENTER);
        panel.add(takeSnapshotButton, BorderLayout.SOUTH);

        modelToView();
    }

    private void buildParamsPanel() {
        paramsPanel = new JPanel(new GridBagLayout());
        
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

        
//        rectangleAreaLabel = new JLabel("area (x,y,w,h)");
//        panel.add(rectangleAreaLabel, gbcLabel);
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
            ocrSettingsLabel.setVisible(vis);
            ocrSettingsField.setVisible(vis);
        });
        paramsPanel.add(enableOCRCheckbox, gbcLabel);
        gbcLabel.gridy++;
        gbcField.gridy++;

        ocrSettingsLabel = new JLabel("OCR settings");
        paramsPanel.add(ocrSettingsLabel, gbcLabel);
        ocrSettingsField = new JTextField(model.getOcrSettingsFilename());
        paramsPanel.add(ocrSettingsField, gbcField);

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

    TransparentFrameScreenArea recordAreaFrame;
    
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
        sessionActive = !sessionActive;
        File outputDir = model.getOutputDir();
        if (sessionActive) {
            if (outputDir == null) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                fileChooser.showOpenDialog(panel);
                outputDir = fileChooser.getSelectedFile();
                model.setOutputDir(outputDir);
            }
            viewToModel();
            String baseFileName = baseFilenameField.getText();
            
            Frame frame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, panel);
            frame.setState(Frame.ICONIFIED);
            
            boolean isEnableOCR = enableOCRCheckbox.isSelected();
            model.setEnableOCR(isEnableOCR);
            if (isEnableOCR) {
                model.setOcrSettingsFilename(ocrSettingsField.getText());
                String ocrFileName = baseFileName.replace("-$i", "").replace(".png", ".txt");
                model.setOcrResultFilename(ocrFileName);
                
                if (ocrInteractivePrompterView == null) {
                    ocrInteractivePrompterView = new OCRInteractivePrompterView(frame);
                }
                model.setOcrInteractivePrompter(ocrInteractivePrompterView);
            }

            
            boolean valid = outputDir != null; 
            
            if (valid) {
                model.startSession(outputDir, baseFileName);
            }
        }
        
        takeSnapshotButton.setEnabled(sessionActive);

        rectangleAreaField.setVisible(!sessionActive);
        revealRectButton.setVisible(!sessionActive);
        
        baseFilenameLabel.setVisible(!sessionActive);
        baseFilenameField.setVisible(!sessionActive);

        outputDirLabel.setVisible(!sessionActive);
        outputDirField.setVisible(!sessionActive);
        
        enableOCRCheckbox.setVisible(!sessionActive);
        ocrSettingsLabel.setVisible(!sessionActive);
        ocrSettingsField.setVisible(!sessionActive);
        
        panel.invalidate();
        panel.validate();
        panel.repaint();
    }

    private void onTakeSnapshotAction() {
        if (model.getOutputDir() == null) {
            onNewSessionAction();
        }
        model.takeSnapshot();
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


}

package fr.an.screencast.ui.swing;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import fr.an.screencast.recorder.ScreenshotRecorder;

public class ScreenshotRecorderView {
    
    private ScreenshotRecorder model;
    
    private JPanel panel;
    
    private JButton editSettingsButton;
    
    private JButton takeSnapshotButton;

    private ScreenshotRecorderSettingsView screenshotRecorderSettingsView;
    
    // ------------------------------------------------------------------------

    public static void main(String[] args) {
        try {
            ScreenshotRecorder model = new ScreenshotRecorder();
            Rectangle recordArea = model.getRecordArea();
            recordArea.width = recordArea.width / 2; 
            
            // temporary for debug
            recordArea.x = 1410; recordArea.y = 987; recordArea.width = 125; recordArea.height = 59;
            
            model.setRecordArea(recordArea);
            
            ScreenshotRecorderView screenshotRecorderView = new ScreenshotRecorderView(model);
            ScreenshotRecorderSettingsView settingsView = new ScreenshotRecorderSettingsView(model);

            OCRInteractivePrompterView ocrInteractivePrompterView = new OCRInteractivePrompterView(screenshotRecorderView);
            model.setOcrInteractivePrompter(ocrInteractivePrompterView);
            
            screenshotRecorderView.setScreenshotRecorderSettingsView(settingsView);
            settingsView.setScreenshotRecorderView(screenshotRecorderView);
            
            screenshotRecorderView.parseArgs(args);
            screenshotRecorderView.run();

            model.setActiveSession(true);
            screenshotRecorderView.model2view_updateActive();
            
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
                System.out.println("Start the screenshot recorder.");
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

        editSettingsButton = new JButton("Edit Settings");
        editSettingsButton.setActionCommand("edit");
        editSettingsButton.addActionListener(e -> onEditSettingsAction());

        takeSnapshotButton = new JButton("Take Snapshot");
        takeSnapshotButton.setEnabled(false);
        takeSnapshotButton.setActionCommand("takeSnapshot");
        takeSnapshotButton.addActionListener(e -> onTakeSnapshotAction());
        
        panel.add(editSettingsButton, BorderLayout.NORTH);
        panel.add(takeSnapshotButton, BorderLayout.CENTER);

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
    
    public void setScreenshotRecorderSettingsView(ScreenshotRecorderSettingsView p) {
        this.screenshotRecorderSettingsView = p;
    }

    public void model2view_updateActive() {
        takeSnapshotButton.setEnabled(true);
    }

    private void onEditSettingsAction() {
        if (screenshotRecorderSettingsView == null) {
            return;//TODO?
        }
        JFrame settingsFrame = (screenshotRecorderSettingsView != null)? (JFrame) SwingUtilities.getAncestorOfClass(JFrame.class, screenshotRecorderSettingsView.getJComponent()) : null;
        if (settingsFrame == null && screenshotRecorderSettingsView != null) {
            settingsFrame = new JFrame();
            settingsFrame.getContentPane().add(screenshotRecorderSettingsView.getJComponent());
            settingsFrame.pack();
        }
        settingsFrame.setVisible(true);
        
        Frame thisFrame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, panel);
        thisFrame.setVisible(false);

        model.setActiveSession(false);
        takeSnapshotButton.setEnabled(false);
    }

    private void onTakeSnapshotAction() {
        if (model.getOutputDir() == null) {
            onEditSettingsAction();
        }
        model.takeSnapshot();
    }


}

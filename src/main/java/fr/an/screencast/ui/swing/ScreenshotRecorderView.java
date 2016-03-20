package fr.an.screencast.ui.swing;

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
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.recorder.ScreenshotRecorder;
import fr.an.screencast.ui.swing.internal.TransparentFrameScreenArea;

public class ScreenshotRecorderView {
    
    private static final Logger LOG = LoggerFactory.getLogger(ScreenshotRecorderView.class);
    
    private ScreenshotRecorder model;

    private JPanel panel;
    private JLabel rectangleAreaLabel;
    private JTextField rectangleAreaField;
    private JButton revealRectButton;
    
    private JLabel baseFilenameLabel;
    private JTextField baseFilenameField;
    
    private JButton newSessionButton;
    private JLabel text;

    private JButton takeSnapshotButton;

    private File outputFile;

    // ------------------------------------------------------------------------

    public static void main(String[] args) {
        try {
            ScreenshotRecorder model = new ScreenshotRecorder();
            Rectangle recordArea = model.getRecordArea();
            recordArea.width = recordArea.width / 2; 
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
        panel = new JPanel(new GridBagLayout());
        GridBagConstraints gbcLabel = new GridBagConstraints();
        // 0, 0, 1, 1, 1.0, 1.0, 0, GridBagConstraints.WEST);
        gbcLabel.gridx = 0; 
        gbcLabel.gridy = 0;
        gbcLabel.gridwidth = 1;
        gbcLabel.gridheight = 1;
        gbcLabel.anchor = GridBagConstraints.WEST;
        gbcLabel.insets = new Insets(5, 5, 5, 5);

        GridBagConstraints gbcField = new GridBagConstraints();
        // 0, 0, 1, 1, 1.0, 1.0, 0, GridBagConstraints.WEST);
        gbcField.gridx = 1; 
        gbcField.gridy = 0;
        gbcField.gridwidth = 1;
        gbcField.gridheight = 1;
        gbcField.anchor = GridBagConstraints.WEST;
        gbcField.insets = new Insets(5, 5, 5, 5);

        
        rectangleAreaLabel = new JLabel("area (x,y,w,h)");
        panel.add(rectangleAreaLabel, gbcLabel);

        rectangleAreaField = new JTextField();
        recordAreaModelToView();
        panel.add(rectangleAreaField, gbcField);

        revealRectButton = new JButton("reveal area");
        revealRectButton.addActionListener(e -> onReavealAreaAction());
        gbcField.gridx++;
        panel.add(revealRectButton, gbcField);
        gbcField.gridx--;
        
        
        gbcLabel.gridy++;
        gbcField.gridy++;

        baseFilenameLabel = new JLabel("fileName");
        panel.add(baseFilenameLabel, gbcLabel);
        baseFilenameField = new JTextField("screenshot-$i.png");
        panel.add(baseFilenameField, gbcField);

        gbcLabel.gridy++;
        gbcField.gridy++;
        
        
        newSessionButton = new JButton("New Session");
        newSessionButton.setActionCommand("new");
        newSessionButton.addActionListener(e -> onNewSessionAction());
        panel.add(newSessionButton, gbcLabel);

        text = new JLabel("Ready to record");
        panel.add(text, gbcField);

        gbcLabel.gridy++;
        gbcField.gridy++;

        takeSnapshotButton = new JButton("Take Snapshot");
        takeSnapshotButton.setActionCommand("takeSnapshot");
        takeSnapshotButton.addActionListener(e -> onTakeSnapshotAction());
        panel.add(takeSnapshotButton, gbcLabel);

    }

    private void recordAreaModelToView() {
        Rectangle r = model.getRecordArea();
        String rectText = "" + r.x + "," + r.y + "," + r.width + "," + r.height;
        rectangleAreaField.setText(rectText);
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
            viewToModelRecordArea();
            recordAreaFrame.setBounds(model.getRecordArea());
            recordAreaFrame.setVisible(true);
            recordAreaFrame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) { updateModelRecordArea(); }
                @Override
                public void componentMoved(ComponentEvent e) { updateModelRecordArea(); }
                
                protected void updateModelRecordArea() {
                    Rectangle r = recordAreaFrame.getBounds();
                    // LOG.debug("record area change: " + r);
                    model.setRecordArea(r);
                    recordAreaModelToView();
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
//        Frame frame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, panel);
//        frame.setState(Frame.ICONIFIED);

        if (outputFile == null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            fileChooser.showOpenDialog(panel);
            File target = fileChooser.getSelectedFile();

            this.outputFile = target;
        }
        if (outputFile != null) {
            viewToModelRecordArea();
            
            String baseFileName = baseFilenameField.getText();
            model.startSession(outputFile, baseFileName);
        }
    }

    private void viewToModelRecordArea() {
        String[] rectCoords = rectangleAreaField.getText().split(",");
        Rectangle r = new Rectangle();
        r.x = Integer.parseInt(rectCoords[0]);
        r.y = Integer.parseInt(rectCoords[1]);
        r.width = Integer.parseInt(rectCoords[2]);
        r.height = Integer.parseInt(rectCoords[3]);
        model.setRecordArea(r);
    }

    private void onTakeSnapshotAction() {
        if (outputFile == null) {
            onNewSessionAction();
        }
        model.takeSnapshot();
    }

}

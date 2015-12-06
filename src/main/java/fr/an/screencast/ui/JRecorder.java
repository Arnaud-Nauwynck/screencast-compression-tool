package fr.an.screencast.ui;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import fr.an.screencast.recorder.ScreenRecorder;
import fr.an.screencast.recorder.ScreenRecorderListener;

public class JRecorder implements ScreenRecorderListener, ActionListener {

    private ScreenRecorder screenRecorder = new ScreenRecorder(this);

    private JPanel panel;
    private JButton control;
    private JLabel text;

    private boolean shuttingDown = false;

    private int displayFrameCount = 0;

    private File outputFile;

    // ------------------------------------------------------------------------

    public static void main(String[] args) {
        try {
            JRecorder app = new JRecorder();
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
                screenRecorder.setUseWhiteCursor(true);
            else {
                System.out.println("Usage: java -cp .. " + JRecorder.class.getName() + " [OPTION]...");
                System.out.println("Start the screen recorder.");
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
                    JRecorder.this.dispose();
                    frame.dispose();
                }
            });

            JRecorder jRecorder = new JRecorder();
            frame.getContentPane().add(jRecorder.getJComponent());
            frame.pack();
            frame.setVisible(true);
        });
    }

    public JRecorder() {
        panel = new JPanel();
        control = new JButton("Start Recording");
        control.setActionCommand("start");
        control.addActionListener(this);
        panel.add(control, BorderLayout.WEST);

        text = new JLabel("Ready to record");
        panel.add(text, BorderLayout.SOUTH);
    }

    public void dispose() {
        shuttingDown = true;
        if (screenRecorder != null) {
            screenRecorder.stopRecording();
        }
    }

    // ------------------------------------------------------------------------

    public JPanel getJComponent() {
        return panel;
    }

    public void startRecording() {
        Frame frame = (Frame) SwingUtilities.getAncestorOfClass(Frame.class, panel);
        frame.setState(Frame.ICONIFIED);

        if (outputFile == null) {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.showSaveDialog(panel);
            File target = fileChooser.getSelectedFile();

            this.outputFile = target;
        }
        if (outputFile != null) {
            screenRecorder.startRecording(outputFile);
        }
    }

    public void actionPerformed(ActionEvent ev) {
        if (ev.getActionCommand().equals("start")) {
            if (!screenRecorder.isRecording()) {
                try {
                    startRecording();
                    control.setActionCommand("stop");
                    control.setText("Stop Recording");
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } else if (ev.getActionCommand().equals("stop")) {
            if (screenRecorder.isRecording()) {
                text.setText("Stopping");
                screenRecorder.stopRecording();
                this.outputFile = null;
                // recordingStopped();
            }
        }
    }

    public void frameRecorded(boolean fullFrame) {
        this.displayFrameCount++;
        if (text != null) {
            text.setText("Frame: " + displayFrameCount);
        }
    }

    public void recordingStopped() {
        if (!shuttingDown) {
            if (screenRecorder.isRecording()) {
                screenRecorder.stopRecording();
                this.outputFile = null;
            }

            this.displayFrameCount = 0;

            control.setActionCommand("start");
            control.setText("Start Recording");

            text.setText("Ready to record");
        }
    }

}

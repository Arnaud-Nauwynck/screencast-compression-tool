package fr.an.screencast.ui.internal;

import java.awt.Color;
import java.awt.GridLayout;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;

import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.player.ScreenPlayerListener.ScreenPlayerListenerAdapter;
import fr.an.screencast.player.VideoStreamPlayer;

public class PlayerStatusBar {

    /** non ui model */
    private VideoStreamPlayer model;
    
    private JPanel mainPanel;
    private JLabel text;
    private JLabel frameLabel;

    private InnerScreenPlayerListener innerListener = new InnerScreenPlayerListener();
    
    // ------------------------------------------------------------------------

    public PlayerStatusBar(VideoStreamPlayer model) {
        this.model = model;
        initComponents();
        model.addListener(innerListener);
    }

    // ------------------------------------------------------------------------
    
    public JComponent getJComponent() {
        return mainPanel;
    }
    
    private void initComponents() {
        mainPanel = new JPanel(new GridLayout(1, 2));
        mainPanel.setBackground(Color.black);

        frameLabel = new JLabel("Frame: 0 Time: 0");
        frameLabel.setBackground(Color.black);
        frameLabel.setForeground(Color.red);
        
        text = new JLabel("No recording selected");
        text.setBackground(Color.black);
        text.setForeground(Color.red);

        mainPanel.add(text);
        mainPanel.add(frameLabel);
    }

    private class InnerScreenPlayerListener extends ScreenPlayerListenerAdapter { 
        
        @Override
        public void onInit(Dim dim) {
            text.setText("Ready to play " + model.getInputFile());
        }

        @Override
        public void onPlayerPlay() {
            text.setText("Playing " + model.getInputFile());
        }

        @Override
        public void onPlayerPlayFastForward() {
            text.setText("Fast Forward " + model.getInputFile());
        }

        @Override
        public void onPlayerStopped() {
            text.setText("No recording selected");
        }

        @Override
        public void onPlayerPaused() {
            text.setText("Finished playing " + model.getInputFile());
        }
        
        @Override
        public void onPlayerReset() {
            text.setText("Ready to play " + model.getInputFile());
        }

        @Override
        public void newFrame() {
            long frameCount = model.getFrameCount();
            long time = model.getFrameTime();
            long timeSeconds = time / 1000;
            long milliseconds = time - 1000*timeSeconds;
            frameLabel.setText("Frame: " + frameCount + " Time: " + timeSeconds + "." + milliseconds);
        }

    }

}

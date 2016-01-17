package fr.an.screencast.ui.swing.internal;

import java.awt.Color;
import java.awt.GridLayout;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.UIManager;

import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.player.ScreenPlayerListener.ScreenPlayerListenerAdapter;
import fr.an.screencast.player.VideoStreamPlayer;

public class PlayerToolbar {

    /** non ui model */
    private VideoStreamPlayer model;

    private JPanel toolbarPanel;
    
    private JButton open;
    private JButton stepButton;
    private JButton play;
    private JButton fastForward;
    private JButton pause;
    private JButton stop;

    private Color activeButtonColor = new Color(248, 229, 179);

    private InnerScreenPlayerListener innerListener = new InnerScreenPlayerListener();
    
    // ------------------------------------------------------------------------

    public PlayerToolbar(VideoStreamPlayer screenPlayer) {
        this.model = screenPlayer;
        initComponents();
        screenPlayer.addListener(innerListener);
    }

    // ------------------------------------------------------------------------

    public JComponent getJComponent() {
        return toolbarPanel;
    }
    
    private void initComponents() {
        this.toolbarPanel = new JPanel(new GridLayout(1, 6));

        open = new JButton("Open Recording");
        open.setActionCommand("open");
        open.addActionListener(e -> onActionOpenFile());

        stepButton = new JButton("Step");
        stepButton.setActionCommand("step");
        stepButton.setEnabled(false);
        stepButton.addActionListener(e -> onActionStep());

        play = new JButton("Play");
        play.setActionCommand("play");
        play.setEnabled(false);
        play.addActionListener(e -> onActionPlay());

        fastForward = new JButton("Fast Forward");
        fastForward.setActionCommand("fastForward");
        fastForward.setEnabled(false);
        fastForward.addActionListener(e -> onActionFastForward());

        pause = new JButton("Pause");
        pause.setActionCommand("pause");
        pause.setEnabled(false);
        pause.addActionListener(e -> onActionPause());

        stop = new JButton("Stop");
        stop.setActionCommand("stop");
        stop.setEnabled(false);
        stop.addActionListener(e -> onActionStop());

        toolbarPanel.add(open);
        toolbarPanel.add(stepButton);
        toolbarPanel.add(play);
        toolbarPanel.add(fastForward);
        toolbarPanel.add(pause);
        toolbarPanel.add(stop);
    }
    

    private void onActionOpenFile() {
        UIManager.put("FileChooser.readOnly", true);
        JFileChooser fileChooser = new JFileChooser();
        File inputFile = model.getInputFile();
        if (inputFile != null) {
            fileChooser.setSelectedFile(inputFile);
        }
        fileChooser.showOpenDialog(toolbarPanel);
        if (fileChooser.getSelectedFile() != null) {
            model.setInputFile(fileChooser.getSelectedFile());

            init();
        }
    }

    public void init() {
        if (model.getInputFile() != null) {
            model.init();
        }
    }

    public void onActionStep() {
        model.step();
    }

    public void onActionPlay() {
        model.play();
    }

    public void onActionFastForward() {
        model.fastforward();
    }

    public void onActionPause() {
        model.pause();
    }

    public void onActionStop() {
        model.stop();
    }

    private class InnerScreenPlayerListener extends ScreenPlayerListenerAdapter { 
        
        @Override
        public void onInit(Dim dim) {
            open.setEnabled(false);
            open.setBackground(null);

            stepButton.setEnabled(true);
            stepButton.setBackground(null);

            play.setEnabled(true);
            play.setBackground(null);

            fastForward.setEnabled(true);
            fastForward.setBackground(null);

            pause.setEnabled(false);
            pause.setBackground(activeButtonColor);

            stop.setEnabled(true);
            stop.setBackground(null);
        }

        @Override
        public void onPlayerPlay() {
            open.setEnabled(false);
            open.setBackground(null);

            stepButton.setEnabled(true);
            stepButton.setBackground(null);

            play.setEnabled(false);
            play.setBackground(activeButtonColor);

            fastForward.setEnabled(true);
            fastForward.setBackground(null);

            pause.setEnabled(true);
            pause.setBackground(null);

            stop.setEnabled(true);
            stop.setBackground(null);
        }

        @Override
        public void onPlayerPlayFastForward() {
            open.setEnabled(false);
            open.setBackground(null);

            stepButton.setEnabled(true);
            stepButton.setBackground(null);

            play.setEnabled(true);
            play.setBackground(null);

            fastForward.setEnabled(false);
            fastForward.setBackground(activeButtonColor);

            pause.setEnabled(true);
            pause.setBackground(null);

            stop.setEnabled(true);
            stop.setBackground(null);
         }


        public void onPlayerStopped() {
            open.setEnabled(true);
            open.setBackground(null);

            stepButton.setEnabled(false);
            stepButton.setBackground(null);

            play.setEnabled(false);
            play.setBackground(null);

            fastForward.setEnabled(false);
            fastForward.setBackground(null);

            pause.setEnabled(false);
            pause.setBackground(null);

            stop.setEnabled(false);
            stop.setBackground(null);
        }

        public void onPlayerPaused() {
            open.setEnabled(false);
            open.setBackground(null);

            stepButton.setEnabled(true);
            stepButton.setBackground(null);

            play.setEnabled(true);
            play.setBackground(null);

            fastForward.setEnabled(true);
            fastForward.setBackground(null);

            pause.setEnabled(false);
            pause.setBackground(activeButtonColor);
            
            stop.setEnabled(true);
            stop.setBackground(null);
        }

        
        @Override
        public void onPlayerReset() {
            open.setEnabled(false);
            open.setBackground(null);

            stepButton.setEnabled(true);
            stepButton.setBackground(null);

            play.setEnabled(true);
            play.setBackground(null);

            fastForward.setEnabled(true);
            fastForward.setBackground(null);

            pause.setEnabled(true);
            pause.setBackground(null);

            stop.setEnabled(true);
            stop.setBackground(null);
        }

    }
}

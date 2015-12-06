package fr.an.screencast.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import fr.an.screencast.compressor.imgstream.VideoStreamFactory;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.player.ScreenPlayer;
import fr.an.screencast.player.ScreenPlayerListener;
import fr.an.screencast.ui.internal.ImageCanvas;

public class JPlayer {

    private VideoStreamFactory videoStreamFactory = VideoStreamFactory.getDefaultInstance();
    private ScreenPlayer screenPlayer;

    private JPanel mainPanel;
    private JScrollPane scrollPaneImage;
    private ImageCanvas imageCanvas;

    private JButton open;
    private JButton stepButton;
    private JButton play;
    private JButton fastForward;
    private JButton pause;
    private JButton stop;

    private JLabel text;
    private JLabel frameLabel;

    private File inputFile;
    private int frameCount;
    private long startTime;

    private Color activeButtonColor = new Color(248, 229, 179);

    private InnerScreenPlayerListener innerPlayerLister = new InnerScreenPlayerListener();
    
    // ------------------------------------------------------------------------

    public static void main(String[] args) {
        JPlayer app = new JPlayer();
        if (args.length == 1) {
            app.inputFile = new File(args[0]);
            app.open();
        }

        SwingUtilities.invokeLater(()-> {
            JFrame frame = new JFrame();
            // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    if (app != null) {
                        app.stop();
                    }
                    frame.dispose();
                }
            });

            frame.getContentPane().add(app.mainPanel);

            frame.pack();
            frame.setVisible(true);
        });

    }

    public JPlayer() {
        initComponents();
    }
    
    // ------------------------------------------------------------------------

    private void initComponents() {
        this.mainPanel = new JPanel(new BorderLayout());
        
        { 
            JPanel toolbarPanel = new JPanel();
            toolbarPanel.setLayout(new GridLayout(1, 6));
    
    
            open = new JButton("Open Recording");
            open.setActionCommand("open");
            open.addActionListener(e -> onActionOpenRecording());
    
            stepButton = new JButton("Step");
            stepButton.setActionCommand("step");
            stepButton.setEnabled(false);
            stepButton.addActionListener(e -> onActionStep());
    
            play = new JButton("Play");
            play.setActionCommand("play");
            play.setEnabled(false);
            play.addActionListener(e -> play());
    
            fastForward = new JButton("Fast Forward");
            fastForward.setActionCommand("fastForward");
            fastForward.setEnabled(false);
            fastForward.addActionListener(e -> fastForward());
    
            pause = new JButton("Pause");
            pause.setActionCommand("pause");
            pause.setEnabled(false);
            pause.addActionListener(e -> pause());
    
            stop = new JButton("Stop");
            stop.setActionCommand("stop");
            stop.setEnabled(false);
            stop.addActionListener(e -> stop());
    
            toolbarPanel.add(open);
            toolbarPanel.add(stepButton);
            toolbarPanel.add(play);
            toolbarPanel.add(fastForward);
            toolbarPanel.add(pause);
            toolbarPanel.add(stop);
    
            mainPanel.add(toolbarPanel, BorderLayout.NORTH);
        }
        
        {
            this.imageCanvas = new ImageCanvas();
            double ratio = .5;
            Dimension prefDim = new Dimension((int) (1900*ratio), (int) (1080*ratio));
            // imageCanvas.setSize(prefDim);
            scrollPaneImage = new JScrollPane(imageCanvas);
            scrollPaneImage.setPreferredSize(prefDim);
            
            mainPanel.add(scrollPaneImage, BorderLayout.CENTER);
        }
        
        { 
            JPanel statusPanel = new JPanel(new GridLayout(1, 2));
            statusPanel.setBackground(Color.black);
    
            frameLabel = new JLabel("Frame: 0 Time: 0");
            frameLabel.setBackground(Color.black);
            frameLabel.setForeground(Color.red);
            text = new JLabel("No recording selected");
            text.setBackground(Color.black);
            text.setForeground(Color.red);
    
            statusPanel.add(text);
            statusPanel.add(frameLabel);
            mainPanel.add(statusPanel, BorderLayout.SOUTH);
        }
    }

    private void onActionOpenRecording() {
        UIManager.put("FileChooser.readOnly", true);
        JFileChooser fileChooser = new JFileChooser();
        if (inputFile != null) {
            fileChooser.setSelectedFile(inputFile);
        }
        fileChooser.showOpenDialog(mainPanel);
        if (fileChooser.getSelectedFile() != null) {
            inputFile = fileChooser.getSelectedFile();

            open();
        }
    }
    

    public void open() {
        if (inputFile != null) {
            try {
                screenPlayer = new ScreenPlayer(innerPlayerLister, videoStreamFactory, inputFile);
                frameCount = 0;
            } catch (Exception e) {
                throw new RuntimeException("Failed", e);
            }
        }

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

        text.setText("Ready to play " + inputFile);
    }

    public void onActionStep() {
        screenPlayer.step();
    }

    public void play() {
        screenPlayer.play();
    }

    public void fastForward() {
        screenPlayer.fastforward();
    }

    public void pause() {
        screenPlayer.pause();
    }

    public void stop() {
        screenPlayer.stop();
    }

    private class InnerScreenPlayerListener implements ScreenPlayerListener { 
        
        @Override
        public void onInit(Dim dim) {
            imageCanvas.setSize(dim.width, dim.height);
            imageCanvas.setPreferredSize(new Dimension(dim.width, dim.height));
            // scrollPaneImage.setSize(dim.width, dim.height);
            imageCanvas.invalidate();
        }

        @Override
        public void onPlayerPlay() {
            open.setEnabled(false);
            open.setBackground(null);

            stepButton.setEnabled(false);
            stepButton.setBackground(null);

            play.setEnabled(false);
            play.setBackground(activeButtonColor);

            fastForward.setEnabled(true);
            fastForward.setBackground(null);

            pause.setEnabled(true);
            pause.setBackground(null);

            stop.setEnabled(true);
            stop.setBackground(null);

            startTime = System.currentTimeMillis();

            text.setText("Playing " + inputFile);
        }

        @Override
        public void onPlayerPlayFastForward() {
            open.setEnabled(false);
            open.setBackground(null);

            stepButton.setEnabled(false);
            stepButton.setBackground(null);

            play.setEnabled(true);
            play.setBackground(null);

            fastForward.setEnabled(false);
            fastForward.setBackground(activeButtonColor);

            pause.setEnabled(true);
            pause.setBackground(null);

            stop.setEnabled(true);
            stop.setBackground(null);

            text.setText("Fast Forward " + inputFile);
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

            text.setText("No recording selected");
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

            text.setText("Finished playing " + inputFile);
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

            frameCount = 0;
            startTime = System.currentTimeMillis();

            text.setText("Ready to play " + inputFile);
        }

        public void showNewImage(Image image) {
            imageCanvas.setImage(image);
        }

        public void newFrame() {
            frameCount++;
            long time = System.currentTimeMillis() - startTime;
            String seconds = "" + time / 1000;
            String milliseconds = String.format("%04d", time % 1000);
            frameLabel.setText("Frame: " + frameCount + " Time: " + seconds + "." + milliseconds);
        }


    }
}

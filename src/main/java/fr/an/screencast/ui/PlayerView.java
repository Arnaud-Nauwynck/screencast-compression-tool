package fr.an.screencast.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;

import fr.an.screencast.compressor.imgstream.VideoStreamFactory;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.player.ScreenPlayerListener.ScreenPlayerListenerAdapter;
import fr.an.screencast.player.VideoStreamPlayer;
import fr.an.screencast.ui.internal.ImageCanvas;
import fr.an.screencast.ui.internal.PlayerStatusBar;
import fr.an.screencast.ui.internal.PlayerToolbar;

public class PlayerView {

    /** non-ui model */
    private VideoStreamPlayer model;
    
    private JPanel mainPanel;
    
    private PlayerToolbar playerToolbar;
    private JScrollPane scrollPaneImage;
    private ImageCanvas imageCanvas;
    private PlayerStatusBar statusBar;

    private InnerScreenPlayerListener innerPlayerLister = new InnerScreenPlayerListener();
    
    // ------------------------------------------------------------------------

    public static void main(final String[] args) {
        SwingUtilities.invokeLater(() -> {
            VideoStreamPlayer model = new VideoStreamPlayer(VideoStreamFactory.getDefaultInstance());
            PlayerView view = new PlayerView(model);
            if (args.length == 1) {
                model.setInputFile(new File(args[0]));
                model.init();
            }
            
            JFrame frame = new JFrame();
            // frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.addWindowListener(new WindowAdapter() {
                public void windowClosing(WindowEvent e) {
                    view.dispose();
                    frame.dispose();
                    System.exit(0);
                }
            });

            frame.getContentPane().add(view.mainPanel);
            frame.pack();
            frame.setVisible(true);
        });
    }

    public PlayerView(VideoStreamPlayer model) {
        this.model = model;
        initComponents();
        model.addListener(innerPlayerLister);
    }
    
    public void dispose() {
        model.stop();
    }
    // ------------------------------------------------------------------------

    private void initComponents() {
        this.mainPanel = new JPanel(new BorderLayout());
        
        this.playerToolbar = new PlayerToolbar(model);
        mainPanel.add(playerToolbar.getJComponent(), BorderLayout.NORTH);
        
        this.imageCanvas = new ImageCanvas();
        double ratio = .5;
        Dimension prefDim = new Dimension((int) (1900*ratio), (int) (1080*ratio));
        scrollPaneImage = new JScrollPane(imageCanvas);
        scrollPaneImage.setPreferredSize(prefDim);
        
        mainPanel.add(scrollPaneImage, BorderLayout.CENTER);
        
        statusBar = new PlayerStatusBar(model);
        mainPanel.add(statusBar.getJComponent(), BorderLayout.SOUTH);
    }

    
    
    private class InnerScreenPlayerListener extends ScreenPlayerListenerAdapter { 
        
        @Override
        public void onInit(Dim dim) {
            imageCanvas.setSize(dim.width, dim.height);
            imageCanvas.setPreferredSize(new Dimension(dim.width, dim.height));
            // scrollPaneImage.setSize(dim.width, dim.height);
            imageCanvas.invalidate();
        }

        public void showNewImage(Image image) {
            imageCanvas.setImage(image);
        }

    }

}

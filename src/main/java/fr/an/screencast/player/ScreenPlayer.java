package fr.an.screencast.player;

import java.awt.image.BufferedImage;
import java.io.File;

import fr.an.screencast.compressor.imgstream.VideoInputStream;
import fr.an.screencast.compressor.imgstream.VideoStreamFactory;
import fr.an.screencast.compressor.utils.Dim;

public class ScreenPlayer {

    private ScreenPlayerListener listener;
    
    private VideoStreamFactory videoStreamFactory;
    
    private File inputFile;

    private VideoInputStream videoInputStream;

    private long startTime;
    private long frameTime;
    private long lastFrameTime;

    private boolean running;
    private boolean paused;
    private boolean fastForward;

    // ------------------------------------------------------------------------

    public ScreenPlayer(ScreenPlayerListener listener, VideoStreamFactory videoStreamFactory, File inputFile) {
        this.listener = listener;
        this.videoStreamFactory = videoStreamFactory;
        this.inputFile = inputFile;
    }

    // ------------------------------------------------------------------------
    
    public void init() {
        this.videoInputStream = videoStreamFactory.createVideoInputStream(inputFile);
        
        paused = true;

        videoInputStream.init();
        
        Dim dim = videoInputStream.getDim();
        listener.onInit(dim);
        
        startTime = System.currentTimeMillis();
        frameTime = 0; // startTime;
        lastFrameTime = 0; // startTime;
    }

    public void play() {
        if (videoInputStream == null) {
            init();
        }
        fastForward = false;
        paused = false;

        startTime = System.currentTimeMillis() - frameTime;
        
        if (running == false) {
            new Thread(() -> runLoop(), "Screen Player").start();
        }
        
        listener.onPlayerPlay();
    }

    public void fastforward() {
        fastForward = true;
        paused = false;
        listener.onPlayerPlayFastForward();
    }

    public void pause() {
        paused = true;
        listener.onPlayerPaused();
    }

    public void stop() {
        paused = false;
        running = false;
        listener.onPlayerStopped();
    }

    public void step() {
        if (running && paused) {
            readFrame();
        }
    }
    
    private void runLoop() {
        running = true;
        while (running) {

            if (paused) {
                while (paused) {
                    try {
                        Thread.sleep(50);
                    } catch (Exception e) {
                    }
                }
                // resuming from pause
                startTime = System.currentTimeMillis() - frameTime;
            }
            
            readFrame();

            if (fastForward == true) {
                startTime -= (frameTime - lastFrameTime);
            } else {
                long presentationTime = startTime + frameTime;
                long time = System.currentTimeMillis();
                long sleep = presentationTime-time; 
                while (!paused && sleep > 100) {
                    try {
                        Thread.sleep(100);
                    } catch (Exception e) {
                    }
                    
                    time = System.currentTimeMillis();
                    sleep = presentationTime-time;
                }

                // System.out.println(
                // "FrameTime:"+frameTime+">"+(System.currentTimeMillis()-startTime));
            }

            lastFrameTime = frameTime;
        }

        listener.onPlayerStopped();
    }

    private void readFrame() {
        boolean hasNext = videoInputStream.readNextImage();
        if (!hasNext) {
            listener.onPlayerStopped();
            return;
        }
        
        this.frameTime = videoInputStream.getPresentationTimestamp();
        BufferedImage image = videoInputStream.getImage();
        
        listener.showNewImage(image);

        listener.newFrame(); // ??
    }
}

package fr.an.screencast.player;

import java.awt.image.BufferedImage;
import java.io.File;

import fr.an.screencast.compressor.imgstream.VideoInputStream;
import fr.an.screencast.compressor.imgstream.VideoStreamFactory;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.player.ScreenPlayerListener.ScreenPlayerListenerSupport;

public class VideoStreamPlayer {

    private VideoStreamFactory videoStreamFactory;
    
    private File inputFile;

    private VideoInputStream videoInputStream;

    private int frameCount;
    private long startTime;
    private long frameTime;
    private long lastFrameTime;

    private volatile boolean running;
    private volatile boolean shouldStop = false;
    private volatile int pauseAfterCount = -1;
    private boolean fastForward;

    private ScreenPlayerListenerSupport listeners = new ScreenPlayerListenerSupport();
    
    // ------------------------------------------------------------------------

    public VideoStreamPlayer(VideoStreamFactory videoStreamFactory) {
        this.videoStreamFactory = videoStreamFactory;
    }

    // ------------------------------------------------------------------------

    public void addListener(ScreenPlayerListener listener) {
        listeners.addListener(listener);
    }
    public void removeListener(ScreenPlayerListener listener) {
        listeners.removeListener(listener);
    }
    
    public File getInputFile() {
        return inputFile;
    }
    
    public void setInputFile(File inputFile) {
        this.inputFile = inputFile;
    }

    public void init() {
        this.videoInputStream = videoStreamFactory.createVideoInputStream(inputFile);
        
        videoInputStream.init();
        
        frameCount = 0;
        startTime = System.currentTimeMillis();
        frameTime = startTime;
        lastFrameTime = startTime;
        
        running = true;
        pauseAfterCount = 0; // 1
        new Thread(() -> runLoop(), "Screen Player").start();
        
        Dim dim = videoInputStream.getDim();
        listeners.onInit(dim);
        
    }

    public void stop() {
        shouldStop = true;
        pauseAfterCount = -1;
        while (running) {
            try {
                Thread.sleep(50);
            } catch (Exception e) {
            }
        }
        
        if (videoInputStream != null) {
            videoInputStream.close();
            videoInputStream = null;
        }
        frameCount = 0;
        listeners.onPlayerStopped();
    }

    public void play() {
        if (videoInputStream == null) {
            return;
        }
        if (running != true || fastForward != false || pauseAfterCount != -1) {
            running = true;
            fastForward = false;
            pauseAfterCount = -1;
            
            startTime = System.currentTimeMillis() - frameTime;
            listeners.onPlayerPlay();
        }
    }

    
    public boolean isFastForward() {
        return fastForward;
    }
    
    public void fastforward() {
        if (videoInputStream == null) {
            return;
        }
        if (running != true || fastForward != true || pauseAfterCount != -1) {
            running = true;
            fastForward = true;
            pauseAfterCount = -1;
            
            startTime = System.currentTimeMillis() - frameTime;
            listeners.onPlayerPlayFastForward();
        }
    }

    public void pause() {
        pauseAfterCount = 1;
    }

    public void step() {
        if (videoInputStream == null) {
            return;
        }
        if (running != true || pauseAfterCount != 1) {
            running = true;
            pauseAfterCount = 1;
        }
    }
    
    private void runLoop() {
        while (!shouldStop) {

            if (pauseAfterCount == 0) {
                listeners.onPlayerPaused();

                while (pauseAfterCount == 0) {
                    try {
                        Thread.sleep(50);
                    } catch (Exception e) {
                    }
                }
                // resuming from pause
                startTime = System.currentTimeMillis() - frameTime;
                // listeners.onPlayerPlay();
            }
            
            readFrame();

            if (pauseAfterCount > 0) {
                pauseAfterCount--;
            }
            
            if (fastForward == true) {
                startTime -= (frameTime - lastFrameTime);
            } else {
                long presentationTime = startTime + frameTime;
                long time = System.currentTimeMillis();
                long sleep = presentationTime-time; 
                while (sleep > 100) {
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
        
        running = false;
        shouldStop = false;
        listeners.onPlayerStopped();
    }

    private void readFrame() {
        if (videoInputStream == null) {
            return; // should not occur
        }
        boolean hasNext = videoInputStream.readNextImage();
        if (!hasNext) {
            running = false;
            shouldStop = true;
            listeners.onPlayerStopped();
            return;
        }
        
        this.frameCount++;
        this.frameTime = videoInputStream.getPresentationTimestamp();
        BufferedImage image = videoInputStream.getImage();
        
        listeners.showNewImage(image);

        listeners.newFrame(); // ??
    }
    
    public int getFrameCount() {
        return frameCount;
    }

    public long getFrameTime() {
        return frameTime;
    }

    public long getFramePresentationTime() {
        return startTime + frameTime;
    }
    
}

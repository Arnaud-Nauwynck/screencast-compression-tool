package fr.an.screencast.recorder;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.LinkedList;
import java.util.Queue;

import fr.an.screencast.compressor.imgstream.VideoOutputStream;
import fr.an.screencast.compressor.utils.Dim;

public class VideoOutputScreenRecorderWorker {

    private VideoOutputStream videoOutput;

    private Rectangle recordArea;

    private DesktopScreenSnaphotProvider screenSnaphostProvider;
    

    private class DataPack {
        public DataPack(int[] newData, long frameTime) {
            this.newData = newData;
            this.frameTime = frameTime;
        }
        public long frameTime;
        public int[] newData;
    }
    private Queue<DataPack> videoOutputEncoderQueue = new LinkedList<DataPack>();
    
    private ScreenRecorderListener listener;

    
    private int frameSize;
    private int[] rawData;

    private volatile boolean recording = false;
    private boolean running = false;

    private long startTime;
    private long frameTime;
    private boolean reset;
    

    // ------------------------------------------------------------------------

    public VideoOutputScreenRecorderWorker(VideoOutputStream videoOutput,
            Rectangle recordArea, 
            DesktopScreenSnaphotProvider screenSnaphostProvider,
            ScreenRecorderListener listener) {
        this.videoOutput = videoOutput;
        this.recordArea = recordArea;
        this.screenSnaphostProvider = screenSnaphostProvider;
        this.listener = listener;
    }

    // ------------------------------------------------------------------------

    public int getFrameSize() {
        return frameSize;
    }

    public void triggerRecordingStop() {
        recording = false;
    }

    public void startRecording() {
        if (recordArea == null) {
            return;
        }

        Dim dim = new Dim(recordArea.width, recordArea.height);
        videoOutput.init(dim);
        
        startTime = System.currentTimeMillis();

        recording = true;
        running = true;

        frameSize = recordArea.width * recordArea.height;

        new Thread(() -> runEncoderQueueComsumerLoop(), "Stream Packer").start();

        new Thread(() -> runScreenSnapshotLoop(), "Screen Recorder").start();
    }

    public void stopRecording() {
        triggerRecordingStop();

        int count = 0;
        while (running == true && count < 10) {
            try {
                Thread.sleep(100);
            } catch (Exception e) {
            }
            count++;
        }
    }

    public boolean isRecording() {
        return recording;
    }
    
    private void runScreenSnapshotLoop() {
        try {
            long lastFrameTime = 0;
            long time = 0;
            while (recording) {
                time = System.currentTimeMillis();
                while (time - lastFrameTime < 190) {
                    try {
                        Thread.sleep(10);
                    } catch (Exception e) {
                    }
                    time = System.currentTimeMillis();
                }
                lastFrameTime = time;

                try {
                    recordFrame();
                } catch (Exception e) {
                    break;
                }
            }
            
        } catch (Exception ex) {
            throw new RuntimeException("Failed", ex);
        }

        running = false;
        recording = false;

        listener.recordingStopped();
    }

    
    private void recordFrame() throws IOException {
        BufferedImage bImage = screenSnaphostProvider.captureScreen(recordArea);
        Point mousePos = screenSnaphostProvider.captureMouseLocation();
        frameTime = System.currentTimeMillis() - startTime;

        screenSnaphostProvider.paintMouseInScreenCapture(bImage, mousePos);

        rawData = new int[frameSize];

        bImage.getRGB(0, 0, recordArea.width, recordArea.height, rawData, 0, recordArea.width);
        // long t3 = System.currentTimeMillis();

        enqueuePacketToEncoder(new DataPack(rawData, frameTime));

        // System.out.println("Times");
        // System.out.println(" capture time:"+(t2-t1));
        // System.out.println(" data grab time:"+(t3-t2));

        listener.frameRecorded(false);
    }

    private void enqueuePacketToEncoder(DataPack pack) {
        while (videoOutputEncoderQueue.size() > 2) {
            try {
                Thread.sleep(10);
            } catch (Exception e) {
            }
        }
        videoOutputEncoderQueue.add(pack);
    }

    private void runEncoderQueueComsumerLoop() {
        while (recording) {
            while (videoOutputEncoderQueue.isEmpty() == false) {
                DataPack pack = videoOutputEncoderQueue.poll();

                try {
                    // long t1 = System.currentTimeMillis();
                    videoOutput.addFrame(pack.frameTime, pack.newData);
                    // long t2 = System.currentTimeMillis();
                    // System.out.println(" pack time:"+(t2-t1));

                    if (reset == true) {
                        reset = false;
                    }
                } catch (Exception e) {
                    recording = false; // abort recording!
                    e.printStackTrace();
                    return;
                }
            }
            while (videoOutputEncoderQueue.isEmpty() == true) {
                try {
                    Thread.sleep(50);
                } catch (Exception e) {
                }
            }
        }
    }
    
}

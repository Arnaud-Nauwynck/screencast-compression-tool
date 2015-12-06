package fr.an.screencast.recorder;

import java.awt.Rectangle;
import java.io.File;

import fr.an.screencast.compressor.imgstream.VideoOutputStream;
import fr.an.screencast.compressor.imgstream.VideoStreamFactory;

public class ScreenRecorder {

    private VideoStreamFactory videoStreamFactory = new VideoStreamFactory();
    
    private boolean useWhiteCursor;

    private VideoOutputScreenRecorderWorker recorder;
    // private File outputFile;

    // private EncoderDecoderFactory encoderFactory = new CapEncoderDecoderFactory();
    private DesktopScreenSnaphotProvider screenSnaphostProvider = new DesktopScreenSnaphotProvider(false, true);

    private ScreenRecorderListener listener;
    
    // ------------------------------------------------------------------------

    public ScreenRecorder(ScreenRecorderListener listener) {
        this.listener = listener;    }

    // ------------------------------------------------------------------------

    public boolean isRecording() {
        return recorder != null && recorder.isRecording();
    }

    public void startRecording(File outputFile) {
        if (recorder != null) {
            return;
        }

        try {
            Rectangle recordArea = screenSnaphostProvider.initialiseScreenCapture();
            
            VideoOutputStream videoOutput = videoStreamFactory.createVideoOutputStream(outputFile);
            
            recorder = new VideoOutputScreenRecorderWorker(videoOutput, recordArea, screenSnaphostProvider, listener);
            
            recorder.startRecording();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void stopRecording() {
        if (recorder == null) {
            return;
        }
        recorder.stopRecording();
        recorder = null;
    }
    
    public boolean isUseWhiteCursor() {
        return useWhiteCursor;
    }

    public void setUseWhiteCursor(boolean useWhiteCursor) {
        this.useWhiteCursor = useWhiteCursor;
    }
    
    public VideoOutputScreenRecorderWorker getRecorder() {
        return recorder;
    }
    
}

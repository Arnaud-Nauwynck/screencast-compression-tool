package fr.an.screencast.recorder;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;

import fr.an.screencast.compressor.imgtool.utils.ImageIOUtils;

public class ScreenshotRecorder {

    private boolean paintCursor;
    private boolean useWhiteCursor;

    private File outputDir;
    private String baseFilename;
    private String formatName = "png";
    private int currentIndex;
    
    private Rectangle recordArea;

    private DesktopScreenSnaphotProvider screenSnaphostProvider = new DesktopScreenSnaphotProvider(false, true);
    
    // ------------------------------------------------------------------------

    public ScreenshotRecorder() {
        this.recordArea = screenSnaphostProvider.initialiseScreenCapture();
    }

    // ------------------------------------------------------------------------

    
    public Rectangle getScreenArea() {
        return screenSnaphostProvider.initialiseScreenCapture();
    }
    
    public Rectangle getRecordArea() {
        return recordArea;
    }
    
    public void setRecordArea(Rectangle recordArea) {
        this.recordArea = recordArea;
    }

    public void startSession(File outputDir, String baseFilename) {
        this.outputDir = outputDir;
        this.baseFilename = baseFilename;
        if (!baseFilename.contains("$i")) {
            baseFilename = "img-$i-" + baseFilename;
        }
        this.currentIndex = 1;
    }
    
    public void takeSnapshot() {
        BufferedImage img = screenSnaphostProvider.captureScreen(recordArea);
        if (paintCursor) {
            Point mousePosition = screenSnaphostProvider.captureMouseLocation();
            screenSnaphostProvider.paintMouseInScreenCapture(img, mousePosition);
        }
        currentIndex++;
        String fileName = baseFilename.replace("$i", Integer.toString(currentIndex));
        File outputFile = new File(outputDir, fileName);
        ImageIOUtils.writeTo(outputFile, img, formatName);
    }

    
    public boolean isPaintCursor() {
        return paintCursor;
    }

    public void setPaintCursor(boolean paintCursor) {
        this.paintCursor = paintCursor;
    }

    public boolean isUseWhiteCursor() {
        return useWhiteCursor;
    }

    public void setUseWhiteCursor(boolean useWhiteCursor) {
        this.useWhiteCursor = useWhiteCursor;
    }
    
}

package fr.an.screencast.recorder;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.compressor.imgtool.ocr.SimpleScreenshotOCRHelper;
import fr.an.screencast.compressor.imgtool.utils.ImageIOUtils;

public class ScreenshotRecorder {
    
    private static final Logger LOG = LoggerFactory.getLogger(ScreenshotRecorder.class);
    
    private DesktopScreenSnaphotProvider screenSnaphostProvider = new DesktopScreenSnaphotProvider(false, true);
    private Rectangle recordArea;
    
    private boolean paintCursor;
    private boolean useWhiteCursor;

    private File outputDir = new File("~/test1");
    private String baseFilename = "screenshot-$i.png";
    private String formatName = "png";
    private int currentIndex = 0;
    
    private boolean enableOCR = false;
    private File ocrSettings = new File("~/.screencast/ocr-settings.xml");
    private String ocrResultFilename = "screenshot.txt";
    
    private SimpleScreenshotOCRHelper ocrHelper = new SimpleScreenshotOCRHelper();
    
    // ------------------------------------------------------------------------

    public ScreenshotRecorder() {
        this.recordArea = screenSnaphostProvider.initialiseScreenCapture();
    }

    // ------------------------------------------------------------------------
    
    public void startSession(File outputDir, String baseFilename, 
            boolean enableOCR, File ocrSettings, String ocrResultFilename) {
        String outputDirText = outputDir.getPath();
        String userHome = System.getProperty("user.home");
        outputDirText = outputDirText.replace("~/", userHome + "/");
        this.outputDir = new File(outputDirText);

        this.baseFilename = baseFilename;
        if (!baseFilename.contains("$i")) {
            baseFilename = "img-$i-" + baseFilename;
        }
        this.enableOCR = enableOCR;
        this.currentIndex = 0;
        this.ocrSettings = ocrSettings;
        this.ocrResultFilename = ocrResultFilename;
    }
    
    public BufferedImage takeSnapshot() {
        if (baseFilename == null) {
            return null;
        }
        LOG.info("take screenshot " + recordArea);
        BufferedImage img = screenSnaphostProvider.captureScreen(recordArea);
        if (paintCursor) {
            Point mousePosition = screenSnaphostProvider.captureMouseLocation();
            screenSnaphostProvider.paintMouseInScreenCapture(img, mousePosition);
        }
        currentIndex++;
        String fileName = baseFilename.replace("$i", Integer.toString(currentIndex));
        File outputFile = new File(outputDir, fileName);
        ImageIOUtils.writeTo(outputFile, img, formatName);
        
        if (enableOCR) {
            String text = ocrHelper.imgToText(img);
            LOG.info("OCR text:" + text);
            if (text != null && !text.isEmpty()) {
                File ocrResultFile = new File(outputDir, ocrResultFilename);
                try {
                    FileUtils.write(ocrResultFile, text, true);
                } catch (IOException ex) {
                    LOG.error("Failed to write append to file " + ocrResultFile, ex);
                }
            }
        }
        
        return img;
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

    public File getOutputDir() {
        return outputDir;
    }

    public void setOutputDir(File outputDir) {
        this.outputDir = outputDir;
    }

    public boolean isEnableOCR() {
        return enableOCR;
    }

    public void setEnableOCR(boolean enableOCR) {
        this.enableOCR = enableOCR;
    }

    public File getOcrSettings() {
        return ocrSettings;
    }

    public void setOcrSettings(File ocrSettings) {
        this.ocrSettings = ocrSettings;
    }

    public String getOcrResultFilename() {
        return ocrResultFilename;
    }

    public void setOcrResultFilename(String ocrResultFilename) {
        this.ocrResultFilename = ocrResultFilename;
    }

    public String getBaseFilename() {
        return baseFilename;
    }

    public void setBaseFilename(String baseFilename) {
        this.baseFilename = baseFilename;
    }

    public String getFormatName() {
        return formatName;
    }

    public void setFormatName(String formatName) {
        this.formatName = formatName;
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(int currentIndex) {
        this.currentIndex = currentIndex;
    }
    
    
    
}

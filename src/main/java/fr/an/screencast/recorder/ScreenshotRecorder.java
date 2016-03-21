package fr.an.screencast.recorder;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.compressor.imgtool.ocr.OCRInteractivePrompter;
import fr.an.screencast.compressor.imgtool.ocr.SimpleScreenshotOCRHelper;
import fr.an.screencast.compressor.imgtool.utils.ImageIOUtils;

public class ScreenshotRecorder {
    
    private static final Logger LOG = LoggerFactory.getLogger(ScreenshotRecorder.class);

    private PropertyChangeSupport pcs = new PropertyChangeSupport(this);

    private DesktopScreenSnaphotProvider screenSnaphostProvider = new DesktopScreenSnaphotProvider(false, true);
    private boolean paintCursor;
    private boolean useWhiteCursor;
    private Rectangle recordArea;

    private boolean activeSession = false;
    
    private File outputDir = new File("~/test1");
    private String baseFilename = "screenshot-$i.png";
    private String formatName = "png";
    
    private boolean enableOCR = true;
    private String ocrSettingsFilename = "~/.screencast/ocr-settings.xml";
    private String ocrResultFilename = "screenshot.txt";
    private SimpleScreenshotOCRHelper ocrHelper = new SimpleScreenshotOCRHelper();
    
    private int currentIndex = 0;
    private BufferedImage currentScreenshotImage;
    private String currentOCRText;
    

    
    // ------------------------------------------------------------------------

    public ScreenshotRecorder() {
        this.recordArea = screenSnaphostProvider.initialiseScreenCapture();
    }

    // ------------------------------------------------------------------------

    public void addPropertyChangeListener(PropertyChangeListener listener) {
        pcs.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
        pcs.removePropertyChangeListener(listener);
    }

    
    public void setActiveSession(boolean p) {
        boolean oldValue = activeSession;
        this.activeSession = p;
        if (activeSession) {
            String outputDirText = outputDir.getPath();
            String userHome = System.getProperty("user.home");
            outputDirText = outputDirText.replace("~/", userHome + "/");
            this.outputDir = new File(outputDirText);

            if (!baseFilename.contains("$i")) {
                baseFilename = "img-$i-" + baseFilename;
            }
            if (enableOCR) {
                this.currentIndex = 0;
                ocrSettingsFilename = ocrSettingsFilename.replace("~/", userHome + "/");
                File ocrSettingsFile = new File(ocrSettingsFilename);
                if (ocrSettingsFile.exists()) {
                    ocrHelper.loadSettings(ocrSettingsFile);
                } else {
                    ocrHelper.initSettings(ocrSettingsFile);
                }
            }
        }
        pcs.firePropertyChange("activeSession", oldValue, activeSession);
    }
    

    public BufferedImage takeSnapshot() {
        if (baseFilename == null) {
            return null;
        }
        LOG.info("take screenshot " + recordArea);
        BufferedImage prevImage = currentScreenshotImage; 
        this.currentScreenshotImage = screenSnaphostProvider.captureScreen(recordArea);
        if (paintCursor) {
            Point mousePosition = screenSnaphostProvider.captureMouseLocation();
            screenSnaphostProvider.paintMouseInScreenCapture(currentScreenshotImage, mousePosition);
        }
        pcs.firePropertyChange("currentScreenshotImage", prevImage, currentScreenshotImage);

        int oldValue = currentIndex; 
        currentIndex++;
        pcs.firePropertyChange("currentIndex", oldValue, currentIndex);
        
        String fileName = baseFilename.replace("$i", Integer.toString(currentIndex));
        File outputFile = new File(outputDir, fileName);
        ImageIOUtils.writeTo(outputFile, currentScreenshotImage, formatName);
        
        if (enableOCR) {
            String prevOCRText = currentOCRText;
            currentOCRText = ocrHelper.imgToText(currentScreenshotImage);
            pcs.firePropertyChange("currentOCRText", prevOCRText, currentOCRText);
            
            LOG.info("OCR text:" + currentOCRText);
            if (currentOCRText != null && !currentOCRText.isEmpty()) {
                File ocrResultFile = new File(outputDir, ocrResultFilename);
                try {
                    FileUtils.write(ocrResultFile, currentOCRText, true);
                } catch (IOException ex) {
                    LOG.error("Failed to write append to file " + ocrResultFile, ex);
                }
            }
        }
        
        return currentScreenshotImage;
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

    public String getOcrSettingsFilename() {
        return ocrSettingsFilename;
    }

    public void setOcrSettingsFilename(String p) {
        this.ocrSettingsFilename = p;
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

    public OCRInteractivePrompter getOcrInteractivePrompter() {
        return ocrHelper.getOcrInteractivePrompter();
    }

    public void setOcrInteractivePrompter(OCRInteractivePrompter ocrInteractivePrompter) {
        ocrHelper.setOcrInteractivePrompter(ocrInteractivePrompter);
    }

    public boolean isActiveSession() {
        return activeSession;
    }    

    
}

package fr.an.screencast.compressor.imgtool.ocr;

import java.awt.image.BufferedImage;
import java.io.File;

import fr.an.screencast.compressor.imgtool.ocr.settings.OCRSettings;
import fr.an.screencast.compressor.imgtool.utils.PtImageData;

public interface OCRInteractivePrompter {

    public void startScreenshotOCR(File ocrSettingsFile, OCRSettings ocrSettings, BufferedImage screenshotImg); 
            
    public void promptForGlyphConnexeComp(PtImageData connexeComp);

    public void finishScreenshotOCR(); 

}

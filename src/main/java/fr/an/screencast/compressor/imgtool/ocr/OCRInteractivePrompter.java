package fr.an.screencast.compressor.imgtool.ocr;

import java.awt.image.BufferedImage;

import fr.an.screencast.compressor.imgtool.ocr.settings.OCRSettings;
import fr.an.screencast.compressor.imgtool.utils.PtImageData;

public interface OCRInteractivePrompter {

    public void promptForGlyphConnexeComp(OCRSettings ocrSettings, BufferedImage img, PtImageData connexeComp);
    
}

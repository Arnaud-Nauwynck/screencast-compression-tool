package fr.an.screencast.compressor.imgtool.ocr.settings;

import java.io.File;

import org.junit.Test;

import fr.an.screencast.compressor.imgtool.utils.ImageData;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;

public class OCRSettingsIOUtilsTest {

    @Test
    public void testReadOCRSettings_dump() {
        File ocrSettingsFile = new File(System.getProperty("user.home") + "/.screencast/ocr-settings.xml");
        OCRSettings ocrSettings = OCRSettingsIOUtils.readOCRSettings(ocrSettingsFile);
        for(OCRGlyphDescr glyph : ocrSettings.getGlyphDescrs()) {
            System.out.println("Glyph: " + glyph.getGlyphDisplayName());
            for(OCRGlyphConnexeComponent connexComp : glyph.getConnexComponents()) {
                System.out.println("connex component: " + connexComp.getOffset() + " file:" + connexComp.getImageDataFilename() + " crc32:" + connexComp.getCrc32());
                ImageData imageData = connexComp.getImageData();
                RGBUtils.dumpFixedRGBString(imageData, System.out);
            }
            System.out.println();
        }
        
    }
    
}

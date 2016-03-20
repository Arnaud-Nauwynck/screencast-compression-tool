package fr.an.screencast.compressor.imgtool.ocr.settings;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import com.thoughtworks.xstream.XStream;

import fr.an.bitwise4j.util.RuntimeIOException;

public final class OCRSettingsIOUtils {

    private OCRSettingsIOUtils() {
    }
    
    private static final XStream DEFAULT_XSTREAM;
    static {
        XStream xstream = new XStream();
        addXStreamAliases(xstream);
        DEFAULT_XSTREAM = xstream;
    }
    
    public static void addXStreamAliases(XStream xstream) {
        xstream.alias("ocrSettings", OCRSettings.class);
        xstream.alias("glyph", ScannedDescrGlyph.class);
        xstream.alias("connexeComponent", ScannedDescrConnexeComponent.class);
    }
    
    public static void writeOCRSettings(File outputFile, OCRSettings src) {
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(outputFile))) {
            DEFAULT_XSTREAM.toXML(src, out);
        } catch(IOException ex) {
            throw new RuntimeIOException("Failed to write file '" + outputFile + "'" , ex);
        }
    }

    public static OCRSettings readOCRSettings(File inputFile) {
        try (InputStream input = new BufferedInputStream(new FileInputStream(inputFile))) {
            Object tmpres = DEFAULT_XSTREAM.fromXML(input);
            return (OCRSettings) tmpres;
        } catch(IOException ex) {
            throw new RuntimeIOException("Failed to read file '" + inputFile +"'", ex);
        }
    }

}

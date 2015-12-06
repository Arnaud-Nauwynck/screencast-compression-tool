package fr.an.screencast.compressor.imgstream;

import java.io.File;

import fr.an.screencast.compressor.imgstream.codecs.cap.CapVideoInputStream;
import fr.an.screencast.compressor.imgstream.codecs.cap.CapVideoOutputStream;

public class VideoStreamFactory {

    private static VideoStreamFactory defaultInstance = new VideoStreamFactory();
    public static VideoStreamFactory getDefaultInstance() {
        return defaultInstance;
    }

    // ------------------------------------------------------------------------

    public VideoStreamFactory() {
    }

    // ------------------------------------------------------------------------
    
    public VideoOutputStream createVideoOutputStream(File outputFile) {
        VideoOutputStream res;
        String fileName = outputFile.getName();
        if (fileName.endsWith(".cap")) {
            res = new CapVideoOutputStream(outputFile);
        } else {
            throw new UnsupportedOperationException();
        }
        return res;
    }


    public VideoInputStream createVideoInputStream(File inputFile) {
        VideoInputStream res;
        String fileName = inputFile.getName();
        if (fileName.endsWith(".cap")) {
            res = new CapVideoInputStream(inputFile);
        } else {
            throw new UnsupportedOperationException();
        }
        return res;
    }

}

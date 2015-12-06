package fr.an.screencast.compressor.imgstream;

import java.io.File;

import fr.an.screencast.compressor.imgstream.codecs.cap.CapVideoOutputStream;

public class VideoStreamFactory {

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
}

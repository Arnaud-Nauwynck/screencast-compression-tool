package fr.an.screencast.compressor.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Serializable;

import org.apache.commons.lang.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSerialisationUtils {
    
    private static final Logger LOG = LoggerFactory.getLogger(FileSerialisationUtils.class);
    
    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T readFromFile(File file) {
        T res;
        long startTime = System.currentTimeMillis();
        
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            res = (T) SerializationUtils.deserialize(in);
        } catch(Exception ex) {
            throw new RuntimeException("Failed to read data from file " + file, ex);
        }

        long timeMillis = System.currentTimeMillis() - startTime;
        if (timeMillis > 1000) {
            LOG.info("reading file " + file + " ... took " + timeMillis + " ms");
        }

        return res;
    }

    public static <T extends Serializable> void writeToFile(T data, File file) {
        long startTime = System.currentTimeMillis();
        
        try (OutputStream in = new BufferedOutputStream(new FileOutputStream(file))) {
            SerializationUtils.serialize(data, in);
        } catch(Exception ex) {
            throw new RuntimeException("Failed to write data from file " + file, ex);
        }
        
        long timeMillis = System.currentTimeMillis() - startTime;
        if (timeMillis > 1000) {
            LOG.info("writing file " + file + " ... took " + timeMillis + " ms");
        }
    }

}

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

public class FileSerialisationUtils {

    @SuppressWarnings("unchecked")
    public static <T extends Serializable> T readFromFile(File file) {
        try (InputStream in = new BufferedInputStream(new FileInputStream(file))) {
            return (T) SerializationUtils.deserialize(in);
        } catch(Exception ex) {
            throw new RuntimeException("Failed to read data from file " + file, ex);
        }
    }

    public static <T extends Serializable> void writeToFile(T data, File file) {
        try (OutputStream in = new BufferedOutputStream(new FileOutputStream(file))) {
            SerializationUtils.serialize(data, in);
        } catch(Exception ex) {
            throw new RuntimeException("Failed to write data from file " + file, ex);
        }
    }

}

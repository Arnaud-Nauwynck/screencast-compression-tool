package fr.an.screencast.compressor;

import java.awt.image.BufferedImage;
import java.io.Closeable;

import fr.an.screencast.compressor.utils.Dim;

public interface VideoInputStream extends Closeable {

    public void init();
    public void close();
    
    public Dim getDim();
    
    public boolean readNextImage();
    
    public BufferedImage getImage();
    public long getPresentationTimestamp();
    
}

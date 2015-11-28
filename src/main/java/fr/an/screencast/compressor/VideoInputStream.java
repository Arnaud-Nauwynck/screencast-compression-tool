package fr.an.screencast.compressor;

import java.awt.image.BufferedImage;
import java.io.Closeable;

public interface VideoInputStream extends Closeable {

    public void init();
    public void close();
    
    public int getWidth();
    public int getHeight();
    
    public boolean readNextImage();
    
    public BufferedImage getImage();
    public long getPresentationTimestamp();
    
}

package fr.an.screencast.compressor.imgstream;

import java.awt.image.BufferedImage;
import java.io.Closeable;

import fr.an.screencast.compressor.utils.Dim;

public interface VideoOutputStream extends Closeable {

    public void init(Dim dim);
    public void close();
    
    public Dim getDim();
    
    public void addFrame(int frameIndex, long frameTime, BufferedImage img);
    
}

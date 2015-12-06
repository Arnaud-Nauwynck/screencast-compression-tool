package fr.an.screencast.compressor.imgstream;

import java.io.Closeable;

import fr.an.screencast.compressor.utils.Dim;

public interface VideoOutputStream extends Closeable {

    public void init(Dim dim);
    public void close();
    
    public Dim getDim();
    
    public void addFrame(long presentationTimestamp, int[] img);
    
//    public void setFrameImage(BufferedImage img);
    
}

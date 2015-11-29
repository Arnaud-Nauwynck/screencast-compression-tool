package fr.an.screencast.compressor.imgstream.codecs.cap;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import fr.an.screencast.compressor.imgstream.VideoInputStream;
import fr.an.screencast.compressor.imgstream.codecs.cap.CapFrameDecompressor.CapFramePacket;
import fr.an.screencast.compressor.utils.Dim;

public class CapVideoInputStream implements VideoInputStream {

    private File inputFile;

    private InputStream inputStream;
    private Dim dim;
    private CapFrameDecompressor decompressor;
    
    private CapFramePacket framePacket;
    private boolean dirtyImage;
    private BufferedImage bufferedImage;
    private long frameTime;
    private boolean finished = false;

    // ------------------------------------------------------------------------
    
    public CapVideoInputStream(File inputFile) {
        this.inputFile = inputFile;
    }

    // ------------------------------------------------------------------------

    public void init() {
        try {
            this.inputStream = new BufferedInputStream(new FileInputStream(inputFile));
                
            int width = (inputStream.read() << 8) + inputStream.read();
            int height = (inputStream.read() << 8) + inputStream.read();
            this.dim = new Dim(width, height);
            this.bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            
            this.decompressor = new CapFrameDecompressor(inputStream, width * height);
        } catch (Exception ex) {
            throw new RuntimeException("Failed init", ex);
        }
    }
    
    public void close() {
        decompressor.close(); // => iStream.close();
        inputStream = null;
    }

    @Override
    public boolean readNextImage() {
        try {
            framePacket = decompressor.unpack();
        } catch (IOException ex) {
            throw new RuntimeException("Failed", ex);
        }
        frameTime = framePacket.getTimeStamp();
        int result = framePacket.getResult();
        if (result == -1) {
            finished = true;
        } else {
            if (result == 0) {
                // same image
            } else {
                dirtyImage = true;
            }
        }
        return ! finished;
    }

    @Override
    public BufferedImage getImage() {
        if (dirtyImage) {
            bufferedImage.setRGB(0, 0, dim.width, dim.height, framePacket.getData(), 0, dim.width);
            dirtyImage = false;
        }
        return bufferedImage;
    }

    @Override
    public long getPresentationTimestamp() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public Dim getDim() {
        return dim;
    }

    public long getFrameTime() {
        return frameTime;
    }

    public boolean isFinished() {
        return finished;
    }
}

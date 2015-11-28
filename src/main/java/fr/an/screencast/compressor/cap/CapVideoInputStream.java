package fr.an.screencast.compressor.cap;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import fr.an.screencast.compressor.VideoInputStream;
import fr.an.screencast.compressor.cap.CapFrameDecompressor.CapFramePacket;

public class CapVideoInputStream implements VideoInputStream {

    private File inputFile;

    private InputStream inputStream;
    private int width, height;
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
                
            this.width = (inputStream.read() << 8) + inputStream.read();
            this.height = (inputStream.read() << 8) + inputStream.read();
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
            bufferedImage.setRGB(0, 0, width, height, framePacket.getData(), 0, width);
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
    public int getWidth() {
        return width;
    }

    @Override
    public int getHeight() {
        return height;
    }

    public long getFrameTime() {
        return frameTime;
    }

    public boolean isFinished() {
        return finished;
    }
}

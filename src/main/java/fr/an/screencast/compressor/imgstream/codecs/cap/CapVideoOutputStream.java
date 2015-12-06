package fr.an.screencast.compressor.imgstream.codecs.cap;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;

import fr.an.screencast.compressor.imgstream.VideoOutputStream;
import fr.an.screencast.compressor.utils.Dim;

public class CapVideoOutputStream implements VideoOutputStream {

    private File outputFile;
    private Dim dim;
    private FramePacket frame;
    private OutputStream oStream;
    
    // ------------------------------------------------------------------------

    public CapVideoOutputStream(File outputFile) {
        this.outputFile = outputFile;
    }

    // ------------------------------------------------------------------------

    @Override
    public void close() {
        try {
            oStream.close();
        } catch (IOException e) {
            throw new RuntimeException("Failed", e);
        }
    }
    
    @Override
    public void init(Dim dim) {
        this.dim = dim; 
        try {
            this.oStream = new BufferedOutputStream(new FileOutputStream(outputFile));
        } catch (FileNotFoundException e) {
            throw new RuntimeException("Failed", e);
        }

        this.frame = new FramePacket(dim.width * dim.height);
        
        try {
            oStream.write((dim.width & 0x0000FF00) >>> 8);
            oStream.write((dim.width & 0x000000FF));
    
            oStream.write((dim.height & 0x0000FF00) >>> 8);
            oStream.write((dim.height & 0x000000FF));
        } catch(IOException ex) {
            throw new RuntimeException("Failed", ex);
        }
    }

    @Override
    public Dim getDim() {
        return dim;
    }

    public void addFrame(long presentationTimestamp, int[] imgData) {
        frame.nextFrame(imgData, presentationTimestamp, false);

        byte[] packed = new byte[imgData.length * 4];

        int inCursor = 0;
        int outCursor = 0;
        @SuppressWarnings("unused")
        int blocks = 0;

        boolean inBlock = true;
        int blockSize = 0;
        byte blockRed = 0;
        byte blockGreen = 0;
        byte blockBlue = 0;

        int blankBlocks = 0;

        // Sentinel value
        int uncompressedCursor = -1;

        byte red;
        byte green;
        byte blue;

        boolean hasChanges = false;
        boolean lastEntry = false;

        while (inCursor < imgData.length) {
            if (inCursor == imgData.length - 1) {
                lastEntry = true;
            }

            if (imgData[inCursor] == frame.previousData[inCursor]) {
                red = 0;
                green = 0;
                blue = 0;
            } else {
                red = (byte) ((imgData[inCursor] & 0x00FF0000) >>> 16);
                green = (byte) ((imgData[inCursor] & 0x0000FF00) >>> 8);
                blue = (byte) ((imgData[inCursor] & 0x000000FF));

                if (red == 0 && green == 0 && blue == 0) {
                    blue = 1;
                }
            }

            if (blockRed == red && blockGreen == green && blockBlue == blue) {
                if (inBlock == false) {
                    if (uncompressedCursor > -1) {
                        blocks++;
                        hasChanges = true;
                        packed[uncompressedCursor] = (byte) (blockSize + 0x80);
                    }
                    inBlock = true;
                    blockSize = 0;
                    blankBlocks = 0;
                } else if (blockSize == 126 || lastEntry == true) {
                    if (blockRed == 0 && blockGreen == 0 && blockBlue == 0) {
                        if (blankBlocks > 0) {
                            blankBlocks++;
                            packed[outCursor - 1] = (byte) blankBlocks;
                        } else {
                            blocks++;
                            blankBlocks++;
                            packed[outCursor] = (byte) 0xFF;
                            outCursor++;
                            packed[outCursor] = (byte) blankBlocks;
                            outCursor++;
                        }
                        if (blankBlocks == 255) {
                            blankBlocks = 0;
                        }
                    } else {
                        blocks++;
                        hasChanges = true;
                        packed[outCursor] = (byte) blockSize;
                        outCursor++;
                        packed[outCursor] = blockRed;
                        outCursor++;
                        packed[outCursor] = blockGreen;
                        outCursor++;
                        packed[outCursor] = blockBlue;
                        outCursor++;

                        blankBlocks = 0;
                    }
                    inBlock = true;
                    blockSize = 0;
                }
            } else {
                if (inBlock == true) {
                    if (blockSize > 0) {
                        blocks++;
                        hasChanges = true;
                        packed[outCursor] = (byte) blockSize;
                        outCursor++;
                        packed[outCursor] = blockRed;
                        outCursor++;
                        packed[outCursor] = blockGreen;
                        outCursor++;
                        packed[outCursor] = blockBlue;
                        outCursor++;
                    }

                    uncompressedCursor = -1;
                    inBlock = false;
                    blockSize = 0;

                    blankBlocks = 0;
                } else if (blockSize == 126 || lastEntry == true) {
                    if (uncompressedCursor > -1) {
                        blocks++;
                        hasChanges = true;
                        packed[uncompressedCursor] = (byte) (blockSize + 0x80);
                    }

                    uncompressedCursor = -1;
                    inBlock = false;
                    blockSize = 0;

                    blankBlocks = 0;
                }

                if (uncompressedCursor == -1) {
                    uncompressedCursor = outCursor;
                    outCursor++;
                }

                packed[outCursor] = red;
                outCursor++;
                packed[outCursor] = green;
                outCursor++;
                packed[outCursor] = blue;
                outCursor++;

                blockRed = red;
                blockGreen = green;
                blockBlue = blue;
            }
            inCursor++;
            blockSize++;
        }

        try {
            oStream.write(((int) frame.frameTime & 0xFF000000) >>> 24);
            oStream.write(((int) frame.frameTime & 0x00FF0000) >>> 16);
            oStream.write(((int) frame.frameTime & 0x0000FF00) >>> 8);
            oStream.write(((int) frame.frameTime & 0x000000FF));
    
            if (hasChanges == false) {
                oStream.write(0);
                oStream.flush();
                frame.newData = frame.previousData;
    
                return;
            } else {
                oStream.write(1);
                oStream.flush();
            }
    
            ByteArrayOutputStream bO = new ByteArrayOutputStream();
            try (GZIPOutputStream zO = new GZIPOutputStream(bO)) {
                zO.write(packed, 0, outCursor);
            }
            bO.close();
    
            byte[] bA = bO.toByteArray();
    
            oStream.write((bA.length & 0xFF000000) >>> 24);
            oStream.write((bA.length & 0x00FF0000) >>> 16);
            oStream.write((bA.length & 0x0000FF00) >>> 8);
            oStream.write((bA.length & 0x000000FF));
    
            oStream.write(bA);
            oStream.flush();
        } catch(IOException ex) {
            throw new RuntimeException("Failed", ex);
        }
    }

    // ------------------------------------------------------------------------

    private class FramePacket {

        private long frameTime;

        private int[] previousData;
        private int[] newData;

        private FramePacket(int frameSize) {
            previousData = new int[frameSize];
        }

        private void nextFrame(int[] frameData, long frameTime, boolean reset) {
            this.frameTime = frameTime;
            previousData = newData;
            newData = null;
            if (previousData == null) {
                previousData = new int[frameData.length];
            }
            if (reset) {
                this.newData = new int[frameData.length];
            } else {
                this.newData = frameData;// new int[frameData.length];
            }
        }
    }

}

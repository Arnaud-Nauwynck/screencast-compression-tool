package fr.an.screencast.compressor.imgstream.codecs.humbleio;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.compressor.imgstream.VideoInputStream;
import fr.an.screencast.compressor.utils.Dim;
import io.humble.video.Decoder;
import io.humble.video.Demuxer;
import io.humble.video.DemuxerStream;
import io.humble.video.MediaDescriptor;
import io.humble.video.MediaPacket;
import io.humble.video.MediaPicture;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;

public class HumbleioVideoInputStream implements VideoInputStream {

    private static final Logger LOG = LoggerFactory.getLogger(HumbleioVideoInputStream.class);
    
    private String filename;
    
    private Demuxer demuxer;
    private Decoder videoDecoder;
    private int videoStreamId;
    
    private Dim dim;
    private MediaPicture picture;
    private MediaPictureConverter converter;
    
    private MediaPacket packet;

    private int frameIndex;
    private BufferedImage image3ByteBGR;
    
    private boolean dirtyImageRGB; 
    private BufferedImage imageRGB;
    private int[] imageRGBDataInts;

    private long presentationTimestamp;
    
    // ------------------------------------------------------------------------
    
    public HumbleioVideoInputStream(String filename) {
        this.filename = filename;
    }

    // ------------------------------------------------------------------------

    public void init() {
        try {
            this.demuxer = Demuxer.make();
            demuxer.open(filename, null, false, true, null, null);
        
            int numStreams = demuxer.getNumStreams();
        
            videoStreamId = -1;
            videoDecoder = null;
            for (int i = 0; i < numStreams; i++) {
                final DemuxerStream stream = demuxer.getStream(i);
                final Decoder decoder = stream.getDecoder();
                if (decoder != null && decoder.getCodecType() == MediaDescriptor.Type.MEDIA_VIDEO) {
                    videoStreamId = i;
                    videoDecoder = decoder;
                    // stop at the first one.
                    break;
                }
            }
            if (videoStreamId == -1) {
                throw new RuntimeException("could not find video stream in container: " + filename);
            }
            
            videoDecoder.open(null, null);
        
            int width = videoDecoder.getWidth();
            int height = videoDecoder.getHeight();
            this.dim = new Dim(width, height);
            picture = MediaPicture.make(width, height, videoDecoder.getPixelFormat());
        
            // converter to BGR_24 for java swing 
            converter = MediaPictureConverterFactory.createConverter(MediaPictureConverterFactory.HUMBLE_BGR_24, picture);
            image3ByteBGR = null; // new BufferedImage(width, height, BufferedImage.TYPE_3BYTE_BGR);
            
            imageRGB = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
            imageRGBDataInts = ((DataBufferInt) imageRGB.getRaster().getDataBuffer()).getData(); 
                    
            packet = MediaPacket.make();
        } catch(Exception ex) {
            throw new RuntimeException("Failed init HumbleioPacketReader " + filename, ex);
        }
    }
    
    public void close() {
        try {
            do {
                videoDecoder.decode(picture, null, 0);
                if (picture.isComplete()) {
                    image3ByteBGR = converter.toImage(image3ByteBGR, picture);
                }
            } while (picture.isComplete());

            demuxer.close();
        } catch(Exception ex) {
            LOG.warn("Faile to close ... ignore, no rethrow", ex);
        }

        demuxer = null;
        videoDecoder = null;
        videoStreamId = -1;
        picture = null;
        converter = null;
        packet = null;
        image3ByteBGR = null;
        imageRGB = null;
        demuxer = null;
    }

    
    public boolean readNextImage() {
        try {
            while (demuxer.read(packet) >= 0) {
                if (packet.getStreamIndex() == videoStreamId) {
                    int offset = 0;
                    int bytesRead = 0;
                    do {
                        bytesRead += videoDecoder.decode(picture, packet, offset);
                        if (picture.isComplete()) {
                            image3ByteBGR = converter.toImage(image3ByteBGR, picture);
                            frameIndex++;
                            
                            this.presentationTimestamp = packet.getPts();
                            
                            
                            dirtyImageRGB = true;
                            return true;
                        }
                        offset += bytesRead;
                    } while (offset < packet.getSize());
                }
            }
        } catch(Exception ex) {
            throw new RuntimeException("Failed readNextImage", ex);
        }
        return false;
    }
    
    public BufferedImage getImage() {
        if (dirtyImageRGB) {
            // convert 3BYTE_BGR to INT_RGB
            Graphics2D g2d = imageRGB.createGraphics();
            g2d.drawImage(image3ByteBGR, 0, 0, null);
            g2d.dispose();
            
            dirtyImageRGB = false;
        }
        return imageRGB;
    }
    
    public int[] getImageDataInts() {
        if (dirtyImageRGB) {
            getImage();
        }
        return imageRGBDataInts;
    }
    
    public long getPresentationTimestamp() {
        return presentationTimestamp;
    }
    
    
    // ------------------------------------------------------------------------


    public String getFilename() {
        return filename;
    }

    public Demuxer getDemuxer() {
        return demuxer;
    }

    public Decoder getVideoDecoder() {
        return videoDecoder;
    }

    public int getVideoStreamId() {
        return videoStreamId;
    }

    public Dim getDim() {
        return dim;
    }

    public MediaPicture getPicture() {
        return picture;
    }

    public MediaPictureConverter getConverter() {
        return converter;
    }

    public BufferedImage getImage3ByteBGR() {
        return image3ByteBGR;
    }

    public MediaPacket getPacket() {
        return packet;
    }

    public int getFrameIndex() {
        return frameIndex;
    }
    
}

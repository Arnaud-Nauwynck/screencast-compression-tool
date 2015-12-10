package fr.an.screencast.compressor.imgstream.codecs.humbleio;

import java.awt.image.BufferedImage;
import java.io.File;

import fr.an.screencast.compressor.imgstream.VideoOutputStream;
import fr.an.screencast.compressor.imgtool.utils.BufferedImageUtils;
import fr.an.screencast.compressor.utils.Dim;
import io.humble.video.Codec;
import io.humble.video.Encoder;
import io.humble.video.MediaPacket;
import io.humble.video.MediaPicture;
import io.humble.video.Muxer;
import io.humble.video.MuxerFormat;
import io.humble.video.PixelFormat;
import io.humble.video.Rational;
import io.humble.video.awt.MediaPictureConverter;
import io.humble.video.awt.MediaPictureConverterFactory;

/**
 * implementation of VideoOutputStream using io.humble.video.Muxer
 */
public class HumbleioVideoOutputStream implements VideoOutputStream {

    private File outputFile;
    private int targetType = BufferedImage.TYPE_3BYTE_BGR;
    
    private Dim dim;
    private int frameRate = 5;
    private String destFormatName;
    private String codecname;
    
    private BufferedImage convertBufferImage;
    private Encoder encoder;
    private MediaPictureConverter converter;
    private MediaPicture picture;
    private MediaPacket packet;
    private Muxer muxer;
    
    // ------------------------------------------------------------------------
    
    public HumbleioVideoOutputStream(File outputFile) {
        this.outputFile = outputFile;
    }

    // ------------------------------------------------------------------------

    @Override
    public void init(Dim dim) {
        final int width = dim.width, height = dim.height;
        convertBufferImage = new BufferedImage(width, height, targetType);
        
        final Rational framerate = Rational.make(1, frameRate);
        
        muxer = Muxer.make(outputFile.getAbsolutePath(), null, destFormatName);
        
        /** Now, we need to decide what type of codec to use to encode video. Muxers
         * have limited sets of codecs they can use. We're going to pick the first one that
         * works, or if the user supplied a codec name, we're going to force-fit that
         * in instead.
         */
        final MuxerFormat format = muxer.getFormat();
        final Codec codec;
        if (codecname != null) {
          codec = Codec.findEncodingCodecByName(codecname);
        } else {
          codec = Codec.findEncodingCodec(format.getDefaultVideoCodecId());
        }
        
        encoder = Encoder.make(codec);
        
        encoder.setWidth(width);
        encoder.setHeight(height);
        // We are going to use 420P as the format because that's what most video formats these days use
        final PixelFormat.Type pixelformat = PixelFormat.Type.PIX_FMT_YUV420P;
        encoder.setPixelFormat(pixelformat);
        encoder.setTimeBase(framerate);
        
        // An annoynace of some formats is that they need global (rather than per-stream) headers,
        // and in that case you have to tell the encoder. And since Encoders are decoupled from
        // Muxers, there is no easy way to know this beyond
        if (format.getFlag(MuxerFormat.Flag.GLOBAL_HEADER)) {
          encoder.setFlag(Encoder.Flag.FLAG_GLOBAL_HEADER, true);
        }
        
        encoder.open(null, null);
        
        muxer.addNewStream(encoder);
        
        try {
            muxer.open(null, null);
        } catch (Exception e) {
            throw new RuntimeException("Failed to open video output stream for file '" + outputFile + "'", e);
        }
        
        converter = null;
        picture = MediaPicture.make(width, height, pixelformat);
        picture.setTimeBase(framerate);
        
        packet = MediaPacket.make();        
    }

    @Override
    public void close() {
        do {
            encoder.encode(packet, picture);

            if (packet.isComplete()) {
                muxer.write(packet, false);
            }
        } while (packet.isComplete());
        
        muxer.close();
        
        muxer = null;
        packet = null;
        encoder = null;
        converter = null;
        picture = null;
    }

    @Override
    public Dim getDim() {
        return dim;
    }

    @Override
    public void addFrame(int frameIndex, long frameTime, BufferedImage frameImage) {
        // convert image to TYPE_3BYTE_BGR
        final BufferedImage screen = BufferedImageUtils.convertToType(convertBufferImage, frameImage, BufferedImage.TYPE_3BYTE_BGR);
                
        // This is LIKELY not in YUV420P format, so we're going to convert it using some handy utilities.
        if (converter == null) {
            converter = MediaPictureConverterFactory.createConverter(screen, picture);
        }
        // use frameIndex, not frameTime despite of javadoc asking "timestamp"  
        converter.toPicture(picture, screen, frameIndex);

        do {
            encoder.encode(packet, picture);

            if (packet.isComplete()) {
                muxer.write(packet, false);
            }
        } while (packet.isComplete());

    }

    
}

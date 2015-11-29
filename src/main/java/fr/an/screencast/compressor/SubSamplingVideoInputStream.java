package fr.an.screencast.compressor;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.util.Arrays;
import java.util.function.BiConsumer;

import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.RGBUtils;

public class SubSamplingVideoInputStream implements VideoInputStream {
    
    private VideoInputStream target;
    private int samplingRate; 
    private BiConsumer<BufferedImage[],BufferedImage> samplingImageFunc;
    
    private SlidingImageArray inputImages;
    private BufferedImage resImage;
    
    // ------------------------------------------------------------------------

    public SubSamplingVideoInputStream(VideoInputStream target, int samplingRate, BiConsumer<BufferedImage[],BufferedImage> samplingImageFunc) {
        this.target = target;
        this.samplingRate = samplingRate;
        this.samplingImageFunc = samplingImageFunc;
    }
    
    // ------------------------------------------------------------------------
    
    public void init() {
        target.init();
        
        final Dim dim = target.getDim();
        this.inputImages = new SlidingImageArray(samplingRate, dim, BufferedImage.TYPE_INT_RGB);
        
        this.resImage = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB); 
    }
    
    
    @Override
    public void close() {
        target.close();
        
        inputImages = null;
        resImage = null;
    }

    @Override
    public Dim getDim() {
        return target.getDim();
    }

    @Override
    public boolean readNextImage() {
        // read ahead "samplingRate" x images
        boolean res = true;
        for(int i = 0; i < samplingRate; i++) {
            res = target.readNextImage();
            if (!res) {
                return false;
            }
            Image image = target.getImage();
            inputImages.slide(image);
        }
        // filter compute 1 image from inputImages
        BufferedImage[] prevImages = inputImages.getPrevImage();
        samplingImageFunc.accept(prevImages, resImage);
        return res;
    }
    
    @Override
    public BufferedImage getImage() {
        return resImage;
    }

    @Override
    public long getPresentationTimestamp() {
        // TODO
        return 0;
    }

    // ------------------------------------------------------------------------

    
    public static BiConsumer<BufferedImage[],BufferedImage> DEFAULT_SAMPLER_RGB_MEDIAN = (images,res) -> defaultSamplerRGBMedian(images, res);;
    
    public static void defaultSamplerRGBMedian(BufferedImage[] images, BufferedImage res) {
        final int samplingRate = images.length;
        int[] rValues = new int[samplingRate];
        int[] gValues = new int[samplingRate];
        int[] bValues = new int[samplingRate];
        int[] aValues = new int[samplingRate];
        
        final int width = res.getWidth();
        final int height = res.getHeight();
        
        final int mid = samplingRate /2;
        for(int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // extract values from samples
                for(int sample = 0; sample < samplingRate; sample++) {
                    BufferedImage img = images[sample];
                    int rgb = img.getRGB(x, y);
                    ColorModel cm = img.getColorModel();
                    int r = cm.getRed(rgb);
                    int g = cm.getGreen(rgb);
                    int b = cm.getBlue(rgb);
                    int a = cm.getAlpha(rgb);
                    
                    rValues[sample] = r;
                    gValues[sample] = g;
                    bValues[sample] = b;
                    aValues[sample] = a;
                }
                // sort values and extract mid point
                Arrays.sort(rValues);
                Arrays.sort(gValues);
                Arrays.sort(bValues);
                Arrays.sort(aValues);
                int rgb = RGBUtils.rgb2Int(rValues[mid], gValues[mid], bValues[mid], aValues[mid]);
                // fill result image
                res.setRGB(x, y, rgb);
            }
        }
    };
    
  
    
    
}

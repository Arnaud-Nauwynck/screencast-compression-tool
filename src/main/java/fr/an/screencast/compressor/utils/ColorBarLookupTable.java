package fr.an.screencast.compressor.utils;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;

public class ColorBarLookupTable {

    private final int[] data;
    
    // ------------------------------------------------------------------------

    public ColorBarLookupTable(int[] src) {
        this.data = new int[src.length];
        System.arraycopy(src, 0, data, 0, src.length);
    }

    public static ColorBarLookupTable getDefault() {
        return newFromResourceName("colorbar-cold-hot.png", 1);
    }
    
    public static ColorBarLookupTable newFromResourceName(String resourceName, int y) {
        URL resource = ColorBarLookupTable.class.getClassLoader().getResource(resourceName);
        return newFromFile(resource, y);
    }
    
    public static ColorBarLookupTable newFromFile(URL file, int y) {
        BufferedImage img;
        try {
            img = ImageIO.read(file);
        } catch (IOException e) {
            throw new RuntimeException("Failed", e);
        }
        int width = img.getWidth();
        int[] data = new int[width];
        img.getRGB(0, y, width, 1, data, 0, 1);
        return new ColorBarLookupTable(data);
    }
    
    // ------------------------------------------------------------------------

    public int interpolateRGB(int value, int min, int max) {
        int idx;
        if (value <= min) {
            idx = 0;
        } else if (value >= max) {
            idx = data.length-1;
        } else {
            idx = (data.length-1) * (value - min) / (max - min);
            // check?
            idx = Math.max(0, Math.min(idx, data.length-1)); 
        }
        return data[idx];
    }

    public int interpolateRGB(double value) {
        return interpolateRGB(value, 0.0, 1.0);
    }
    
    public int interpolateRGB(double value, double min, double max) {
        int idx;
        if (value <= min) {
            idx = 0;
        } else if (value >= max) {
            idx = data.length-1;
        } else {
            idx = (int) ((data.length-1) * (value - min) / (max - min));
            // check?
            idx = Math.max(0, Math.min(idx, data.length-1)); 
        }
        return data[idx];
    }

}

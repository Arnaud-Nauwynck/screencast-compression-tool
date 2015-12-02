package fr.an.screencast.compressor.imgtool.utils;

public class HSVColor {

    public float h;
    public float s;
    public int v; //=max

    public int min;
    public int delta;
    public int hdiff;
    
    public void setFromRGB(int r, int g, int b) {
        // Color.RGBToHSV();

        min = Math.min(Math.min(r, g), b);
        int max = Math.max(Math.max(r, g), b);

        // V
        v = max;

        delta = max - min;

        // S
        if (max != 0) {
            s = (float) delta / max;

            // H
            if (r == max)
                hdiff = (g - b); // between yellow & magenta
            else if (g == max)
                hdiff = 2*delta + (b - r); // between cyan & yellow
            else
                hdiff = 4*delta + (r - g); // between magenta & cyan

            h = (delta != 0)? (float)hdiff / delta : 0.0f;
            h *= 60; // degrees
            if (h < 0)
                h += 360;

        } else {
            s = 0;
            h = -1;
            return;
        }

    }
}

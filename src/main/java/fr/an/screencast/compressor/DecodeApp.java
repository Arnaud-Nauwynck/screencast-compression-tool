package fr.an.screencast.compressor;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.compressor.cap.CapVideoInputStream;
import fr.an.screencast.compressor.ui.DeltaImageAnalysisPanel;
import fr.an.screencast.compressor.utils.ImageRasterUtils;
import fr.an.screencast.compressor.utils.RGBUtils;

public class DecodeApp {

    private static final Logger LOG = LoggerFactory.getLogger(DecodeApp.class);
    

    private String filename;
    
    private int subSamplingRate = 3;
    private int prevSlidingLen = 3; 

    // ------------------------------------------------------------------------

    public static void main(String[] args) throws InterruptedException, IOException {
        DecodeApp app = new DecodeApp();
        app.parseArgs(args);
        app.run();
    }

    public DecodeApp() {
    }

    // ------------------------------------------------------------------------
    
    private void parseArgs(String[] args) {
        for(int i = 0; i < args.length; i++) {
            if (args[i].equals("-i")) {
                filename = args[++i];
            }
        }
        
        if (filename == null) {
            filename = "test1.avi";
        }
    }

    public void run() {
        try {
            processVideo();
        } catch(Exception ex) {
            LOG.error("Failed", ex);
        }
    }
    
    private void processVideo() throws Exception {
        long sleepFrameMillis = 200;
        
        VideoInputStream videoInput;
        if (filename.endsWith(".cap")) {
            videoInput = new CapVideoInputStream(new File(filename));
            
        } else {
            HumbleioVideoInputStream rawVideoInput = new HumbleioVideoInputStream(filename);
            
            videoInput = new SubSamplingVideoInputStream(rawVideoInput, subSamplingRate, 
                SubSamplingVideoInputStream.DEFAULT_SAMPLER_RGB_MEDIAN);
            sleepFrameMillis *= subSamplingRate;
        }
        
        videoInput.init();
        
        final int width = videoInput.getWidth();
        final int height = videoInput.getHeight();

        
        SlidingImageArray slidingImages = new SlidingImageArray(prevSlidingLen, width, height, BufferedImage.TYPE_INT_RGB);
        
        DeltaImageAnalysisResult deltaImages = new DeltaImageAnalysisResult(width, height, BufferedImage.TYPE_INT_RGB); 
        BufferedImage diffImageRGB = deltaImages.getDiffImage();
        BufferedImage deltaImageRGB = deltaImages.getDeltaImage();

        BufferedImage prevImageRGB = slidingImages.getPrevImage()[1]; // ref will change..
        int[] prevImageRGBDataInts;
        ColorModel cm = slidingImages.getPrevImage()[0].getColorModel();
        
        
        int frameIndex = 0;
        
        
        DeltaImageAnalysis delta = new DeltaImageAnalysis(width, height, null, null);

        JFrame appFrame = new JFrame();
        DeltaImageAnalysisPanel deltaAnalysisPanel = new DeltaImageAnalysisPanel();
        appFrame.getContentPane().add(deltaAnalysisPanel.getJComponent());
        appFrame.pack();
        appFrame.setVisible(true);
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        while(videoInput.readNextImage()) {
            BufferedImage imageRGB = videoInput.getImage(); // read 3 images ... sub-sampling using median
            
            slidingImages.slide(imageRGB);
            
//            if (videoInput.getFrameIndex() < prevSlidingLen) {
//                continue;
//            }
            prevImageRGB = slidingImages.getPrevImage()[1];
            // prevImageRGBDataInts = slidingImages.getPrevImageDataInts()[1];
                    
            Graphics2D diffGc = diffImageRGB.createGraphics();
            diffGc.setColor(Color.WHITE); // BLACK
            diffGc.fillRect(0, 0, width, height);
            
            for(int y = 0; y < height; y++) {
                for (int x = 0; x < width; x++) {
                    int prevRgb = prevImageRGB .getRGB(x, y);
                    int rgb = imageRGB.getRGB(x, y);
                    int dr = Math.abs(cm.getRed(prevRgb) - cm.getRed(rgb));
                    int dg = Math.abs(cm.getGreen(prevRgb) - cm.getGreen(rgb));
                    int db = Math.abs(cm.getBlue(prevRgb) - cm.getBlue(rgb));
                    int diff = (dr + dg + db);
                    if (diff != 0) {
                        diffImageRGB.setRGB(x, y, RGBUtils.rgb2Int256(dr*2, dg*2, db*2, 0));
                    }
                }                            
            }
            diffGc.dispose();
            
            Graphics2D deltaGc = deltaImageRGB.createGraphics();
            deltaGc.setColor(Color.WHITE);
            deltaGc.fillRect(0,  0, width, height);
            
            // TODO ...
            System.out.println("[" + frameIndex + "]");
        
            delta.setData(ImageRasterUtils.toInts(prevImageRGB), ImageRasterUtils.toInts(imageRGB));
            
            // *** compute diffs ***
            delta.computeDiff();
            
            int countDiffLine = delta.getCountDiffLine();
            if (countDiffLine != 0) {
                Rectangle diffRect = delta.getDiffRect();
                System.out.println("  diff rect: " + diffRect);
                
                deltaGc.setColor(Color.GRAY);
                // deltaGc.fillRect(diffRect.x, diffRect.y, diffRect.width, diffRect.height);
                deltaGc.drawRect(diffRect.x-1, diffRect.y-1, diffRect.width+1, diffRect.height+1);
                
                deltaGc.setColor(Color.BLACK);
                for(Rectangle r : delta.getDiffRects()) {
                    deltaGc.drawRect(r.x-1, r.y-1, r.width+1, r.height+1);
                }
            }

            deltaGc.dispose();
            
            deltaAnalysisPanel.asyncSetImages(prevImageRGB, imageRGB, diffImageRGB, deltaImageRGB);

            
            Thread.sleep(sleepFrameMillis);
        }

        videoInput.close();
    }

}

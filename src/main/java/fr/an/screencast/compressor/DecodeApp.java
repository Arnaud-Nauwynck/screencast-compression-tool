package fr.an.screencast.compressor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.compressor.imgstream.SlidingImageArray;
import fr.an.screencast.compressor.imgstream.SubSamplingVideoInputStream;
import fr.an.screencast.compressor.imgstream.VideoInputStream;
import fr.an.screencast.compressor.imgstream.codecs.cap.CapVideoInputStream;
import fr.an.screencast.compressor.imgstream.codecs.humbleio.HumbleioVideoInputStream;
import fr.an.screencast.compressor.imgtool.delta.DeltaImageAnalysis;
import fr.an.screencast.compressor.imgtool.delta.DeltaImageAnalysisResult;
import fr.an.screencast.compressor.imgtool.utils.ImageData;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.ui.DeltaImageAnalysisPanel;
import fr.an.screencast.compressor.utils.Dim;

public class DecodeApp {

    private static final Logger LOG = LoggerFactory.getLogger(DecodeApp.class);
    

    private String filename;
    
    private int subSamplingRate = 3;
    private int prevSlidingLen = 3; 

    private int skipFrameCount = 7;
    
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
            String arg = args[i];
            if (arg.equals("-i")) {
                filename = args[++i];
            } else {
                throw new RuntimeException("Unrecognised arg '" + arg + "'");
            }
        }
        
        if (filename == null) {
            File testInputFile = new File("src/test/data/live-coding.cap");
            if (testInputFile.exists()) {
                LOG.warn("missing arg '-i <<inputFile>>  ... using found test file '" + testInputFile + "'");
            } else {
                throw new RuntimeException("missing arg '-i <<inputFile>>'");
            }
        }
        File inputFile = new File(filename);
        if (! inputFile.exists()) {
            throw new RuntimeException("File not found: '" + inputFile.getAbsolutePath() + "'");
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
        
        final Dim dim = videoInput.getDim();
        
        SlidingImageArray slidingImages = new SlidingImageArray(prevSlidingLen, dim, BufferedImage.TYPE_INT_RGB);
        
        DeltaImageAnalysisResult deltaImages = new DeltaImageAnalysisResult(dim, BufferedImage.TYPE_INT_RGB); 
        BufferedImage diffImageRGB = deltaImages.getDiffImage();
        BufferedImage deltaImageRGB = deltaImages.getDeltaImage();

        BufferedImage prevImageRGB = slidingImages.getPrevImage()[1]; // ref will change..
        int[] prevImageRGBDataInts;
        ColorModel cm = slidingImages.getPrevImage()[0].getColorModel();
        
        
        int frameIndex = 0;
        
        
        DeltaImageAnalysis delta = new DeltaImageAnalysis(dim, null, null);

        JFrame appFrame = new JFrame();
        DeltaImageAnalysisPanel deltaAnalysisPanel = new DeltaImageAnalysisPanel();
        appFrame.getContentPane().add(deltaAnalysisPanel.getJComponent());
        appFrame.pack();
        appFrame.setVisible(true);
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        if (skipFrameCount > 0) {
            LOG.info("SKIP " + skipFrameCount + " Frames");
            while(skipFrameCount > 0 && videoInput.readNextImage()) {
                skipFrameCount--;
                BufferedImage imageRGB = videoInput.getImage(); // read 3 images ... sub-sampling using median
                frameIndex++;
                slidingImages.slide(imageRGB);
            }
        }
        
        while(videoInput.readNextImage()) {
            BufferedImage imageRGB = videoInput.getImage(); // read 3 images ... sub-sampling using median
            frameIndex++;
            
            slidingImages.slide(imageRGB);
            
//            if (videoInput.getFrameIndex() < prevSlidingLen) {
//                continue;
//            }
            prevImageRGB = slidingImages.getPrevImage()[1];
            // prevImageRGBDataInts = slidingImages.getPrevImageDataInts()[1];
                    
            Graphics2D diffGc = diffImageRGB.createGraphics();
            diffGc.setColor(Color.WHITE); // BLACK
            diffGc.fillRect(0, 0, dim.width, dim.height);

            Graphics2D deltaGc = deltaImageRGB.createGraphics();
            deltaGc.setColor(Color.BLACK);
            deltaGc.fillRect(0,  0, dim.width, dim.height);

            for(int y = 0; y < dim.height; y++) {
                for (int x = 0; x < dim.width; x++) {
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
            
            
            // TODO ...
            System.out.println("[" + frameIndex + "]");
        
            delta.setData(ImageRasterUtils.toInts(prevImageRGB), ImageRasterUtils.toInts(imageRGB));
            
            // *** compute diffs ***
            delta.computeDiff();
            
            int firstDiffPtx = delta.getFirstDiffPtx(); 
                    // delta.getRawFirstDiffPtx();
            int firstDiffPty = delta.getFirstDiffPty();
            if (firstDiffPtx != -1) {

                
                // show integral diff
                boolean showDiffCountIntegralImage = true; 
                if (showDiffCountIntegralImage) {
                    ImageData diffCountIntegralImageData = 
                            // delta.getDiffCountIntegralImageData();
                            // delta.getDiffCountHorizontalIntegralImageData();
                            delta.getDiffCountVerticalIntegralImageData();
                    int[] integral = diffCountIntegralImageData.getData();
                    int[] horIntegral = delta.getDiffCountHorizontalIntegralImageData().getData();
                    int[] vertIntegral = delta.getDiffCountVerticalIntegralImageData().getData();
                    for(int y = 0, idx_xy=0; y < dim.height; y++) {
                        for (int x = 0; x < dim.width; x++,idx_xy++) {
                            int count = // integral[idx_xy];
                                horIntegral[idx_xy] + vertIntegral[idx_xy];
                            int countClam = (count != 0)? 50+count : 0; 
                            deltaImageRGB.setRGB(x, y, RGBUtils.rgb2Int256(countClam, countClam, countClam, 0));
                        }                            
                    }
                }
                
                
                List<Rectangle> diffRects = delta.getDiffRects();
                if (! diffRects.isEmpty()) {
    //                Rectangle diffRect = delta.getDiffRect();
    //                System.out.println("  diff rect: " + diffRect);
    //                
    //                deltaGc.setColor(Color.GRAY);
    //                // deltaGc.fillRect(diffRect.x, diffRect.y, diffRect.width, diffRect.height);
    //                deltaGc.drawRect(diffRect.x-1, diffRect.y-1, diffRect.width+1, diffRect.height+1);
                    
                    deltaGc.setColor(Color.RED);
                    for(Rectangle r : diffRects) {
                        deltaGc.drawRect(r.x-1, r.y-1, r.width+1, r.height+1);
                    }
                }
                

                deltaGc.setColor(Color.RED);
                int markerLineLen = 100;
//                deltaGc.drawLine(firstDiffPtx-1, firstDiffPty-1-markerLineLen, firstDiffPtx-1, firstDiffPty-1);
//                deltaGc.drawLine(firstDiffPtx-1-markerLineLen, firstDiffPty-1, firstDiffPtx-1, firstDiffPty-1);
                deltaGc.setStroke(new BasicStroke(2));
                deltaGc.drawLine(firstDiffPtx-markerLineLen, firstDiffPty-markerLineLen, firstDiffPtx, firstDiffPty);
                if (firstDiffPtx < 10 || firstDiffPty < 10) {
                    int markerLen = 20;
                    deltaGc.fillRect(firstDiffPtx-markerLen, firstDiffPty-markerLen, 2*markerLen, 2*markerLen);
                }
            }
            
            
            deltaGc.dispose();
            
            deltaAnalysisPanel.asyncSetImages(prevImageRGB, imageRGB, diffImageRGB, deltaImageRGB);

            boolean debugRedo = false;
            if (debugRedo) {
                delta.computeDiff();
            }
            
            
            Thread.sleep(sleepFrameMillis);
        }

        videoInput.close();
    }

}

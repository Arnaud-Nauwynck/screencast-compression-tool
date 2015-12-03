package fr.an.screencast.compressor;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.compressor.imgstream.SlidingImageArray;
import fr.an.screencast.compressor.imgstream.VideoInputStream;
import fr.an.screencast.compressor.imgstream.codecs.cap.CapVideoInputStream;
import fr.an.screencast.compressor.imgstream.codecs.humbleio.HumbleioVideoInputStream;
import fr.an.screencast.compressor.imgtool.color.ColorMapAnalysis;
import fr.an.screencast.compressor.imgtool.delta.DeltaImageAnalysis;
import fr.an.screencast.compressor.imgtool.delta.DeltaImageAnalysisResult;
import fr.an.screencast.compressor.imgtool.utils.ImageData;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.ui.DeltaImageAnalysisPanel;
import fr.an.screencast.compressor.utils.ColorBarLookupTable;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.FileSerialisationUtils;
import fr.an.screencast.compressor.utils.ProgessPrinter;
import fr.an.screencast.compressor.utils.Pt;
import fr.an.screencast.compressor.utils.Rect;

public class DecodeApp {

    private static final Logger LOG = LoggerFactory.getLogger(DecodeApp.class);
    
    private static boolean DEBUG = false;
    private static boolean DEBUG_PAINT_DETAILED = true;
    
    private String filename;
    
    private boolean useCache = true;
    private File cacheDir = new File("compute-cache");
    
    private int subSamplingRate = 3;
    private int prevSlidingLen = 3; 
    
    /**
     * example: compute on file live-coding.cap  
     *   using LRU=4 => max LRU Change:28
     *   using LRU=10 => max LRU Change:16
     *   using LRU=15 => max LRU Change:
     */
    private int colorLRUSize = 10;
    

    private ProgessPrinter progressPrinter = new ProgessPrinter(System.out, 50, 1000); // print '.' every 50 frames,  "[frameIndex]" every 1000

    private int processFrameFreq = 1;  // 10;
    private int skipFrameCount = 0;
    private int skipAfterFrameIndex = Integer.MAX_VALUE; // 500;

    private boolean debugDrawFirstDiffPtMarker = false;
    
    private ColorBarLookupTable colorBarLookupTable = ColorBarLookupTable.getDefault();
    
    private Dim dim; // read from videoInput
    
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
        
        if (! cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    public void run() {
        try {
            processVideo();
        } catch(Exception ex) {
            LOG.error("Failed", ex);
        }
    }
    
    private VideoInputStream initVideo() {
        VideoInputStream videoInput;
        if (filename.endsWith(".cap")) {
            videoInput = new CapVideoInputStream(new File(filename));
        } else {
            videoInput = new HumbleioVideoInputStream(filename);
//            videoInput = new SubSamplingVideoInputStream(videoInput, subSamplingRate, SubSamplingVideoInputStream.DEFAULT_SAMPLER_RGB_MEDIAN);
        }
        
        videoInput.init();
        return videoInput;
    }
    
    private void processVideo() throws Exception {
        long sleepFrameMillis = 200;
        
        {
            VideoInputStream videoInput = initVideo();
            this.dim = videoInput.getDim();
            
            videoInput.close();
        }
        
        if (! filename.endsWith(".cap")) {
            sleepFrameMillis *= subSamplingRate;
        }
        
        
        SlidingImageArray slidingImages = new SlidingImageArray(prevSlidingLen, dim, BufferedImage.TYPE_INT_RGB);
        
        DeltaImageAnalysisResult deltaImages = new DeltaImageAnalysisResult(dim, BufferedImage.TYPE_INT_RGB); 
        BufferedImage diffImageRGB = deltaImages.getDiffImage();
        BufferedImage deltaImageRGB = deltaImages.getDeltaImage();
        
        BufferedImage[] bufferImgs = new BufferedImage[14];
        for (int i = 0; i < bufferImgs.length; i++) { 
            bufferImgs[i] = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
        }
        
        
        int frameRate = 5;

        LOG.info("decoding video : " + dim + " - display progress every " 
                + progressPrinter.getDisplayProgressFrequency() + " frames " + " = " + (progressPrinter.getDisplayProgressFrequency()/frameRate) + " s");

        JFrame appFrame = new JFrame();
        DeltaImageAnalysisPanel deltaAnalysisPanel = new DeltaImageAnalysisPanel();
        appFrame.getContentPane().add(deltaAnalysisPanel.getJComponent());
        appFrame.pack();
        appFrame.setVisible(true);
        appFrame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        LOG.info("SCAN 1/2 ... ColorMapAnalysis");
        doColorAnalysis(bufferImgs, deltaAnalysisPanel);
        
        LOG.info("SCAN 2/2 ... DeltaAnalysis");
        doDeltaAnalysis(sleepFrameMillis, slidingImages, diffImageRGB, deltaImageRGB, deltaAnalysisPanel);

    }

    private void doDeltaAnalysis(long sleepFrameMillis, SlidingImageArray slidingImages, BufferedImage diffImageRGB, BufferedImage deltaImageRGB,
            DeltaImageAnalysisPanel deltaAnalysisPanel) throws InterruptedException {
        BufferedImage prevImageRGB;
        progressPrinter.reset();
        DeltaImageAnalysis delta = null;
        File cacheDeltaAnalysisFile = new File(cacheDir, new File(filename).getName() + "-delta.ser");
        if (useCache && cacheDeltaAnalysisFile.exists() && cacheDeltaAnalysisFile.canRead()) {
            LOG.info("reading color analysis from cache file: " + cacheDeltaAnalysisFile);
            delta = FileSerialisationUtils.readFromFile(cacheDeltaAnalysisFile);
        } 
        if (delta == null) {
            delta = new DeltaImageAnalysis(dim, null, null);

            VideoInputStream videoInput = initVideo();
            int frameIndex = 0;
            if (skipFrameCount > 0) {
                LOG.info("SKIP " + skipFrameCount + " Frames");
                while(frameIndex < skipFrameCount && videoInput.readNextImage()) {
                    BufferedImage imageRGB = videoInput.getImage();
                    frameIndex++;
                    slidingImages.slide(imageRGB);
                }
            }
            while(videoInput.readNextImage()) {
                BufferedImage imageRGB = videoInput.getImage(); // read 3 images ... sub-sampling using median
                frameIndex++;
                progressPrinter.next();
                
                slidingImages.slide(imageRGB);
                
    //            if (videoInput.getFrameIndex() < prevSlidingLen) {
    //                continue;
    //            }
                prevImageRGB = slidingImages.getPrevImage()[1];
                // prevImageRGBDataInts = slidingImages.getPrevImageDataInts()[1];
                
                delta.setData(ImageRasterUtils.toInts(prevImageRGB), ImageRasterUtils.toInts(imageRGB));
                
                // *** compute diffs ***
                delta.computeDiff();
                
                debugDrawDeltaAnalysis(deltaAnalysisPanel, delta, diffImageRGB, deltaImageRGB, prevImageRGB, imageRGB);
    
                boolean debugRedo = false;
                if (debugRedo) {
                    delta.computeDiff();
                }
                
                
                Thread.sleep(sleepFrameMillis);
                
            }
            videoInput.close();
            videoInput = initVideo();
            
            if (useCache) {
                LOG.info("writing delta analysis to cache file: " + cacheDeltaAnalysisFile);
                FileSerialisationUtils.writeToFile(delta, cacheDeltaAnalysisFile);
            }
        }
    }

    private void doColorAnalysis(BufferedImage[] bufferImgs, 
            DeltaImageAnalysisPanel deltaAnalysisPanel) throws InterruptedException {
        progressPrinter.reset();
        ColorMapAnalysis colorAnalysis = null;
        File cacheColorAnalysisFile = new File(cacheDir, new File(filename).getName() + "-colormap.ser");
        if (useCache && cacheColorAnalysisFile.exists() && cacheColorAnalysisFile.canRead()) {
            LOG.info("reading color analysis from cache file: " + cacheColorAnalysisFile);
            colorAnalysis = FileSerialisationUtils.readFromFile(cacheColorAnalysisFile);
        } 
        if (colorAnalysis == null) {
            colorAnalysis = new ColorMapAnalysis(dim, colorLRUSize);
            
            VideoInputStream videoInput = initVideo();
            int frameIndex = 0;
            if (skipFrameCount > 0) {
                LOG.info("SKIP " + skipFrameCount + " Frames");
                while(frameIndex < skipFrameCount && videoInput.readNextImage()) {
                    // BufferedImage imageRGB = videoInput.getImage();
                    frameIndex++;
                }
            }
            while(videoInput.readNextImage()) {
                BufferedImage imageRGB = videoInput.getImage();
                frameIndex++;
                if (frameIndex % processFrameFreq != 0) {
                    continue; // heuristic optim: SKIP frames...
                }
                progressPrinter.next();

                colorAnalysis.processImage(imageRGB);
                                
                if (frameIndex >= skipAfterFrameIndex) {
                    // TODO slow ... heuristic optim: skip remaining 
                    break;
                }
            }
            videoInput.close();
            
            if (useCache) {
                LOG.info("writing color analysis to cache file: " + cacheColorAnalysisFile);
                try {
                    FileSerialisationUtils.writeToFile(colorAnalysis, cacheColorAnalysisFile);
                } catch(Exception ex) {
                    LOG.error("Failed to write to cache file: " + cacheColorAnalysisFile, ex);
                }
            }
            
            colorAnalysis.dump();
    
        }
        debugDrawColorAnalysis(deltaAnalysisPanel, colorAnalysis, bufferImgs);
    }

    private void debugDrawDeltaAnalysis(DeltaImageAnalysisPanel deltaAnalysisPanel, DeltaImageAnalysis delta, 
            BufferedImage diffImageRGB, BufferedImage deltaImageRGB, BufferedImage prevImageRGB, BufferedImage imageRGB) {

        { 
            Graphics2D diffGc = diffImageRGB.createGraphics();
            diffGc.setColor(Color.WHITE); // BLACK
            diffGc.fillRect(0, 0, dim.width, dim.height);

            ColorModel cm = prevImageRGB.getColorModel();
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
        }
        
        Graphics2D deltaGc = deltaImageRGB.createGraphics();
        deltaGc.setColor(Color.BLACK);
        deltaGc.fillRect(0,  0, dim.width, dim.height);

        
        
        Pt firstDiffPt = delta.getFirstDiffPt(); 
                // delta.getRawFirstDiffPtx();
        if (firstDiffPt.x != -1) {
            
            // show integral diff
            boolean showDiffCountIntegralImage = true; 
            if (showDiffCountIntegralImage) {
                ImageData diffCountIntegralImageData = 
                        // delta.getDiffCountIntegralImageData();
                        // delta.getDiffCountHorizontalIntegralImageData();
                        delta.getDiffVerticalIntegralImageData();
                int[] integral = diffCountIntegralImageData.getData();
                int[] horIntegral = delta.getDiffHorizontalIntegralImageData().getData();
                int[] vertIntegral = delta.getDiffVerticalIntegralImageData().getData();
                for(int y = 0, idx_xy=0; y < dim.height; y++) {
                    for (int x = 0; x < dim.width; x++,idx_xy++) {
                        int count = // integral[idx_xy];
                            horIntegral[idx_xy] + vertIntegral[idx_xy];
                        int countClam = (count != 0)? 50+count : 0; 
                        deltaImageRGB.setRGB(x, y, RGBUtils.rgb2Int256(countClam, countClam, countClam, 0));
                    }                            
                }
            }
            
            
            List<Rect> diffRects = delta.getDiffRects();
            if (! diffRects.isEmpty()) {
   //                Rectangle diffRect = delta.getDiffRect();
   //                System.out.println("  diff rect: " + diffRect);
   //                
   //                deltaGc.setColor(Color.GRAY);
   //                // deltaGc.fillRect(diffRect.x, diffRect.y, diffRect.width, diffRect.height);
   //                deltaGc.drawRect(diffRect.x-1, diffRect.y-1, diffRect.width+1, diffRect.height+1);
                
                deltaGc.setColor(Color.RED);
                int thick = 2;
                deltaGc.setStroke(new BasicStroke(thick));
                for(Rect r : diffRects) {
                    // draw enlarge thick pixel
                    deltaGc.drawRect(r.fromX-thick, r.fromY-thick, r.getWidth()+2*thick, r.getHeight()+2*thick);
                }
            }
            

            if (debugDrawFirstDiffPtMarker) {
                deltaGc.setColor(Color.ORANGE);
                int markerLineLen = 100;
   //                deltaGc.drawLine(firstDiffPtx-1, firstDiffPty-1-markerLineLen, firstDiffPtx-1, firstDiffPty-1);
   //                deltaGc.drawLine(firstDiffPtx-1-markerLineLen, firstDiffPty-1, firstDiffPtx-1, firstDiffPty-1);
                deltaGc.setStroke(new BasicStroke(3));
                deltaGc.drawLine(firstDiffPt.x-markerLineLen, firstDiffPt.y-markerLineLen, firstDiffPt.x, firstDiffPt.y);
                if (firstDiffPt.x < 10 || firstDiffPt.y < 10) {
                    int markerLen = 20;
                    deltaGc.fillRect(firstDiffPt.x-markerLen, firstDiffPt.y-markerLen, 2*markerLen, 2*markerLen);
                }
            }
        }
        
        
        deltaGc.dispose();
        
        deltaAnalysisPanel.asyncSetImages(prevImageRGB, imageRGB, diffImageRGB, deltaImageRGB);
    }

    private void debugDrawColorAnalysis(DeltaImageAnalysisPanel deltaAnalysisPanel, ColorMapAnalysis colorAnalysis, 
            BufferedImage[] bufferImgs)
            throws InterruptedException {
        {
            LOG.info("display min-max images");
            
            BufferedImage minImg = bufferImgs[0];
            BufferedImage maxImg = bufferImgs[1];
            BufferedImage minR = bufferImgs[2];
            BufferedImage maxR = bufferImgs[3];
            BufferedImage minG = bufferImgs[4];
            BufferedImage maxG = bufferImgs[5];
            BufferedImage minB = bufferImgs[6];
            BufferedImage maxB = bufferImgs[7];
            BufferedImage rangeImg = bufferImgs[8];
            BufferedImage rangeR = bufferImgs[9];
            BufferedImage rangeG = bufferImgs[10];
            BufferedImage rangeB = bufferImgs[11];
            
            colorAnalysis.debugDrawMinMax(colorBarLookupTable, 
                    minImg, maxImg, 
                    minR, maxR, minG, maxG, minB, maxB,
                    rangeImg, rangeR, rangeG, rangeB);
            
            deltaAnalysisPanel.asyncSetImages(minImg, maxImg, minR, maxR);
            Thread.sleep(1000);
            
            if (DEBUG_PAINT_DETAILED) {
                LOG.info("display min (total,R,G,B) images");
                deltaAnalysisPanel.asyncSetImages(minImg, minR, minG, maxB);
                Thread.sleep(1000);
                
                LOG.info("display max (total,R,G,B) images");
                deltaAnalysisPanel.asyncSetImages(maxImg, maxR, maxG, maxB);
                Thread.sleep(1000);
   
                LOG.info("display range (total,R,G,B) images");
                deltaAnalysisPanel.asyncSetImages(rangeImg, rangeR, rangeG, rangeB);
                Thread.sleep(1000);
   
            }
        }
            
        {
            LOG.info("display ColorMap Size per Pixel (using " + colorLRUSize + " LRU Colors while scanning)");
            BufferedImage countLRUColorImg = bufferImgs[0];
            BufferedImage countLRUColorLowImg = bufferImgs[1];
            BufferedImage countLRUColorMidImg = bufferImgs[2];
            BufferedImage countLRUColorHighImg = bufferImgs[3];
            
            colorAnalysis.debugDrawLRUColorCounts(colorBarLookupTable, countLRUColorImg, countLRUColorLowImg, countLRUColorMidImg, countLRUColorHighImg);
            
            deltaAnalysisPanel.asyncSetImages(countLRUColorImg, countLRUColorLowImg, countLRUColorMidImg, countLRUColorHighImg);
            
            // TODO TOADD draw frequency of color swaps per pixel 
            
            Thread.sleep(1000);
        }
    }

}

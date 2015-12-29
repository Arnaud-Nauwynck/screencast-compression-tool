package fr.an.screencast.ui;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.compressor.imgstream.VideoStreamFactory;
import fr.an.screencast.compressor.imgstream.codecs.deltabitstream.DeltaOpFrame2BitStreamStructDataEncoder;
import fr.an.screencast.compressor.imgtool.color.ColorMapAnalysis;
import fr.an.screencast.compressor.imgtool.delta.DeltaImageAnalysisProcessor;
import fr.an.screencast.compressor.imgtool.delta.DeltaImageAnalysisResult;
import fr.an.screencast.compressor.imgtool.delta.DeltaOperation;
import fr.an.screencast.compressor.imgtool.delta.FrameDelta;
import fr.an.screencast.compressor.imgtool.delta.FrameDeltaDetailed;
import fr.an.screencast.compressor.imgtool.delta.FrameRectDelta;
import fr.an.screencast.compressor.imgtool.delta.ops.AddAndDrawGlyphRectDeltaOp;
import fr.an.screencast.compressor.imgtool.delta.ops.AddGlyphDeltaOp;
import fr.an.screencast.compressor.imgtool.delta.ops.DrawGlyphRectDeltaOp;
import fr.an.screencast.compressor.imgtool.delta.ops.DrawRectImageDeltaOp;
import fr.an.screencast.compressor.imgtool.delta.ops.FillRectDeltaOp;
import fr.an.screencast.compressor.imgtool.delta.ops.MostUsedColorFillRectDeltaOp;
import fr.an.screencast.compressor.imgtool.delta.ops.RestorePrevImageRectDeltaOp;
import fr.an.screencast.compressor.imgtool.utils.RGBUtils;
import fr.an.screencast.compressor.utils.ColorBarLookupTable;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.FileSerialisationUtils;
import fr.an.screencast.compressor.utils.ProgessPrinter;
import fr.an.screencast.compressor.utils.Rect;
import fr.an.screencast.player.ScreenPlayerListener.ScreenPlayerListenerAdapter;
import fr.an.screencast.player.VideoStreamPlayer;
import fr.an.screencast.ui.internal.DeltaImageAnalysisPanel;
import fr.an.screencast.ui.internal.PlayerStatusBar;
import fr.an.screencast.ui.internal.PlayerToolbar;

public class DecodeDiffView {

    private static final Logger LOG = LoggerFactory.getLogger(DecodeDiffView.class);
    
    private static boolean DEBUG = false;
    private static boolean DEBUG_PAINT_DETAILED = true;
    private boolean debugDraw = 
            true; 
//            false; // use false to benchmark compression performances
    
    private String filename;
    
    private boolean useCache = false;
    private File cacheDir = new File("compute-cache");
    
    private int prevSlidingLen = 3;
    private int perPixelLRUHistory = 8;
    
    /**
     * example: compute on file live-coding.cap  
     *   using LRU=4 => max LRU Change:28
     *   using LRU=10 => max LRU Change:16
     *   using LRU=15 => max LRU Change:
     */
    private int colorLRUSize = 10;
    
    private int frameRate = 5;
    private ProgessPrinter progressPrinter = new ProgessPrinter(System.out, frameRate, 50, 1000); // print '.' every 50 frames,  "[frameIndex]" every 1000
        
    private ColorBarLookupTable colorBarLookupTable = ColorBarLookupTable.getDefault();
    
    private Dim dim; // read from videoInput

    private VideoStreamPlayer videoStreamPlayer;
    
    private JPanel mainPanel;
    private DeltaImageAnalysisPanel deltaAnalysisPanel;
    private PlayerToolbar playerToolBar;
    private PlayerStatusBar playerStatusBar;
    
    private DeltaImageAnalysisProcessorListener innerDeltaImageAnalysisProcessor;
    
    // ------------------------------------------------------------------------

    public static void main(String[] args) throws InterruptedException, IOException {
        DecodeDiffView view = new DecodeDiffView();
        view.parseArgs(args);
        view.run();
    }

    public DecodeDiffView() {
        this.videoStreamPlayer = new VideoStreamPlayer(VideoStreamFactory.getDefaultInstance());
                
        this.mainPanel = new JPanel(new BorderLayout());
        this.playerToolBar = new PlayerToolbar(videoStreamPlayer);
        this.playerStatusBar = new PlayerStatusBar(videoStreamPlayer);
        this.deltaAnalysisPanel = new DeltaImageAnalysisPanel();
        
        mainPanel.add(playerToolBar.getJComponent(), BorderLayout.NORTH);
        mainPanel.add(deltaAnalysisPanel.getJComponent(), BorderLayout.CENTER);
        mainPanel.add(playerStatusBar.getJComponent(), BorderLayout.SOUTH);
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
            File testInputFile = new File("data/live-coding.cap");
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
        videoStreamPlayer.setInputFile(inputFile);

        if (! cacheDir.exists()) {
            cacheDir.mkdirs();
        }
    }

    public void run() {
        JFrame frame = new JFrame();
                
        frame.getContentPane().add(mainPanel, BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        try {
            processVideo();
        } catch(Exception ex) {
            LOG.error("Failed", ex);
        }
    }
    
    private class DeltaImageAnalysisProcessorListener extends ScreenPlayerListenerAdapter {
        VideoStreamPlayer videoStreamPlayer;
        File cacheDeltaAnalysisFile;
        DeltaImageAnalysisResult deltaResult;
        DeltaImageAnalysisProcessor deltaImageAnalysisProcessor;
        
        DeltaOpFrame2BitStreamStructDataEncoder deltaOpFrameBitsEncoder;
        
        public DeltaImageAnalysisProcessorListener(VideoStreamPlayer videoStreamPlayer, File cacheDeltaAnalysisFile, 
                DeltaImageAnalysisResult deltaResult, FrameDeltaDetailed deltaFrameDetailed,
                File deltaOpFrameEncoderOutputFile) {
            this.videoStreamPlayer = videoStreamPlayer;
            this.cacheDeltaAnalysisFile = cacheDeltaAnalysisFile;
            this.deltaResult = deltaResult;
            this.deltaImageAnalysisProcessor = new DeltaImageAnalysisProcessor(deltaResult, deltaFrameDetailed, prevSlidingLen, perPixelLRUHistory);
            this.deltaOpFrameBitsEncoder = new DeltaOpFrame2BitStreamStructDataEncoder(deltaOpFrameEncoderOutputFile);
        }
        
        @Override
        public void onInit(Dim dim) {
            DecodeDiffView.this.dim = dim;
            
            deltaImageAnalysisProcessor.init(dim);
            
            deltaOpFrameBitsEncoder.init(dim);
            
            progressPrinter.reset();
            LOG.info("decoding video : " + dim + " - " + progressPrinter.toStringFrequencyInfo());
        }

        @Override
        public void onPlayerStopped() {
            if (useCache) {
                LOG.info("writing delta analysis to cache file: " + cacheDeltaAnalysisFile);
                FileSerialisationUtils.writeToFile(deltaResult, cacheDeltaAnalysisFile);
            }
            
            deltaOpFrameBitsEncoder.close();
        }

        @Override
        public void showNewImage(BufferedImage imageRGB) {
            progressPrinter.next();
            int frameIndex = videoStreamPlayer.getFrameCount();
            
            FrameDeltaDetailed frameDeltaDetailed = deltaImageAnalysisProcessor.processImage(frameIndex, imageRGB);
            
            deltaOpFrameBitsEncoder.encodeDeltaOpFrame(frameDeltaDetailed, imageRGB);
        }
        
    }
    
    private class DeltaImageAnalysisViewerListener extends ScreenPlayerListenerAdapter {
        VideoStreamPlayer videoStreamPlayer;
        DeltaImageAnalysisResult deltaResult;
        FrameDeltaDetailed deltaFrameDetailed;
        int frameDeltaIndex = 0;

        BufferedImage prevImageRGB;
        BufferedImage currImageRGB;
        BufferedImage diffImageRGB;
        BufferedImage deltaImageRGB;
        
        Graphics2D deltaGc;

        public DeltaImageAnalysisViewerListener(VideoStreamPlayer videoStreamPlayer, DeltaImageAnalysisResult deltaResult, FrameDeltaDetailed deltaFrameDetailed) {
            this.videoStreamPlayer = videoStreamPlayer;
            this.deltaResult = deltaResult;
            this.deltaFrameDetailed = deltaFrameDetailed;
        }

        @Override
        public void onInit(Dim dim) {
            DecodeDiffView.this.dim = dim;
            prevImageRGB = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
            currImageRGB = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
            diffImageRGB = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
            deltaImageRGB = new BufferedImage(dim.width, dim.height, BufferedImage.TYPE_INT_RGB);
            
            deltaGc = deltaImageRGB.createGraphics();

            deltaGc.setColor(Color.BLACK);
            deltaGc.fillRect(0,  0, dim.width, dim.height);
        }
        
        @Override
        public void showNewImage(BufferedImage imageRGB) {
            FrameDelta frameDelta = deltaResult.getLastFrameDelta();
            int frameCount = videoStreamPlayer.getFrameCount();
            
            if (! videoStreamPlayer.isFastForward() || (frameCount%25 == 0)) {
                // slide previous and copy current 
                BufferedImage tmp = prevImageRGB;
                prevImageRGB = currImageRGB;
                currImageRGB = tmp;
                imageRGB.copyData(currImageRGB.getRaster());


                debugDrawDeltaAnalysis(prevImageRGB, currImageRGB, frameDelta, diffImageRGB, deltaGc);


                BufferedImage bottomLeftImg = diffImageRGB;
                // temporary display reduce color img
                if (deltaFrameDetailed != null) {
                    bottomLeftImg = deltaFrameDetailed.getColorReduceImg();
                }
            
                deltaAnalysisPanel.asyncSetImages(prevImageRGB, currImageRGB, bottomLeftImg, deltaImageRGB);

                deltaGc.setColor(Color.BLACK);
                deltaGc.fillRect(0,  0, dim.width, dim.height);
            } else {
                // accumulate delta changes in image
                debugDrawDelta(deltaGc, frameDelta);

            }
        }
    }
    
    
    private void processVideo() throws Exception {
        DeltaImageAnalysisResult deltaResult = null;
        File cacheDeltaAnalysisFile = new File(cacheDir, new File(filename).getName() + "-delta.ser");
        File deltaOpFrameEncoderOutputFile = new File(cacheDir, new File(filename).getName() + "-deltaop-video.bin");
        FrameDeltaDetailed deltaFrameDetailed = new FrameDeltaDetailed();
        if (useCache && cacheDeltaAnalysisFile.exists() && cacheDeltaAnalysisFile.canRead()) {
            LOG.info("reading color analysis from cache file: " + cacheDeltaAnalysisFile);
            deltaResult = FileSerialisationUtils.readFromFile(cacheDeltaAnalysisFile);
        } else {
            deltaResult = new DeltaImageAnalysisResult();
            innerDeltaImageAnalysisProcessor = new DeltaImageAnalysisProcessorListener(videoStreamPlayer, cacheDeltaAnalysisFile, deltaResult, deltaFrameDetailed,
                deltaOpFrameEncoderOutputFile);
            
            videoStreamPlayer.addListener(innerDeltaImageAnalysisProcessor);
        }

        if (debugDraw) {
            DeltaImageAnalysisViewerListener viewerListener = new DeltaImageAnalysisViewerListener(videoStreamPlayer, deltaResult, deltaFrameDetailed); 
            videoStreamPlayer.addListener(viewerListener);
        }
        
        videoStreamPlayer.init();
    }


//    private void doColorAnalysis(BufferedImage[] bufferImgs, 
//            DeltaImageAnalysisPanel deltaAnalysisPanel) throws InterruptedException {
//        progressPrinter.reset();
//        ColorMapAnalysis colorAnalysis = null;
//        File cacheColorAnalysisFile = new File(cacheDir, new File(filename).getName() + "-colormap.ser");
//        if (useCache && cacheColorAnalysisFile.exists() && cacheColorAnalysisFile.canRead()) {
//            LOG.info("reading color analysis from cache file: " + cacheColorAnalysisFile);
//            colorAnalysis = FileSerialisationUtils.readFromFile(cacheColorAnalysisFile);
//        } 
//        if (colorAnalysis == null) {
//            colorAnalysis = new ColorMapAnalysis(dim, colorLRUSize);
//            
//            VideoInputStream videoInput = initVideo();
//            int frameIndex = 0;
//            if (skipFrameCount > 0) {
//                LOG.info("SKIP " + skipFrameCount + " Frames");
//                while(frameIndex < skipFrameCount && videoInput.readNextImage()) {
//                    // BufferedImage imageRGB = videoInput.getImage();
//                    frameIndex++;
//                }
//            }
//            while(videoInput.readNextImage()) {
//                BufferedImage imageRGB = videoInput.getImage();
//                progressPrinter.next();
//                frameIndex++;
//                if (frameIndex % processFrameFreq != 0) {
//                    continue; // skip frame
//                }
//
//                colorAnalysis.processImage(imageRGB);
//                                
//                if (frameIndex >= skipAfterFrameIndex) {
//                    // TODO slow ... heuristic optim: skip remaining 
//                    break;
//                }
//            }
//            videoInput.close();
//            
//            if (useCache) {
//                LOG.info("writing color analysis to cache file: " + cacheColorAnalysisFile);
//                try {
//                    FileSerialisationUtils.writeToFile(colorAnalysis, cacheColorAnalysisFile);
//                } catch(Exception ex) {
//                    LOG.error("Failed to write to cache file: " + cacheColorAnalysisFile, ex);
//                }
//            }
//            
//            colorAnalysis.dump();
//    
//        }
//        debugDrawColorAnalysis(deltaAnalysisPanel, colorAnalysis, bufferImgs);
//    }

    private void debugDrawDeltaAnalysis(
            BufferedImage prevImageRGB, BufferedImage currImageRGB,
            FrameDelta frameDelta, 
            BufferedImage diffImageRGB, Graphics2D deltaGc) {
        
        { 
            Graphics2D diffGc = diffImageRGB.createGraphics();
            diffGc.setColor(Color.WHITE); // BLACK
            diffGc.fillRect(0, 0, dim.width, dim.height);

            ColorModel cm = prevImageRGB.getColorModel();
            for(int y = 0; y < dim.height; y++) {
                for (int x = 0; x < dim.width; x++) {
                    int prevRgb = prevImageRGB .getRGB(x, y);
                    int rgb = currImageRGB.getRGB(x, y);
                    int dr = Math.abs(cm.getRed(prevRgb) - cm.getRed(rgb));
                    int dg = Math.abs(cm.getGreen(prevRgb) - cm.getGreen(rgb));
                    int db = Math.abs(cm.getBlue(prevRgb) - cm.getBlue(rgb));
                    int diff = (dr + dg + db);
                    int diffColor = (diff != 0)? RGBUtils.rgb2Int256(dr*2, dg*2, db*2, 0) : 0;
                    diffImageRGB.setRGB(x, y, diffColor);
                }
            }
            diffGc.dispose();
        }
        
        // no fill... accumulate changes
        // deltaGc.setColor(Color.BLACK);
        // deltaGc.fillRect(0,  0, dim.width, dim.height);

        debugDrawDelta(deltaGc, frameDelta);
        
    }

    private void debugDrawDelta(Graphics2D deltaGc, FrameDelta frameDelta) {
        
        List<FrameRectDelta> deltas = (frameDelta != null)? frameDelta.getDeltas() : Collections.emptyList();
        if (! deltas.isEmpty()) {
            // LOG.info("found diff rects:" + rects.size());
            deltaGc.setColor(Color.RED);
            int thick = 2;
            deltaGc.setStroke(new BasicStroke(thick));
            int thick2 = thick*2;
            
            for(FrameRectDelta delta : deltas) {
                // draw thick pixel (reduce rect for border)
                Rect r = delta.getRect();

                deltaGc.setColor(Color.RED);

                List<DeltaOperation> deltaOps = delta.getDeltaOperations();
                if (deltaOps != null && !deltaOps.isEmpty()) {
                    for(DeltaOperation op : deltaOps) {
    
                        if (op instanceof FillRectDeltaOp) {
                            FillRectDeltaOp op2 = (FillRectDeltaOp) op;
                            deltaGc.setColor(Color.WHITE);
                            
                        } else if (op instanceof DrawRectImageDeltaOp) {
                            DrawRectImageDeltaOp op2 = (DrawRectImageDeltaOp) op;
                            deltaGc.setColor(Color.RED);
                            
                        } else if (op instanceof RestorePrevImageRectDeltaOp) {
                            // found restore op for rect! => draw in blue
                            RestorePrevImageRectDeltaOp restoreOp = (RestorePrevImageRectDeltaOp) op;
                            List<Rect> detailedMergeRects = restoreOp.getDetailedMergeRects();
                            if (detailedMergeRects.size() > 1) {
                                Color lightBlue = new Color(0, 0, 50);
                                deltaGc.setColor(lightBlue);
                                r.graphicsFillRect(deltaGc);
    
                                deltaGc.setColor(Color.BLUE);
                                for(Rect detailedMergeRect : detailedMergeRects) {
                                    detailedMergeRect.graphicsDrawRectErode(deltaGc, 1);
                                }
                            }
                            deltaGc.setColor(Color.BLUE);
                        } else if (op instanceof MostUsedColorFillRectDeltaOp) {
                            MostUsedColorFillRectDeltaOp op2 = (MostUsedColorFillRectDeltaOp) op;
                            Rect subRect = op2.getRect();
                            int color = op2.getFillColor();
                            deltaGc.setColor(new Color(color));
                            subRect.graphicsFillRect(deltaGc);
                            // op.getColorCount();
                            
                        } else if (op instanceof AddAndDrawGlyphRectDeltaOp) {
                            // AddAndDrawGlyphRectDeltaOp op2 = (AddAndDrawGlyphRectDeltaOp) op;
                            // Rect opRect = op2.getRect();
                            deltaGc.setColor(Color.ORANGE);
    
                        } else if (op instanceof DrawGlyphRectDeltaOp) {
                            // DrawGlyphRectDeltaOp op2 = (DrawGlyphRectDeltaOp) op;
                            // Rect opRect = op2.getRect();
                            deltaGc.setColor(Color.GREEN);
    
                        } else if (op instanceof AddGlyphDeltaOp) {
                            // nothing to draw!
                        
                        } else {
                            // unrecognised DeltaOp !!
                        }
                    }
                }
                r.graphicsDrawRectErode(deltaGc, thick2);
                
                // TODO paint within rect
            }
        }
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

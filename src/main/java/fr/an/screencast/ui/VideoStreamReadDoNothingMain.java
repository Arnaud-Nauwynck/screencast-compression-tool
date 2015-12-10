package fr.an.screencast.ui;

import java.awt.image.BufferedImage;
import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.compressor.imgstream.VideoInputStream;
import fr.an.screencast.compressor.imgstream.VideoOutputStream;
import fr.an.screencast.compressor.imgstream.VideoStreamFactory;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.ProgessPrinter;

public class VideoStreamReadDoNothingMain {
    
    private static final Logger LOG = LoggerFactory.getLogger(VideoStreamReadDoNothingMain.class);
    
    private File inputFile;
    private VideoStreamFactory videoStreamFactory = VideoStreamFactory.getDefaultInstance();
    

    public static void main(String[] args) {
        try {
            VideoStreamReadDoNothingMain app = new VideoStreamReadDoNothingMain();
            app.parseArgs(args);
            app.run();
            
            System.out.println("Finished");
        } catch(Exception ex) {
            System.err.println("Failed ... exiting");
            ex.printStackTrace(System.err);
        }
    }

    private void parseArgs(String[] args) {
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("-i".equals(arg)) {
                inputFile = new File(args[++i]);
            } else {
                throw new RuntimeException("Failed unrecognized arg '" + arg + "'");
            }
        }
    }
    
    private void run() {
        try (VideoInputStream in = videoStreamFactory.createVideoInputStream(inputFile)) {
            in.init();
            
            int frameRate = 5;
            Dim dim = in.getDim();
            LOG.info("reading video file '" + inputFile + "', dim: " + dim);
            
            ProgessPrinter progressPrinter = new ProgessPrinter(System.out, frameRate, 50, 1000); // print '.' every 50 frames,  "[frameIndex]" every 1000

            long startTime = System.currentTimeMillis();
            
            int frameIndex = 0;
            while(in.readNextImage()) {
               BufferedImage img = in.getImage();
               
               frameIndex++;
               progressPrinter.next();
            }
            progressPrinter.println();
            
            long millis = System.currentTimeMillis() - startTime;
            LOG.info("done read " + frameIndex + " frames, took " + millis + " ms (=" + (1000*frameIndex/millis) + " ms/1000xframes)");
        }
    }

}

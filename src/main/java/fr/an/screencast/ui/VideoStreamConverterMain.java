package fr.an.screencast.ui;

import java.awt.image.BufferedImage;
import java.io.File;

import fr.an.screencast.compressor.imgstream.VideoInputStream;
import fr.an.screencast.compressor.imgstream.VideoOutputStream;
import fr.an.screencast.compressor.imgstream.VideoStreamFactory;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.ProgessPrinter;

public class VideoStreamConverterMain {

    private File inputFile;
    private File outputFile;
    private VideoStreamFactory videoStreamFactory = VideoStreamFactory.getDefaultInstance();
    

    public static void main(String[] args) {
        try {
            VideoStreamConverterMain app = new VideoStreamConverterMain();
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
            } else if ("-o".equals(arg)) {
                outputFile = new File(args[++i]);
            } else {
                throw new RuntimeException("Failed unrecognized arg '" + arg + "'");
            }
        }
    }
    
    private void run() {
        try (VideoInputStream in = videoStreamFactory.createVideoInputStream(inputFile)) {
            try (VideoOutputStream out = videoStreamFactory.createVideoOutputStream(outputFile)) {
                
                in.init();
                
                Dim dim = in.getDim();
                out.init(dim);
                
                int frameRate = 5;
                long millisBetweenFrame = 1000/frameRate;
                
                ProgessPrinter progressPrinter = new ProgessPrinter(System.out, frameRate, 50, 1000); // print '.' every 50 frames,  "[frameIndex]" every 1000

                
                int frameIndex = 0;
                while(in.readNextImage()) {
                   BufferedImage img = in.getImage();
                   
                   long frameTime = frameIndex * millisBetweenFrame;
                   
                   // convert?
                   out.addFrame(frameIndex, frameTime, img);
                   
                   frameIndex++;
                   progressPrinter.next();
                }
            }
        }
    }

}

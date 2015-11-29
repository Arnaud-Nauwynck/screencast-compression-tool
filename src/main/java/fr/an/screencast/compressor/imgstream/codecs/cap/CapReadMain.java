package fr.an.screencast.compressor.imgstream.codecs.cap;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.compressor.utils.Dim;

public class CapReadMain {
    
    private static final Logger LOG = LoggerFactory.getLogger(CapReadMain.class);
    
    private boolean DEBUG = false;
    private File inputFile;
    
    // ------------------------------------------------------------------------
    
    public static void main(String[] args) throws InterruptedException, IOException {
        CapReadMain app = new CapReadMain();
        app.parseArgs(args);
        try {
            app.run();
            System.out.println("Finished , exiting");
        } catch(Exception ex) {
            LOG.error("Failed", ex);
            System.err.println("Failed, exiting");
        }
    }

    public CapReadMain() {
    }

    // ------------------------------------------------------------------------
    
    private void parseArgs(String[] args) {
        for(int i = 0; i < args.length; i++) {
            if (args[i].equals("-i")) {
                inputFile = new File(args[++i]);
            } else if (args[i].equals("--debug")) {
                DEBUG = true;
            } else {
                throw new RuntimeException("Unrecognised arg " + args[i]);
            }
        }

        if (! inputFile.exists()) {
            throw new RuntimeException("File not found");
        }
    }
    
    public void run() throws Exception {
        LOG.info("reading .cap file: " + inputFile);
        try(CapVideoInputStream capVideoInputStream = new CapVideoInputStream(inputFile)) {
            capVideoInputStream.init();
            
            Dim dim = capVideoInputStream.getDim();
            int frameRate = 5;
            int displayProgressEvery = 10 * frameRate; // 10s
            LOG.info("decoding video : " + dim + " - display progress every " + displayProgressEvery + " frames = " + (displayProgressEvery/frameRate) + " s");
            int displayFrameCountEvery = 1000;
            
            int frameCount = 0;
            while(capVideoInputStream.readNextImage()) {
                BufferedImage frameImage = capVideoInputStream.getImage();
                frameCount++;
                
                if (DEBUG) {
                    System.out.println("[" + frameCount + "]");
                }
                if (frameCount % displayProgressEvery == 0) {
                    System.out.print(".");
                }
                if (frameCount % displayFrameCountEvery == 0) {
                    System.out.print("\n[" + frameCount + "] ");
                }
            }
        }
    }
}

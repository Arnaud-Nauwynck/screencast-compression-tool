package fr.an.screencast.compressor.imgstream.codecs.cap;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.ProgessPrinter;

public class CapReadMain {
    
    private static final Logger LOG = LoggerFactory.getLogger(CapReadMain.class);
    
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
            ProgessPrinter progress = new ProgessPrinter(System.out, frameRate, 50, 1000);
            LOG.info("decoding video : " + dim + " - " + progress.toStringFrequencyInfo());
            
            while(capVideoInputStream.readNextImage()) {
                BufferedImage frameImage = capVideoInputStream.getImage();
                if (frameImage == null) continue;
                
                progress.next();
            }
        }
    }
}

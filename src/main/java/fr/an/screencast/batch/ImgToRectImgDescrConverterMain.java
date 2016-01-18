package fr.an.screencast.batch;

import java.awt.image.BufferedImage;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.io.FileUtils;

import fr.an.bitwise4j.bits.BitOutputStream;
import fr.an.bitwise4j.bits.OutputStreamToBitOutputStream;
import fr.an.bitwise4j.encoder.structio.BitStreamStructDataOutput;
import fr.an.bitwise4j.encoder.structio.StructDataOutput;
import fr.an.bitwise4j.util.RuntimeIOException;
import fr.an.screencast.compressor.imgtool.rectdescr.RectImgDescrAnalyzer;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.RectImgDescrAST.RectImgDescr;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.codec.BitStreamOutputRectImgDescrVisitor;
import fr.an.screencast.compressor.imgtool.rectdescr.ast.codec.RectImgDescrCodecConfig;
import fr.an.screencast.compressor.imgtool.utils.ImageIOUtils;
import fr.an.screencast.compressor.imgtool.utils.ImageRasterUtils;
import fr.an.screencast.compressor.utils.Dim;
import fr.an.screencast.compressor.utils.Rect;

/**
 * batch conversion tool to convert input file "png", "bmp"... into compressed ".rimgd" (=Rectangular Image Description)
 */
public class ImgToRectImgDescrConverterMain {

    private static final String DEFAULT_FILE_SUFFIX_RECT_IMG_DESR = ".rimgd";

    private File inputFile;
    private File destDir;
    private String outputFilename;
    
    private boolean inputFilesListFromStdin = false;
    private int parallelCount = 6;
    private long skipFileSmallerThan = 1024*5;
    
    private boolean verboseMode = false;
    private boolean dryRun = false;
    
    
    private RectImgDescrCodecConfig codecConfig = new RectImgDescrCodecConfig();

    private static final NumberFormat NUMBER_FMT_1 = new DecimalFormat("#.#");
    
    public static void main(String[] args) {
        ImgToRectImgDescrConverterMain app = new ImgToRectImgDescrConverterMain();
        try {
            app.parseArgs(args);
            app.run();
        } catch(Exception ex) {
            ex.printStackTrace(System.err);
            System.err.println("Failed .. exiting");
            System.exit(-1);
        }
    }

    public void parseArgs(String[] args) {
        for(int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("-i".equals(arg)) {
                inputFile = new File(args[++i]);
            } else if ("--inputFilesListFromStdin".equals(arg) || "-stdin".equals(arg)) {
                inputFilesListFromStdin = true;
            } else if ("-j".equals(arg)) {
                parallelCount = Integer.parseInt(args[++i]);
            } else if ("-d".equals(arg)) {
                destDir = new File(args[++i]);
            } else if ("-o".equals(arg)) {
                outputFilename = args[++i];
            } else if ("-v".equals(arg)) {
                verboseMode = true;
            } else if ("--dryRun".equals(arg)) {
                dryRun = true;
            } else {
                throw new IllegalArgumentException("Unrecognized argument '" + arg + "'");
            }
        }
    }
    
    public void run() {
        if (! inputFilesListFromStdin) {
            if (inputFile == null) {
                throw new IllegalArgumentException("expecting inputFile argument '-i'");
            }
            if (destDir == null) {
                destDir = inputFile.getParentFile(); 
            }
            if (outputFilename == null) {
                outputFilename = toFilenameSuffixRectImgDescr(inputFile.getName());
            }
        
            File outputFile = new File(destDir, outputFilename);
            doConvertFile(inputFile, outputFile);
        } else {
            BufferedReader stdinReader = new BufferedReader(new InputStreamReader(System.in));
            doConvertMultipleFilesFromLineReader(stdinReader);
        }
    }

    private String toFilenameSuffixRectImgDescr(String name) {
        int lastIndexDot = name.lastIndexOf('.');
        if (lastIndexDot != -1) {
            name = name.substring(0, lastIndexDot);
        }
        name += DEFAULT_FILE_SUFFIX_RECT_IMG_DESR;
        return name;
    }

    private void doConvertFile(File inputFile, File outputFile) {
        long startTime = System.currentTimeMillis();
        // step 1/3: read file format ("png", "bmp", ...)
        BufferedImage img = ImageIOUtils.read(null, inputFile);
                
        // step 2/3: analyse image for AST rectangular graphics drawing primitives 
        Dim dim = new Dim(img.getWidth(), img.getHeight());
        Rect imgRect = Rect.newDim(dim);
        RectImgDescrAnalyzer analyzer = new RectImgDescrAnalyzer(dim);
        analyzer.setImg(ImageRasterUtils.toInts(img));

        // *** the biggy : analyse image ***
        RectImgDescr imgRectDescr = analyzer.analyze(imgRect);

        // step 3/3: encode result description into file using binary compression
        OutputStream fileOutputStream;
        if (!dryRun) {
            try {
                fileOutputStream = new BufferedOutputStream(new FileOutputStream(outputFile));
            } catch (FileNotFoundException ex) {
                throw new RuntimeIOException("Failed to open file for writing '" + outputFile + "'", ex);
            }
        } else {
            fileOutputStream = new ByteArrayOutputStream();
        }
        BitOutputStream bitOut = new OutputStreamToBitOutputStream(fileOutputStream);
        StructDataOutput structOut = new BitStreamStructDataOutput(bitOut);
        try {
            BitStreamOutputRectImgDescrVisitor encoder = new BitStreamOutputRectImgDescrVisitor(codecConfig, structOut);
            // *** do write encode ***
            encoder.writeTopLevel(imgRectDescr);
        } finally {
            structOut.close();
        }
        
        long outputLen;
        if (! dryRun) {
            outputLen = outputFile.length();
        } else {
            outputLen = ((ByteArrayOutputStream) fileOutputStream).size();
        }
        
        long millis = System.currentTimeMillis() - startTime;
        if (verboseMode) {
            long inputLen = inputFile.length();
            System.out.println((dryRun?"(dryRun) ":"") 
                + "convert '"+ inputFile + "' to '" + outputFile + "'"
                + " compression: " + byteCountToDisplaySize(inputLen) + " -> " + byteCountToDisplaySize(outputLen)
                + " = " + ((inputLen < outputLen)? " ######BIGGER!####### " : "")
                + NUMBER_FMT_1.format(100.0*(outputLen-inputLen)/inputLen) + "%"
                + " .. took " + millis + " ms");
        }
    }

    private void doConvertMultipleFilesFromLineReader(BufferedReader stdinReader) {
        String line;
        ExecutorService execService = Executors.newFixedThreadPool(parallelCount);
        AtomicInteger remainCount = new AtomicInteger();
        try {
            remainCount.incrementAndGet();
            while((line = stdinReader.readLine()) != null) {
                File inputFile = new File(line);
                if (!inputFile.exists() || !inputFile.canRead() 
                        || inputFile.length() < skipFileSmallerThan) {
                    continue;
                }
                File outputFile = new File(inputFile.getParentFile(), toFilenameSuffixRectImgDescr(inputFile.getName()));
                remainCount.incrementAndGet();
                Runnable runnable = () -> {
                    try {
                        doConvertFile(inputFile, outputFile);
                    } catch(Exception ex) {
                        System.err.println("Failed to convert file '" + inputFile + "' ex:" + ex.getMessage() + ".. ignore, do nothing!");
                    }
                    remainCount.decrementAndGet();
                };
                execService.submit(runnable);
                
                while (remainCount.get() > 50) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        break;
                    }
                }
            }
            remainCount.decrementAndGet();
        } catch(IOException ex) {
            // 
        }

        wait_loop: for(;;) {
            int remain = remainCount.get();
            if (remain == 0) {
                execService.shutdownNow();
                break;
            }
            System.out.println("#... remaining " + remain);
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                break wait_loop;
            }
        }

    }


    public static String byteCountToDisplaySize(long len) {
        return FileUtils.byteCountToDisplaySize(len);
    }
}

package fr.an.screencast.ui.swing.qrcode;

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.ReaderException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.QRCodeReader;

import fr.an.screencast.recorder.DesktopScreenSnaphotProvider;

/**
 * model associated to QRCodeDecoderRecorderView<BR/>
 * 
 * take screenshot of rectangular record area, decode QRCode, concatenate text result
 */
public class QRCodeDecoderRecorderModel {
    
    private static final Logger LOG = LoggerFactory.getLogger(QRCodeDecoderRecorderModel.class);
    
    private DesktopScreenSnaphotProvider screenSnaphostProvider = new DesktopScreenSnaphotProvider(false, true);

    private Rectangle recordArea = new Rectangle(50, 197, 720, 720); 
    
    private Map<DecodeHintType, Object> qrHints = new HashMap<>();
    
    private int qrCodeW = 300;
    private int qrCodeH = 300;
    
    private String fullText = "";
    
    private int currentQRCodeFragmentIndex = -1;
    
    private BufferedImage currentScreenshotImg;
    private String currentText;
    
    // ------------------------------------------------------------------------

    public QRCodeDecoderRecorderModel() {
    }

    // ------------------------------------------------------------------------

    public void takeSnapshot() {
        currentScreenshotImg = screenSnaphostProvider.captureScreen(recordArea);
        
        LuminanceSource source = new BufferedImageLuminanceSource(currentScreenshotImg);

        BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
        
        String prevText = currentText;
        try {
            Result result = decode(bitmap);
            
            currentText = result.getText();
            
        } catch(Exception ex) {
            currentText = "\n<<<<<<<< FAILED to decode QRCode: " + ex.getMessage() + ">>>>>>>>>>>>\n";
            LOG.error("Failed to decode", ex);
        }
        
        if (prevText == null || !prevText.equals(currentText)) {
            currentQRCodeFragmentIndex++;
            fullText += currentText;
        }
    }
    
    private Result decode(BinaryBitmap bitmap) {
        QRCodeReader qrCodeReader = new QRCodeReader();

        int[] decodeHintAllowedLength = new int[] { qrCodeW, qrCodeH }; // ???
        qrHints.put(DecodeHintType.ALLOWED_LENGTHS, decodeHintAllowedLength);
        qrHints.put(DecodeHintType.TRY_HARDER, Boolean.TRUE);
        // qrHints.put(DecodeHintType.CHARACTER_SET, ); 
            
        Result result;
        try {
            result = qrCodeReader.decode(bitmap, qrHints);
        } catch (ReaderException ex) {
            throw new RuntimeException("Failed to decode", ex);
        }
        return result;
    }
    
    public int getQrCodeW() {
        return qrCodeW;
    }

    public void setQrCodeW(int qrCodeW) {
        this.qrCodeW = qrCodeW;
    }

    public int getQrCodeH() {
        return qrCodeH;
    }

    public void setQrCodeH(int qrCodeH) {
        this.qrCodeH = qrCodeH;
    }

    public void setCurrentQRCodeFragmentIndex(int p) {
        currentQRCodeFragmentIndex = p;
    }

    public int getCurrentQRCodeFragmentIndex() {
        return currentQRCodeFragmentIndex;
    }

    public Rectangle getRecordArea() {
        return recordArea;
    }

    public void setRecordArea(Rectangle recordArea) {
        this.recordArea = recordArea;
    }

    public String getFullText() {
        return fullText;
    }

    public void setFullText(String fullText) {
        this.fullText = fullText;
    }

    public String getCurrentText() {
        return currentText;
    }

    public BufferedImage getCurrentScreenshotImg() {
        return currentScreenshotImg;
    }
    
}

package fr.an.screencast.ui.swing.qrcode;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import com.google.zxing.qrcode.encoder.ByteMatrix;
import com.google.zxing.qrcode.encoder.Encoder;
import com.google.zxing.qrcode.encoder.QRCode;

/**
 * model associated to QRCodeEncoderPlayerView<BR/>
 * compute minimum number of QRCode fragments, given max accepted width/height QRCode dimension
 */
public class QRCodeEncoderPlayerModel {
    
    private static final Logger LOG = LoggerFactory.getLogger(QRCodeEncoderPlayerModel.class);
    
    private BarcodeFormat qrCodeFormat = BarcodeFormat.QR_CODE;
    private ErrorCorrectionLevel errorCorrectionLevel = ErrorCorrectionLevel.H;

    private Map<EncodeHintType, Object> qrHints;
    
    public static class QRCodeTextFragment {
        String text;
        // BitMatrix bitMatrix
        BufferedImage img;
        
        public QRCodeTextFragment(String text, BufferedImage img) {
            this.text = text;
            this.img = img;
        }
        
        public String getText() {
            return text;
        }
        public BufferedImage getImg() {
            return img;
        }
        
    }
    
    
    private double ratioAreaPerText = 400.0*400/2330;
    
    private int qrCodeW = 400;
    private int qrCodeH = 400;
    private String text;
    private List<QRCodeTextFragment> qrCodeTextFragments = new ArrayList<>();
    
    private int currentQRCodeFragmentIndex = -1;
    
    // ------------------------------------------------------------------------

    public QRCodeEncoderPlayerModel() {
        qrHints = new HashMap<>();
        if (errorCorrectionLevel != null) {
            qrHints.put(EncodeHintType.ERROR_CORRECTION, errorCorrectionLevel);
        }
    }

    // ------------------------------------------------------------------------

    public void computeQRCodes(String text, int qrCodeW, int qrCodeH) {
        this.qrCodeW = qrCodeW;
        this.qrCodeH = qrCodeH;
        this.text = text;
        qrCodeTextFragments.clear();
        // compute..
        
        
        String remainText = text;
        while(! remainText.isEmpty()) {
            int splitLen = findGreatestTextLenForQRDim(remainText);
            if (splitLen > 100) {
                splitLen -= 10; // HACK heuristic??
            }
            String subText = remainText.substring(0, splitLen);
            
            try {
                QRCodeWriter qrCodeWriter = new QRCodeWriter();
                BitMatrix bitMatrix = qrCodeWriter.encode(subText, qrCodeFormat, qrCodeW, qrCodeH, qrHints);
                BufferedImage image = MatrixToImageWriter.toBufferedImage(bitMatrix);
                qrCodeTextFragments.add(new QRCodeTextFragment(subText, image));
            } catch (WriterException ex) {
                LOG.error("Failed", ex);
            }
            
            if (splitLen == remainText.length()) {
                break;
            }
            remainText = remainText.substring(splitLen+1, remainText.length());
        }
    }

    // naive implementation .. SLOW!
    protected int naive_findGreatestTextLenForQRDim(String text, int from) {
        for(int i = from; i > 0; i--) {
            String subText = text.substring(0, i);
            if (0 == computeAcceptTextForQRDim(subText)) {
                return i;
            }
        }
        throw new RuntimeException("should not occur!");
    }

    private int findGreatestTextLenForQRDim(String text) {
        final int len = text.length();
        int min = 0, max = text.length();
        int maxExtraArea = computeAcceptTextForQRDim(text);
        if (maxExtraArea == 0) {
            return max;
        }
        int mid = 0;
        int expectedLen = (int) (qrCodeW*qrCodeW /ratioAreaPerText);
        if (len >= expectedLen) {
            mid = expectedLen;
            String subText = text.substring(0, mid);
            int minExtraArea = computeAcceptTextForQRDim(subText);
            if (minExtraArea == 0) {
                min = mid;
                mid = mid + mid/20; // +5%
            } else {
                max = mid-1;
                mid = mid - mid/20; // -5%
            }
        }
        if (len >= mid && mid > 0) {
            String subText = text.substring(0, mid);
            int minExtraArea = computeAcceptTextForQRDim(subText);
            if (minExtraArea == 0) {
                min = mid;
            } else {
                max = mid-1;
            }
        }
        
        
        while(min < max) {
            mid = (min + max) / 2;
            String subText = text.substring(0, mid);
            int midExtraArea = computeAcceptTextForQRDim(subText);
            if (midExtraArea == 0) {
                if (min == mid) {
                    break;
                }
                min = mid;
            } else {
                max = mid-1;
            }
        }
        
        assert min == naive_findGreatestTextLenForQRDim(text, min+1);
        return min;
    }

    
    private int computeAcceptTextForQRDim(String text) {
        QRCode qrCode;
        try {
            qrCode = Encoder.encode(text, errorCorrectionLevel, qrHints);
        } catch (WriterException ex) {
            // LOG.error("Failed", ex);
            return 1; // throw new RuntimeException("Failed QRCode encode", ex);
        }
        ByteMatrix qrCodeMatrix = qrCode.getMatrix();
        if (qrCodeMatrix == null) {
          throw new IllegalStateException();
        }
        int inputWidth = qrCodeMatrix.getWidth();
        int inputHeight = qrCodeMatrix.getHeight();
        if (inputWidth < qrCodeW && inputHeight < qrCodeH) return 0;
        int extraW = (inputWidth < qrCodeW)? 1 : qrCodeW-inputWidth;
        int extraH = (inputHeight < qrCodeH)? 1 : qrCodeH-inputHeight;
        return extraW*extraH;
    }

    public int getQrCodeW() {
        return qrCodeW;
    }

    public int getQrCodeH() {
        return qrCodeH;
    }

    public String getText() {
        return text;
    }

    public List<QRCodeTextFragment> getQrCodeTextFragments() {
        return qrCodeTextFragments;
    }

    public void setCurrentQRCodeFragmentIndex(int p) {
        currentQRCodeFragmentIndex = p;
    }

    public int getCurrentQRCodeFragmentIndex() {
        return currentQRCodeFragmentIndex;
    }

    

}

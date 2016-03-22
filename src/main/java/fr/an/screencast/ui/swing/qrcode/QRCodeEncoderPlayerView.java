package fr.an.screencast.ui.swing.qrcode;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.image.BufferedImage;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.SwingUtilities;

import fr.an.screencast.ui.swing.internal.ImageCanvas;
import fr.an.screencast.ui.swing.qrcode.QRCodeEncoderPlayerModel.QRCodeTextFragment;

/**
 * a "Text to QRCode(s)" player view (with main application)
 * 
 */
public class QRCodeEncoderPlayerView {
    
    private QRCodeEncoderPlayerModel model = new QRCodeEncoderPlayerModel();
    
    private JTabbedPane tabbedPane;
    
    private JPanel inputTabPanel;
    private JToolBar inputToolbar;
    private JTextField imageSizeField;
    private JButton computeQRCodeButton;
    private JScrollPane inputTextScrollPane;
    private JTextArea inputTextArea;
    
    private JPanel playerTabPanel;
    private JToolBar playerToolbar;
    private JLabel currentQRCodeFragmentLabel;
    private JButton prevQRCodeButton;
    private JButton nextQRCodeButton;    
    private ImageCanvas qrCodeImageCanvas;

    
    // ------------------------------------------------------------------------

    /**
     * @param args
     */
    public static void main(String[] args) {
        try {
            doMain(args);
        }  catch(Exception ex) {
            System.err.println("Failed");
            ex.printStackTrace(System.err);
        }
    }
    
    public static void doMain(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                QRCodeEncoderPlayerView view = new QRCodeEncoderPlayerView();
                
                JFrame frame = new JFrame();
                frame.getContentPane().add(view.tabbedPane);
                
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            }
        });
    }

    public QRCodeEncoderPlayerView() {
        initUI();
    }

    // ------------------------------------------------------------------------
    
    private void initUI() {
        tabbedPane = new JTabbedPane();
        
        { // inputTabPanel
            inputTabPanel = new JPanel(new BorderLayout());
            
            inputToolbar = new JToolBar();
            inputTabPanel.add(inputToolbar, BorderLayout.NORTH);
            
            imageSizeField = new JTextField();
            inputToolbar.add(imageSizeField);
            
            computeQRCodeButton = new JButton("compute QRCode(s)");
            inputToolbar.add(computeQRCodeButton);
            computeQRCodeButton.addActionListener(e -> onComputeQRCodesAction());
            
            inputTextArea = new JTextArea();
            inputTextScrollPane = new JScrollPane(inputTextArea);
            inputTabPanel.add(inputTextScrollPane, BorderLayout.CENTER);
        }
        
        { // playerTabPanel
            playerTabPanel = new JPanel(new BorderLayout());

            playerToolbar = new JToolBar();
            playerTabPanel.add(playerToolbar, BorderLayout.NORTH);
            
            currentQRCodeFragmentLabel = new JLabel();
            playerToolbar.add(currentQRCodeFragmentLabel);
            prevQRCodeButton = new JButton("<");
            playerToolbar.add(prevQRCodeButton);
            prevQRCodeButton.addActionListener(e -> onPrevQRCodeAction());
                
            nextQRCodeButton = new JButton(">");
            playerToolbar.add(nextQRCodeButton);
            nextQRCodeButton.addActionListener(e -> onNextQRCodeAction());
            
            qrCodeImageCanvas = new ImageCanvas();
            int zoom = 1;
            qrCodeImageCanvas.setPreferredSize(new Dimension(zoom*model.getQrCodeW(), zoom*model.getQrCodeH()));
            playerTabPanel.add(qrCodeImageCanvas, BorderLayout.CENTER);
        }

        tabbedPane.add("input", inputTabPanel);
        tabbedPane.add("player", playerTabPanel);        

        model2view();
    }

    private void onComputeQRCodesAction() {
        String[] qrCodeDimText = imageSizeField.getText().split(",");
        int w = Integer.parseInt(qrCodeDimText[0]);
        int h = Integer.parseInt(qrCodeDimText[1]);
        String text = inputTextArea.getText();
        model.computeQRCodes(text, w, h);
        model.setCurrentQRCodeFragmentIndex(0);
        
        tabbedPane.setSelectedIndex(1);
        model2view();
    }

    private void onNextQRCodeAction() {
        int currentFragmentIndex = model.getCurrentQRCodeFragmentIndex();
        if (currentFragmentIndex == -1) {
            onComputeQRCodesAction();
            currentFragmentIndex = 0;
            model.setCurrentQRCodeFragmentIndex(currentFragmentIndex);
        } else {
            int qrFragmentsCount = model.getQrCodeTextFragments().size();
            if (currentFragmentIndex+1 < qrFragmentsCount) {
                model.setCurrentQRCodeFragmentIndex(++currentFragmentIndex);
            }
        }
        model2view();
    }

    private void onPrevQRCodeAction() {
        int currentFragmentIndex = model.getCurrentQRCodeFragmentIndex();
        if (currentFragmentIndex == -1) {
            onComputeQRCodesAction();
            currentFragmentIndex = 0;
            model.setCurrentQRCodeFragmentIndex(currentFragmentIndex);
        } else {
            if (0 <= currentFragmentIndex-1) {
                model.setCurrentQRCodeFragmentIndex(--currentFragmentIndex);
            }
        }
        model2view();
    }

    
    private void model2view() {
        imageSizeField.setText(model.getQrCodeW() + "," + model.getQrCodeH());
        
        int currentFragmentIndex = model.getCurrentQRCodeFragmentIndex();
        int qrFragmentsCount = model.getQrCodeTextFragments().size();
        String currText = currentFragmentIndex + "/" + qrFragmentsCount;
        currentQRCodeFragmentLabel.setText(currText);

        QRCodeTextFragment fragment = (0 <= currentFragmentIndex && currentFragmentIndex < qrFragmentsCount)? model.getQrCodeTextFragments().get(currentFragmentIndex) : null;
        if (fragment != null) {
            BufferedImage img = fragment.getImg();
            qrCodeImageCanvas.setImage(img);
        } else {
            qrCodeImageCanvas.setImage(null);
        }
        qrCodeImageCanvas.repaint();
    }

}

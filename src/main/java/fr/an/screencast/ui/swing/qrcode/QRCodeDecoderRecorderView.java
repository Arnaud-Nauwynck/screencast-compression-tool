package fr.an.screencast.ui.swing.qrcode;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;

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
import fr.an.screencast.ui.swing.internal.TransparentFrameScreenArea;

/**
 * a "QRCode(s) to Text" screenshot recorder view (with main application)
 * 
 */
public class QRCodeDecoderRecorderView {
    
    private QRCodeDecoderRecorderModel model = new QRCodeDecoderRecorderModel();
    
    private JTabbedPane tabbedPane;

    private JPanel recorderTabPanel;
    private JToolBar recorderToolbar;
    private JTextField recordAreaField;
    private JButton revealRecordAreaButton;
    private JLabel currentQRCodeFragmentLabel;
    private JButton nextQRCodeButton;
    private JButton clearTextButton;
    private JButton updateTextButton;
    
    private JScrollPane outputTextScrollPane;
    private JTextArea outputTextArea;

    private JPanel detailImageTabPanel;
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
                QRCodeDecoderRecorderView view = new QRCodeDecoderRecorderView();
                
                JFrame frame = new JFrame();
                frame.getContentPane().add(view.tabbedPane);
                
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.pack();
                frame.setVisible(true);
            }
        });
    }

    public QRCodeDecoderRecorderView() {
        initUI();
    }

    // ------------------------------------------------------------------------
    
    private void initUI() {
        tabbedPane = new JTabbedPane();
        

        { // recorderTabPanel
            recorderTabPanel = new JPanel(new BorderLayout());

            recorderToolbar = new JToolBar();
            recorderTabPanel.add(recorderToolbar, BorderLayout.NORTH);
            
            nextQRCodeButton = new JButton("Take QR Snapshot>>");
            recorderToolbar.add(nextQRCodeButton);
            nextQRCodeButton.addActionListener(e -> onNextQRCodeAction());

            currentQRCodeFragmentLabel = new JLabel();
            recorderToolbar.add(currentQRCodeFragmentLabel);
            
            recordAreaField = new JTextField();
            recorderToolbar.add(recordAreaField);
            
            revealRecordAreaButton = new JButton("reveal area");
            revealRecordAreaButton.addActionListener(e -> onRevealRecordAreaAction());
            recorderToolbar.add(revealRecordAreaButton);
            
            clearTextButton = new JButton("Clear");
            recorderToolbar.add(clearTextButton);
            clearTextButton.addActionListener(e -> onClearTextAction());

            updateTextButton = new JButton("Update");
            recorderToolbar.add(updateTextButton);
            updateTextButton.addActionListener(e -> onSetTextAction());

            outputTextArea = new JTextArea();
            outputTextScrollPane = new JScrollPane(outputTextArea);
            recorderTabPanel.add(outputTextScrollPane, BorderLayout.CENTER);

        }
        
        { // detailImageTabPanel
            detailImageTabPanel = new JPanel(new BorderLayout());

            qrCodeImageCanvas = new ImageCanvas();
            int zoom = 1;
            qrCodeImageCanvas.setPreferredSize(new Dimension(zoom*model.getQrCodeW(), zoom*model.getQrCodeH()));
            detailImageTabPanel.add(qrCodeImageCanvas, BorderLayout.CENTER);

        }
        
        tabbedPane.add("recorder", recorderTabPanel);        
        tabbedPane.add("img", detailImageTabPanel);

        model2view();
    }

    private void onNextQRCodeAction() {
        model.takeSnapshot();
        model2view();
    }

    private void onSetTextAction() {
        model.setFullText(outputTextArea.getText());
    }

    private void onClearTextAction() {
        model.setFullText("");
        outputTextArea.setText("");
    }
    
    protected TransparentFrameScreenArea recordAreaFrame;
    
    private void onRevealRecordAreaAction() {
        recordArea_viewToModel();
        if (recordAreaFrame == null) {
            recordAreaFrame = new TransparentFrameScreenArea();
            recordAreaFrame.setBounds(model.getRecordArea());
            recordAreaFrame.setVisible(true);
            recordAreaFrame.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    updateModelRecordArea();
                }

                @Override
                public void componentMoved(ComponentEvent e) {
                    updateModelRecordArea();
                }

                protected void updateModelRecordArea() {
                    Rectangle r = recordAreaFrame.getBounds();
                    model.setRecordArea(r);
                    recordArea_modelToView();
                }
            });
        } else {
            model.setRecordArea(recordAreaFrame.getBounds());
            recordAreaFrame.setVisible(false);
            recordAreaFrame.dispose();
            recordAreaFrame = null;
        }
    }

    
    
    private void recordArea_viewToModel() {
        String[] coordTexts = recordAreaField.getText().split(",");
        int x = Integer.parseInt(coordTexts[0]);
        int y = Integer.parseInt(coordTexts[1]);
        int w = Integer.parseInt(coordTexts[2]);
        int h = Integer.parseInt(coordTexts[3]);
        model.setRecordArea(new Rectangle(x, y, w, h));
    }

    private void recordArea_modelToView() {
        Rectangle r = model.getRecordArea();
        String coord = r.x + "," + r.y + "," + (int)r.getWidth() + "," + (int)r.getHeight();
        recordAreaField.setText(coord);
    }
    
    private void model2view() {
        recordArea_modelToView();
        
        String fullText = model.getFullText();
        outputTextArea.setText(fullText);
        
        currentQRCodeFragmentLabel.setText("" + model.getCurrentQRCodeFragmentIndex());

        qrCodeImageCanvas.setImage(model.getCurrentScreenshotImg());
        qrCodeImageCanvas.repaint();
    }

}

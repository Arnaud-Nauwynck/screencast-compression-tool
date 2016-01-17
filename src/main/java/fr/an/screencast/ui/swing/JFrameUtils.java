package fr.an.screencast.ui.swing;

import java.util.function.Supplier;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class JFrameUtils {

    public static JFrame openFrame(String title, Supplier<JComponent> componentBuilder) {
        JFrame[] res = new JFrame[1];
        try {
            SwingUtilities.invokeAndWait(() -> {
                JFrame frame = new JFrame();
                JComponent component = componentBuilder.get();
                frame.getContentPane().add(component);
                frame.setTitle(title);
                frame.pack();
                frame.setVisible(true);
                res[0] = frame;
            });
        } catch (Exception ex) {
            throw new RuntimeException("Failed to create JFrame", ex);
        }
        return res[0];
    }
    
}

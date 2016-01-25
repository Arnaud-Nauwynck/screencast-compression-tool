package fr.an.screencast.ui.swing;

import java.awt.event.ActionListener;

import javax.swing.JButton;

public class JButtonUtils {

    public static JButton snew(String text, Runnable action) {
        return snew(text, e -> action.run());
    }
    
    public static JButton snew(String text, ActionListener actionListener) {
        JButton res = new JButton(text);
        res.addActionListener(actionListener);
        return res;
    }
}

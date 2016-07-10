package de.neemann.digital.gui.components.terminal;

import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * A simple keyboard implementation
 *
 * @author hneemann
 */
public class KeyboardDialog extends JDialog {
    private final JLabel textLabel;
    private final Object textLock = new Object();
    private String text;

    /**
     * Create a new Instance
     *
     * @param owner the owner frame
     */
    public KeyboardDialog(Frame owner) {
        super(owner, Lang.get("elem_Keyboard"), false);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);

        textLabel = new JLabel("Enter Text       ");
        textLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        getContentPane().add(textLabel);
        text = "";

        textLabel.setFocusable(true);
        textLabel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                String t;
                synchronized (textLock) {
                    text += e.getKeyChar();
                    t = text;
                }
                textLabel.setText(t);
            }
        });

        setAlwaysOnTop(true);
        pack();
        setLocationRelativeTo(owner);
    }

    /**
     * @return the oldest char
     */
    public int getChar() {
        if (text.length() == 0)
            return 0;
        else {
            return text.charAt(0);
        }
    }

    /**
     * consumes the oldest char
     */
    public void consumeChar() {
        if (text.length() > 0) {
            String t;
            synchronized (textLock) {
                text = text.substring(1);
                t = text;
            }
            SwingUtilities.invokeLater(() -> textLabel.setText(t));
        }
    }
}

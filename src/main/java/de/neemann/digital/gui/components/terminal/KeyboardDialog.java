/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.terminal;

import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * A simple keyboard implementation
 */
public class KeyboardDialog extends JDialog implements Keyboard.KeyboardInterface {
    private final JLabel textLabel;
    private final Object textLock = new Object();
    private String text;

    /**
     * Create a new Instance
     *
     * @param owner             the owner frame
     * @param keyboard          the keyboard node which has opened this dialog
     * @param keyPressedHandler handler called every time a key is typed
     */
    public KeyboardDialog(Frame owner, Keyboard keyboard, KeyPressedHandler keyPressedHandler) {
        super(owner, Lang.get("elem_Keyboard") + " " + keyboard.getLabel(), false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        textLabel = new JLabel(Lang.get("msg_enterText") + "          ");
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
                keyPressedHandler.keyPressed(keyboard);
                textLabel.setText(t);
            }
        });

        pack();
        setLocationRelativeTo(owner);
        setVisible(true);

        keyboard.setKeyboardDialog(this);
    }

    @Override
    public int getChar() {
        synchronized (textLock) {
            if (text.length() == 0)
                return 0;
            else
                return text.charAt(0);
        }
    }

    @Override
    public boolean isChar() {
        synchronized (textLock) {
            return text.length() > 0;
        }
    }

    @Override
    public void removeChar() {
        synchronized (textLock) {
            if (text.length() > 0) {
                text = text.substring(1);
                final String t = text;
                SwingUtilities.invokeLater(() -> textLabel.setText(t));
            }
        }
    }

    /**
     * The handler called if a key is typed.
     */
    public interface KeyPressedHandler {
        /**
         * Called if a key is typed
         *
         * @param keyboard the keyboard used
         */
        void keyPressed(Keyboard keyboard);
    }
}

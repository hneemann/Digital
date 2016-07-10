package de.neemann.digital.gui.components;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.gui.sync.Sync;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Dialog to edit a single value.
 * Used to enter a multi bit input value
 *
 * @author hneemann
 */
public final class SingleValueDialog extends JDialog {

    private String returnText;

    /**
     * Creates a new instance
     *
     * @param pos  the position to show the dialog
     * @param text the text to edit
     */
    private SingleValueDialog(Point pos, String text) {
        super((Frame) null, Lang.get("attr_dialogTitle"), true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JTextField textField = new JTextField(30);
        textField.setText(text);
        getContentPane().add(textField);

        textField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                returnText = textField.getText();
                dispose();
            }
        });

        pack();
        setLocation(pos.x, pos.y);
    }

    /**
     * edits the given value
     *
     * @return result or null if dialog closed without something entered
     */
    private String showDialog() {
        setVisible(true);
        return returnText;
    }

    /**
     * Edits a single value
     *
     * @param pos   the position to pop up the dialog
     * @param value the value to edit
     */
    public static void editValue(Point pos, ObservableValue value, Sync modelSync) {
        String ret = new SingleValueDialog(pos, value.getValueString()).showDialog();
        if (ret != null) {
            ret = ret.trim();
            if (ret.equals("?") && value.supportsHighZ()) {
                modelSync.access(() -> {
                    value.setHighZ(true);
                });
            } else {
                try {
                    long l = Long.decode(ret);
                    modelSync.access(() -> {
                        value.set(l, false);
                    });
                } catch (NumberFormatException e) {

                }
            }
        }
    }
}

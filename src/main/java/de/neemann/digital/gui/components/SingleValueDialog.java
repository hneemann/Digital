package de.neemann.digital.gui.components;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * @author hneemann
 */
public class SingleValueDialog extends JDialog {

    private String returnText;

    public SingleValueDialog(Point pos, String text) {
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
    public String showDialog() {
        setVisible(true);
        return returnText;
    }

    public static void editValue(Point pos, ObservableValue value) {
        String ret = new SingleValueDialog(pos, value.getValueString()).showDialog();
        if (ret != null) {
            ret = ret.trim();
            if (ret.equals("?") && value.supportsHighZ()) {
                value.setHighZ(true);
            } else {
                try {
                    long l = Long.decode(ret);
                    value.set(l, false);
                } catch (NumberFormatException e) {

                }
            }
        }
    }
}

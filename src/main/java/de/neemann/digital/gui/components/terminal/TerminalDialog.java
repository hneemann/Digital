package de.neemann.digital.gui.components.terminal;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;

import javax.swing.*;
import java.awt.*;


/**
 * @author hneemann
 */
public class TerminalDialog extends JDialog {
    private final JTextArea textArea;
    private final int width;
    private int pos;

    public TerminalDialog(ElementAttributes attr) {
        super((JFrame) null, attr.get(Keys.Label), false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        width = attr.get(Keys.TermWidth);
        textArea = new JTextArea(attr.get(Keys.TermHeight), width);
        textArea.setFont(new Font("monospaced", Font.PLAIN, 12));
        getContentPane().add(new JScrollPane(textArea));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void addChar(char value) {
        if (value == 13 || value == 10) {
            pos = 0;
            textArea.append("\n");
        } else {
            textArea.append("" + value);
            pos++;
            if (pos == width) {
                pos = 0;
                textArea.append("\n");
            }
        }
    }
}

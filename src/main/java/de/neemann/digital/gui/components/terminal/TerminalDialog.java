package de.neemann.digital.gui.components.terminal;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;

import javax.swing.*;
import java.awt.*;


/**
 * The dialog which shows the terminal
 *
 * @author hneemann
 */
public class TerminalDialog extends JDialog {
    private final JTextArea textArea;
    private final int width;
    private int pos;

    /**
     * Creates a new instance
     *
     * @param attr the terminals attributes
     */
    public TerminalDialog(ElementAttributes attr) {
        super((JFrame) null, attr.get(Keys.LABEL), false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        width = attr.get(Keys.TERM_WIDTH);
        textArea = new JTextArea(attr.get(Keys.TERM_HEIGHT), width);
        textArea.setFont(new Font("monospaced", Font.PLAIN, 12));
        getContentPane().add(new JScrollPane(textArea));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Adds a char to the terminal dialog
     *
     * @param value the character
     */
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

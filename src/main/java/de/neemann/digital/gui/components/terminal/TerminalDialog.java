package de.neemann.digital.gui.components.terminal;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;


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
        super((JFrame) null, attr.getLabel(), false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setAlwaysOnTop(true);
        width = attr.get(Keys.TERM_WIDTH);
        textArea = new JTextArea(attr.get(Keys.TERM_HEIGHT), width);
        textArea.setFont(new Font("monospaced", Font.PLAIN, 12));
        getContentPane().add(new JScrollPane(textArea));

        JToolBar toolBar = new JToolBar();
        toolBar.add(new ToolTipAction(Lang.get("menu_terminalDelete"), CircuitComponent.ICON_DELETE) {
            @Override
            public void actionPerformed(ActionEvent e) {
                textArea.setText("");
            }
        }.setToolTip(Lang.get("menu_terminalDelete_tt")).createJButtonNoText());
        getContentPane().add(toolBar, BorderLayout.NORTH);

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

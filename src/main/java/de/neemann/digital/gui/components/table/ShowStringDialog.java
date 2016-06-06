package de.neemann.digital.gui.components.table;

import javax.swing.*;

/**
 * Shows a simple string
 *
 * @author hneemann
 */
public class ShowStringDialog extends JDialog {

    /**
     * Creates a new instance
     *
     * @param parent the parent
     * @param str    the pin map to show
     */
    public ShowStringDialog(JFrame parent, String title, String str) {
        super(parent, title);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JTextArea text = new JTextArea(str);
        text.setEditable(false);
        getContentPane().add(new JScrollPane(text));

        pack();
        setLocationRelativeTo(parent);
        setAlwaysOnTop(true);
    }
}

package de.neemann.digital.gui.components.table;

import javax.swing.*;
import java.awt.*;

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
        init(parent, str);
    }

    /**
     * Creates a new instance
     *
     * @param parent the parent
     * @param str    the pin map to show
     */
    public ShowStringDialog(JDialog parent, String title, String str) {
        super(parent, title);
        init(parent, str);
    }

    private void init(Component parent, String str) {
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JTextArea text = new JTextArea(str);
        text.setEditable(false);
        text.setFont(new JLabel().getFont());
        text.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        getContentPane().add(new JScrollPane(text));

        pack();
        setLocationRelativeTo(parent);
        setAlwaysOnTop(true);
    }
}

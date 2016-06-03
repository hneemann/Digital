package de.neemann.digital.gui.components.table;

import de.neemann.digital.builder.PinMap;

import javax.swing.*;

/**
 * Shows a pin mapping instance {@link PinMap}
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

        JTextArea text = new JTextArea(str);
        text.setEditable(false);
        getContentPane().add(new JScrollPane(text));

        pack();
        setLocationRelativeTo(parent);
    }
}

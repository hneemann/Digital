package de.neemann.digital.gui.components.table;

import de.neemann.digital.builder.PinMap;
import de.neemann.digital.lang.Lang;

import javax.swing.*;

/**
 * Shows a pin mapping instance {@link PinMap}
 *
 * @author hneemann
 */
public class ShowPinMapDialog extends JDialog {

    /**
     * Creates a new instance
     *
     * @param parent     the parent
     * @param pinMapping the pin map to show
     */
    public ShowPinMapDialog(JFrame parent, PinMap pinMapping) {
        super(parent, Lang.get("win_pinMapDialog"));

        JTextArea text = new JTextArea(pinMapping.toString());
        text.setEditable(false);
        getContentPane().add(new JScrollPane(text));

        pack();
        setLocationRelativeTo(parent);
    }
}

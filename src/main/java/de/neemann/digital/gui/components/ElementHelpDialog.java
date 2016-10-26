package de.neemann.digital.gui.components;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * Simple Dialog to show an elements help text.
 * <p>
 * Created by hneemann on 25.10.16.
 */
public class ElementHelpDialog extends JDialog {

    private static final int MIN_WIDTH = 300;
    private static final int MIN_HEIGHT = 400;

    /**
     * Creates a new instance
     *
     * @param parent            the parents dialog
     * @param elementType       the type of the element
     * @param elementAttributes the attributes of this element
     */
    public ElementHelpDialog(JDialog parent, ElementTypeDescription elementType, ElementAttributes elementAttributes) {
        super(parent, Lang.get("attr_help"), true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        final String description = elementType.getDetailedDescription(elementAttributes);
        JTextArea textfield = new JTextArea(description);
        textfield.setEditable(false);
        textfield.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));
        getContentPane().add(new JScrollPane(textfield));

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttons.add(new JButton(new AbstractAction(Lang.get("ok")) {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                dispose();
            }
        }));
        getContentPane().add(buttons, BorderLayout.SOUTH);

        pack();
        Dimension r = getSize();
        if (r.width < MIN_WIDTH) r.width = MIN_WIDTH;
        if (r.height < MIN_HEIGHT) r.height = MIN_HEIGHT;
        setSize(r);
        setLocationRelativeTo(parent);
    }
}

package de.neemann.digital.gui.components;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.*;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;

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

        final String description = getDetailedDescription(elementType, elementAttributes);
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

    /**
     * Creates a detailed human readable description of this element
     *
     *
     * @param et the element to describe
     * @param elementAttributes the actual attributes of the element to describe
     * @return the human readable description of this element
     */
    private String getDetailedDescription(ElementTypeDescription et, ElementAttributes elementAttributes) {
        StringBuilder sb = new StringBuilder();
        sb.append(et.getTranslatedName()).append("\n");
        String descr = et.getDescription(elementAttributes);
        if (!descr.equals(et.getTranslatedName()))
            sb.append("\n").append(StringUtils.breakLines(et.getDescription(elementAttributes))).append("\n");

        try {
            PinDescriptions inputs = et.getInputDescription(elementAttributes);
            sb.append("\n").append(Lang.get("elem_Help_inputs")).append(":\n");
            if (inputs != null && inputs.size() > 0) {
                for (PinDescription i : inputs)
                    appendNameAndDescription(sb, i.getName(), i.getDescription());
            } else {
                sb.append("  -\n");
            }
        } catch (NodeException e) {
            e.printStackTrace();
        }

        PinDescriptions outputs = et.getOutputDescriptions(elementAttributes);
        sb.append("\n").append(Lang.get("elem_Help_outputs")).append(":\n");
        if (outputs != null && outputs.size() > 0) {
            for (PinDescription i : outputs)
                appendNameAndDescription(sb, i.getName(), i.getDescription());
        } else {
            sb.append("  -\n");
        }

        if (et.getAttributeList().size() > 0) {
            sb.append("\n").append(Lang.get("elem_Help_attributes")).append(":\n");
            for (Key k : et.getAttributeList()) {
                appendNameAndDescription(sb, k.getName(), k.getDescription());
            }
        }

        return sb.toString();
    }

    private static void appendNameAndDescription(StringBuilder sb, String name, String description) {
        sb.append("  ").append(name);
        if (!name.equals(description))
            sb.append(": ").append(description);
        sb.append("\n");
    }

}

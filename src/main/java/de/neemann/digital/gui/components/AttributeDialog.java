package de.neemann.digital.gui.components;

import de.neemann.digital.core.part.AttributeKey;
import de.neemann.digital.core.part.PartAttributes;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;

/**
 * @author hneemann
 */
public class AttributeDialog extends JDialog {

    public AttributeDialog(Point pos, ArrayList<AttributeKey> list, PartAttributes partAttributes) {
        super((Frame) null, "Attributes", true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JTable table = new JTable(new AttributeTableModel(list, partAttributes));
        getContentPane().add(table);

        pack();
        setLocation(pos.x, pos.y);
    }


    public void showDialog() {
        setVisible(true);
    }
}

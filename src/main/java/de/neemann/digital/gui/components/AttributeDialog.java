package de.neemann.digital.gui.components;

import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.lang.Lang;
import de.process.utils.gui.ErrorMessage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * @author hneemann
 */
public class AttributeDialog extends JDialog {

    private final ArrayList<EditorHolder> editors;
    private boolean changed = false;

    public AttributeDialog(Point pos, ArrayList<AttributeKey> list, ElementAttributes elementAttributes) {
        super((Frame) null, Lang.get("attr_dialogTitle"), true);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new DialogLayout());

        getContentPane().add(new JScrollPane(panel));

        editors = new ArrayList<>();

        for (AttributeKey key : list) {
            panel.add(new JLabel(key.getName() + ":  "), DialogLayout.LABEL);
            Editor e = EditorFactory.INSTANCE.create(key.getValueClass(), elementAttributes.get(key));
            editors.add(new EditorHolder(e, key));
            panel.add(e.getComponent(elementAttributes), DialogLayout.INPUT);
        }

        JButton okButton = new JButton(new AbstractAction(Lang.get("ok")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    setEditedValues(elementAttributes);
                    changed = true;
                    dispose();
                } catch (RuntimeException err) {
                    new ErrorMessage(Lang.get("msg_errorEditingValue")).addCause(err).setComponent(AttributeDialog.this).show();
                }
            }
        });
        getContentPane().add(okButton, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(okButton);

        pack();

        if (pos == null)
            setLocationRelativeTo(null);
        else
            setLocation(pos.x, pos.y);
    }

    private void setEditedValues(ElementAttributes attr) {
        for (EditorHolder e : editors)
            e.setTo(attr);
    }

    public boolean showDialog() {
        setVisible(true);
        return changed;
    }

    private class EditorHolder<T> {
        private final Editor<T> e;
        private final AttributeKey<T> key;

        public EditorHolder(Editor<T> e, AttributeKey<T> key) {
            this.e = e;
            this.key = key;
        }

        public void setTo(ElementAttributes attr) {
            T value = e.getValue();
            attr.set(key, value);
        }
    }

    public static void main(String[] args) {
        ArrayList<AttributeKey> list = new ArrayList<>();
        list.add(AttributeKey.Bits);
        list.add(AttributeKey.Label);
        list.add(AttributeKey.Color);
        list.add(AttributeKey.Signed);
        ElementAttributes values = new ElementAttributes();
        AttributeDialog d = new AttributeDialog(null, list, values);
        d.showDialog();
        System.out.println(values);
    }

}

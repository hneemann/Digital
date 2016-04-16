package de.neemann.digital.gui.components;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;

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

    public AttributeDialog(Component parent, ArrayList<Key> list, ElementAttributes elementAttributes) {
        this(parent, null, list, elementAttributes);
    }

    public AttributeDialog(Component parent, Point pos, ArrayList<Key> list, ElementAttributes elementAttributes) {
        super(SwingUtilities.getWindowAncestor(parent), Lang.get("attr_dialogTitle"), ModalityType.APPLICATION_MODAL);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        JPanel panel = new JPanel(new DialogLayout());

        getContentPane().add(new JScrollPane(panel));

        editors = new ArrayList<>();

        for (Key key : list) {
            Editor e = EditorFactory.INSTANCE.create(key, elementAttributes.get(key));
            editors.add(new EditorHolder(e, key));
            e.addToPanel(panel, key, elementAttributes);
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

    private static class EditorHolder<T> {
        private final Editor<T> e;
        private final Key<T> key;

        private EditorHolder(Editor<T> e, Key<T> key) {
            this.e = e;
            this.key = key;
        }

        public void setTo(ElementAttributes attr) {
            T value = e.getValue();
            attr.set(key, value);
        }
    }

}

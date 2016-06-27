package de.neemann.digital.gui.components;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;

/**
 * @author hneemann
 */
public class AttributeDialog extends JDialog {

    private final java.util.List<EditorHolder> editors;
    private final JPanel panel;
    private final Component parent;
    private final Point pos;
    private boolean changed = false;

    /**
     * Creates a new instance
     *
     * @param parent            the parent
     * @param list              the list of keys which are to edit
     * @param elementAttributes the data stored
     */
    public AttributeDialog(Component parent, java.util.List<Key> list, ElementAttributes elementAttributes) {
        this(parent, null, list, elementAttributes);
    }

    /**
     * Creates a new instance
     *
     * @param parent            the parent
     * @param pos               the position to pop up the dialog
     * @param list              the list of keys which are to edit
     * @param elementAttributes the data stored
     */
    public AttributeDialog(Component parent, Point pos, java.util.List<Key> list, ElementAttributes elementAttributes) {
        super(SwingUtilities.getWindowAncestor(parent), Lang.get("attr_dialogTitle"), ModalityType.APPLICATION_MODAL);
        this.parent = parent;
        this.pos = pos;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        panel = new JPanel(new DialogLayout());

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

        JButton cancelButton = new JButton(new AbstractAction(Lang.get("cancel")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(cancelButton);
        buttonPanel.add(okButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(okButton);
    }

    /**
     * Adds a button to this dialog
     *
     * @param label  a label
     * @param action the action
     * @return this for chained calls
     */
    public AttributeDialog addButton(String label, ToolTipAction action) {
        panel.add(new JLabel(label), DialogLayout.LABEL);
        panel.add(action.createJButton(), DialogLayout.INPUT);
        return this;
    }

    private void setEditedValues(ElementAttributes attr) {
        for (EditorHolder e : editors)
            e.setTo(attr);
    }

    /**
     * shows the dialog
     *
     * @return true if data was changed
     */
    public boolean showDialog() {
        pack();

        if (pos == null)
            setLocationRelativeTo(parent);
        else
            setLocation(pos.x, pos.y);

        setVisible(true);
        return changed;
    }

    private static final class EditorHolder<T> {
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

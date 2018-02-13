package de.neemann.digital.gui.components;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.gui.Main;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.Screen;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Dialog used to edit Attributes.
 * The Dialog is configured by a list of {@link Key} instances, which are defined in the
 * {@link de.neemann.digital.core.element.Keys} class. The values are stored in an instance of
 * {@link ElementAttributes} which is a mostly a observable hash map.
 * This class is used to create the dialogs used to edit the element attributes but also to edit the
 * system settings, the model settings and the models attributes,
 *
 * @author hneemann
 */
public class AttributeDialog extends JDialog {
    private final java.util.List<EditorHolder> editors;
    private final JPanel panel;
    private final Window parent;
    private final Point pos;
    private final ElementAttributes originalAttributes;
    private final ElementAttributes modifiedAttributes;
    private final JPanel buttonPanel;
    private final ConstraintsBuilder constraints;
    private HashMap<Key, JCheckBox> checkBoxes;
    private JComponent topMostTextComponent;
    private VisualElement visualElement;
    private boolean okPressed = false;

    /**
     * Creates a new instance
     *
     * @param parent            the parent
     * @param list              the list of keys which are to edit
     * @param elementAttributes the data stored
     */
    public AttributeDialog(Window parent, java.util.List<Key> list, ElementAttributes elementAttributes) {
        this(parent, null, list, elementAttributes, false);
    }

    /**
     * Creates a new instance
     *
     * @param parent            the parent
     * @param pos               the position to pop up the dialog
     * @param list              the list of keys which are to edit
     * @param elementAttributes the data stored
     */
    public AttributeDialog(Window parent, Point pos, java.util.List<Key> list, ElementAttributes elementAttributes) {
        this(parent, pos, list, elementAttributes, false);
    }

    /**
     * Creates a new instance
     *
     * @param parent            the parent
     * @param pos               the position to pop up the dialog
     * @param list              the list of keys which are to edit
     * @param elementAttributes the initial data to modify
     * @param addCheckBoxes     th true check boxes behind the attributes are added
     */
    public AttributeDialog(Window parent, Point pos, java.util.List<Key> list, ElementAttributes elementAttributes, boolean addCheckBoxes) {
        super(parent, Lang.get("attr_dialogTitle"), ModalityType.APPLICATION_MODAL);
        this.parent = parent;
        this.pos = pos;
        this.originalAttributes = elementAttributes;
        this.modifiedAttributes = new ElementAttributes(elementAttributes);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        panel = new JPanel(new GridBagLayout());

        getContentPane().add(new JScrollPane(panel));

        editors = new ArrayList<>();

        topMostTextComponent = null;
        constraints = new ConstraintsBuilder().inset(3).fill();
        for (Key key : list) {
            Editor e = EditorFactory.INSTANCE.create(key, modifiedAttributes.get(key));
            editors.add(new EditorHolder(e, key));
            e.addToPanel(panel, key, modifiedAttributes, this, constraints);
            if (addCheckBoxes) {
                if (checkBoxes == null)
                    checkBoxes = new HashMap<>();
                JCheckBox checkBox = new JCheckBox();
                checkBox.setSelected(true);
                checkBox.setToolTipText(Lang.get("msg_modifyThisAttribute"));
                checkBoxes.put(key, checkBox);
                panel.add(checkBox, constraints.x(2));
                checkBox.addChangeListener(event -> e.setEnabled(checkBox.isSelected()));
            }
            constraints.nextRow();

            if (topMostTextComponent == null && e instanceof EditorFactory.StringEditor)
                topMostTextComponent = ((EditorFactory.StringEditor) e).getTextComponent();

            final Key dependsOn = key.getDependsOn();
            if (dependsOn != null) {
                for (EditorHolder ed : editors) {
                    if (ed.key.getKey().equals(dependsOn.getKey())) {
                        ed.setDependantEditor(e, key.isDependsOnInverted());
                    }
                }
            }

        }

        JButton okButton = new JButton(new AbstractAction(Lang.get("ok")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    fireOk();
                } catch (Editor.EditorParseException err) {
                    new ErrorMessage(Lang.get("msg_errorEditingValue")).addCause(err).show(AttributeDialog.this);
                }
            }
        });

        final AbstractAction cancel = new AbstractAction(Lang.get("cancel")) {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        };

        buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(new JButton(cancel));
        buttonPanel.add(okButton);

        getContentPane().add(buttonPanel, BorderLayout.SOUTH);

        getRootPane().setDefaultButton(okButton);
        getRootPane().registerKeyboardAction(cancel,
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0),
                JComponent.WHEN_IN_FOCUSED_WINDOW);
    }

    /**
     * Closes the dialog and stores modified values
     *
     * @throws Editor.EditorParseException Editor.EditorParseException
     */
    public void fireOk() throws Editor.EditorParseException {
        storeEditedValues();
        okPressed = true;
        dispose();
    }

    /**
     * @return the keys check boxes
     */
    public HashMap<Key, JCheckBox> getCheckBoxes() {
        return checkBoxes;
    }

    /**
     * Adds a button to this dialog
     *
     * @param label  a label
     * @param action the action
     * @return this for chained calls
     */
    public AttributeDialog addButton(String label, ToolTipAction action) {
        panel.add(new JLabel(label), constraints);
        panel.add(action.createJButton(), constraints.x(1));
        constraints.nextRow();
        return this;
    }

    /**
     * Adds a button to the botton of this dialog
     *
     * @param action the action
     * @return this for chained calls
     */
    public AttributeDialog addButton(ToolTipAction action) {
        buttonPanel.add(action.createJButton(), 0);
        return this;
    }

    /**
     * store gui fields to attributes
     *
     * @throws Editor.EditorParseException Editor.EditorParseException
     */
    public void storeEditedValues() throws Editor.EditorParseException {
        for (EditorHolder e : editors)
            e.setTo(modifiedAttributes);
    }

    /**
     * Shows the dialog
     *
     * @return the new attributes of null if nothing has changed
     */
    public ElementAttributes showDialog() {
        pack();

        if (pos == null)
            setLocationRelativeTo(parent);
        else
            Screen.setLocation(this, pos, true);

        if (topMostTextComponent != null)
            SwingUtilities.invokeLater(() -> topMostTextComponent.requestFocusInWindow());

        setVisible(true);
        if (okPressed && !originalAttributes.equals(modifiedAttributes))
            return modifiedAttributes;
        else
            return null;
    }

    /**
     * @return the dialogs parent
     */
    public Window getDialogParent() {
        return parent;
    }


    /**
     * @return true if ok is pressed
     */
    public boolean isOkPressed() {
        return okPressed;
    }

    /**
     * @return the containing Main instance or null
     */
    public Main getMain() {  // ToDo: is a hack! find a better solution for getting the main frame
        if (parent instanceof Main)
            return (Main) parent;
        return null;
    }

    /**
     * @return the visual element of this dialog, maybe null
     */
    public VisualElement getVisualElement() {
        return visualElement;
    }

    /**
     * Sets the visual element of this dialog
     *
     * @param visualElement the visual element which attributes are edited
     * @return this for chained calls
     */
    public AttributeDialog setVisualElement(VisualElement visualElement) {
        this.visualElement = visualElement;
        return this;
    }

    private static final class EditorHolder<T> {
        private final Editor<T> e;
        private final Key<T> key;

        private EditorHolder(Editor<T> e, Key<T> key) {
            this.e = e;
            this.key = key;
        }

        public void setTo(ElementAttributes attr) throws Editor.EditorParseException {
            T value = e.getValue();
            attr.set(key, value);
        }

        void setDependantEditor(Editor editor, boolean inverted) {
            if (key.getValueClass() != Boolean.class)
                throw new RuntimeException("key " + key.getName() + " is not a bool key");

            EditorFactory.BooleanEditor bool = (EditorFactory.BooleanEditor) e;
            editor.setEnabled(bool.getValue() ^ inverted);

            bool.getCheckBox().addActionListener(actionEvent -> editor.setEnabled(bool.getValue() ^ inverted));
        }
    }

}

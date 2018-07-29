/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
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
import java.util.List;

/**
 * Dialog used to edit Attributes.
 * The Dialog is configured by a list of {@link Key} instances, which are defined in the
 * {@link de.neemann.digital.core.element.Keys} class. The values are stored in an instance of
 * {@link ElementAttributes} which is a essentially a observable hash map.
 * This class is used to create the dialogs used to edit the element attributes but also to edit the
 * system settings, the model settings and the models attributes,
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

        editors = new ArrayList<>();

        topMostTextComponent = null;
        constraints = new ConstraintsBuilder().inset(3).fill();

        JPanel secondaryPanel = null;
        ConstraintsBuilder secondaryConstraints = null;

        boolean enableTwoTabs = !addCheckBoxes && enableTwoTabs(list);

        if (enableTwoTabs) {
            secondaryPanel = new JPanel(new GridBagLayout());
            secondaryConstraints = new ConstraintsBuilder().inset(3).fill();
        }

        boolean isSecondary = false;
        for (Key key : list) {
            Editor e = EditorFactory.INSTANCE.create(key, modifiedAttributes.get(key));
            editors.add(new EditorHolder(e, key));
            if (key.isSecondary() && enableTwoTabs) {
                e.addToPanel(secondaryPanel, key, modifiedAttributes, this, secondaryConstraints);
                isSecondary = true;
            } else
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

            if (key.isSecondary() && enableTwoTabs)
                secondaryConstraints.nextRow();
            else
                constraints.nextRow();

            if (topMostTextComponent == null && e instanceof EditorFactory.StringEditor)
                topMostTextComponent = ((EditorFactory.StringEditor) e).getTextComponent();

            final Key dependsOn = key.getDependsOn();
            if (dependsOn != null) {
                for (EditorHolder ed : editors) {
                    if (ed.key.getKey().equals(dependsOn.getKey())) {
                        ed.setDependantEditor(e, key.getCheckEnabled());
                    }
                }
            }

        }

        if (isSecondary) {
            JTabbedPane tp = new JTabbedPane(JTabbedPane.TOP);
            tp.addTab(Lang.get("attr_primary"), new JScrollPane(panel));
            tp.addTab(Lang.get("attr_secondary"), new JScrollPane(secondaryPanel));
            getContentPane().add(tp);
        } else
            getContentPane().add(new JScrollPane(panel));


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
     * Returns true if two tabs are to be used.
     *
     * @param list the list a keys
     * @return true if two tabs are to be used.
     */
    private boolean enableTwoTabs(List<Key> list) {
        int secCount = 0;
        int primCount = 0;
        for (Key k : list) {
            if (k.isSecondary())
                secCount++;
            else
                primCount++;
        }

        return (primCount > 1) && (secCount > 1);
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
    HashMap<Key, JCheckBox> getCheckBoxes() {
        return checkBoxes;
    }

    /**
     * Adds a button to this dialog
     *
     * @param label  a label
     * @param action the action
     * @return this for chained calls
     */
    AttributeDialog addButton(String label, ToolTipAction action) {
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
    AttributeDialog addButton(ToolTipAction action) {
        buttonPanel.add(action.createJButton(), 0);
        return this;
    }

    /**
     * store gui fields to attributes
     *
     * @throws Editor.EditorParseException Editor.EditorParseException
     */
    void storeEditedValues() throws Editor.EditorParseException {
        for (EditorHolder e : editors)
            e.setTo(modifiedAttributes);
    }

    /**
     * update gui fields with attributes
     *
     * @throws Editor.EditorParseException Editor.EditorParseException
     */
    void updateEditedValues() throws Editor.EditorParseException {
        for (EditorHolder e : editors)
            e.getFrom(modifiedAttributes);
    }

    /**
     * @return the modified attributes
     */
    ElementAttributes getModifiedAttributes() {
        return modifiedAttributes;
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
    Window getDialogParent() {
        return parent;
    }


    /**
     * @return true if ok is pressed
     */
    boolean isOkPressed() {
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

        void getFrom(ElementAttributes attr) {
            T value = attr.get(key);
            e.setValue(value);
        }

        void setDependantEditor(Editor editor, Key.CheckEnabled<T> checkEnabled) {
            try {
                editor.setEnabled(checkEnabled.isEnabled(e.getValue()));
            } catch (Editor.EditorParseException e1) {
                e1.printStackTrace();
            }

            e.addActionListener(actionEvent -> {
                        try {
                            editor.setEnabled(checkEnabled.isEnabled(e.getValue()));
                        } catch (Editor.EditorParseException e1) {
                            e1.printStackTrace();
                        }
                    }
            );
        }
    }

}

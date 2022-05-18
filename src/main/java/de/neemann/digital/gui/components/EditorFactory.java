/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.core.Bits;
import de.neemann.digital.core.*;
import de.neemann.digital.core.element.*;
import de.neemann.digital.core.extern.Application;
import de.neemann.digital.core.extern.PortDefinition;
import de.neemann.digital.core.io.InValue;
import de.neemann.digital.core.io.MIDIHelper;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.rom.ROMManagerFile;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.graphics.ColorScheme;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.model.InverterConfig;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.shapes.custom.CustomShapeDescription;
import de.neemann.digital.gui.Main;
import de.neemann.digital.gui.components.table.ShowStringDialog;
import de.neemann.digital.gui.components.testing.TestCaseDescriptionEditor;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestCaseDescription;
import de.neemann.gui.*;
import de.neemann.gui.language.Bundle;
import de.neemann.gui.language.Language;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import javax.swing.undo.UndoManager;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

/**
 * Factory used to create an editor for a given key/value pair.
 */
public final class EditorFactory {

    /**
     * The single EditorFactory instance.
     */
    static final EditorFactory INSTANCE = new EditorFactory();
    private final HashMap<Class<?>, Class<? extends Editor>> map = new HashMap<>();

    private EditorFactory() {
        add(String.class, StringEditor.class);
        add(Integer.class, IntegerEditor.class);
        add(Long.class, LongEditor.class);
        add(InValue.class, InValueEditor.class);
        add(File.class, FileEditor.class);
        add(Color.class, ColorEditor.class);
        add(Boolean.class, BooleanEditor.class);
        add(DataField.class, DataFieldEditor.class);
        add(Rotation.class, RotationEditor.class);
        add(Language.class, LanguageEditor.class);
        add(TestCaseDescription.class, TestCaseDescriptionEditor.class);
        add(InverterConfig.class, InverterConfigEditor.class);
        add(ROMManagerFile.class, ROMManagerEditor.class);
        add(Application.Type.class, ApplicationTypeEditor.class);
        add(CustomShapeDescription.class, CustomShapeEditor.class);
        add(ColorScheme.class, ColorSchemeEditor.class);
    }

    private <T> void add(Class<T> clazz, Class<? extends Editor<T>> editor) {
        map.put(clazz, editor);
    }

    /**
     * Creates a new Editor
     *
     * @param key   the key
     * @param value the value
     * @param <T>   the type of the value
     * @return the editor
     */
    public <T> Editor<T> create(Key<T> key, T value) {
        if (key == Keys.MIDI_INSTRUMENT)
            return (Editor<T>) new MidiInstrumentEditor(value.toString());

        Class<? extends Editor> fac = map.get(key.getValueClass());

        if (fac == null) {
            if (key instanceof Key.KeyEnum)
                return new EnumEditor((Enum) value, key);
            throw new RuntimeException("no editor found for " + key.getValueClass().getSimpleName());
        }

        try {
            Constructor<? extends Editor> c = fac.getConstructor(value.getClass(), Key.class);
            return c.newInstance(value, key);
        } catch (Exception e) {
            throw new RuntimeException("error creating editor", e);
        }
    }

    /**
     * Simple single component editor
     *
     * @param <T> the type to edit
     */
    public static abstract class LabelEditor<T> implements Editor<T> {
        private AttributeDialog attributeDialog;
        private boolean labelAtTop = false;
        private JComponent component;
        private JLabel label;

        @Override
        public void addToPanel(EditorPanel panel, Key<T> key, ElementAttributes elementAttributes, AttributeDialog attributeDialog) {
            this.attributeDialog = attributeDialog;
            label = new JLabel(key.getName() + ":  ");
            final String description = new LineBreaker().toHTML().breakLines(key.getDescription());
            label.setToolTipText(description);
            component = getComponent(elementAttributes);
            component.setToolTipText(description);
            if (labelAtTop) {
                panel.add(label, cb -> cb.width(2));
                panel.nextRow();
                panel.add(component, cb -> cb.width(2).dynamicWidth().dynamicHeight());
            } else {
                panel.add(label);
                panel.add(component, cb -> cb.x(1).dynamicWidth());
            }
        }

        /**
         * @return the containing dialog
         */
        public AttributeDialog getAttributeDialog() {
            return attributeDialog;
        }

        /**
         * returns the editor component
         *
         * @param elementAttributes the elements attributes
         * @return the component
         */
        protected abstract JComponent getComponent(ElementAttributes elementAttributes);

        @Override
        public void setEnabled(boolean enabled) {
            label.setEnabled(enabled);
            component.setEnabled(enabled);
        }

        /**
         * Sets the position of the label
         *
         * @param labelAtTop if true the label is placed at the top of the editing component.
         */
        void setLabelAtTop(boolean labelAtTop) {
            this.labelAtTop = labelAtTop;
        }
    }

    //Checkstyle flags redundant modifiers, which are not redundant. Maybe a bug in checkstyle?
    //CHECKSTYLE.OFF: RedundantModifier
    final static class StringEditor extends LabelEditor<String> {
        private static final String FILE_KEY = "_File";

        private final JTextComponent text;
        private final JComponent compToAdd;
        private final UndoManager undoManager;
        private JPopupMenu popup;

        public StringEditor(String value, Key<String> key) {
            if (key instanceof Key.LongString) {
                Key.LongString k = (Key.LongString) key;
                text = addF1Traversal(new JTextArea(k.getRows(), k.getColumns()));
                final JScrollPane scrollPane = new JScrollPane(text);

                if (k.getLineNumbers()) {
                    final TextLineNumber textLineNumber = new TextLineNumber(text, 3);
                    scrollPane.setRowHeaderView(textLineNumber);
                    text.setFont(new Font(Font.MONOSPACED, Font.PLAIN, Screen.getInstance().getFontSize()));
                }

                text.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        checkPopup(e);
                    }

                    public void mouseClicked(MouseEvent e) {
                        checkPopup(e);
                    }

                    public void mouseReleased(MouseEvent e) {
                        checkPopup(e);
                    }

                    private void checkPopup(MouseEvent e) {
                        if (e.isPopupTrigger()) {
                            getPopupMenu(key.getKey()).show(text, e.getX(), e.getY());
                        }
                    }
                });

                this.compToAdd = scrollPane;

                setLabelAtTop(true);
            } else {
                text = addF1Traversal(new JTextField(10));
                compToAdd = text;
            }
            text.setText(value);

            undoManager = createUndoManager(text);
        }

        JPopupMenu getPopupMenu(String keyName) {
            if (popup == null) {
                final String fileKey = keyName + FILE_KEY;
                popup = new JPopupMenu();
                popup.add(new ToolTipAction(Lang.get("btn_load")) {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        ElementAttributes attr = getAttributeDialog().getModifiedAttributes();
                        JFileChooser fc = new MyFileChooser();
                        fc.setSelectedFile(attr.getFile(fileKey));
                        if (fc.showOpenDialog(getAttributeDialog()) == JFileChooser.APPROVE_OPTION) {
                            File f = fc.getSelectedFile();
                            attr.setFile(fileKey, f);
                            try (InputStream in = new FileInputStream(f)) {
                                StringBuilder sb = new StringBuilder();
                                byte[] data = new byte[4096];
                                int len;
                                while ((len = in.read(data)) > 0)
                                    sb.append(new String(data, 0, len));

                                text.setText(sb.toString());
                            } catch (IOException e) {
                                new ErrorMessage(Lang.get("msg_errorReadingFile"))
                                        .addCause(e)
                                        .show(getAttributeDialog());
                            }
                        }
                    }
                }.createJMenuItem());
                popup.add(new ToolTipAction(Lang.get("btn_save")) {
                    @Override
                    public void actionPerformed(ActionEvent actionEvent) {
                        ElementAttributes attr = getAttributeDialog().getModifiedAttributes();
                        JFileChooser fc = new MyFileChooser();
                        fc.setSelectedFile(attr.getFile(fileKey));
                        if (fc.showSaveDialog(getAttributeDialog()) == JFileChooser.APPROVE_OPTION) {
                            File f = fc.getSelectedFile();
                            attr.setFile(fileKey, f);
                            try (OutputStream out = new FileOutputStream(f)) {
                                String s = text.getText();
                                out.write(s.getBytes());
                            } catch (IOException e) {
                                new ErrorMessage(Lang.get("msg_errorWritingFile"))
                                        .addCause(e)
                                        .show(getAttributeDialog());
                            }
                        }
                    }
                }.createJMenuItem());
            }
            return popup;
        }

        @Override
        public JComponent getComponent(ElementAttributes attr) {
            return compToAdd;
        }

        @Override
        public String getValue() {
            return text.getText().trim();
        }

        @Override
        public void setValue(String value) {
            if (!text.getText().equals(value)) {
                text.setText(value);
                undoManager.discardAllEdits();
            }
        }

        public JTextComponent getTextComponent() {
            return text;
        }
    }

    /**
     * Adds F1 as a focus traversal key to a text components.
     *
     * @param text The text component
     * @param <TC> the concrete type of the text component
     * @return the given text component
     */
    public static <TC extends JTextComponent> TC addF1Traversal(TC text) {
        HashSet<AWTKeyStroke> set = new HashSet<>(text.getFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS));
        set.add(KeyStroke.getKeyStroke("F1"));
        text.setFocusTraversalKeys(KeyboardFocusManager.FORWARD_TRAVERSAL_KEYS, set);
        return text;
    }

    private final static class IntegerEditor extends LabelEditor<Integer> {
        private static final IntegerValue[] DEFAULTS = createIntegerValues(0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16);

        private static IntegerValue[] createIntegerValues(int... values) {
            if (values == null)
                return null;

            IntegerValue[] v = new IntegerValue[values.length];
            for (int i = 0; i < v.length; i++)
                v[i] = new IntegerValue(values[i]);
            return v;
        }

        private final JComboBox<IntegerValue> comboBox;
        private final Key<Integer> key;

        public IntegerEditor(Integer value, Key<Integer> key) {
            this.key = key;
            IntegerValue[] selects = null;
            if (key instanceof Key.KeyInteger) {
                selects = createIntegerValues(((Key.KeyInteger) key).getComboBoxValues());
            }
            if (selects == null)
                selects = DEFAULTS;

            if (key instanceof Key.KeyInteger) {
                selects = cleanupSelects((Key.KeyInteger) key, selects);
            }

            comboBox = new JComboBox<>(selects);
            comboBox.setEditable(true);
            comboBox.setSelectedItem(new IntegerValue(value));
        }

        private static IntegerValue[] cleanupSelects(Key.KeyInteger key, IntegerValue[] selects) {
            if (!key.isMinOrMaxSet())
                return selects;

            ArrayList<IntegerValue> allowed = new ArrayList<>(selects.length);
            boolean minAdded = false;
            boolean maxAdded = false;
            for (IntegerValue iv : selects) {
                int v = iv.value;
                if (v <= key.getMin()) {
                    if (!minAdded) {
                        allowed.add(new IntegerValue(key.getMin()));
                        minAdded = true;
                    }
                } else if (v >= key.getMax()) {
                    if (!maxAdded) {
                        allowed.add(new IntegerValue(key.getMax()));
                        maxAdded = true;
                    }
                } else
                    allowed.add(iv);
            }
            return allowed.toArray(new IntegerValue[0]);
        }

        @Override
        public JComponent getComponent(ElementAttributes attr) {
            return comboBox;
        }

        @Override
        public Integer getValue() throws EditorParseException {
            Object item = comboBox.getSelectedItem();
            int value = 0;
            if (item instanceof Number)
                value = ((Number) item).intValue();
            else if (item instanceof IntegerValue)
                value = ((IntegerValue) item).getValue();
            else {
                try {
                    value = (int) Bits.decode(item.toString());
                } catch (Bits.NumberFormatException e) {
                    throw new EditorParseException(e);
                }
            }

            if (key instanceof Key.KeyInteger) {
                int min = ((Key.KeyInteger) key).getMin();
                if (value < min)
                    value = min;
                int max = ((Key.KeyInteger) key).getMax();
                if (value > max)
                    value = max;
            }

            return value;
        }

        @Override
        public void setValue(Integer value) {
            comboBox.setSelectedItem(new IntegerValue(value));
        }

        private static final class IntegerValue {
            private final int value;

            private IntegerValue(int value) {
                this.value = value;
            }

            private Integer getValue() {
                return value;
            }

            @Override
            public String toString() {
                if (value == Integer.MAX_VALUE)
                    return Lang.get("maxValue");
                else
                    return Integer.toString(value);
            }
        }
    }

    private final static class LongEditor extends LabelEditor<Long> {
        private static final Long[] DEFAULTS = {0L, 1L, 2L, 3L, 4L, 5L, 6L, 7L, 8L, 9L, 10L, 11L, 12L, 13L, 14L, 15L, 16L};
        private final JComboBox<Long> comboBox;

        public LongEditor(Long value, Key<Long> key) {
            comboBox = new JComboBox<>(DEFAULTS);
            comboBox.setEditable(true);
            comboBox.setSelectedItem(value.toString());
        }

        @Override
        public JComponent getComponent(ElementAttributes attr) {
            return comboBox;
        }

        @Override
        public void addToPanel(EditorPanel panel, Key<Long> key, ElementAttributes attr, AttributeDialog attributeDialog) {
            if (key.isAdaptiveIntFormat()) {
                Value value = new Value(attr.get(key), attr.getBits());
                comboBox.setSelectedItem(attr.getValueFormatter().formatToEdit(value));
            }
            super.addToPanel(panel, key, attr, attributeDialog);
        }

        @Override
        public Long getValue() throws EditorParseException {
            Object item = comboBox.getSelectedItem();
            long value = 0;
            if (item instanceof Number)
                value = ((Number) item).longValue();
            else {
                try {
                    value = Bits.decode(item.toString(), true);
                } catch (Bits.NumberFormatException e) {
                    throw new EditorParseException(e);
                }
            }
            return value;
        }

        @Override
        public void setValue(Long value) {
            comboBox.setSelectedItem(value.toString());
        }
    }

    private final static class InValueEditor extends LabelEditor<InValue> {
        private static final String[] DEFAULTS = {"Z", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16"};
        private final JComboBox<String> comboBox;

        public InValueEditor(InValue value, Key<Integer> key) {
            comboBox = new JComboBox<>(DEFAULTS);
            comboBox.setEditable(true);
            comboBox.setSelectedItem(value.toString());
        }

        @Override
        public void addToPanel(EditorPanel panel, Key<InValue> key, ElementAttributes attr, AttributeDialog attributeDialog) {
            if (key.isAdaptiveIntFormat()) {
                Value value = new Value(attr.get(key), attr.getBits());
                comboBox.setSelectedItem(attr.getValueFormatter().formatToEdit(value));
            }
            super.addToPanel(panel, key, attr, attributeDialog);
        }

        @Override
        public JComponent getComponent(ElementAttributes attr) {
            return comboBox;
        }

        @Override
        public InValue getValue() throws EditorParseException {
            Object item = comboBox.getSelectedItem();
            try {
                return new InValue(item.toString());
            } catch (Bits.NumberFormatException e) {
                throw new EditorParseException(e);
            }
        }

        @Override
        public void setValue(InValue value) {
            comboBox.setSelectedItem(value.toString());
        }
    }

    final static class BooleanEditor implements Editor<Boolean> {

        private final JCheckBox bool;

        public BooleanEditor(Boolean value, Key<Boolean> key) {
            bool = new JCheckBox(key.getName(), value);
            bool.setToolTipText(new LineBreaker().toHTML().breakLines(key.getDescription()));
        }

        @Override
        public Boolean getValue() {
            return bool.isSelected();
        }

        @Override
        public void addToPanel(EditorPanel panel, Key key, ElementAttributes elementAttributes, AttributeDialog attributeDialog) {
            panel.add(bool, cb -> cb.width(2));
        }

        @Override
        public void setEnabled(boolean enabled) {
            bool.setEnabled(enabled);
        }

        @Override
        public void setValue(Boolean value) {
            bool.setEnabled(value);
        }

        @Override
        public void addActionListener(ActionListener al) {
            bool.addActionListener(al);
        }
    }

    private final static class ColorEditor extends LabelEditor<Color> {

        private Color color;
        private final JButton button;

        public ColorEditor(Color value, Key<Color> key) {
            this.color = value;
            button = new JButton(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Color col = JColorChooser.showDialog(button, Lang.get("msg_color"), color);
                    if (col != null) {
                        // JColorChooser returns child classes from Color under certain circumstances.
                        // The following line ensures that color is a Color instance.
                        color = new Color(col.getRed(), col.getGreen(), col.getBlue(), col.getAlpha());
                        button.setBackground(color);
                    }
                }
            }) {
                @Override
                protected void paintComponent(Graphics graphics) {
                    graphics.setColor(Color.WHITE);
                    graphics.fillRect(0, 0, getWidth(), getHeight());
                    super.paintComponent(graphics);
                }
            };
            button.setPreferredSize(new Dimension(10, Screen.getInstance().getFontSize() * 3 / 2));
            button.setBackground(color);
        }

        @Override
        public JComponent getComponent(ElementAttributes attr) {
            return button;
        }

        @Override
        public Color getValue() {
            return color;
        }

        @Override
        public void setValue(Color value) {
            this.color = value;
            button.setBackground(color);
        }
    }

    private final static class FileEditor extends LabelEditor<File> {

        private final JPanel panel;
        private final JTextField textField;
        private final boolean directoryOnly;
        private final JButton button;

        public FileEditor(File value, Key<File> key) {
            if (key instanceof Key.KeyFile)
                directoryOnly = ((Key.KeyFile) key).isDirectoryOnly();
            else
                directoryOnly = false;

            panel = new JPanel(new BorderLayout());
            textField = new JTextField(value.getPath(), 20);
            button = new JButton(new AbstractAction("...") {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fc = new MyFileChooser(FileEditor.this.getValue());
                    if (directoryOnly)
                        fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
                    if (fc.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION)
                        textField.setText(fc.getSelectedFile().getPath());
                }
            });
            panel.add(textField, BorderLayout.CENTER);
            panel.add(button, BorderLayout.EAST);
        }

        @Override
        public JComponent getComponent(ElementAttributes attr) {
            return panel;
        }

        @Override
        public File getValue() {
            return new File(textField.getText());
        }

        @Override
        public void setValue(File value) {
            textField.setText(value.getPath());
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            textField.setEnabled(enabled);
            button.setEnabled(enabled);
        }
    }

    private final static class DataFieldEditor extends LabelEditor<DataField> {

        private DataField data;
        private boolean majorModification = false;
        private JButton editButton;

        public DataFieldEditor(DataField data, Key<DataField> key) {
            this.data = data;
        }

        @Override
        public JComponent getComponent(ElementAttributes attr) {
            JPanel panel = new JPanel(new GridLayout(1, 2));
            editButton = new ToolTipAction(Lang.get("btn_edit")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        getAttributeDialog().storeEditedValues();
                        int dataBits = attr.get(Keys.BITS);
                        int addrBits = getAddrBits(attr);
                        DataEditor de = new DataEditor(panel, data, dataBits, addrBits, false, SyncAccess.NOSYNC, attr.getValueFormatter());
                        if (attr.get(Keys.AUTO_RELOAD_ROM))
                            de.setFileName(attr.getFile(Keys.LAST_DATA_FILE, getAttributeDialog().getRootFile()));
                        if (de.showDialog()) {
                            DataField mod = de.getModifiedDataField();
                            if (!data.equals(mod))
                                majorModification = true;
                            data = mod;
                        }
                    } catch (EditorParseException e1) {
                        new ErrorMessage(Lang.get("msg_invalidEditorValue")).addCause(e1).show(panel);
                    }
                }
            }.createJButton();
            panel.add(editButton);
            return panel;
        }

        private int getAddrBits(ElementAttributes attr) {
            // INPUT_COUNT and ADDR_BITS must have the same default value!!!
            // If INPUT_COUNT is not present (default value is used) the default value of
            // ADDR_BITS is used. This works only if both have the same default value!!!
            if (attr.contains(Keys.INPUT_COUNT)) {
                // used to handle the LUT
                return attr.get(Keys.INPUT_COUNT);
            } else {
                // memory, RAM/ROM
                return attr.get(Keys.ADDR_BITS);
            }
        }

        @Override
        public DataField getValue() {
            data.trim();
            return data;
        }

        @Override
        public void setEnabled(boolean enabled) {
            super.setEnabled(enabled);
            editButton.setEnabled(enabled);
        }

        @Override
        public boolean invisibleModification() {
            return majorModification;
        }

        @Override
        public void setValue(DataField value) {
            this.data = value;
        }
    }

    private final static class RotationEditor extends LabelEditor<Rotation> {
        private static final String[] LIST = new String[]{Lang.get("rot_0"), Lang.get("rot_90"), Lang.get("rot_180"), Lang.get("rot_270")};

        private final Rotation rotation;
        private JComboBox<String> comb;

        public RotationEditor(Rotation rotation, Key<Rotation> key) {
            this.rotation = rotation;
        }

        @Override
        public JComponent getComponent(ElementAttributes elementAttributes) {
            comb = new JComboBox<>(LIST);
            comb.setSelectedIndex(rotation.getRotation());
            return comb;
        }

        @Override
        public Rotation getValue() {
            return new Rotation(comb.getSelectedIndex());
        }

        @Override
        public void setValue(Rotation value) {
            comb.setSelectedIndex(value.getRotation());
        }
    }

    private static class EnumEditor<E extends Enum> extends LabelEditor<E> {
        private final JComboBox comboBox;
        private final E[] values;
        private final String[] names;

        public EnumEditor(Enum value, Key<E> key) {
            if (!(key instanceof Key.KeyEnum))
                throw new RuntimeException("wrong enum type");
            this.names = ((Key.KeyEnum<E>) key).getNames();
            this.values = ((Key.KeyEnum<E>) key).getValues();

            comboBox = new JComboBox<>(names);
            comboBox.setSelectedIndex(value.ordinal());
        }

        @Override
        protected JComponent getComponent(ElementAttributes elementAttributes) {
            return comboBox;
        }

        @Override
        public E getValue() {
            return values[comboBox.getSelectedIndex()];
        }

        @Override
        public void setValue(E value) {
            comboBox.setSelectedIndex(value.ordinal());
        }

        @Override
        public void addActionListener(ActionListener actionListener) {
            comboBox.addActionListener(actionListener);
        }
    }

    private static final class ApplicationTypeEditor extends EnumEditor<Application.Type> {
        private JComboBox combo;
        private JButton checkButton;

        public ApplicationTypeEditor(Application.Type value, Key<Application.Type> key) {
            super(value, key);
        }

        @Override
        protected JComponent getComponent(ElementAttributes elementAttributes) {
            combo = (JComboBox) super.getComponent(elementAttributes);
            checkButton = new ToolTipAction(Lang.get("btn_checkCode")) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    int n = combo.getSelectedIndex();
                    if (n >= 0) {
                        Application.Type appType = Application.Type.values()[n];
                        Application app = Application.create(appType, elementAttributes);
                        if (app != null) {
                            try {
                                getAttributeDialog().storeEditedValues();
                                File root = getAttributeDialog().getRootFile();
                                final boolean consistent = app.ensureConsistency(elementAttributes, root);
                                if (consistent)
                                    getAttributeDialog().updateEditedValues();

                                PortDefinition ins = new PortDefinition(elementAttributes.get(Keys.EXTERNAL_INPUTS));
                                PortDefinition outs = new PortDefinition(elementAttributes.get(Keys.EXTERNAL_OUTPUTS));
                                String label = elementAttributes.getLabel();
                                try {
                                    String code = Application.getCode(elementAttributes, root);

                                    String message = app.checkCode(label, code, ins, outs, root);
                                    if (message != null && !message.isEmpty()) {
                                        createError(consistent, Lang.get("msg_checkResult") + "\n\n" + message).show(getAttributeDialog());
                                    }
                                } catch (IOException e) {
                                    createError(consistent, Lang.get("msg_checkResult")).addCause(e).show(getAttributeDialog());
                                }
                            } catch (EditorParseException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                private ErrorMessage createError(boolean consistent, String message) {
                    if (!consistent)
                        message = Lang.get("msg_codeNotConsistent") + "\n\n" + message;
                    return new ErrorMessage(message);
                }

            }.setToolTip(Lang.get("btn_checkCode_tt")).createJButton();
            combo.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    enableButton(elementAttributes);
                }
            });


            JPanel p = new JPanel(new BorderLayout());
            p.add(combo);
            p.add(checkButton, BorderLayout.EAST);

            enableButton(elementAttributes);

            return p;
        }

        void enableButton(ElementAttributes attr) {
            int n = combo.getSelectedIndex();
            if (n >= 0) {
                Application.Type appType = Application.Type.values()[n];
                Application app = Application.create(appType, attr);
                if (app != null)
                    checkButton.setEnabled(app.checkSupported());
            }
        }
    }

    private static class LanguageEditor extends LabelEditor<Language> {
        private final JComboBox<Language> comb;

        public LanguageEditor(Language language, Key<Language> key) {
            Bundle b = Lang.getBundle();
            List<Language> supLang = b.getSupportedLanguages();
            comb = new JComboBox<>(supLang.toArray(new Language[0]));
            comb.setSelectedItem(language);
        }

        @Override
        protected JComponent getComponent(ElementAttributes elementAttributes) {
            return comb;
        }

        @Override
        public Language getValue() {
            return (Language) comb.getSelectedItem();
        }

        @Override
        public void setValue(Language value) {
            comb.setSelectedItem(value);
        }
    }

    private static class InverterConfigEditor extends LabelEditor<InverterConfig> {

        private final JButton button;
        private InverterConfig inverterConfig;
        private ElementAttributes elementAttributes;

        public InverterConfigEditor(InverterConfig aInverterConfig, Key<InverterConfig> key) {
            this.inverterConfig = aInverterConfig;
            button = new JButton(new ToolTipAction(getButtonText()) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    VisualElement ve = getAttributeDialog().getVisualElement();
                    Window p = getAttributeDialog().getDialogParent();
                    if (ve != null && p instanceof Main) {
                        try {
                            getAttributeDialog().storeEditedValues();
                            ElementTypeDescription d = ((Main) p).getCircuitComponent().getLibrary().getElementType(ve.getElementName());
                            PinDescriptions in = d.getInputDescription(elementAttributes);
                            InputSelectDialog dialog = new InputSelectDialog(getAttributeDialog(), in, inverterConfig);
                            if (dialog.showDialog()) {
                                inverterConfig = dialog.getInverterConfig();
                                button.setText(getButtonText());
                            }
                        } catch (ElementNotFoundException | NodeException e) {
                            new ErrorMessage(Lang.get("msg_errGettingPinNames")).addCause(e).show(getAttributeDialog());
                        } catch (EditorParseException e) {
                            new ErrorMessage(Lang.get("msg_invalidEditorValue")).addCause(e).show(getAttributeDialog());
                        }
                    }
                }
            });
        }

        private String getButtonText() {
            if (inverterConfig.isEmpty())
                return Lang.get("msg_none");
            return inverterConfig.toString();
        }

        @Override
        public InverterConfig getValue() {
            return inverterConfig;
        }

        @Override
        protected JComponent getComponent(ElementAttributes elementAttributes) {
            this.elementAttributes = elementAttributes;
            return button;
        }

        @Override
        public void setValue(InverterConfig value) {
            inverterConfig = value;
            button.setText(getButtonText());
        }
    }

    private final static class InputSelectDialog extends JDialog {
        private final ArrayList<JCheckBox> boxes;
        private boolean ok = false;

        private InputSelectDialog(JDialog parent, PinDescriptions pins, InverterConfig inverterConfig) {
            super(parent, Lang.get("msg_inputsToInvert"), true);
            setDefaultCloseOperation(DISPOSE_ON_CLOSE);

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
            boxes = new ArrayList<>();
            for (PinDescription p : pins) {
                JCheckBox cb = new JCheckBox(p.getName());
                cb.setSelected(inverterConfig.contains(p.getName()));
                boxes.add(cb);
                panel.add(cb);
            }
            int pad = Screen.getInstance().getFontSize();
            panel.setBorder(BorderFactory.createEmptyBorder(pad, pad, pad, pad));
            getContentPane().add(panel);
            getContentPane().add(new JButton(new AbstractAction(Lang.get("ok")) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    ok = true;
                    dispose();
                }
            }), BorderLayout.SOUTH);

            pack();
            setLocationRelativeTo(parent);
        }

        public boolean showDialog() {
            setVisible(true);
            return ok;
        }

        private InverterConfig getInverterConfig() {
            InverterConfig.Builder ic = new InverterConfig.Builder();
            for (JCheckBox cb : boxes) {
                if (cb.isSelected())
                    ic.add(cb.getText());
            }
            return ic.build();
        }
    }

    private static class ROMManagerEditor extends LabelEditor<ROMManagerFile> {
        private final JPanel buttons;
        private ROMManagerFile romManager;

        public ROMManagerEditor(ROMManagerFile aRomManager, Key<ROMManagerFile> key) {
            this.romManager = aRomManager;
            buttons = new JPanel(new GridLayout(1, 2));
            buttons.add(new ToolTipAction(Lang.get("btn_help")) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    new ShowStringDialog(
                            getAttributeDialog(),
                            Lang.get("win_romDialogHelpTitle"),
                            Lang.get("msg_romDialogHelp"), true)
                            .setVisible(true);
                }
            }.createJButton());
            buttons.add(new ToolTipAction(Lang.get("btn_edit")) {
                @Override
                public void actionPerformed(ActionEvent actionEvent) {
                    Main main = getAttributeDialog().getMain();
                    if (main != null) {
                        final ROMEditorDialog romEditorDialog;
                        try {
                            CircuitComponent circuitComponent = main.getCircuitComponent();
                            Model model = new ModelCreator(circuitComponent.getCircuit(), circuitComponent.getLibrary()).createModel(false);
                            try {
                                romEditorDialog = new ROMEditorDialog(
                                        getAttributeDialog(),
                                        model,
                                        romManager);
                                if (romEditorDialog.showDialog())
                                    romManager = romEditorDialog.getROMManager();
                            } finally {
                                model.close();
                            }
                        } catch (ElementNotFoundException | PinException | NodeException e) {
                            new ErrorMessage(Lang.get("msg_errorCreatingModel")).addCause(e).show(getAttributeDialog());
                        }
                    }
                }
            }.createJButton());
        }

        @Override
        protected JComponent getComponent(ElementAttributes elementAttributes) {
            return buttons;
        }

        @Override
        public ROMManagerFile getValue() {
            return romManager;
        }

        @Override
        public void setValue(ROMManagerFile value) {
            romManager = value;
        }
    }

    private static final class MidiInstrumentEditor extends LabelEditor<String> {
        private JComboBox<String> comb;

        private MidiInstrumentEditor(String instrument) {
            String[] instruments;
            try {
                instruments = MIDIHelper.getInstance().getInstruments();
            } catch (NodeException e) {
                instruments = new String[]{"MIDI not available"};
            }
            comb = new JComboBox<>(instruments);
            comb.setSelectedItem(instrument);
        }

        @Override
        protected JComponent getComponent(ElementAttributes elementAttributes) {
            return comb;
        }

        @Override
        public String getValue() {
            return (String) comb.getSelectedItem();
        }

        @Override
        public void setValue(String value) {
            comb.setSelectedItem(value);
        }
    }

    /**
     * Enables undo in the given text component.
     *
     * @param text the text component
     * @return the undo manager
     */
    public static UndoManager createUndoManager(JTextComponent text) {
        final UndoManager undoManager;
        undoManager = new UndoManager();
        text.getDocument().addUndoableEditListener(undoManager);
        text.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_Z && (e.getModifiersEx() & ToolTipAction.getCTRLMask()) != 0) {
                    if (undoManager.canUndo())
                        undoManager.undo();
                } else if (e.getKeyCode() == KeyEvent.VK_Y && (e.getModifiersEx() & ToolTipAction.getCTRLMask()) != 0) {
                    if (undoManager.canRedo())
                        undoManager.redo();
                }
            }
        });
        return undoManager;
    }

}

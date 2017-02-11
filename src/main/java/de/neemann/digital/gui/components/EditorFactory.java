package de.neemann.digital.gui.components;

import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.Rotation;
import de.neemann.digital.core.io.IntFormat;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.ROM;
import de.neemann.digital.gui.components.testing.TestDataEditor;
import de.neemann.digital.gui.sync.NoSync;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestData;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.StringUtils;
import de.neemann.gui.ToolTipAction;
import de.neemann.gui.language.Bundle;
import de.neemann.gui.language.Language;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.List;

/**
 * @author hneemann
 */
public final class EditorFactory {

    /**
     * The single EditorFactory instance.
     */
    public static final EditorFactory INSTANCE = new EditorFactory();
    private HashMap<Class<?>, Class<? extends Editor>> map = new HashMap<>();

    private EditorFactory() {
        add(String.class, StringEditor.class);
        add(Integer.class, IntegerEditor.class);
        add(Color.class, ColorEditor.class);
        add(Boolean.class, BooleanEditor.class);
        add(DataField.class, DataFieldEditor.class);
        add(Rotation.class, RotationEditor.class);
        add(IntFormat.class, IntFormatsEditor.class);
        add(Language.class, LanguageEditor.class);
        add(TestData.class, TestDataEditor.class);
        add(FormatToExpression.class, FormatEditor.class);
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
        Class<? extends Editor> fac = map.get(key.getValueClass());
        if (fac == null)
            throw new RuntimeException("no editor found for " + key.getValueClass().getSimpleName());

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

        @Override
        public void addToPanel(JPanel panel, Key key, ElementAttributes elementAttributes, AttributeDialog attributeDialog) {
            this.attributeDialog = attributeDialog;
            JLabel label = new JLabel(key.getName() + ":  ");
            if (labelAtTop)
                label.setVerticalAlignment(JLabel.TOP);
            final String description = StringUtils.textToHTML(key.getDescription());
            label.setToolTipText(description);
            panel.add(label, DialogLayout.LABEL);
            JComponent component = getComponent(elementAttributes);
            component.setToolTipText(description);
            panel.add(component, DialogLayout.INPUT);
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

        /**
         * Sets the position of the label
         *
         * @param labelAtTop if true the label is placed at the top of the editing component.
         */
        public void setLabelAtTop(boolean labelAtTop) {
            this.labelAtTop = labelAtTop;
        }
    }

    private final static class StringEditor extends LabelEditor<String> {

        private final JTextComponent text;
        private final JComponent compToAdd;

        public StringEditor(String value, Key<String> key) {
            if (key instanceof Key.LongString) {
                text = new JTextArea(6, 30);
                compToAdd = new JScrollPane(text);
                setLabelAtTop(true);
            } else {
                text = new JTextField(10);
                compToAdd = text;
            }
            text.setText(value);
        }

        @Override
        public JComponent getComponent(ElementAttributes attr) {
            return compToAdd;
        }

        @Override
        public String getValue() {
            return text.getText();
        }

    }

    private final static class IntegerEditor extends LabelEditor<Integer> {
        private static final Integer[] DEFAULTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
        private final JComboBox<Integer> comboBox;
        private final Key<Integer> key;

        public IntegerEditor(Integer value, Key<Integer> key) {
            this.key = key;
            Integer[] selects = null;
            if (key instanceof Key.KeyInteger) {
                selects = ((Key.KeyInteger) key).getComboBoxValues();
            }
            if (selects == null)
                selects = DEFAULTS;
            comboBox = new JComboBox<>(selects);
            comboBox.setEditable(true);
            comboBox.setSelectedItem(value);
        }

        @Override
        public JComponent getComponent(ElementAttributes attr) {
            return comboBox;
        }

        @Override
        public Integer getValue() {
            Object item = comboBox.getSelectedItem();
            int value = 0;
            if (item instanceof Number)
                value = ((Number) item).intValue();
            else {
                value = Integer.decode(item.toString());
            }

            if (key instanceof Key.KeyInteger) {
                int min = ((Key.KeyInteger) key).getMin();
                if (value < min)
                    value = min;
            }

            return value;
        }
    }

    private final static class BooleanEditor implements Editor<Boolean> {

        private final JCheckBox bool;

        public BooleanEditor(Boolean value, Key<Boolean> key) {
            bool = new JCheckBox(key.getName(), value);
            bool.setToolTipText(StringUtils.textToHTML(key.getDescription()));
        }

        @Override
        public Boolean getValue() {
            return bool.isSelected();
        }

        @Override
        public void addToPanel(JPanel panel, Key key, ElementAttributes elementAttributes, AttributeDialog attributeDialog) {
            panel.add(bool, DialogLayout.BOTH);
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
                        color = col;
                        button.setBackground(color);
                    }
                }
            });
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
    }

    private final static class DataFieldEditor extends LabelEditor<DataField> {

        private DataField data;

        public DataFieldEditor(DataField data, Key<DataField> key) {
            this.data = data;
        }

        @Override
        public JComponent getComponent(ElementAttributes attr) {
            JPanel panel = new JPanel(new FlowLayout());
            panel.add(new ToolTipAction(Lang.get("btn_edit")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    int bits = attr.get(Keys.BITS);
                    int size;
                    if (attr.contains(Keys.INPUT_COUNT)) {
                        // used to handle the LUT
                        size = 1 << attr.get(Keys.INPUT_COUNT);
                    } else {
                        // memory, RAM/ROM
                        size = 1 << attr.get(Keys.ADDR_BITS);
                    }
                    DataEditor de = new DataEditor(panel, data, size, bits, false, NoSync.INST);
                    if (de.showDialog()) {
                        data = de.getModifiedDataField();
                    }
                }
            }.createJButton());
            panel.add(new ToolTipAction(Lang.get("btn_load")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fc = new JFileChooser();
                    fc.setSelectedFile(attr.getFile(ROM.LAST_DATA_FILE_KEY));
                    fc.setFileFilter(new FileNameExtensionFilter("hex", "hex"));
                    if (fc.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                        attr.setFile(ROM.LAST_DATA_FILE_KEY, fc.getSelectedFile());
                        try {
                            data = new DataField(fc.getSelectedFile());
                        } catch (IOException e1) {
                            new ErrorMessage(Lang.get("msg_errorReadingFile")).addCause(e1).show(panel);
                        }
                    }
                }
            }.createJButton());
            panel.add(new ToolTipAction(Lang.get("btn_reload")) {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            try {
                                data = new DataField(attr.getFile(ROM.LAST_DATA_FILE_KEY));
                            } catch (IOException e1) {
                                new ErrorMessage(Lang.get("msg_errorReadingFile")).addCause(e1).show(panel);
                            }
                        }
                    }
                            .setActive(attr.getFile(ROM.LAST_DATA_FILE_KEY) != null)
                            .setToolTip(Lang.get("btn_reload_tt"))
                            .createJButton()
            );
            panel.add(new ToolTipAction(Lang.get("btn_save")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fc = new JFileChooser();
                    fc.setSelectedFile(attr.getFile(ROM.LAST_DATA_FILE_KEY));
                    fc.setFileFilter(new FileNameExtensionFilter("hex", "hex"));
                    if (fc.showSaveDialog(panel) == JFileChooser.APPROVE_OPTION) {
                        attr.setFile(ROM.LAST_DATA_FILE_KEY, fc.getSelectedFile());
                        try {
                            data.saveTo(fc.getSelectedFile());
                        } catch (IOException e1) {
                            new ErrorMessage(Lang.get("msg_errorWritingFile")).addCause(e1).show(panel);
                        }
                    }
                }
            }.createJButton());
            return panel;
        }

        @Override
        public DataField getValue() {
            return data.getMinimized();
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
    }

    private static final class IntFormatsEditor extends EnumEditor<IntFormat> {
        public IntFormatsEditor(IntFormat value, Key<IntFormat> key) {
            super(value, key);
        }
    }

    private static class LanguageEditor extends LabelEditor<Language> {
        private JComboBox comb;

        public LanguageEditor(Language language, Key<Rotation> key) {
            Bundle b = Lang.getBundle();
            List<Language> supLang = b.getSupportedLanguages();
            comb = new JComboBox<>(supLang.toArray(new Language[supLang.size()]));
            comb.setSelectedItem(Lang.currentLanguage());
        }

        @Override
        protected JComponent getComponent(ElementAttributes elementAttributes) {
            return comb;
        }

        @Override
        public Language getValue() {
            return (Language) comb.getSelectedItem();
        }
    }

    private static class FormatEditor extends LabelEditor<FormatToExpression> {
        private JComboBox comb;

        public FormatEditor(FormatToExpression format, Key<Rotation> key) {
            FormatToExpression[] formats = FormatToExpression.getAvailFormats();
            comb = new JComboBox<>(formats);
            comb.setSelectedItem(format);
        }

        @Override
        protected JComponent getComponent(ElementAttributes elementAttributes) {
            return comb;
        }

        @Override
        public FormatToExpression getValue() {
            return (FormatToExpression) comb.getSelectedItem();
        }
    }
}

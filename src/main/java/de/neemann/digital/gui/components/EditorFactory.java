package de.neemann.digital.gui.components;

import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Rotation;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.lang.Lang;
import de.neemann.gui.ErrorMessage;
import de.neemann.gui.ToolTipAction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.HashMap;

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
    public <T> Editor<T> create(AttributeKey<T> key, T value) {
        Class<? extends Editor> fac = map.get(key.getValueClass());
        if (fac == null)
            throw new RuntimeException("no editor found for " + key.getValueClass().getSimpleName());

        try {
            Constructor<? extends Editor> c = fac.getConstructor(value.getClass(), AttributeKey.class);
            return c.newInstance(value, key);
        } catch (Exception e) {
            throw new RuntimeException("error creating editor", e);
        }
    }

    private static abstract class LabelEditor<T> implements Editor<T> {
        @Override
        public void addToPanel(JPanel panel, AttributeKey key, ElementAttributes elementAttributes) {
            panel.add(new JLabel(key.getName() + ":  "), DialogLayout.LABEL);
            panel.add(getComponent(elementAttributes), DialogLayout.INPUT);
        }

        protected abstract Component getComponent(ElementAttributes elementAttributes);
    }

    private final static class StringEditor extends LabelEditor<String> {

        private final JTextField text;

        public StringEditor(String value, AttributeKey<String> key) {
            text = new JTextField(10);
            text.setText(value);
        }

        @Override
        public Component getComponent(ElementAttributes attr) {
            return text;
        }

        @Override
        public String getValue() {
            return text.getText();
        }

    }

    private final static class IntegerEditor extends LabelEditor<Integer> {
        private final JComboBox<Integer> comboBox;

        public IntegerEditor(Integer value, AttributeKey<Integer> key) {
            Integer[] selects = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
            if (key instanceof AttributeKey.AttributeKeyInteger) {
                selects = ((AttributeKey.AttributeKeyInteger) key).getComboBoxValues();
            }
            comboBox = new JComboBox<>(selects);
            comboBox.setEditable(true);
            comboBox.setSelectedItem(value);
        }

        @Override
        public Component getComponent(ElementAttributes attr) {
            return comboBox;
        }

        @Override
        public Integer getValue() {
            Object item = comboBox.getSelectedItem();
            if (item instanceof Number)
                return ((Number) item).intValue();
            else {
                return Integer.decode(item.toString());
            }
        }
    }

    private final static class BooleanEditor implements Editor<Boolean> {

        private final JCheckBox bool;

        public BooleanEditor(Boolean value, AttributeKey<Boolean> key) {
            bool = new JCheckBox(key.getName(), value);
        }

        @Override
        public Boolean getValue() {
            return bool.isSelected();
        }

        @Override
        public void addToPanel(JPanel panel, AttributeKey key, ElementAttributes elementAttributes) {
            panel.add(bool, DialogLayout.BOTH);
        }
    }

    private final static class ColorEditor extends LabelEditor<Color> {

        private Color color;
        private final JButton button;

        public ColorEditor(Color value, AttributeKey<Color> key) {
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
        public Component getComponent(ElementAttributes attr) {
            return button;
        }

        @Override
        public Color getValue() {
            return color;
        }
    }

    private final static class DataFieldEditor extends LabelEditor<DataField> {

        private DataField data;

        public DataFieldEditor(DataField data, AttributeKey<DataField> key) {
            this.data = data;
        }

        @Override
        public Component getComponent(ElementAttributes attr) {
            JPanel panel = new JPanel(new FlowLayout());
            panel.add(new ToolTipAction(Lang.get("btn_edit")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    DataEditor de = new DataEditor(panel, data, attr);
                    if (de.showDialog()) {
                        data = de.getDataField();
                    }
                }
            }.createJButton());
            panel.add(new ToolTipAction(Lang.get("btn_load")) {
                @Override
                public void actionPerformed(ActionEvent e) {
                    JFileChooser fc = new JFileChooser();
                    fc.setSelectedFile(attr.getFile("lastDataFile"));
                    if (fc.showOpenDialog(panel) == JFileChooser.APPROVE_OPTION) {
                        attr.setFile("lastDataFile", fc.getSelectedFile());
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
                                data = new DataField(attr.getFile("lastDataFile"));
                            } catch (IOException e1) {
                                new ErrorMessage(Lang.get("msg_errorReadingFile")).addCause(e1).show(panel);
                            }
                        }
                    }
                            .setActive(attr.getFile("lastDataFile") != null)
                            .setToolTip(Lang.get("btn_reload_tt"))
                            .createJButton()
            );
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

        public RotationEditor(Rotation rotation, AttributeKey<Rotation> key) {
            this.rotation = rotation;
        }

        @Override
        public Component getComponent(ElementAttributes elementAttributes) {
            comb = new JComboBox<>(LIST);
            comb.setSelectedIndex(rotation.getRotation());
            return comb;
        }

        @Override
        public Rotation getValue() {
            return new Rotation(comb.getSelectedIndex());
        }
    }
}

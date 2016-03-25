package de.neemann.digital.gui.components;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.lang.reflect.Constructor;
import java.util.HashMap;

/**
 * @author hneemann
 */
public final class EditorFactory {

    public static final EditorFactory INSTANCE = new EditorFactory();
    private HashMap<Class<?>, Class<? extends Editor>> map = new HashMap<>();

    private EditorFactory() {
        add(String.class, StringEditor.class);
        add(Integer.class, IntegerEditor.class);
        add(Color.class, ColorEditor.class);
        add(Boolean.class, BooleanEditor.class);
    }

    public <T> void add(Class<T> clazz, Class<? extends Editor<T>> editor) {
        map.put(clazz, editor);
    }

    public <T> Editor<T> create(Class<T> clazz, T value) {
        Class<? extends Editor> fac = map.get(clazz);
        if (fac == null)
            throw new RuntimeException("no editor found for " + clazz.getSimpleName());

        try {
            Constructor<? extends Editor> c = fac.getConstructor(value.getClass());
            return c.newInstance(value);
        } catch (Exception e) {
            throw new RuntimeException("error creating editor", e);
        }
    }

    private static class StringEditor implements Editor<String> {

        private final JTextField text;

        public StringEditor(String value) {
            text = new JTextField(value);
        }

        @Override
        public Component getComponent() {
            return text;
        }

        @Override
        public String getValue() {
            return text.getText();
        }
    }

    private static class IntegerEditor implements Editor<Integer> {
        private final JComboBox<Integer> comboBox;

        public IntegerEditor(Integer value) {
            comboBox = new JComboBox<>(new Integer[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16});
            comboBox.setEditable(true);
            comboBox.setSelectedItem(value);
        }

        @Override
        public Component getComponent() {
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

    private static class BooleanEditor implements Editor<Boolean> {

        private final JCheckBox bool;

        public BooleanEditor(Boolean value) {
            bool = new JCheckBox("", value);
        }

        @Override
        public Component getComponent() {
            return bool;
        }

        @Override
        public Boolean getValue() {
            return bool.isSelected();
        }
    }


    private static class ColorEditor implements Editor<Color> {

        private Color color;
        private final JButton button;

        public ColorEditor(Color value) {
            this.color = value;
            button = new JButton(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    Color col = JColorChooser.showDialog(button, "Color", color);
                    if (col != null) {
                        color = col;
                        button.setBackground(color);
                    }
                }
            });
            button.setBackground(color);
        }

        @Override
        public Component getComponent() {
            return button;
        }

        @Override
        public Color getValue() {
            return color;
        }
    }

}

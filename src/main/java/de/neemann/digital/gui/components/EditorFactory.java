package de.neemann.digital.gui.components;

import java.awt.*;
import java.util.HashMap;

/**
 * @author hneemann
 */
public final class EditorFactory {

    public static final EditorFactory INSTANCE = new EditorFactory();
    private HashMap<Class, Editor> map = new HashMap<>();

    private EditorFactory() {
        add(Integer.class, new EditorString<Integer>() {
            @Override
            public Integer toValue(String text) {
                return Integer.parseInt(text);
            }
        });
        add(Double.class, new EditorString<Double>() {
            @Override
            public Double toValue(String text) {
                return Double.parseDouble(text);
            }
        });
        add(String.class, new EditorString<String>() {
            @Override
            public String toValue(String text) {
                return text;
            }
        });
        add(Color.class, new Editor<Color>() {
            @Override
            public String toText(Color value) {
                return Integer.toHexString(value.getRGB() & 0xffffff);
            }

            @Override
            public Color toValue(String text) {
                return new Color(Integer.parseInt(text.toUpperCase(), 16));
            }
        });
    }

    private <T> void add(Class<T> clazz, Editor<T> editor) {
        map.put(clazz, editor);
    }

    public <T> Editor<T> get(Class<T> clazz) {
        Editor ed = map.get(clazz);
        if (ed == null)
            throw new RuntimeException("no editor for " + clazz.getName());
        return ed;
    }

    private abstract class EditorString<T> implements Editor<T> {
        @Override
        public String toText(T value) {
            return value.toString();
        }
    }
}

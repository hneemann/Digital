package de.neemann.digital.core.element;

import de.neemann.digital.lang.Lang;

/**
 * Class is used to define the keys used to access the models attributes
 *
 * @param <VALUE> the keys value type
 * @author hneemann
 */
public class Key<VALUE> {
    private final String key;
    private final VALUE def;
    private final String name;
    private final String description;

    /**
     * Creates a new Key
     *
     * @param key the key
     * @param def the default value
     */
    public Key(String key, VALUE def) {
        this.key = key;
        String langName = "key_" + key.replace(" ", "");
        this.name = Lang.get(langName);
        this.description = Lang.getNull(langName + "_tt");
        if (def == null)
            throw new NullPointerException();
        this.def = def;
    }

    /**
     * Returns the attributes key
     *
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * Returns the attributes display name
     *
     * @return thr name
     */
    public String getName() {
        return name;
    }

    /**
     * @return the default value of this key
     */
    public VALUE getDefault() {
        return def;
    }

    /**
     * @return The values class
     */
    public Class getValueClass() {
        return def.getClass();
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * @return the keys description
     */
    public String getDescription() {
        if (description != null)
            return description;
        else
            return name;
    }

    /**
     * A integer attribute.
     * Stores additional combo box values
     */
    public static class KeyInteger extends Key<Integer> {
        private Integer[] values;
        private int min = Integer.MIN_VALUE;

        KeyInteger(String key, Integer def) {
            super(key, def);
        }

        KeyInteger setComboBoxValues(Integer[] values) {
            this.values = values;
            return this;
        }

        KeyInteger setMin(int min) {
            this.min = min;
            return this;
        }

        /**
         * @return the values to show in the combo box
         */
        public Integer[] getComboBoxValues() {
            return values;
        }

        /**
         * @return the min value
         */
        public int getMin() {
            return min;
        }
    }

    /**
     * A bits attribute.
     * Stores additional combo box values
     */
    static final class KeyBits extends KeyInteger {
        private static final Integer[] VALUES = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};

        KeyBits(String key) {
            super(key, 1);
            setMin(1);
            setComboBoxValues(VALUES);
        }
    }

    /**
     * Used to store enum values
     *
     * @param <E> the enum type
     */
    public static final class KeyEnum<E extends Enum> extends Key<E> {
        private final E[] values;
        private final String[] names;

        KeyEnum(String key, E def, E[] values) {
            super(key, def);
            this.values = values;

            names = new String[values.length];
            for (int i = 0; i < values.length; i++)
                names[i] = Lang.get("key_" + key.replace(" ", "") + "_" + values[i].name());
        }

        /**
         * @return the enums values
         */
        public E[] getValues() {
            return values;
        }

        /**
         * @return the enums translated names
         */
        public String[] getNames() {
            return names;
        }
    }

    /**
     * A special string key to flag long multi line strings.
     */
    public static final class LongString extends Key<String> {
        /**
         * Creates a new Key
         *
         * @param key the key
         */
        public LongString(String key) {
            super(key, "");
        }
    }
}

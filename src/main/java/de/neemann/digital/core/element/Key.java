package de.neemann.digital.core.element;

import de.neemann.digital.lang.Lang;

import java.io.File;

/**
 * Class is used to define the keys used to access the models attributes
 *
 * @param <VALUE> the keys value type
 * @author hneemann
 */
public class Key<VALUE> {
    private final String key;
    private final VALUE def;
    private final String langKey;
    private boolean groupEditAllowed = false;

    /**
     * Creates a new Key
     *
     * @param key the key
     * @param def the default value
     */
    public Key(String key, VALUE def) {
        this.key = key;
        langKey = "key_" + key.replace(" ", "");
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
        return Lang.get(langKey);
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
        return getName();
    }

    /**
     * @return the keys description
     */
    public String getDescription() {
        String d = Lang.getNull(langKey + "_tt");
        if (d != null)
            return d;
        else
            return getName();
    }

    /**
     * @return the language key
     */
    public String getLangKey() {
        return langKey;
    }

    /**
     * @return true if group edit is allowed
     */
    public boolean isGroupEditAllowed() {
        return groupEditAllowed;
    }

    Key<VALUE> setGroupEditAllowed(boolean groupEditAllowed) {
        this.groupEditAllowed = groupEditAllowed;
        return this;
    }

    /**
     * A integer attribute.
     * Stores additional combo box values
     */
    public static class KeyInteger extends Key<Integer> {
        private Integer[] values;
        private int min = Integer.MIN_VALUE;
        private int max = Integer.MAX_VALUE;

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

        KeyInteger setMax(int max) {
            this.max = max;
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

        /**
         * @return the max value
         */
        public int getMax() {
            return max;
        }
    }

    /**
     * A bits attribute.
     * Stores additional combo box values
     */
    static final class KeyBits extends KeyInteger {
        private static final Integer[] VALUES = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 32};

        KeyBits(String key, Integer def) {
            super(key, def);
            setMin(1);
            super.setMax(64);
            setComboBoxValues(VALUES);
            setGroupEditAllowed(true);
        }

        KeyBits setMax(int bits) {
            super.setMax(bits);
            return this;
        }
    }

    /**
     * Stores a file
     */
    public static final class KeyFile extends Key<File> {


        private boolean directoryOnly;

        KeyFile(String key, File def) {
            super(key, def);
            setDirectoryOnly(false);
        }

        /**
         * Set the directory only mode
         *
         * @param directoryOnly if true you can select only directories
         * @return this for chained calls
         */
        public KeyFile setDirectoryOnly(boolean directoryOnly) {
            this.directoryOnly = directoryOnly;
            return this;
        }

        /**
         * @return true if you can select only directories
         */
        public boolean isDirectoryOnly() {
            return directoryOnly;
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
                names[i] = Lang.get(getLangKey(values[i]));
            setGroupEditAllowed(true);
        }

        /**
         * creates the language key for the enum values
         *
         * @param value the value
         * @return the language key
         */
        public String getLangKey(E value) {
            return getLangKey() + "_" + value.name();
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

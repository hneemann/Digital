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
    private Key dependsOn;
    private boolean dependsOnInvert;

    // Both values are always null in digital.
    // Both are only used within a custom implemented component.
    private String name;
    private String description;

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
     * @return the name of the key
     */
    public String getName() {
        if (name != null)
            return name;
        else
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
        if (description != null)
            return description;
        else {
            String d = Lang.getNull(langKey + "_tt");
            if (d != null)
                return d;
            else
                return getName();
        }
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

    /**
     * Allows this attribute in group edit.
     *
     * @return this for chained calls
     */
    public Key<VALUE> allowGroupEdit() {
        this.groupEditAllowed = true;
        return this;
    }

    /**
     * Sets the name of this key.
     * Is not used in Digital at all.
     * This method can be used to define custom keys in custom java components.
     *
     * @param name the name of the key
     * @return this for chained calls
     */
    public Key<VALUE> setName(String name) {
        this.name = name;
        return this;
    }

    /**
     * Sets the description of this key.
     * Is not used in Digital at all.
     * This method can be used to define custom keys in custom java components.
     *
     * @param description the name of the key
     * @return this for chained calls
     */
    public Key<VALUE> setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * @return returns the key this key depends on
     */
    public Key getDependsOn() {
        return dependsOn;
    }

    /**
     * @return true if dependency is inverted
     */
    public boolean isDependsOnInverted() {
        return dependsOnInvert;
    }

    /**
     * Sets the key this key depends on.
     *
     * @param key the key where this key depends on
     * @return this for chained calls
     */
    public Key<VALUE> setDependsOn(Key key) {
        return setDependsOn(key, false);
    }

    /**
     * Sets the key this key depends on.
     *
     * @param key    the key where this key depends on
     * @param invert if true dependency is inverted
     * @return this for chained calls
     */
    public Key<VALUE> setDependsOn(Key key, boolean invert) {
        this.dependsOn = key;
        this.dependsOnInvert = invert;
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

        /**
         * Creates a new instance
         *
         * @param key the key to use
         * @param def the default value
         */
        public KeyInteger(String key, Integer def) {
            super(key, def);
        }

        /**
         * Sets the values to use in the combo box.
         *
         * @param values the values
         * @return this for chained calls
         */
        public KeyInteger setComboBoxValues(Integer[] values) {
            this.values = values;
            return this;
        }

        /**
         * Sets the minimal value which is allowed.
         *
         * @param min the minimal value allowed
         * @return this for chained calls
         */
        public KeyInteger setMin(int min) {
            this.min = min;
            return this;
        }

        /**
         * Sets the maximal value which is allowed.
         *
         * @param max the  maximal value allowed
         * @return this for chained calls
         */
        public KeyInteger setMax(int max) {
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
    public static final class KeyBits extends KeyInteger {
        private static final Integer[] VALUES = new Integer[]{1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 32};

        /**
         * Creates a new bits key
         *
         * @param key the key
         * @param def the default value
         */
        public KeyBits(String key, Integer def) {
            super(key, def);
            setMin(1);
            setMax(64);
            setComboBoxValues(VALUES);
            allowGroupEdit();
        }
    }

    /**
     * Stores a file
     */
    public static final class KeyFile extends Key<File> {

        private boolean directoryOnly;

        /**
         * Creates a new file key
         *
         * @param key the key
         * @param def the default file
         */
        public KeyFile(String key, File def) {
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

        /**
         * Creates a new emum key
         *
         * @param key    the key
         * @param def    the default value
         * @param values the possible values
         */
        public KeyEnum(String key, E def, E[] values) {
            super(key, def);
            this.values = values;

            names = new String[values.length];
            for (int i = 0; i < values.length; i++)
                names[i] = Lang.get(getLangKey(values[i]));
            allowGroupEdit();
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

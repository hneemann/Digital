/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.element;

import de.neemann.digital.lang.Lang;

import java.io.File;

/**
 * Class is used to define the keys used to access the models attributes
 *
 * @param <VALUE> the keys value type
 */
public class Key<VALUE> {
    private final String key;
    private final DefaultFactory<VALUE> defFactory;
    private String langKey;
    private boolean groupEditAllowed = false;
    private Key dependsOn;
    private CheckEnabled checkEnabled;
    private boolean isSecondary;
    private boolean requiresRestart = false;
    private boolean requiresRepaint = false;
    private String panelId;

    // Both values are always null in digital.
    // Both are only used within a custom implemented component.
    private String name;
    private String description;
    private boolean adaptiveIntFormat;

    /**
     * Creates a new Key.
     * Use this constructor only if the def value is not mutable!
     *
     * @param key the key
     * @param def the default value
     */
    public Key(String key, VALUE def) {
        this(key, () -> def);
        if (def == null)
            throw new NullPointerException();
    }

    /**
     * Creates a new Key.
     * Use this constructor if the def value is mutable!
     *
     * @param key        the key
     * @param defFactory the factory to create a default value
     */
    public Key(String key, DefaultFactory<VALUE> defFactory) {
        this.key = key;
        langKey = "key_" + key.replace(" ", "");
        if (defFactory == null)
            throw new NullPointerException();
        this.defFactory = defFactory;
    }

    /**
     * Allows to shate translations between keys
     *
     * @param key the key which translation is to share
     * @return this for chained calls
     */
    Key<VALUE> useTranslationOf(Key key) {
        langKey = key.langKey;
        return this;
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
        return defFactory.createDefault();
    }

    /**
     * @return The values class
     */
    public Class getValueClass() {
        return getDefault().getClass();
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
    public CheckEnabled getCheckEnabled() {
        return checkEnabled;
    }

    /**
     * Sets a bool dependency for this key
     *
     * @param key the key which this key depends on
     * @return this for chained calls
     */
    public Key<VALUE> setDependsOn(Key<Boolean> key) {
        return setDependsOn(key, o -> o);
    }

    /**
     * Sets the key this key depends on.
     *
     * @param key          the key where this key depends on
     * @param checkEnabled function which determines if the editor is enabled or not
     * @param <KV>         type of key which this key depends on
     * @return this for chained calls
     */
    public <KV> Key<VALUE> setDependsOn(Key<KV> key, CheckEnabled<KV> checkEnabled) {
        this.dependsOn = key;
        this.checkEnabled = checkEnabled;
        return this;
    }

    /**
     * @return true is this is a secondary attribute
     */
    public boolean isSecondary() {
        return isSecondary;
    }

    /**
     * Makes this attribute to be a secondary attribute
     *
     * @return this for chained calls
     */
    public Key<VALUE> setSecondary() {
        isSecondary = true;
        return this;
    }

    /**
     * Called if the modification of this setting needs a restart.
     *
     * @return this for chained calls
     */
    public Key<VALUE> setRequiresRestart() {
        requiresRestart = true;
        return this;
    }

    /**
     * @return true if changing this value needs a restart
     */
    public boolean getRequiresRestart() {
        return requiresRestart;
    }

    /**
     * Called if this setting needs a repaint.
     * This means, that the circuit graphics became invalid
     * if this setting has changed.
     *
     * @return this for chained calls
     */
    public Key<VALUE> setRequiresRepaint() {
        requiresRepaint = true;
        return this;
    }

    /**
     * @return true if changing this value needs a repaint
     */
    public boolean getRequiresRepaint() {
        return requiresRepaint;
    }

    /**
     * Enables an adaptive int format in number editors.
     * This means that the string representation of the number is controlled
     * by the IntFormat stored in the elements attributes.
     *
     * @return this for chained calls
     */
    public Key<VALUE> setAdaptiveIntFormat() {
        adaptiveIntFormat = true;
        return this;
    }

    /**
     * @return true if adaptive int format is required
     */
    public boolean isAdaptiveIntFormat() {
        return adaptiveIntFormat;
    }

    /**
     * Moves this key to the panel with the given id
     *
     * @param panelId the panel id
     * @return this for chained calls
     */
    public Key<VALUE> setPanelId(String panelId) {
        this.panelId = panelId;
        return this;
    }

    /**
     * @return the panel id, null if no panel is set
     */
    public String getPanelId() {
        return panelId;
    }

    /**
     * A integer attribute.
     * Stores additional combo box values
     */
    public static class KeyInteger extends Key<Integer> {
        private int[] values;
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
        public KeyInteger setComboBoxValues(int... values) {
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
        public int[] getComboBoxValues() {
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

        /**
         * @return true if either min or max is set
         */
        public boolean isMinOrMaxSet() {
            return max != Integer.MAX_VALUE || min != Integer.MIN_VALUE;
        }
    }

    /**
     * A bits attribute.
     * Stores additional combo box values
     */
    public static final class KeyBits extends KeyInteger {

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
            setComboBoxValues(1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 32);
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
        private final boolean toString;

        /**
         * Creates a new emum key
         *
         * @param key    the key
         * @param def    the default value
         * @param values the possible values
         */
        public KeyEnum(String key, E def, E[] values) {
            this(key, def, values, false);
        }

        /**
         * Creates a new emum key
         *
         * @param key      the key
         * @param def      the default value
         * @param values   the possible values
         * @param toString if true, the names are not taken from the language file but created by calling toString()
         */
        public KeyEnum(String key, E def, E[] values, boolean toString) {
            super(key, def);
            this.values = values;
            this.toString = toString;

            names = new String[values.length];
            if (toString)
                for (int i = 0; i < values.length; i++)
                    names[i] = values[i].toString();
            else
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

        /**
         * @return true if this enum key uses toString to create the display names
         */
        public boolean usesToString() {
            return toString;
        }
    }

    /**
     * A special string key to flag long multi line strings.
     */
    public static final class LongString extends Key<String> {
        private int rows = 6;
        private int columns = 0;
        private boolean lineNumbers = false;

        /**
         * Creates a new Key
         *
         * @param key the key
         */
        public LongString(String key) {
            super(key, "");
        }

        /**
         * Creates a new Key
         *
         * @param key the key
         * @param def the default value
         */
        public LongString(String key, String def) {
            super(key, def);
        }

        /**
         * @return the rows of the editor field
         */
        public int getRows() {
            return rows;
        }

        /**
         * Sets the rows in the editor
         *
         * @param rows the number ow rows
         * @return this for chained calls
         */
        public LongString setRows(int rows) {
            this.rows = rows;
            return this;
        }

        /**
         * @return the coloums of the editor field
         */
        public int getColumns() {
            return columns;
        }


        /**
         * Sets the columns in the editor
         *
         * @param columns the number ow rows
         * @return this for chained calls
         */
        public LongString setColumns(int columns) {
            this.columns = columns;
            return this;
        }

        /**
         * Sets the line numbers attribute
         *
         * @param lineNumbers true if line numbers should be visibla
         * @return this for chained calls
         */
        public LongString setLineNumbers(boolean lineNumbers) {
            this.lineNumbers = lineNumbers;
            return this;
        }

        /**
         * @return true if line numbers are visible
         */
        public boolean getLineNumbers() {
            return lineNumbers;
        }
    }

    /**
     * Interface to define a dependancy of a key from an other key
     *
     * @param <T> the type of the key
     */
    public interface CheckEnabled<T> {
        /**
         * Returns true if editor is enabled
         *
         * @param t the value the editor depends on
         * @return true if editor is enabled
         */
        boolean isEnabled(T t);
    }

    /**
     * Used to provide a default value if the value is mutable.
     *
     * @param <VALUE> the type of the value
     */
    public interface DefaultFactory<VALUE> {
        /**
         * Called to create a new default value.
         *
         * @return the default value
         */
        VALUE createDefault();
    }
}

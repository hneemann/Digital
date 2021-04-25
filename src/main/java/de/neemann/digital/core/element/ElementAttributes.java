/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.element;

import de.neemann.digital.FileLocator;
import de.neemann.digital.core.ValueFormatter;
import de.neemann.digital.hdl.hgs.HGSMap;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Describes one concrete Part.
 * Its a Key value list, which is used to store the different elements attributes.
 */
public class ElementAttributes implements HGSMap {
    private static final String PROGRAM_MEMORY = "PROGRAM MEMORY";

    private HashMap<String, Object> attributes;
    private transient ArrayList<AttributeListener> listeners;
    private transient HashMap<String, Object> cache;

    /**
     * Creates a new instance
     */
    public ElementAttributes() {
    }

    /**
     * Creates a deep copy of the given attributes
     *
     * @param proto the ElementAttributes to copy
     */
    public ElementAttributes(ElementAttributes proto) {
        if (proto.attributes != null) {
            attributes = new HashMap<>();
            for (Map.Entry<String, Object> e : proto.attributes.entrySet()) {
                attributes.put(e.getKey(), e.getValue());
            }
        }
    }

    /**
     * Gets a value from the map.
     * If the value is not present the default value is returned
     *
     * @param key     the key
     * @param <VALUE> the type of the value
     * @return the value
     */
    @SuppressWarnings("unchecked")
    public <VALUE> VALUE get(Key<VALUE> key) {
        if (attributes == null)
            return key.getDefault();
        else {
            VALUE value = (VALUE) attributes.get(key.getKey());
            if (value == null)
                return key.getDefault();

            // needed to fix files with int constants!
            if ((key == Keys.VALUE || key == Keys.DEFAULT) && value instanceof Integer) {
                value = (VALUE) Long.valueOf(((Integer) value).longValue());
                attributes.put(key.getKey(), value);
            }

            // needed to fix files with int pin numbers!
            if (key == Keys.PINNUMBER && value instanceof Integer) {
                value = (VALUE) value.toString();
                attributes.put(key.getKey(), value);
            }

            // needed to fix files with int pin numbers!
            if (key == Keys.LAST_DATA_FILE && value instanceof String) {
                value = (VALUE) new File(value.toString());
                attributes.put(key.getKey(), value);
            }

            return value;
        }
    }

    /**
     * Checks if a value is present.
     *
     * @param key     the key
     * @param <VALUE> the type of the value
     * @return true if value is present
     */
    public <VALUE> boolean contains(Key<VALUE> key) {
        if (attributes == null)
            return false;
        else
            return attributes.containsKey(key.getKey());
    }

    /**
     * Sets a value
     *
     * @param key     the key
     * @param value   the value
     * @param <VALUE> the type of the value
     * @return this to chain calls
     */
    public <VALUE> ElementAttributes set(Key<VALUE> key, VALUE value) {
        if (value != get(key)) {
            if (value.equals(key.getDefault())) {
                if (attributes != null) {
                    attributes.remove(key.getKey());
                    if (attributes.isEmpty())
                        attributes = null;
                }
            } else {
                if (attributes == null)
                    attributes = new HashMap<>();
                attributes.put(key.getKey(), value);
            }
            fireValueChanged();
        }
        return this;
    }

    private void fireValueChanged() {
        if (listeners != null)
            for (AttributeListener l : listeners)
                l.attributeChanged();
    }

    /**
     * Adds a listener to this class
     *
     * @param listener the listener
     */
    public void addListener(AttributeListener listener) {
        if (listeners == null)
            listeners = new ArrayList<>();
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    /**
     * removes a listener to this class
     *
     * @param listener the listener
     */
    public void removeListener(AttributeListener listener) {
        if (listeners != null)
            listeners.remove(listener);
    }

    /**
     * Returns the bits count stored in this attributes.
     * Its a short hand for get(AttributeKey.BITS)
     *
     * @return the number of bits
     */
    public int getBits() {
        return get(Keys.BITS);
    }

    /**
     * Returns the label stored in this attributes.
     * Its a short hand for get(AttributeKey.LABEL)
     *
     * @return the label
     */
    public String getLabel() {
        return get(Keys.LABEL);
    }

    /**
     * @return true if this is flagged as program memory.
     */
    public boolean isProgramMemory() {
        return getLabel().contains(PROGRAM_MEMORY) || get(Keys.IS_PROGRAM_MEMORY);
    }

    /**
     * @return the int number format
     */
    public ValueFormatter getValueFormatter() {
        return get(Keys.INT_FORMAT).createFormatter(this);
    }

    /**
     * Sets the bit count to this map.
     * Shorthand for set(AttributeKey.BITS, bits);
     *
     * @param bits the number of bits
     * @return this tp chain calls
     */
    public ElementAttributes setBits(int bits) {
        set(Keys.BITS, bits);
        return this;
    }

    @Override
    public String toString() {
        return "ElementAttributes{"
                + "attributes=" + attributes
                + '}';
    }

    /**
     * @return true if map is empty
     */
    public boolean isEmpty() {
        if (attributes == null)
            return true;
        return attributes.isEmpty();
    }

    /**
     * Gets a file stored in the map
     *
     * @param key  the file key
     * @param root the projects main folder
     * @return the file
     */
    public File getFile(Key<File> key, File root) {
        File f = get(key);
        if (root != null)
            f = new FileLocator(f).setLibraryRoot(root).locate();
        return f;
    }

    /**
     * Gets a file stored directly in the map.
     * Intended to be used as a convenient function in the gui context.
     * Not intended to be used in the model creation context.
     *
     * @param fileKey the file key
     * @return the file
     */
    public File getFile(String fileKey) {
        if (attributes != null) {
            Object f = attributes.get(fileKey);
            if (f != null)
                return new File(f.toString().trim());
        }
        return null;
    }

    /**
     * Stores a file directly in the map
     *
     * @param fileKey the key
     * @param file    the file
     */
    public void setFile(String fileKey, File file) {
        if (file == null) {
            if (attributes != null)
                attributes.remove(fileKey);
        } else if (!file.equals(getFile(fileKey))) {
            if (attributes == null)
                attributes = new HashMap<>();
            attributes.put(fileKey, file.getPath());
            fireValueChanged();
        }
    }

    /**
     * Apply the given attributes to this set
     *
     * @param elementAttributes the attributes to copy, maybe null
     */
    public void getValuesFrom(ElementAttributes elementAttributes) {
        if (elementAttributes == null)
            return;

        if (attributes != null)
            attributes.clear();
        else
            attributes = new HashMap<>();

        if (elementAttributes.attributes != null)
            attributes.putAll(elementAttributes.attributes);

        if (attributes.isEmpty())
            attributes = null;
        fireValueChanged();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ElementAttributes that = (ElementAttributes) o;

        return attributes != null ? attributes.equals(that.attributes) : that.attributes == null;
    }

    @Override
    public int hashCode() {
        return attributes != null ? attributes.hashCode() : 0;
    }

    /**
     * Checks if the values in both attributes are equal
     *
     * @param key     the key
     * @param other   the other attribute set
     * @param <VALUE> the type og the value
     * @return true if both values are equal
     */
    public <VALUE> boolean equalsKey(Key<VALUE> key, ElementAttributes other) {
        return get(key).equals(other.get(key));
    }

    /**
     * @return an integer pin number
     */
    public int getIntPinNumber() {
        String pin = get(Keys.PINNUMBER);
        try {
            return Integer.parseInt(pin);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    @Override
    public Object hgsMapGet(String key) {
        Key k = Keys.getKeyByName(key);
        if (k == null) {
            if (attributes == null)
                return null;
            else
                return attributes.get(key);
        } else
            return get(k);
    }

    /**
     * Adds a value to the instance specific cache
     *
     * @param key   key
     * @param value value
     */
    public void putToCache(String key, Object value) {
        if (cache == null)
            cache = new HashMap<>();
        cache.put(key, value);
    }

    /**
     * Requests a value from the cache
     *
     * @param key the key
     * @return the value
     */
    public Object getFromCache(String key) {
        if (cache == null)
            return null;
        return cache.get(key);
    }

    /**
     * Removes an entry from the cache.
     *
     * @param key the key to remove
     * @return the previous value associated with key, or null if there was no mapping for key.
     */
    public Object removeFromCache(String key) {
        if (cache == null)
            return null;
        return cache.remove(key);
    }

}

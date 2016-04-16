package de.neemann.digital.core.element;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Describes one concrete Part.
 * Its a Key value list, which is used to store the diferent elements attributes.
 *
 * @author hneemann
 */
public class ElementAttributes {
    private HashMap<String, Object> attributes;
    private transient ArrayList<AttributeListener> listeners;

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
            fireValueChanged(key);
        }
        return this;
    }

    private void fireValueChanged(Key key) {
        if (listeners != null)
            for (AttributeListener l : listeners)
                l.attributeChanged(key);
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
     * Its a short hand for get(AttributeKey.Bits)
     *
     * @return the number of bits
     */
    public int getBits() {
        return get(Keys.Bits);
    }

    /**
     * Returns the label stored in this attributes.
     * Its a short hand for get(AttributeKey.Label)
     *
     * @return the label
     */
    public String getLabel() {
        return get(Keys.Label);
    }

    /**
     * Sets the bit count to this map.
     * Shorthand for set(AttributeKey.Bits, bits);
     *
     * @param bits the number of bits
     * @return this tp chain calls
     */
    public ElementAttributes setBits(int bits) {
        set(Keys.Bits, bits);
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
     * Gets a file stored directly in the map
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
        if (attributes == null)
            attributes = new HashMap<>();
        attributes.put(fileKey, file.getPath());
    }

}

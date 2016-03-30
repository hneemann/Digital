package de.neemann.digital.core.element;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Describes one concrete Part.
 *
 * @author hneemann
 */
public class ElementAttributes {
    private HashMap<String, Object> attributes;
    private transient ArrayList<AttributeListener> listeners;

    public ElementAttributes() {
    }

    public ElementAttributes(ElementAttributes proto) {
        if (proto.attributes != null) {
            attributes = new HashMap<>();
            for (Map.Entry<String, Object> e : proto.attributes.entrySet()) {
                attributes.put(e.getKey(), e.getValue());
            }
        }
    }

    public <VALUE> VALUE get(AttributeKey<VALUE> key) {
        if (attributes == null)
            return key.getDefault();
        else {
            VALUE value = (VALUE) attributes.get(key.getKey());
            if (value == null)
                return key.getDefault();
            return value;
        }
    }

    public <VALUE> boolean contains(AttributeKey<VALUE> key) {
        if (attributes == null)
            return false;
        else
            return attributes.containsKey(key.getKey());
    }

    public <VALUE> ElementAttributes set(AttributeKey<VALUE> key, VALUE value) {
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

    private void fireValueChanged(AttributeKey key) {
        if (listeners != null)
            for (AttributeListener l : listeners)
                l.attributeChanged(key);
    }

    public void addListener(AttributeListener listener) {
        if (listeners == null)
            listeners = new ArrayList<>();
        if (!listeners.contains(listener))
            listeners.add(listener);
    }

    public void removeListener(AttributeListener listener) {
        if (listeners != null)
            listeners.remove(listener);
    }

    public int getBits() {
        return get(AttributeKey.Bits);
    }

    public ElementAttributes setBits(int bits) {
        set(AttributeKey.Bits, bits);
        return this;
    }

    @Override
    public String toString() {
        return "ElementAttributes{" +
                "attributes=" + attributes +
                '}';
    }

    public boolean isEmpty() {
        if (attributes == null)
            return true;
        return attributes.isEmpty();
    }

    public File getFile(String fileKey) {
        if (attributes != null) {
            Object f = attributes.get(fileKey);
            if (f != null)
                return new File(f.toString());
        }
        return null;
    }

    public void setFile(String fileKey, File file) {
        if (attributes == null)
            attributes = new HashMap<>();
        attributes.put(fileKey, file.getPath());
    }
}

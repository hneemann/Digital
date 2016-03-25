package de.neemann.digital.core.element;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Describes one concrete Part.
 *
 * @author hneemann
 */
public class ElementAttributes {
    private HashMap<AttributeKey, Object> attributes;
    private transient ArrayList<AttributeListener> listeners;

    public ElementAttributes() {
    }

    public ElementAttributes(ElementAttributes proto) {
        if (proto.attributes != null) {
            attributes = new HashMap<>();
            for (Map.Entry<AttributeKey, Object> e : proto.attributes.entrySet()) {
                attributes.put(e.getKey(), e.getValue());
            }
        }
    }

    public <VALUE> VALUE get(AttributeKey<VALUE> key) {
        if (attributes == null)
            return key.getDefault();
        else {
            VALUE value = (VALUE) attributes.get(key);
            if (value == null)
                return key.getDefault();
            return value;
        }
    }

    public <VALUE> ElementAttributes set(AttributeKey<VALUE> key, VALUE value) {
        if (value != get(key)) {
            if (value.equals(key.getDefault())) {
                if (attributes != null)
                    attributes.remove(key);
            } else {
                if (attributes == null)
                    attributes = new HashMap<>();
                attributes.put(key, value);
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
}

package de.neemann.digital.core.part;

import java.util.HashMap;

/**
 * Describes one concrete Part.
 *
 * @author hneemann
 */
public class PartAttributes {
    private HashMap<AttributeKey, Object> attributes;

    public PartAttributes() {
        attributes = new HashMap<>();
    }

    public <VALUE> VALUE get(AttributeKey<VALUE> key) {
        VALUE value = (VALUE) attributes.get(key);
        if (value == null)
            return key.getDefault();
        return value;
    }

    public <VALUE> void set(AttributeKey<VALUE> key, VALUE value) {
        if (value.equals(key.getDefault()))
            attributes.remove(key);
        else
            attributes.put(key, value);
    }

    public PartAttributes bits(int bits) {
        set(AttributeKey.Bits, bits);
        return this;
    }
}

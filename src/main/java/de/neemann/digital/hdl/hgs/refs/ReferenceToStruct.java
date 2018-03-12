/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.refs;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.hdl.hgs.Context;
import de.neemann.digital.hdl.hgs.EvalException;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles the access to a struct
 */
public class ReferenceToStruct implements Reference {
    private final Reference parent;
    private final String name;
    private static final HashMap<String, Key> KEY_MAP = new HashMap<>();

    static {
        for (Field k : Keys.class.getDeclaredFields()) {
            if (Modifier.isStatic(k.getModifiers()) && Key.class.isAssignableFrom(k.getType())) {
                try {
                    Key key = (Key) k.get(null);
                    KEY_MAP.put(key.getKey(), key);
                } catch (IllegalAccessException e) {
                    throw new RuntimeException("error ecessing the Keys");
                }
            }
        }
    }

    /**
     * Creates a new struct access
     *
     * @param parent the parent
     * @param name   the field name
     */
    public ReferenceToStruct(Reference parent, String name) {
        this.parent = parent;
        this.name = name;
    }

    @Override
    public void set(Context context, Object value) throws EvalException {
        Object m = parent.get(context);
        if (m instanceof Map)
            ((Map) m).put(name, value);
        else
            throw new EvalException("not a map");
    }

    @Override
    public Object get(Context context) throws EvalException {
        Object m = parent.get(context);
        if (m instanceof Map)
            return ((Map) m).get(name);
        if (m instanceof ElementAttributes) {
            Key key = KEY_MAP.get(name);
            if (key == null)
                throw new EvalException("invallid key: " + name);
            return ((ElementAttributes) m).get(key);
        } else
            throw new EvalException("not a map");
    }


}

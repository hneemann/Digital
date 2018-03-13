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

import java.util.Map;

/**
 * Handles the access to a struct
 */
public class ReferenceToStruct implements Reference {
    private final Reference parent;
    private final String name;

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
            throw new EvalException("not a map: " + m);
    }

    @Override
    public Object get(Context context) throws EvalException {
        Object m = parent.get(context);
        if (m instanceof Map)
            return ((Map) m).get(name);
        else if (m instanceof ElementAttributes) {
            Key key = Keys.getKeyByName(name);
            if (key == null)
                throw new EvalException("invalid key: " + name);
            return ((ElementAttributes) m).get(key);
        } else
            throw new EvalException("not a map: " + m);
    }


}

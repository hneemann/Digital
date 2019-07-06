/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.refs;

import de.neemann.digital.hdl.hgs.Context;
import de.neemann.digital.hdl.hgs.HGSEvalException;
import de.neemann.digital.hdl.hgs.HGSMap;
import de.neemann.digital.hdl.hgs.Value;

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
    public void declareVar(Context context, Object initial) throws HGSEvalException {
        final HGSMap hgsMap = Value.toMap(parent.get(context));
        if (hgsMap.hgsMapGet(name) != null)
            throw new HGSEvalException("Value '" + name + "' redeclared in struct!");

        hgsMap.hgsMapPut(name, initial);
    }

    @Override
    public void set(Context context, Object value) throws HGSEvalException {
        final HGSMap hgsMap = Value.toMap(parent.get(context));
        if (hgsMap.hgsMapGet(name) == null)
            throw new HGSEvalException("Value '" + name + "' not declared in struct!");
        hgsMap.hgsMapPut(name, value);
    }

    @Override
    public Object get(Context context) throws HGSEvalException {
        final Object value = Value.toMap(parent.get(context)).hgsMapGet(name);
        if (value == null)
            throw new HGSEvalException("Value '" + name + "' is not available in the struct!");
        return value;
    }

    /**
     * @return  the parent reference
     */
    public Reference getParent() {
        return parent;
    }

    /**
     * @return the struct field name
     */
    public String getName() {
        return name;
    }
}

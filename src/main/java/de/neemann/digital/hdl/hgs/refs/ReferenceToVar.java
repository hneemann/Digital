/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.refs;

import de.neemann.digital.hdl.hgs.Context;
import de.neemann.digital.hdl.hgs.EvalException;

/**
 * Handles the access to a simple variable
 */
public class ReferenceToVar implements Reference {
    private final String name;

    /**
     * Creates a new variable access
     *
     * @param name the name of the variable
     */
    public ReferenceToVar(String name) {
        this.name = name;
    }

    @Override
    public void set(Context context, Object value) {
        context.setVar(name, value);
    }

    @Override
    public Object get(Context context) throws EvalException {
        return context.getVar(name);
    }
}

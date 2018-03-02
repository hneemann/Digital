/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression;

import de.neemann.digital.lang.Lang;

import java.util.HashMap;

/**
 * Simple {@link Context} based on a HashMap
 */
public class ContextMap implements Context {
    private HashMap<Variable, Boolean> map;

    /**
     * Creates a new instance
     */
    public ContextMap() {
        map = new HashMap<>();
    }

    @Override
    public boolean get(Variable variable) throws ExpressionException {
        Boolean aBoolean = map.get(variable);
        if (aBoolean == null)
            throw new ExpressionException(Lang.get("err_varNotDefined_N", variable));
        return aBoolean;
    }

    /**
     * Sets a value
     *
     * @param v the variable
     * @param b the variables value
     * @return this for call chaining
     */
    public ContextMap set(Variable v, boolean b) {
        map.put(v, b);
        return this;
    }
}

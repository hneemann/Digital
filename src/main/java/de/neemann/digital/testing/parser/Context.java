/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser;

import de.neemann.digital.lang.Lang;

import java.util.HashMap;

/**
 * The context of the calculations.
 */
public class Context {
    private final Context parent;
    private HashMap<String, Long> map;

    /**
     * Creates an empty context
     */
    public Context() {
        this(null);
    }

    /**
     * Creates an empty context
     *
     * @param parent the parents context
     */
    public Context(Context parent) {
        this.parent = parent;
    }

    /**
     * Returns the value of a variable
     *
     * @param name the name of the variable
     * @return the value
     * @throws ParserException if the variable does not exist
     */
    public long getVar(String name) throws ParserException {
        if (map == null || !map.containsKey(name)) {
            if (parent == null)
                throw new ParserException(Lang.get("err_variable_N0_notFound", name));
            return parent.getVar(name);
        } else
            return map.get(name);
    }

    /**
     * Sets a variable in this context
     *
     * @param varName the variables name
     * @param value   the value
     * @return this for chained calls
     */
    public Context setVar(String varName, long value) {
        if (map == null)
            map = new HashMap<>();
        map.put(varName, value);
        return this;
    }
}

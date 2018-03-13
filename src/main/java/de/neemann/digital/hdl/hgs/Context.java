/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

import java.util.HashMap;

/**
 * The evaluation context
 */
public class Context {
    private final Context parent;
    private final StringBuilder code;
    private HashMap<String, Object> map;
    private int recordStart = 0;

    /**
     * Creates a new context
     */
    public Context() {
        this(null, new StringBuilder());
    }

    /**
     * Creates a new context
     *
     * @param parent the parent context
     */
    public Context(Context parent) {
        this(parent, null);
    }

    /**
     * Creates a new context
     *
     * @param parent the parent context
     * @param code   the code
     */
    public Context(Context parent, StringBuilder code) {
        this.parent = parent;
        this.code = code;
        map = new HashMap<>();
    }

    /**
     * Returns true if this context contains a mapping for the specified key.
     *
     * @param name the key
     * @return true if value is present
     */
    public boolean contains(String name) {
        if (map.containsKey(name))
            return true;
        else {
            if (parent != null)
                return parent.contains(name);
            else
                return false;
        }
    }

    /**
     * Get a variable
     *
     * @param name the name
     * @return the value
     * @throws EvalException EvalException
     */
    public Object getVar(String name) throws EvalException {
        Object v = map.get(name);
        if (v == null) {

            if (name.equals("output"))
                return code.toString();

            if (parent == null)
                throw new EvalException("variable not found: " + name);
            else
                return parent.getVar(name);
        } else
            return v;
    }

    /**
     * Set a variable
     *
     * @param name name
     * @param val  value
     * @return this for chained calls
     */
    public Context setVar(String name, Object val) {
        map.put(name, val);
        return this;
    }

    /**
     * Prints code to the context
     *
     * @param str the string to print
     * @return this for chained calls
     */
    public Context print(String str) {
        if (code != null)
            code.append(str);
        else {
            parent.print(str);
        }
        return this;
    }

    @Override
    public String toString() {
        if (code != null)
            return code.toString();
        else
            return map.toString();
    }

    /**
     * @return the output length
     */
    public int length() {
        return code.length();
    }

}

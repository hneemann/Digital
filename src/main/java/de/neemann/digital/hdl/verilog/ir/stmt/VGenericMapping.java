/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir.stmt;

/**
 *
 * @author ideras
 */
public class VGenericMapping {
    private final String name;
    private final String value;

    /**
     * Initialize a new instance
     *
     * @param name the name
     * @param value the value
     */
    public VGenericMapping(String name, String value) {
        this.name = name;
        this.value = value;
    }

    /**
     * Returns the name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the value
     *
     * @return the value
     */
    public String getValue() {
        return value;
    }
}

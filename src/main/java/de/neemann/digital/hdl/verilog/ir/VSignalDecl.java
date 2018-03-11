/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir;

import de.neemann.digital.hdl.model.Signal;

/**
 * Represents a verilog signal.
 *
 * @author ideras
 */
public class VSignalDecl {
    private String name;
    private final int bits;
    private final Type type;

    /**
     * The signal type
     */
    public enum Type {
        /**
         * Wire
         */
        WIRE,
        /**
         * Reg
         */
        REG
    };

    /**
     * Initialize a new verilog signal declaration
     *
     * @param name the name
     * @param bits the number of bits
     * @param type the type of signal
     */
    public VSignalDecl(String name, int bits, Type type) {
        this.name = name;
        this.bits = bits;
        this.type = type;
    }

    /**
     * Initialize a new wire type signal declaration
     *
     * @param name the name
     * @param bits the number of bits
     */
    public VSignalDecl(String name, int bits) {
        this(name, bits, Type.WIRE);
    }

    /**
     * Initialize a new wire type signal declaration
     *
     * @param s the signal
     */
    public VSignalDecl(Signal s) {
        this(s, Type.WIRE);
    }

    /**
     * Initialize a new signal declaration of the type specified
     *
     * @param s the signal
     * @param type the type
     */
    public VSignalDecl(Signal s, Type type) {
        this(s.getName(), s.getBits(), type);
    }

    /**
     * Returns the signal type
     *
     * @return the signal type
     */
    public Type getType() {
        return type;
    }

    /**
     * Return the name of the signal
     *
     * @return the name of the signal
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the place name
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the number of bits
     *
     * @return the number of bits
     */
    public int getBits() {
        return bits;
    }
}

/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.rt;

/**
 *
 * @author ideras
 */
public class StringValue extends RtValue {
    private final String value;

    /**
     * Creates a new instance.
     *
     * @param value the string value.
     */
    public StringValue(String value) {
        this.value = value;
    }

    /**
     * Return the string value associated.
     *
     * @return the string value.
     */
    public String getValue() {
        return value;
    }

    @Override
    public Type getType() {
        return Type.STRING;
    }

    @Override
    public String toString() {
        return "\"" + value + "\"";
    }


}

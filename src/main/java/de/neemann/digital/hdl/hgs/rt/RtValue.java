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
public abstract class RtValue {
    /**
     * A value type
     */
    public enum Type {
        /**
         * integer type
         */
        INT,
        /**
         * string type
         */
        STRING,
        /**
         * struct type
         */
        STRUCT,
        /**
         * array type
         */
        ARRAY,
        /**
         * a type for a null value
         */
        NOTHING
    };

    /**
     * Returns the type of the value.
     *
     * @return the type
     */
    public abstract Type getType();

    /**
     * Determines is this value is an integer.
     *
     * @return true is the value is integer, false otherwise.
     */
    public boolean isInt() {
        return (getType() == Type.INT);
    }

        /**
     * Determines is this value is string.
     *
     * @return true is the value is string, false otherwise.
     */
    public boolean isString() {
        return (getType() == Type.STRING);
    }

    /**
     * Determines is this value is an array.
     *
     * @return true is the value is an array, false otherwise.
     */
    public boolean isArray() {
        return (getType() == Type.ARRAY);
    }

    /**
     * Determines is this value is a struct.
     *
     * @return true is the value is a struct, false otherwise.
     */
    public boolean isStruct() {
        return (getType() == Type.STRUCT);
    }
}

/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

/**
 * Simple expression
 * Created by Helmut.Neemann on 02.12.2016.
 */
public interface Expression {
    /**
     * calculates the value
     *
     * @param c the context of the calculation
     * @return the long value result
     * @throws EvalException EvalException
     */
    Object value(Context c) throws EvalException;

    /**
     * Converts the given value to a long
     *
     * @param value the value to convert
     * @return the long
     * @throws EvalException EvalException
     */
    static long toLong(Object value) throws EvalException {
        if (value instanceof Number)
            return ((Number) value).longValue();
        throw new EvalException("not a number: " + value.toString());
    }

    /**
     * Converts the given value to an int
     *
     * @param value the value to convert
     * @return the int value
     * @throws EvalException EvalException
     */
    static int toInt(Object value) throws EvalException {
        if (value instanceof Number)
            return ((Number) value).intValue();
        throw new EvalException("not a number: " + value.toString());
    }

    /**
     * Converts the given value to a bool
     *
     * @param value the value to convert
     * @return the bool value
     * @throws EvalException EvalException
     */
    static boolean toBool(Object value) throws EvalException {
        if (value instanceof Number)
            return ((Number) value).longValue() != 0;
        if (value instanceof Boolean)
            return ((Boolean) value);
        throw new EvalException("must be an integer or a bool, is: " + value.getClass().getSimpleName());
    }

    /**
     * Converts the given value to a string
     *
     * @param value the value to convert
     * @return the string
     * @throws EvalException EvalException
     */
    static String toString(Object value) throws EvalException {
        if (value instanceof String)
            return value.toString();
        throw new EvalException("must be a string, is a " + value.getClass().getSimpleName());
    }

    /**
     * Adds two values
     *
     * @param a a value
     * @param b a value
     * @return the sum
     * @throws EvalException EvalException
     */
    static Object add(Object a, Object b) throws EvalException {
        if (a instanceof Number && b instanceof Number)
            return ((Number) a).longValue() + ((Number) b).longValue();
        if (a instanceof String || b instanceof String)
            return a.toString() + b.toString();
        throw new EvalException("arguments must be int or string, not " + a.getClass().getSimpleName() + "+" + b.getClass().getSimpleName());
    }

    /**
     * Performs an or operation
     *
     * @param a a value
     * @param b a value
     * @return the or'ed values
     * @throws EvalException EvalException
     */
    static Object or(Object a, Object b) throws EvalException {
        if (a instanceof Number && b instanceof Number)
            return ((Number) a).longValue() | ((Number) b).longValue();
        if (a instanceof Boolean && b instanceof Boolean)
            return ((Boolean) a) || ((Boolean) b);
        throw new EvalException("arguments must be int or bool, not " + a.getClass().getSimpleName() + "+" + b.getClass().getSimpleName());
    }

    /**
     * Performs an xor operation
     *
     * @param a a value
     * @param b a value
     * @return the xor'ed values
     * @throws EvalException EvalException
     */
    static Object xor(Object a, Object b) throws EvalException {
        if (a instanceof Number && b instanceof Number)
            return ((Number) a).longValue() ^ ((Number) b).longValue();
        if (a instanceof Boolean && b instanceof Boolean)
            return !a.equals(b);
        throw new EvalException("arguments must be int or bool, not " + a.getClass().getSimpleName() + "+" + b.getClass().getSimpleName());
    }

    /**
     * Performs an and operation
     *
     * @param a a value
     * @param b a value
     * @return the and'ed values
     * @throws EvalException EvalException
     */
    static Object and(Object a, Object b) throws EvalException {
        if (a instanceof Number && b instanceof Number)
            return ((Number) a).longValue() & ((Number) b).longValue();
        if (a instanceof Boolean && b instanceof Boolean)
            return ((Boolean) a) && ((Boolean) b);
        throw new EvalException("arguments must be int or bool, not " + a.getClass().getSimpleName() + "+" + b.getClass().getSimpleName());
    }

    /**
     * Performs a not operation
     *
     * @param value a value
     * @return the inverted value
     * @throws EvalException EvalException
     */
    static Object not(Object value) throws EvalException {
        if (value instanceof Number)
            return ~((Number) value).longValue();
        if (value instanceof Boolean)
            return !((Boolean) value);
        throw new EvalException("argument must be int or bool, is " + value.getClass().getSimpleName());
    }

}

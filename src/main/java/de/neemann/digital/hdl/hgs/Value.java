/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

import java.util.List;
import java.util.Map;

import static de.neemann.digital.hdl.hgs.Tokenizer.isWhiteSpace;

/**
 * Helpers for values
 */
public final class Value {

    private Value() {
    }

    /**
     * Converts the given value to a long
     *
     * @param value the value to convert
     * @return the long
     * @throws HGSEvalException HGSEvalException
     */
    public static long toLong(Object value) throws HGSEvalException {
        if (value instanceof Number)
            return ((Number) value).longValue();
        throw new HGSEvalException("not a number: " + value.toString());
    }

    /**
     * Converts the given value to a double
     *
     * @param value the value to convert
     * @return the long
     * @throws HGSEvalException HGSEvalException
     */
    public static double toDouble(Object value) throws HGSEvalException {
        if (value instanceof Number)
            return ((Number) value).doubleValue();
        throw new HGSEvalException("not a number: " + value.toString());
    }

    /**
     * Converts the given value to an int
     *
     * @param value the value to convert
     * @return the int value
     * @throws HGSEvalException HGSEvalException
     */
    public static int toInt(Object value) throws HGSEvalException {
        if (value instanceof Number)
            return ((Number) value).intValue();
        throw new HGSEvalException("not a number: " + value.toString());
    }

    /**
     * Converts the given value to a bool
     *
     * @param value the value to convert
     * @return the bool value
     * @throws HGSEvalException HGSEvalException
     */
    public static boolean toBool(Object value) throws HGSEvalException {
        if (value instanceof Number)
            return ((Number) value).longValue() != 0;
        if (value instanceof Boolean)
            return ((Boolean) value);
        throw new HGSEvalException("Must be an integer or a bool, is: " + value.getClass().getSimpleName() + "=" + value);
    }

    /**
     * Converts the given value to a string
     *
     * @param value the value to convert
     * @return the string
     * @throws HGSEvalException HGSEvalException
     */
    public static String toString(Object value) throws HGSEvalException {
        if (value instanceof String)
            return value.toString();
        throw new HGSEvalException("must be a string, is a " + value.getClass().getSimpleName());
    }

    /**
     * Converts the given value to an array
     *
     * @param value the value to convert
     * @return the function
     * @throws HGSEvalException HGSEvalException
     */
    public static HGSArray toArray(Object value) throws HGSEvalException {
        if (value instanceof HGSArray)
            return (HGSArray) value;
        if (value instanceof List)
            return new HGSArrayList((List) value);
        throw new HGSEvalException("must be an array, is a " + value.getClass().getSimpleName());
    }

    /**
     * Converts the given value to a map
     *
     * @param value the value to convert
     * @return the function
     * @throws HGSEvalException HGSEvalException
     */
    public static HGSMap toMap(Object value) throws HGSEvalException {
        if (value instanceof HGSMap)
            return (HGSMap) value;
        if (value instanceof Map)
            return new HGSMapMap((Map) value);
        throw new HGSEvalException("must be a map, is a " + value.getClass().getSimpleName());
    }

    /**
     * Compares two values
     *
     * @param a a value
     * @param b a value
     * @return true if both values are equal
     */
    public static boolean equals(Object a, Object b) {
        if (a instanceof Double || b instanceof Double)
            return a.equals(b);
        else if (a instanceof Number && b instanceof Number)
            return ((Number) a).longValue() == ((Number) b).longValue();
        else if (a instanceof String || b instanceof String)
            return a.toString().equals(b.toString());
        else
            return a.equals(b);
    }

    /**
     * Adds two values
     *
     * @param a a value
     * @param b a value
     * @return the sum
     * @throws HGSEvalException HGSEvalException
     */
    public static Object add(Object a, Object b) throws HGSEvalException {
        if (a instanceof Double || b instanceof Double)
            return toDouble(a) + toDouble(b);
        if (a instanceof Number && b instanceof Number)
            return ((Number) a).longValue() + ((Number) b).longValue();
        if (a instanceof String || b instanceof String)
            return a.toString() + b.toString();
        throw new HGSEvalException("arguments must be int or string, not " + a.getClass().getSimpleName() + "+" + b.getClass().getSimpleName());
    }

    /**
     * Subtracts two values
     *
     * @param a a value
     * @param b a value
     * @return the sum
     * @throws HGSEvalException HGSEvalException
     */
    public static Object sub(Object a, Object b) throws HGSEvalException {
        if (a instanceof Double || b instanceof Double)
            return toDouble(a) - toDouble(b);
        if (a instanceof Number && b instanceof Number)
            return ((Number) a).longValue() - ((Number) b).longValue();
        throw new HGSEvalException("arguments must be int or double, not " + a.getClass().getSimpleName() + "+" + b.getClass().getSimpleName());
    }

    /**
     * Multiplies two values
     *
     * @param a a value
     * @param b a value
     * @return the product
     * @throws HGSEvalException HGSEvalException
     */
    public static Object mul(Object a, Object b) throws HGSEvalException {
        if (a instanceof Double || b instanceof Double)
            return toDouble(a) * toDouble(b);
        if (a instanceof Number && b instanceof Number)
            return ((Number) a).longValue() * ((Number) b).longValue();
        throw new HGSEvalException("arguments must be int or double, not " + a.getClass().getSimpleName() + "+" + b.getClass().getSimpleName());
    }

    /**
     * Divides two numbers
     *
     * @param a a value
     * @param b a value
     * @return the quotient
     * @throws HGSEvalException HGSEvalException
     */
    public static Object div(Object a, Object b) throws HGSEvalException {
        if (a instanceof Double || b instanceof Double)
            return toDouble(a) / toDouble(b);
        if (a instanceof Number && b instanceof Number)
            return ((Number) a).longValue() / ((Number) b).longValue();
        throw new HGSEvalException("arguments must be int or double, not " + a.getClass().getSimpleName() + "+" + b.getClass().getSimpleName());
    }

    /**
     * Performs an or operation
     *
     * @param a a value
     * @param b a value
     * @return the or'ed values
     * @throws HGSEvalException HGSEvalException
     */
    public static Object or(Object a, Object b) throws HGSEvalException {
        if (a instanceof Number && b instanceof Number)
            return ((Number) a).longValue() | ((Number) b).longValue();
        return toBool(a) || toBool(b);
    }

    /**
     * Performs an xor operation
     *
     * @param a a value
     * @param b a value
     * @return the xor'ed values
     * @throws HGSEvalException HGSEvalException
     */
    public static Object xor(Object a, Object b) throws HGSEvalException {
        if (a instanceof Number && b instanceof Number)
            return ((Number) a).longValue() ^ ((Number) b).longValue();
        return toBool(a) ^ toBool(b);
    }

    /**
     * Performs an and operation
     *
     * @param a a value
     * @param b a value
     * @return the and'ed values
     * @throws HGSEvalException HGSEvalException
     */
    public static Object and(Object a, Object b) throws HGSEvalException {
        if (a instanceof Number && b instanceof Number)
            return ((Number) a).longValue() & ((Number) b).longValue();
        return toBool(a) && toBool(b);
    }

    /**
     * Performs a not operation
     *
     * @param value a value
     * @return the inverted value
     * @throws HGSEvalException HGSEvalException
     */
    public static Object not(Object value) throws HGSEvalException {
        if (value instanceof Number)
            return ~((Number) value).longValue();
        return !toBool(value);
    }

    /**
     * Changes the sign of the given value
     *
     * @param value the value
     * @return value with changed sign
     * @throws HGSEvalException HGSEvalException
     */
    public static Object neg(Object value) throws HGSEvalException {
        if (value instanceof Double)
            return -(Double) value;
        return -toLong(value);
    }

    /**
     * Helper compare two values
     *
     * @param a a value
     * @param b a value
     * @return true if a&lt;b
     * @throws HGSEvalException HGSEvalException
     */
    public static boolean less(Object a, Object b) throws HGSEvalException {
        if (a instanceof Double || b instanceof Double)
            return toDouble(a) < toDouble(b);
        if (a instanceof Number && b instanceof Number)
            return toLong(a) < toLong(b);
        if (a instanceof String && b instanceof String)
            return a.toString().compareTo(b.toString()) < 0;
        throw new HGSEvalException("arguments must be int, double or string, not " + a.getClass().getSimpleName() + "+" + b.getClass().getSimpleName());
    }

    /**
     * Helper compare two values
     *
     * @param a a value
     * @param b a value
     * @return true if a<=b
     * @throws HGSEvalException HGSEvalException
     */
    public static boolean lessEqual(Object a, Object b) throws HGSEvalException {
        if (a instanceof Double || b instanceof Double)
            return toDouble(a) <= toDouble(b);
        if (a instanceof Number && b instanceof Number)
            return toLong(a) <= toLong(b);
        if (a instanceof String && b instanceof String)
            return a.toString().compareTo(b.toString()) <= 0;
        throw new HGSEvalException("arguments must be int, double or string, not " + a.getClass().getSimpleName() + "+" + b.getClass().getSimpleName());
    }

    /**
     * Trims spaces at the right side of the string.
     *
     * @param str the string
     * @return the trimmed string
     */
    public static String trimRight(String str) {
        int initial = str.length() - 1;
        int pos = initial;
        while (pos >= 0 && isWhiteSpace(str.charAt(pos)))
            pos--;
        if (pos == initial)
            return str;
        else
            return str.substring(0, pos + 1);
    }

    /**
     * Trims spaces at the left side of the string.
     *
     * @param str the string
     * @return the trimmed string
     */
    public static String trimLeft(String str) {
        int pos = 0;
        while (pos < str.length() && isWhiteSpace(str.charAt(pos)))
            pos++;
        if (pos == 0)
            return str;
        else
            return str.substring(pos);
    }

    private static final class HGSArrayList implements HGSArray {
        private final List<Object> list;

        private HGSArrayList(List<Object> list) {
            this.list = list;
        }

        @Override
        public int hgsArraySize() {
            return list.size();
        }

        @Override
        public void hgsArrayAdd(Object initial) {
            list.add(initial);
        }

        @Override
        public void hgsArraySet(int i, Object val) {
            list.set(i, val);
        }

        @Override
        public Object hgsArrayGet(int i) {
            return list.get(i);
        }
    }

    private static final class HGSMapMap implements HGSMap {
        private final Map<String, Object> map;

        private HGSMapMap(Map<String, Object> map) {
            this.map = map;
        }

        @Override
        public void hgsMapPut(String key, Object val) {
            map.put(key, val);
        }

        @Override
        public Object hgsMapGet(String key) {
            return map.get(key);
        }
    }

}

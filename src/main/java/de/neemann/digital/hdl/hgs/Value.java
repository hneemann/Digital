/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

import java.util.List;
import java.util.Map;

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
        throw new HGSEvalException("must be an integer or a bool, is: " + value.getClass().getSimpleName());
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
     * Converts the given value to an array
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
        if (a instanceof Number && b instanceof Number)
            return ((Number) a).longValue() == ((Number) b).longValue();
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
        if (a instanceof Number && b instanceof Number)
            return ((Number) a).longValue() + ((Number) b).longValue();
        if (a instanceof String || b instanceof String)
            return a.toString() + b.toString();
        throw new HGSEvalException("arguments must be int or string, not " + a.getClass().getSimpleName() + "+" + b.getClass().getSimpleName());
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
        if (a instanceof Boolean && b instanceof Boolean)
            return ((Boolean) a) || ((Boolean) b);
        throw new HGSEvalException("arguments must be int or bool, not " + a.getClass().getSimpleName() + "+" + b.getClass().getSimpleName());
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
        if (a instanceof Boolean && b instanceof Boolean)
            return !a.equals(b);
        throw new HGSEvalException("arguments must be int or bool, not " + a.getClass().getSimpleName() + "+" + b.getClass().getSimpleName());
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
        if (a instanceof Boolean && b instanceof Boolean)
            return ((Boolean) a) && ((Boolean) b);
        throw new HGSEvalException("arguments must be int or bool, not " + a.getClass().getSimpleName() + "+" + b.getClass().getSimpleName());
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
        if (value instanceof Boolean)
            return !((Boolean) value);
        throw new HGSEvalException("argument must be int or bool, is " + value.getClass().getSimpleName());
    }

    private static final class HGSArrayList implements HGSArray {
        private final List list;

        private HGSArrayList(List list) {
            this.list = list;
        }

        @Override
        public int hgsArraySize() {
            return list.size();
        }

        @Override
        public void hgsArraySet(int i, Object val) {
            while (list.size() <= i)
                list.add(null);
            list.set(i, val);
        }

        @Override
        public Object hgsArrayGet(int i) {
            return list.get(i);
        }
    }

    private static final class HGSMapMap implements HGSMap {
        private final Map map;

        private HGSMapMap(Map map) {
            this.map = map;
        }

        @Override
        public void hgsMapPut(String key, Object val) {
            map.put(key, val);
        }

        @Override
        public Object hgsMapGet(String key) throws HGSEvalException {
            final Object val = map.get(key);
            if (val == null)
                throw new HGSEvalException("key " + key + " not found in map");
            return val;
        }
    }

}

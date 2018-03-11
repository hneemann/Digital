/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.quinemc;

/**
 */
public enum ThreeStateValue {
    /**
     * zero or false
     */
    zero,
    /**
     * one or true
     */
    one,
    /**
     * dont care
     */
    dontCare;


    /**
     * Create a value from a bool
     *
     * @param bool the bool
     * @return the created ThreeStateValue
     */
    public static ThreeStateValue value(boolean bool) {
        if (bool) {
            return one;
        } else {
            return zero;
        }
    }

    /**
     * Create a value from an int
     * 0 and 1 work as expected, any other value means "dont care"
     *
     * @param value the value
     * @return the created ThreeStateValue
     */
    public static ThreeStateValue value(int value) {
        switch (value) {
            case 0:
                return ThreeStateValue.zero;
            case 1:
                return ThreeStateValue.one;
            default:
                return ThreeStateValue.dontCare;
        }
    }

    /**
     * this value as an integer
     *
     * @return the int value, 2 mans "don't care"
     */
    public int asInt() {
        return ordinal();
    }

    /**
     * @return the inverted value; DC remains DC
     */
    public ThreeStateValue invert() {
        switch (this) {
            case zero:
                return one;
            case one:
                return zero;
            default:
                return dontCare;
        }
    }

    /**
     * @return returns the value as a bool
     */
    public boolean bool() {
        switch (this) {
            case zero:
                return false;
            case one:
                return true;
            default:
                throw new RuntimeException("don't care not allowed");
        }
    }

    @Override
    public String toString() {
        switch (this) {
            case zero:
                return "0";
            case one:
                return "1";
            default:
                return "X";
        }
    }
}

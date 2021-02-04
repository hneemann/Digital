/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

/**
 * Used to format a Value
 */
public interface ValueFormatter {
    /**
     * Formats the value.
     * Uses this method to create a string which is only shown to the user.
     * If the user is able to edit the string use {@link ValueFormatter#formatToEdit(Value)} instead.
     *
     * @param inValue the value to format
     * @return the formatted value as a string
     */
    String formatToView(Value inValue);

    /**
     * Formats the value.
     * Creates a string which can be parsed by {@link Bits#decode(String)}
     *
     * @param inValue the value to format
     * @return the formatted value as a string
     * @see Bits#decode(String)
     */
    String formatToEdit(Value inValue);

    /**
     * Return the number of characters required to format a number with the given bit width.
     *
     * @param bits the number of bits
     * @return the number of characters required
     */
    int strLen(int bits);

    /**
     * Returns true if formatter is suited to be used as a formatter for the addresses in a
     * hex editor like table view.
     *
     * @return true if formatter is suited to display addresses
     */
    boolean isSuitedForAddresses();

    /**
     * Moves the given value
     *
     * @param initial the initial value
     * @param bits    the bits used of the initial value
     * @param inc     the increment, is i between -1 and 1
     * @return the modified value
     */
    long dragValue(long initial, int bits, double inc);

    /**
     * Returns true if there should be a separator in front of the given bit.
     *
     * @param bits the number of bits in the value to format
     * @param bit  the bit in question
     * @return true if there should be a separator in front of the given bit.
     */
    default boolean isSeparatorInFrontOf(int bits, int bit) {
        return false;
    }
}

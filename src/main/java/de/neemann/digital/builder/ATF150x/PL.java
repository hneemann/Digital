/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.ATF150x;

import java.util.ArrayList;

/**
 * Used to create a pin list
 */
public class PL {

    private final ArrayList<Integer> pinList;

    /**
     * Creates a new instance
     */
    public PL() {
        pinList = new ArrayList<>();
    }

    /**
     * Adds some pins to the list
     *
     * @param p the pin numbers to add
     * @return this for chained calls
     */
    public PL p(int... p) {
        for (int j : p) pinList.add(j);
        return this;
    }

    /**
     * Excludes some pins to the list
     *
     * @param p the pin numbers to exclude
     * @return this for chained calls
     */
    public PL e(int... p) {
        for (int j : p) pinList.remove((Object) j);
        return this;
    }

    /**
     * Adds a pin interval
     *
     * @param from min pin number
     * @param to   max pin number
     * @return this for chained calls
     */
    public PL pi(int from, int to) {
        for (int i = from; i <= to; i++)
            pinList.add(i);
        return this;
    }

    /**
     * Creates an int array
     *
     * @return the array
     */
    public int[] pins() {
        int[] pins = new int[pinList.size()];
        for (int i = 0; i < pinList.size(); i++)
            pins[i] = pinList.get(i);
        return pins;
    }
}

/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import de.neemann.digital.core.Signal;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Splits the pins string of a signal
 */
public class SplitPinString {

    private static final SplitPinString EMPTY = new SplitPinString();

    /**
     * Creates a new instance
     *
     * @param s the signal
     * @return the port pins used
     */
    public static SplitPinString create(Signal s) {
        if (s.missingPinNumber())
            return EMPTY;
        return create(s.getPinNumber());
    }

    /**
     * Creates a new instance
     *
     * @param pinStr the pins definition;
     * @return the port pins used
     */
    public static SplitPinString create(String pinStr) {
        if (pinStr == null || pinStr.trim().length() == 0)
            return EMPTY;

        StringTokenizer st = new StringTokenizer(pinStr, ",;");
        ArrayList<String> pins = new ArrayList<>();
        while (st.hasMoreTokens())
            pins.add(st.nextToken().trim());

        return new PinsArray(pins);
    }


    private SplitPinString() {
    }

    /**
     * Returns the pin of a given port
     *
     * @param i the port number
     * @return the pin
     */
    public String getPin(int i) {
        return null;
    }

    private final static class PinsArray extends SplitPinString {
        private final ArrayList<String> pins;

        private PinsArray(ArrayList<String> pins) {
            this.pins = pins;
        }

        @Override
        public String getPin(int i) {
            if (i >= pins.size())
                return null;
            else
                return pins.get(i);
        }
    }
}

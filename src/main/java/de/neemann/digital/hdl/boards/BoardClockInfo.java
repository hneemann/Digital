/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.boards;

/**
 * Board clock pin information
 */
public class BoardClockInfo {
    private final String pinNumber; // The pin number
    private final double clkPeriod; // Clock period in nanoseconds

    /**
     * Initialize a new instance
     *
     * @param pinNumber The pin number
     * @param clkPeriod The clock period in nanoseconds
     */
    public BoardClockInfo(String pinNumber, double clkPeriod) {
        this.pinNumber = pinNumber;
        this.clkPeriod = clkPeriod;
    }

    /**
     * Returns the pin number
     *
     * @return the pin number
     */
    public String getPinNumber() {
        return pinNumber;
    }

    /**
     * Returns the clock period
     *
     * @return the clock period
     */
    public double getClkPeriod() {
        return clkPeriod;
    }
}

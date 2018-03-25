/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2.clock;

import de.neemann.digital.hdl.model2.HDLPort;

/**
 * A clock information bean.
 */
public class ClockInfo {
    private HDLPort clockPort;
    private int frequency;

    /**
     * Creates a new instance.
     *
     * @param clockPort the clock port
     * @param frequency the clock frequency
     */
    public ClockInfo(HDLPort clockPort, int frequency) {
        this.clockPort = clockPort;
        this.frequency = frequency;
    }

    /**
     * @return the clock port
     */
    public HDLPort getClockPort() {
        return clockPort;
    }

    /**
     * @return the clocks frequency
     */
    public int getFrequency() {
        return frequency;
    }
}

package de.neemann.digital.hdl.model;

/**
 * A clock input.
 */
public class HDLClock {
    private final Port clockPort;
    private final int frequency;

    /**
     * Creates a new instance
     *
     * @param clockPort the clock port
     * @param frequency the frequency
     */
    public HDLClock(Port clockPort, int frequency) {
        this.clockPort = clockPort;
        this.frequency = frequency;
    }

    /**
     * @return the clock frequency
     */
    public int getFrequency() {
        return frequency;
    }

    /**
     * @return the clock port
     */
    public Port getClockPort() {
        return clockPort;
    }
}

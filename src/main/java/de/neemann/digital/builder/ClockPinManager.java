package de.neemann.digital.builder;

public class ClockPinManager {
    private int clockPin;

    public int getClockPin() {
        return clockPin;
    }

    public void setClockPin(int clockPin) {
        if (clockPin > 0)
            this.clockPin = clockPin;
    }
}

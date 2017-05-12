package de.neemann.digital.core;

/**
 * A simple storage bean for signals
 */
public final class Signal implements Comparable<Signal> {
    private final String name;
    private final ObservableValue value;
    private int pinNumber;

    /**
     * Creates a new Instance
     *
     * @param name  the name of the Signal
     * @param value the signals value
     */
    public Signal(String name, ObservableValue value) {
        if (name == null) this.name = null;
        else this.name = name.trim().replace(' ', '_');
        this.value = value;
    }

    /**
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the pin number
     *
     * @param pinNumber the pin number
     * @return this for chained calls
     */
    public Signal setPinNumber(int pinNumber) {
        this.pinNumber = pinNumber;
        return this;
    }

    /**
     * Gets the number of this pin.
     *
     * @return the pin number of -1 if no pin is given
     * @throws NodeException invalid pin number
     */
    public int getPinNumber() throws NodeException {
        return pinNumber;
    }

    /**
     * @return the value
     */
    public ObservableValue getValue() {
        return value;
    }

    @Override
    public int compareTo(Signal o) {
        return name.compareTo(o.name);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Signal signal = (Signal) o;

        return name.equals(signal.name);

    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns true if this signal is a valid signal.
     * Valid means there is a name and the value is non null
     *
     * @return true if signal is valid
     */
    public boolean isValid() {
        return name != null && name.length() > 0 && value != null;
    }


}

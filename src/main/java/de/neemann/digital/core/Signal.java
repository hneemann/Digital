package de.neemann.digital.core;

/**
 * A simple storage bean for signals
 */
public final class Signal implements Comparable<Signal> {
    private final String name;
    private final ObservableValue value;
    private final Setter setter;
    private IntFormat format = IntFormat.def;
    private String pinNumber;
    private ObservableValue bidirectionalReader;

    /**
     * Creates a new Instance
     *
     * @param name  the name of the Signal
     * @param value the signals value
     */
    public Signal(String name, ObservableValue value) {
        this(name, value, null);
    }

    /**
     * Creates a new Instance
     *
     * @param name   the name of the Signal
     * @param value  the signals value
     * @param setter used to set this signal value, maybe null
     */
    public Signal(String name, ObservableValue value, Setter setter) {
        this.setter = setter;
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
    public Signal setPinNumber(String pinNumber) {
        this.pinNumber = pinNumber;
        return this;
    }

    /**
     * Sets the integer format to create a string
     *
     * @param format the format
     * @return this for chained calls
     */
    public Signal setFormat(IntFormat format) {
        if (format != null)
            this.format = format;
        return this;
    }


    /**
     * Gets the number of this pin.
     *
     * @return the pin number, or null if no pin is given
     */
    public String getPinNumber() {
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
     * @return the value in the specified format
     */
    public String getValueString() {
        return format.formatToEdit(value.getCopy());
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


    /**
     * @return true if a pin number is missing
     */
    public boolean missingPinNumber() {
        return pinNumber == null || pinNumber.trim().length() == 0;
    }

    /**
     * @return the setter for this value
     */
    public Setter getSetter() {
        return setter;
    }

    /**
     * If a signal is bidirectional the input is set.
     *
     * @param bidirectionalReader the corresponding input value
     * @return this for chained calls
     */
    public Signal setBidirectionalReader(ObservableValue bidirectionalReader) {
        this.bidirectionalReader = bidirectionalReader;
        return this;
    }

    /**
     * @return the bidirectional reader, maybe null
     */
    public ObservableValue getBidirectionalReader() {
        return bidirectionalReader;
    }

    /**
     * Setter interface to set a value
     */
    public interface Setter {
        /**
         * Used to set a value.
         * Has to modify the inner state and also has to update the outputs.
         *
         * @param value the value to set
         * @param highZ true is value is in high z state
         */
        void set(long value, boolean highZ);
    }
}

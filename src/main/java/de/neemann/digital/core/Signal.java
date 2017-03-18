package de.neemann.digital.core;

import java.util.StringTokenizer;

/**
 * A simple storage bean for signals
 */
public final class Signal implements Comparable<Signal> {
    private final String name;
    private final ObservableValue value;
    private String description;

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
     * @return the signals description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description
     *
     * @param description the description
     * @return this for chained calls
     */
    public Signal setDescription(String description) {
        this.description = description;
        return this;
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

    /**
     * Gets the number of this pin.
     *
     * @return the pin number of -1 if no pin is given
     * @throws NodeException invalid pin number
     */
    public int getPinNumber() throws NodeException {
        if (getDescription() != null && getDescription().length() > 0) {
            StringTokenizer st = new StringTokenizer(getDescription(), "\n\r");
            while (st.hasMoreTokens()) {
                String line = st.nextToken();
                if (line.toLowerCase().startsWith("pin ")) {
                    String intStr = line.substring(4).trim();
                    try {
                        return Integer.parseInt(intStr);
                    } catch (NumberFormatException e) {
                        throw new NodeException("invalid pin assignment " + getName() + "=" + intStr);
                    }
                }
            }
        }
        return -1;
    }

}

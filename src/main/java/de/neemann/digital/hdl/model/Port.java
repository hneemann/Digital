package de.neemann.digital.hdl.model;

/**
 * represents a port
 */
public class Port {

    /**
     * a ports direction
     */
    public enum Direction {in, out}

    private final String name;
    private final Direction direction;
    private int bits = 0;
    private Signal signal;

    /**
     * Creates a new port
     *
     * @param name      the name
     * @param direction the direction
     */
    public Port(String name, Direction direction) {
        this.name = name;
        this.direction = direction;
    }

    /**
     * @return the name of the port
     */
    public String getName() {
        return name;
    }

    /**
     * sets the sugnal which this port is to connect to
     *
     * @param signal the signal
     */
    public void setSignal(Signal signal) {
        this.signal = signal;
    }

    /**
     * @return the signal this port is to connect to
     */
    public Signal getSignal() {
        return signal;
    }

    @Override
    public String toString() {
        return direction + " " + name + "[" + bits + "] " + " (" + signal + ")";
    }

    /**
     * @return the number of bits
     */
    public int getBits() {
        return bits;
    }

    /**
     * Sets the number of bits of this signal
     *
     * @param bits the number of bits
     */
    public void setBits(int bits) {
        this.bits = bits;
    }

    /**
     * @return the direction of this port
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * Helper to ensure that this port has the expected direction
     *
     * @param expected the expected direction
     * @return this for chained calls
     * @throws HDLException HDLException
     */
    public Port ensure(Direction expected) throws HDLException {
        if (expected != direction)
            throw new HDLException("wrong direction");
        return this;
    }

}

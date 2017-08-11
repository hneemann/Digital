package de.neemann.digital.hdl.model;

/**
 * represents a port
 */
public class Port {

    /**
     * Prefix used for ports.
     */
    public static final String PREFIX = "PORT_";

    /**
     * a ports direction
     */
    public enum Direction {
        /**
         * port is input
         */
        in,
        /**
         * port is output
         */
        out
    }

    private final String name;
    private final String origName;
    private final Direction direction;
    private int bits = 0;
    private Signal signal;
    private boolean isConstant;
    private long constant;
    private String pinNumber;

    /**
     * Creates a new port
     *
     * @param name      the name
     * @param direction the direction
     */
    public Port(String name, Direction direction) {
        this.origName = name;

        name = name
                .replace('-', '_')
                .replace("\u00AC", "not")
                .replace("=", "eq")
                .replace("<", "le")
                .replace(">", "gr");

        this.name = PREFIX + name;
        this.direction = direction;
    }

    /**
     * Copy constructor
     *
     * @param p port to copy
     */
    public Port(Port p) {
        this(p.origName, p.direction);
        bits = p.bits;
        signal = p.signal;
    }

    /**
     * @return the name of the port
     */
    public String getName() {
        return name;
    }

    /**
     * @return the original name
     */
    public String getOrigName() {
        return origName;
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
     * Sets this port to a constant value
     *
     * @param constant the value
     */
    public void setConstant(long constant) {
        isConstant = true;
        this.constant = constant;
    }

    /**
     * @return true if this port is a constant
     */
    public boolean isConstant() {
        return isConstant;
    }

    /**
     * @return the constant value
     */
    public long getConstant() {
        return constant;
    }

    /**
     * Sets the pin number
     *
     * @param number the pin number
     */
    public void setPinNumber(String number) {
        this.pinNumber = number;
    }

    /**
     * @return the pin number
     */
    public String getPinNumber() {
        return pinNumber;
    }

}

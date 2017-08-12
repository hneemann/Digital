package de.neemann.digital.hdl.model;

import de.neemann.digital.lang.Lang;

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
     * @throws HDLException if name is invalid
     */
    public Port(String name, Direction direction) throws HDLException {
        this.origName = name;

        name = name
                .replace('-', '_')
                .replace("\u00AC", "not")
                .replace("=", "eq")
                .replace("<", "le")
                .replace(">", "gr");

        if (!isNameValid(name))
            throw new HDLException(Lang.get("err_notAValidName", name));

        this.name = PREFIX + name;
        this.direction = direction;
    }

    /**
     * Does a name cleanup to avoid illegal vhdl names
     *
     * @param name the original name
     * @return the cleand up name
     */
    public static boolean isNameValid(String name) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < name.length(); i++) {
            char c = name.charAt(i);
            if (!((c >= '0' && c <= '9')
                    || (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || (c == '_')))
                return false;
        }
        return name.length() > 0 && name.charAt(name.length() - 1) != '_';
    }

    /**
     * Copy constructor
     *
     * @param p port to copy
     * @throws HDLException if name is invalid
     */
    public Port(Port p) throws HDLException {
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
     * @return this for chained calls
     */
    public Port setBits(int bits) {
        this.bits = bits;
        return this;
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

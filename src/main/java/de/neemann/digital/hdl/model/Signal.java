package de.neemann.digital.hdl.model;

import de.neemann.digital.lang.Lang;

import java.util.ArrayList;

/**
 * represents a signal
 */
public class Signal implements Comparable<Signal> {
    private final String name;
    private final ArrayList<Port> ports;
    private int bits;
    private Port.Direction portDirection;
    private boolean written = false;
    private HDLConstant constant;
    private boolean isConstant;

    /**
     * Creates a new signal
     *
     * @param number the name of the signal
     */
    public Signal(int number) {
        this("S" + number);
    }

    /**
     * Creates a new signal
     *
     * @param name the name of the signal
     */
    public Signal(String name) {
        this.name = name;
        ports = new ArrayList<>();
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Add a port to this signal
     *
     * @param port the port
     * @return this for chained calls
     */
    public Signal addPort(Port port) {
        port.setSignal(this);
        ports.add(port);
        return this;
    }

    /**
     * Checks if all ports connected to this signal have the same bit size.
     * Also distributes the bit size to all input ports.
     *
     * @throws HDLException HDLException
     */
    public void checkBits() throws HDLException {
        boolean isInput = false;
        for (Port p : ports) {
            int portBits = p.getBits();
            if (portBits != 0) {
                if (bits == 0)
                    bits = portBits;
                else {
                    if (bits != portBits)
                        throw new HDLException(Lang.get("err_notAllOutputsSameBits"));
                }
            }
            if (p.getDirection() == Port.Direction.in)
                isInput = true;
            if (p.getDirection() == Port.Direction.out && p.isConstant())
                setConstant(p.getConstant());
        }
        if (isInput && bits == 0)
            throw new HDLException(Lang.get("err_noOutConnectedToWire", ports));

        for (Port p : ports)
            p.setBits(bits);
    }

    /**
     * Marks this signal as a signal describing a port
     *
     * @param direction the direction of the port
     * @return this for chained calls
     */
    public Signal setIsPort(Port.Direction direction) {
        portDirection = direction;
        return this;
    }

    /**
     * @return true if this signal represents a port
     */
    public boolean isPort() {
        return portDirection != null;
    }

    /**
     * @return the number of bits
     */
    public int getBits() {
        return bits;
    }

    /**
     * @return the name of the signal
     */
    public String getName() {
        return name;
    }


    @Override
    public int compareTo(Signal signal) {
        return name.compareTo(signal.name);
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

    /**
     * Set the number of bits
     *
     * @param bits the number of bits
     * @return this for chained calls
     */
    public Signal setBits(int bits) {
        this.bits = bits;
        return this;
    }

    /**
     * Sets this signal to "is written"
     *
     * @throws HDLException HDLException
     */
    public void setIsWritten() throws HDLException {
        if (written)
            throw new HDLException(name + " is written twice! Tristate outputs not available inside an fpga!");
        written = true;
    }

    /**
     * Checks if this signal is written
     *
     * @return true if signal is written
     */
    public boolean isWritten() {
        return written;
    }

    /**
     * @return the list of ports connected to this signal
     */
    public ArrayList<Port> getPorts() {
        return ports;
    }

    private void setConstant(HDLConstant constant) {
        isConstant = true;
        this.constant = constant;
    }

    /**
     * @return true if this is a constant
     */
    public boolean isConstant() {
        return isConstant;
    }

    /**
     * @return the constant value
     */
    public HDLConstant getConstant() {
        return constant;
    }

    /**
     * Replaces this signal in all associated ports.
     *
     * @param s the new signal used by all ports of this signal
     */
    public void replaceWith(Signal s) {
        s.setBits(bits);
        for (Port p : ports)
            s.addPort(p);
        ports.clear();
    }

}

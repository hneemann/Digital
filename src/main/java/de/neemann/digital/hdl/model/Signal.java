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
    private boolean isPort = false;
    private boolean written = false;

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
     */
    public void addPort(Port port) {
        port.setSignal(this);
        ports.add(port);
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
        }
        if (isInput && bits == 0)
            throw new HDLException(Lang.get("err_noOutConnectedToWire", ports));

        for (Port p : ports)
            p.setBits(bits);
    }

    /**
     * Marks this signal as a signal describing a port
     *
     * @return this for chained calls
     */
    public Signal setIsPort() {
        isPort = true;
        return this;
    }

    /**
     * @return true if this signal represents a port
     */
    public boolean isPort() {
        return isPort;
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

    /**
     * Set the number of bits
     *
     * @param bits the number of bits
     */
    public void setBits(int bits) {
        this.bits = bits;
    }

    /**
     * Sets this signal to "is written"
     *
     * @throws HDLException HDLException
     */
    public void setIsWritten() throws HDLException {
        if (written)
            throw new HDLException(name + " is written twice");
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
}

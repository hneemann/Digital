package de.neemann.digital.hdl.model;

import de.neemann.digital.lang.Lang;

import java.util.ArrayList;

/**
 * represents a signal
 */
public class Signal {
    private final String name;
    private final ArrayList<Port> ports;
    private int bits;
    private boolean isPort = false;
    private Signal sNeg;

    /**
     * Creates a new signal
     *
     * @param name the name of the signal
     */
    public Signal(String name) {
        this.name = "SIG_" + name;
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

        if (sNeg != null)
            sNeg.bits = bits;

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

    /**
     * Sets a signal which bit size is the same as this signal.
     * Used for the generated inverter nodes.
     *
     * @param sNeg the signal to track
     */
    public void copyBitsTo(Signal sNeg) {
        this.sNeg = sNeg;
    }
}

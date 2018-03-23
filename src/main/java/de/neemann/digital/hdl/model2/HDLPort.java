/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;

/**
 * A port
 */
public class HDLPort implements Printable {

    /**
     * The ports direction
     */
    public enum Direction {
        /**
         * input
         */
        IN,
        /**
         * output
         */
        OUT
    }

    private final String name;
    private final Direction direction;
    private int bits;
    private HDLNet net;
    private String pinNumber;

    /**
     * Creates a new instance
     *
     * @param name      the name of the port
     * @param net       the net of this port
     * @param direction the ports direction
     * @param bits      the bit width
     * @throws HDLException HDLException
     */
    public HDLPort(String name, HDLNet net, Direction direction, int bits) throws HDLException {
        this.name = name;
        this.net = net;
        this.direction = direction;
        this.bits = bits;

        if (net != null)
            net.addPort(this);
    }

    /**
     * Sets the pin number to this port
     *
     * @param pinNumber the pin number
     * @return this for chained calls
     */
    public HDLPort setPinNumber(String pinNumber) {
        this.pinNumber = pinNumber;
        return this;
    }

    /**
     * @return the net of this port
     */
    public HDLNet getNet() {
        return net;
    }

    /**
     * Sets the net of this port
     *
     * @param net the net
     * @throws HDLException HDLException
     */
    public void setNet(HDLNet net) throws HDLException {
        if (this.net != null)
            this.net.remove(this);
        this.net = net;
        net.addPort(this);
    }

    /**
     * @return the name of this port
     */
    public String getName() {
        return name;
    }

    /**
     * @return the bit width of this port
     */
    public int getBits() {
        return bits;
    }

    /**
     * Sets the bit width of this port
     *
     * @param bits the number of bits
     */
    public void setBits(int bits) {
        this.bits = bits;
    }

    /**
     * @return the ports direction
     */
    public Direction getDirection() {
        return direction;
    }

    /**
     * @return the pin number of this port
     */
    public String getPinNumber() {
        return pinNumber;
    }

    @Override
    public String toString() {
        return direction + " " + name + "(" + bits + ")";
    }

    @Override
    public void print(CodePrinter out) throws IOException {
        out.print(name).print(":").print(bits);
    }
}

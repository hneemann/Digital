/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.lang.Lang;

import java.io.IOException;

/**
 * A port
 */
public class HDLPort implements Printable, HasName {

    /**
     * The ports direction
     */
    public enum Direction {
        /**
         * input
         * Caution: a circuits output components port has type IN because it reads the value to
         * output, seen from inside the node.
         */
        IN,
        /**
         * output
         * Caution: a circuits input components port has type OUT because it defines a value,
         * seen from inside the node.
         */
        OUT,
        /**
         * Bidirectional port
         */
        INOUT
    }

    private String name;
    private Direction direction;
    private int bits;
    private String description;
    private boolean isClock;
    private HDLNet net;
    private String pinNumber;
    private HDLNode parent;

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
        this.name = name.trim();
        if (this.name.length() == 0)
            throw new HDLException(Lang.get("err_vhdlANameIsMissing"));
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
     * Sets the description of this port.
     *
     * @param description the description
     * @return this for chained calls
     */
    public HDLPort setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * @return the ports description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return true if this port has a description
     */
    public boolean hasDescription() {
        return description != null && description.trim().length() > 0;
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
        if (net != null)
            net.addPort(this);
    }

    @Override
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

    /**
     * Sets the clock port flag
     */
    public void setIsClock() {
        isClock = true;
    }

    /**
     * @return true if this is a clock port
     */
    public boolean isClock() {
        return isClock;
    }

    /**
     * Sets the parent node
     *
     * @param parent the parent node
     */
    public void setParent(HDLNode parent) {
        this.parent = parent;
    }

    /**
     * @return the parent node
     */
    public HDLNode getParent() {
        return parent;
    }

    @Override
    public String toString() {
        return direction + " " + name + "(" + bits + ")";
    }

    @Override
    public void print(CodePrinter out) throws IOException {
        out.print(name).print(":").print(bits);
        if (net != null) {
            if (net.getOutput() == this)
                out.print(" defines (");
            else
                out.print(" reads (");
            net.print(out);
            out.print(")");
        } else
            out.print(" is not used");
    }

    /**
     * Renames this port
     *
     * @param renaming the renaming algorithm
     */
    public void rename(HDLModel.Renaming renaming) {
        name = renaming.checkName(name);
    }

    /**
     * Sets this port to a inOut mode port
     */
    public void setInOut() {
        direction = Direction.INOUT;
    }

}

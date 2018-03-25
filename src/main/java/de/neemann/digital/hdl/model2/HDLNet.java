/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Represents a net.
 * A net can have only one input and several outputs.
 */
public class HDLNet implements Printable {
    private String name;
    private ArrayList<HDLPort> inputs;
    private HDLPort output;
    private boolean needsVariable = true;
    private boolean isInput;

    /**
     * Creates a new net
     *
     * @param name the nets name
     */
    public HDLNet(String name) {
        this.name = name;
        inputs = new ArrayList<>();
    }

    /**
     * Adds a port to this net.
     *
     * @param hdlPort the port to add
     * @throws HDLException HDLException
     */
    public void addPort(HDLPort hdlPort) throws HDLException {
        if (hdlPort.getDirection().equals(HDLPort.Direction.OUT)) {
            if (output != null)
                throw new HDLException("multiple outputs connected to net " + name + ": " + output + " and " + hdlPort);
            output = hdlPort;
        } else
            inputs.add(hdlPort);
    }

    /**
     * @return the inputs which are connected to this net.
     */
    public ArrayList<HDLPort> getInputs() {
        return inputs;
    }

    /**
     * @return the output which defines the nets value
     */
    public HDLPort getOutput() {
        return output;
    }

    @Override
    public String toString() {
        return name + " (" + output + " " + inputs + ")";
    }

    void fixBits() throws HDLException {
        if (output == null)
            throw new HDLException("no output connected to net");
        final int bits = output.getBits();
        if (bits == 0)
            throw new HDLException("no bit number set for output " + output.getName());

        for (HDLPort i : inputs)
            i.setBits(bits);

    }

    void remove(HDLPort p) {
        if (p == output)
            output = null;
        else
            inputs.remove(p);
    }

    @Override
    public void print(CodePrinter out) throws IOException {
        out.print(name).print("->").print(inputs.size());
    }

    /**
     * @return the name of the net
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the net
     *
     * @param name the name to use
     */
    public void setName(String name) {
        this.name = name;
    }

    void setIsInput(String name) {
        this.needsVariable = false;
        this.isInput = true;
        this.name = name;
    }

    /**
     * @return true if tins net represents a nodes input
     */
    public boolean isInput() {
        return isInput;
    }

    /**
     * @return true if this net needs a temp variable to represent the value
     */
    public boolean needsVariable() {
        return needsVariable;
    }

    void setIsOutput(String name, boolean singleRead) {
        if (singleRead) {
            this.name = name;
            needsVariable = false;
        } else
            this.name = name + "_temp";
    }

    /**
     * @return the number of bits on this net
     */
    public int getBits() {
        return output.getBits();
    }

    /**
     * resets the output of this net
     */
    public void resetOutput() {
        output = null;
        name = null;
    }

    /**
     * Renames this net.
     *
     * @param renaming the renaming algorithm
     */
    public void rename(HDLModel.Renaming renaming) {
        name = renaming.checkName(name);
    }

}

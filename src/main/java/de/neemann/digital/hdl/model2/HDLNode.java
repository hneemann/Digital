/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The base class of all nodes
 */
public class HDLNode {
    private final String elementName;
    private final ElementAttributes elementAttributes;
    private final HDLModel.BitProvider bitProvider;
    private final ArrayList<HDLPort> inputs;
    private final ArrayList<HDLPort> outputs;
    private String specializedName;

    /**
     * Creates e new instance
     *
     * @param elementName       the instances name
     * @param elementAttributes the attributes
     * @param bitProvider       the bit provider which provides the outputs bit width
     */
    public HDLNode(String elementName, ElementAttributes elementAttributes, HDLModel.BitProvider bitProvider) {
        this.elementName = elementName;
        this.elementAttributes = elementAttributes;
        this.bitProvider = bitProvider;
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();
    }

    void addInput(HDLPort port) {
        inputs.add(port);
    }

    void addOutput(HDLPort port) {
        outputs.add(port);
    }

    @Override
    public String toString() {
        return elementName + " " + inputs + " " + outputs;
    }

    /**
     * @return the elements name
     */
    public String getElementName() {
        return elementName;
    }

    /**
     * @return the elements attributes
     */
    public ElementAttributes getElementAttributes() {
        return elementAttributes;
    }

    /**
     * @return the nodes inputs
     */
    public ArrayList<HDLPort> getInputs() {
        return inputs;
    }

    /**
     * @return the nodes single outputs
     */
    public HDLPort getOutput() {
        return outputs.get(0);
    }

    /**
     * @return the list of outputs
     */
    public ArrayList<HDLPort> getOutputs() {
        return outputs;
    }

    int getBits(String name) {
        return bitProvider.getBits(name);
    }

    /**
     * Prints a simple text representation of the node
     *
     * @param out the CondePrinter to print to
     * @throws IOException IOException
     */
    public void print(CodePrinter out) throws IOException {
        out.print("in");
        printWithLocal(out, inputs);
        out.print("out");
        printWithLocal(out, outputs);
    }

    private void printWithLocal(CodePrinter out, ArrayList<HDLPort> ports) throws IOException {
        boolean first = true;
        for (HDLPort p : ports) {
            if (first) {
                first = false;
                out.print("(");
            } else
                out.print(", ");
            p.print(out);
            if (p.getNet() == null)
                out.print(" is not used");
            else {
                out.print(" is ");
                p.getNet().print(out);
            }
        }
        if (first)
            out.print("(");

        out.println(")");
    }

    /**
     * Returns true if the node has the given port as an input
     *
     * @param i the port to search for
     * @return true if the given port is a input of this node
     */
    public boolean hasInput(HDLPort i) {
        for (HDLPort p : inputs)
            if (p.getNet() == i.getNet())
                return true;
        return false;
    }

    /**
     * Replaces a net in this node
     *
     * @param oldNet the old net
     * @param newNet the new net
     */
    public void replaceNet(HDLNet oldNet, HDLNet newNet) {
    }

    public void setSpecializedName(String specializedName) {
        this.specializedName = specializedName;
    }

    public String getSpecializedName() {
        return specializedName;
    }

    public void rename(HDLModel.Renaming renaming) {
        for (HDLPort p : outputs)
            p.rename(renaming);
        for (HDLPort p : inputs)
            p.rename(renaming);
    }
}

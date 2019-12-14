/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.hdl.model2.expression.Expression;
import de.neemann.digital.hdl.model2.expression.Visitor;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;
import java.util.ArrayList;

/**
 * The base class of all nodes
 */
public abstract class HDLNode {
    private final String elementName;
    private final ElementAttributes elementAttributes;
    private final HDLModel.BitProvider bitProvider;
    private final ArrayList<HDLPort> inputs;
    private final ArrayList<HDLPort> outputs;
    private final ArrayList<HDLPort> inOutputs;
    private String hdlEntityName;

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
        inOutputs = new ArrayList<>();
    }

    /**
     * Adds a port to this node
     *
     * @param port the port to add
     * @return this for chained calls
     */
    public HDLNode addPort(HDLPort port) {
        switch (port.getDirection()) {
            case IN:
                inputs.add(port);
                break;
            case OUT:
                outputs.add(port);
                break;
            case INOUT:
                inOutputs.add(port);
                break;
        }

        port.setParent(this);

        return this;
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

    /**
     * @return the list of inOutputs
     */
    public ArrayList<HDLPort> getInOutputs() {
        return inOutputs;
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
        if (!inOutputs.isEmpty()) {
            out.print("inOut");
            printWithLocal(out, inOutputs);
        }
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

    /**
     * Sets the name to use in the target language
     *
     * @param hdlEntityName the name of the node in the target hdl
     */
    public void setHdlEntityName(String hdlEntityName) {
        this.hdlEntityName = hdlEntityName;
    }

    /**
     * @return the name of the node in the target hdl
     */
    public String getHdlEntityName() {
        return hdlEntityName;
    }

    /**
     * Renames the signals in this node.
     *
     * @param renaming the renaming algorithm
     */
    public void rename(HDLModel.Renaming renaming) {
        for (HDLPort p : outputs)
            p.rename(renaming);
        for (HDLPort p : inputs)
            p.rename(renaming);
        for (HDLPort p : inOutputs)
            p.rename(renaming);
    }

    /**
     * Called to replace a net by an expression.
     *
     * @param net        the net to replace
     * @param expression the expression to use instead
     */
    public abstract void replaceNetByExpression(HDLNet net, Expression expression);

    /**
     * Traverses all expressions
     *
     * @param visitor the visitor
     * @param <V>     the type of the visitor
     * @return the visitor for chained calls
     */
    public abstract <V extends Visitor> V traverseExpressions(V visitor);
}

/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

import de.neemann.digital.core.wiring.Splitter;
import de.neemann.digital.hdl.model2.expression.Expression;
import de.neemann.digital.hdl.model2.expression.Visitor;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;

/**
 * A One to Many splitter
 * A Many to Many splitter is build with a ManyToOne and a OneToMany splitter.
 */
public class HDLNodeSplitterOneToMany extends HDLNode {
    private final Splitter.Ports outputSplit;

    /**
     * Creates a new instance
     *
     * @param node        the original splitter node
     * @param outputSplit output splitting
     */
    HDLNodeSplitterOneToMany(HDLNode node, Splitter.Ports outputSplit) {
        super(node.getElementName(), node.getElementAttributes(), null);
        this.outputSplit = outputSplit;
        for (HDLPort p : node.getInputs())
            addPort(p);
        for (HDLPort p : node.getOutputs())
            addPort(p);
    }

    /**
     * @return the output splitting
     */
    public Splitter.Ports getOutputSplit() {
        return outputSplit;
    }

    @Override
    public void print(CodePrinter out) throws IOException {
        super.print(out);
        int i = 0;
        HDLPort in = getInputs().get(0);
        for (Splitter.Port sp : outputSplit) {
            HDLPort p = getOutputs().get(i++);
            out.print(p.getNet().getName()).print(" := ").print(in.getNet().getName())
                    .print("(").print(sp.getPos()).print("-").print(sp.getPos() + sp.getBits() - 1).println(")");
        }
    }

    @Override
    public void replaceNetByExpression(HDLNet net, Expression expression) {
    }

    @Override
    public <V extends Visitor> V traverseExpressions(V visitor) {
        throw new RuntimeException("HDLNodeSplitterOneToMany is not expression based!");
    }

    /**
     * @return the source signals name
     */
    public String getSourceSignal() {
        return getInputs().get(0).getNet().getName();
    }
}

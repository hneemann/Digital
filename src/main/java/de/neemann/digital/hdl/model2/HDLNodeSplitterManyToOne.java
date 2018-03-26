/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

import de.neemann.digital.core.wiring.Splitter;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;

/**
 * The Many to One splitter.
 * A Many to Many splitter is build with a ManyToOne and a OneToMany splitter.
 */
public class HDLNodeSplitterManyToOne extends HDLNode {
    private final Splitter.Ports inputSplit;

    /**
     * Creates a new instance
     *
     * @param node       the original splitter node
     * @param inputSplit input splitting
     */
    HDLNodeSplitterManyToOne(HDLNode node, Splitter.Ports inputSplit) {
        super(node.getElementName(), node.getElementAttributes(), null);
        this.inputSplit = inputSplit;
        for (HDLPort p : node.getInputs())
            addPort(p);
        for (HDLPort p : node.getOutputs())
            addPort(p);
    }

    /**
     * @return the input splitting
     */
    public Splitter.Ports getInputSplit() {
        return inputSplit;
    }

    /**
     * @return the targets signal name
     */
    public String getTargetSignal() {
        return getOutput().getNet().getName();
    }

    @Override
    public void print(CodePrinter out) throws IOException {
        super.print(out);
        int i = 0;
        HDLPort o = getOutput();
        for (Splitter.Port sp : inputSplit) {
            HDLPort p = getInputs().get(i++);
            out.print(o.getNet().getName())
                    .print("(").print(sp.getPos()).print("-").print(sp.getPos() + sp.getBits() - 1).print(")")
                    .print(" := ").println(p.getNet().getName());
        }
    }


}

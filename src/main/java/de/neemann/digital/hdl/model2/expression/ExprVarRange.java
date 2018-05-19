/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2.expression;

import de.neemann.digital.hdl.model2.HDLNet;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;

/**
 * A reference to a net slice
 */
public class ExprVarRange implements Expression, ExprUsingNet {
    private HDLNet net;
    private final int msb;
    private final int lsb;

    /**
     * creates a new net reference
     *
     * @param net the net
     * @param msb most significant bit to use
     * @param lsb least significant bit to use
     */
    public ExprVarRange(HDLNet net, int msb, int lsb) {
        this.net = net;
        this.msb = msb;
        this.lsb = lsb;
    }

    @Override
    public HDLNet getNet() {
        return net;
    }

    /**
     * @return the msb
     */
    public int getMsb() {
        return msb;
    }

    /**
     * @return the lsb
     */
    public int getLsb() {
        return lsb;
    }

    @Override
    public void print(CodePrinter out) throws IOException {
        out.print(net.getName()).print("(").print(msb).print("-").print(lsb).print(")");
    }

    @Override
    public void replace(HDLNet net, Expression expression) {
        if (net == this.net)
            throw new RuntimeException("should not happen!");
    }
}

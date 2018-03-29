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
 * A reference to a net
 */
public class ExprVar implements Expression, ExprUsingNet {
    private HDLNet net;

    /**
     * creates a new net reference
     *
     * @param net the net
     */
    public ExprVar(HDLNet net) {
        if (net == null)
            throw new NullPointerException("net is null");
        this.net = net;
    }

    @Override
    public HDLNet getNet() {
        return net;
    }

    @Override
    public void print(CodePrinter out) throws IOException {
        out.print(net.getName());
    }

    @Override
    public void replace(HDLNet net, Expression expression) {
        if (net == this.net)
            throw new RuntimeException("should not happen!");
    }

}

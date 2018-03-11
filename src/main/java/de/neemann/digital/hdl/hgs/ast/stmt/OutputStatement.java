/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.ast.stmt;

import de.neemann.digital.hdl.hgs.rt.HGSRuntimeContext;

/**
 *
 * @author ideras
 */
public class OutputStatement extends Statement {
    private final String outString;

    /**
     * Creates a new instance.
     *
     * @param outString the string to send to the output.
     */
    public OutputStatement(String outString) {
        super(0);
        this.outString = outString;
    }

    /**
     * Returns the output string
     *
     * @return the output string.
     */
    public String getOutString() {
        return outString;
    }

    @Override
    public void execute(HGSRuntimeContext ctx) {
        ctx.print(outString);
    }

}

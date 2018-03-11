/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir.stmt;

import de.neemann.digital.hdl.model.Signal;
import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.expr.VDelegatedExpr;
import java.io.IOException;

/**
 * Represents an empty statement.
 *
 * @author ideras
 */
public class VEmptyStatement extends VStatement {
    /**
     * Initialize a new instance
     *
     * @param place the statement place
     */
    public VEmptyStatement(VStatementPlace place) {
        super(place);
    }

    @Override
    public VExpr resolveToExpr(VerilogCodeBuilder vcBuilder) {
        Signal signal = null;
        VStatementPlace place = getPlace();

        if (place.getSignalCount() == 1) {
            signal = place.getSignalList().get(0);
        }
        vcBuilder.registerStatement(this, signal);
        VExpr expr = new VDelegatedExpr(place.getLSideExpr(), this).setSignal(signal);

        vcBuilder.setCodeIrForSignal(signal, expr);

        return expr;
    }

    @Override
    public void writeSourceCode(VerilogCodeBuilder vcBuilder, CodePrinter out) throws IOException {
        throw new RuntimeException("Cannot call to VEmptyStatement.writeSourceCode");
    }
}

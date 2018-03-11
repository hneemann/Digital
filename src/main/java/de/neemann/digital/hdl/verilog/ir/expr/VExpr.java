/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir.expr;

import de.neemann.digital.hdl.model.Signal;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.stmt.VAssignStatement;
import de.neemann.digital.hdl.verilog.ir.VIRNode;
import de.neemann.digital.hdl.verilog.ir.VOperator;
import de.neemann.digital.hdl.verilog.ir.VSignalDecl;
import de.neemann.digital.hdl.verilog.ir.stmt.VStatement;
import de.neemann.digital.hdl.verilog.ir.stmt.VStatementPlace;

/**
 *
 * @author ideras
 */
public abstract class VExpr extends VIRNode {
    private Signal signal;

    /**
     * Expression node base constructor
     */
    public VExpr() {
        super();
        this.signal = null;
    }

    /**
     * The expression operator.
     *
     * @return the expression operator.
     */
    public VOperator getOper() {
        return VOperator.NONE;
    }

    /**
     * Returns the signal
     *
     * @return the signal
     */
    public Signal getSignal() {
        return signal;
    }

    /**
     * Sets the signal computed by this expression
     *
     * @param signal the signal
     * @return reference to this object to allow chaining
     */
    public VExpr setSignal(Signal signal) {
        this.signal = signal;
        return this; // Allow chaining
    }

    @Override
    public boolean isExpr() {
        return true;
    }

    @Override
    public VExpr resolveToExpr(VerilogCodeBuilder vcBuilder) {
        if (signal != null && signal.getPorts().size() > 2) {
            vcBuilder.registerAndAddSignalDecl(signal, VSignalDecl.Type.WIRE);
            return resolveToIdExpr(vcBuilder);
        }
        return this;
    }

    /**
     * Returns an ID expression
     *
     * @param vcBuilder the verilog builder instance.
     * @return the expression
     */
    public VExpr resolveToIdExpr(VerilogCodeBuilder vcBuilder) {
        if (signal == null) {
            throw new RuntimeException("BUG in the machine: resolveToIdExpr called with null signal.");
        }
        VStatement stmt = new VAssignStatement(new VStatementPlace(signal), this);

        vcBuilder.registerAndAddSignalDecl(signal, VSignalDecl.Type.WIRE);
        vcBuilder.registerStatement(stmt, signal);
        VExpr expr = new VIdExpr(signal.getName());

        expr.setSignal(signal);
        vcBuilder.setCodeIrForSignal(signal, expr);

        return expr;
    }

    /**
     * Return the verilog source code for this expression.
     *
     * @param vcBuilder the code builder.
     * @return the verilog source code.
     */
    public abstract String getSourceCode(VerilogCodeBuilder vcBuilder);
}

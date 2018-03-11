/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir.stmt;

import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.VIRNode;
import java.io.IOException;

/**
 * Represents a verilog assign statement.
 * A assign statement can appear inside an always block or outside.
 *
 * @author ideras
 */
public class VAssignStatement extends VStatement {
    private final VExpr rightExpr;
    private Type assignType;

    /**
     * The assignment type
     */
    public enum Type {
        /**
         * Blocking
         */
        BLOCKING,
        /**
         * Non blocking
         */
        NON_BLOCKING
    };

    /**
     * Creates a new assign statement instance.
     *
     * @param place The left side of the assignment.
     * @param rightExpr  The right side of the assignment.
     * @param assignType The assignment type (blocking or non blocking)
     */
    public VAssignStatement(VStatementPlace place, VExpr rightExpr, Type assignType) {
        super(place);
        this.rightExpr = rightExpr;
        this.assignType = assignType;
    }

    /**
     * Creates a new blocking assign statement instance.
     *
     * @param place The left side of the assignment.
     * @param rightExpr  The right side of the assignment.
     */
    public VAssignStatement(VStatementPlace place, VExpr rightExpr) {
        this(place, rightExpr, Type.BLOCKING);
    }

    /**
     * Returns the expression node
     *
     * @return the expression node
     */
    public VIRNode getExpr() {
        return rightExpr;
    }

    /**
     * Returns the type of the assign
     *
     * @return the type of the assign
     */
    public Type getAssignType() {
        return assignType;
    }

    @Override
    public void writeSourceCode(VerilogCodeBuilder vcBuilder, CodePrinter out) throws IOException {
        String leftCode = getRootPlace().getLSideExpr().getSourceCode(vcBuilder);

        out.print(leftCode)
           .print(assignType == Type.BLOCKING? " = " : " <= ")
           .print(rightExpr.getSourceCode(vcBuilder))
           .print(";");
    }
}

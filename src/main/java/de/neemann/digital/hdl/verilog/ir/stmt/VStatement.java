/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir.stmt;

import de.neemann.digital.hdl.model.Signal;
import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.verilog.ir.expr.VIdExpr;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.verilog.VerilogCodeBuilder;
import de.neemann.digital.hdl.verilog.ir.VIRNode;
import java.io.IOException;

/**
 * Statement IR (intermediate representation) node base class.
 *
 * @author ideras
 */
public abstract class VStatement extends VIRNode {
    private VStatementPlace place;
    private VStatement parentStmt;

    /**
     * Statement node base constructor
     *
     * @param place the statement place
     */
    public VStatement(VStatementPlace place) {
        super();
        this.place = place;
        parentStmt = null;
    }

    /**
     * Returns the place (signals) this statement computes.
     *
     * @return the places
     */
    public VStatementPlace getPlace() {
        return place;
    }

    /**
     * The place of topmost parent statement
     *
     * @return the root place
     */
    public VStatementPlace getRootPlace() {
        VStatement stmt = this;
        VStatementPlace resPlace = null;

        while (stmt != null) {
            resPlace = stmt.place;
            stmt = stmt.parentStmt;
        }

        return resPlace;
    }

    /**
     * Returns the parent statement
     *
     * @return the parent statement
     */
    public VStatement getParentStatement() {
        return parentStmt;
    }

    /**
     * Sets the parent statement
     *
     * @param parentStmt the new parent
     */
    public void setParent(VStatement parentStmt) {
        this.parentStmt = parentStmt;
    }

    /**
     * Sets the place this statement computes.
     *
     * @param place the new place
     */
    public void setPlace(VStatementPlace place) {
        this.place = place;
    }

    @Override
    public boolean isStatement() {
        return true;
    }

    @Override
    public VExpr resolveToExpr(VerilogCodeBuilder vcBuilder) {
        Signal signal = null;

        if (place.getSignalCount() == 1) {
            signal = place.getSignalList().get(0);
        }

        vcBuilder.registerStatement(this, signal);
        VExpr expr = place.getLSideExpr();

        for (Signal s : place.getSignalList()) {
            VExpr idExpr = new VIdExpr(s.getName()).setSignal(s);

            vcBuilder.addDeclaration(s.getName());
            vcBuilder.setCodeIrForSignal(s, idExpr);
        }

        return expr;
    }

    /**
     * Generate the verilog source and writes it to the output.
     *
     * @param vcBuilder the code builder.
     * @param out the output stream.
     * @throws IOException IOException
     */
    public abstract void writeSourceCode(VerilogCodeBuilder vcBuilder, CodePrinter out) throws IOException;
}

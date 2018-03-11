/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog.ir.stmt;

import de.neemann.digital.hdl.verilog.ir.expr.VConcatExpr;
import de.neemann.digital.hdl.verilog.ir.expr.VExpr;
import de.neemann.digital.hdl.verilog.ir.expr.VIdExpr;
import de.neemann.digital.hdl.model.Signal;
import java.util.ArrayList;

/**
 * Represents the signal(s) computed by one statement.
 *
 * @author ideras
 */
public class VStatementPlace {
    private ArrayList<Signal> signalList;
    private VExpr lsideExpr = null;

    /**
     * Initialize a new statement place
     *
     * @param signals the list of HDL signals
     */
    public VStatementPlace(Signal... signals) {
        set(signals);
    }

    /**
     * Initialize the signal list
     *
     * @param signals the signals
     */
    public final void set(Signal... signals) {
        signalList = new ArrayList<>();

        for (Signal s : signals) {
            signalList.add(s);
        }
        buildLSideExpr();
    }

    /**
     * Return the number of signals
     *
     * @return the number of signals
     */
    public int getSignalCount() {
        return signalList.size();
    }

    /**
     * Return the signal list
     *
     * @return the signal list
     */
    public ArrayList<Signal> getSignalList() {
        return signalList;
    }

    /**
     * Builds the verilog expression for the statement place
     */
    private void buildLSideExpr() {
        if (signalList.size() == 1) {
            Signal s = signalList.get(0);
            lsideExpr = new VIdExpr(s.getName());
            lsideExpr.setSignal(s);
        } else {
            ArrayList<VExpr> exprList = new ArrayList<>();

            for (Signal s : signalList) {
                VExpr expr = new VIdExpr(s.getName());
                expr.setSignal(s);
                exprList.add(expr);
            }

            lsideExpr = new VConcatExpr(exprList);
        }
    }

    /**
     * Return the verilog expression
     *
     * @return the verilog expression
     */
    public VExpr getLSideExpr() {
        if (lsideExpr == null) {
            buildLSideExpr();
        }
        return lsideExpr;
    }
}

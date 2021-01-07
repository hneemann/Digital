/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2.expression;

import de.neemann.digital.hdl.model2.HDLNet;
import de.neemann.digital.hdl.model2.Printable;

/**
 * Represents a expression
 */
public interface Expression extends Printable {

    /**
     * Replaces a net with an expression
     *
     * @param net        the net to replace
     * @param expression the expression to use instead ot the net
     */
    void replace(HDLNet net, Expression expression);

    /**
     * Traverses all expressions
     *
     * @param visitor the visitor
     */
    default void traverse(Visitor visitor) {
        visitor.visit(this);
    }


    /**
     * Tries to optimize the expression by replacing it by a optimized one.
     *
     * @param eo the optimizer
     */
    default void optimize(ExpressionOptimizer eo) {
    }

    /**
     * Helper to check if an expression is a net reference
     *
     * @param expr the expression to check
     * @param net  the net
     * @return true if the expression is a reference to the given net
     */
    static boolean isVar(Expression expr, HDLNet net) {
        return expr instanceof ExprVar && ((ExprVar) expr).getNet() == net;
    }

}

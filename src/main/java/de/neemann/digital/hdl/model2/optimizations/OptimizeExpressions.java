/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2.optimizations;

import de.neemann.digital.hdl.model2.HDLCircuit;
import de.neemann.digital.hdl.model2.HDLException;
import de.neemann.digital.hdl.model2.HDLNode;
import de.neemann.digital.hdl.model2.HDLNodeAssignment;
import de.neemann.digital.hdl.model2.expression.ExpressionOptimizer;

/**
 * Optimization which addresses the used expressions.
 */
public class OptimizeExpressions implements Optimization {

    private final ExpressionOptimizer eo;

    /**
     * Creates a new instance
     *
     * @param eo the expression optimizer to use.
     */
    public OptimizeExpressions(ExpressionOptimizer eo) {
        this.eo = eo;
    }

    @Override
    public void optimize(HDLCircuit circuit) throws HDLException {
        for (HDLNode n : circuit.getNodes()) {
            if (n instanceof HDLNodeAssignment) {
                HDLNodeAssignment a = (HDLNodeAssignment) n;
                a.optimize(eo);
            }
        }
    }
}

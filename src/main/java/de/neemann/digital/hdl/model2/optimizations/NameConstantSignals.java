/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2.optimizations;

import de.neemann.digital.hdl.model2.*;
import de.neemann.digital.hdl.model2.expression.ExprConstant;

/**
 * Create proper constant signals names.
 * Use only if {@link MergeConstants} is applied at first.
 */
public class NameConstantSignals implements Optimization {
    @Override
    public void optimize(HDLCircuit circuit) throws HDLException {
        for (HDLNode n : circuit.getNodes()) {
            ExprConstant con = ExprConstant.isConstant(n);
            if (con != null) {
                HDLNet net = ((HDLNodeAssignment) n).getTargetNet();
                if (net.getName() == null)
                    net.setName("const" + con.getBits() + "b" + con.getValue());
            }
        }
    }
}

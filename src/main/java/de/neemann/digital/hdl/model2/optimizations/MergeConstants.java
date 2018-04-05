/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2.optimizations;

import de.neemann.digital.hdl.model2.*;
import de.neemann.digital.hdl.model2.expression.ExprConstant;
import de.neemann.digital.hdl.model2.expression.ExprVar;

import java.util.ArrayList;

/**
 * Multiple usages of the same constant are mapped to a single constant signal.
 */
public class MergeConstants implements Optimization {

    private HDLCircuit circuit;

    @Override
    public void optimize(HDLCircuit circuit) throws HDLException {
        this.circuit = circuit;
        ArrayList<HDLNode> nodes = circuit.getNodes();
        int n1 = 0;
        while (n1 < nodes.size()) {
            final HDLNode node1 = nodes.get(n1);
            ExprConstant con1 = ExprConstant.isConstant(node1);
            if (con1 != null) {
                //modification of loop variable n2 is intended!
                //CHECKSTYLE.OFF: ModifiedControlVariable
                for (int n2 = n1 + 1; n2 < nodes.size(); n2++) {
                    final HDLNode node2 = nodes.get(n2);
                    ExprConstant con2 = ExprConstant.isConstant(node2);
                    if (con2 != null) {
                        if (con1.isEqualTo(con2)) {
                            merge(node1, node2);
                            nodes.remove(n2);
                            n2--;
                        }
                    }
                }
                //CHECKSTYLE.ON: ModifiedControlVariable
            }

            n1++;
        }
    }

    private void merge(HDLNode node1, HDLNode node2) throws HDLException {
        HDLNet usedNet = node1.getOutput().getNet();
        HDLNet obsoleteNet = node2.getOutput().getNet();

        final ArrayList<HDLPort> ins = obsoleteNet.getInputs();
        for (HDLPort p : ins.toArray(new HDLPort[ins.size()]))
            p.setNet(usedNet);

        circuit.replaceNetByExpression(obsoleteNet, new ExprVar(usedNet));

        circuit.removeNet(obsoleteNet);
    }

}

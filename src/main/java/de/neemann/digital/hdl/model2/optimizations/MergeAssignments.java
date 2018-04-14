/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2.optimizations;

import de.neemann.digital.hdl.model2.*;
import de.neemann.digital.hdl.model2.expression.ExprVarRange;
import de.neemann.digital.hdl.model2.expression.Expression;
import de.neemann.digital.hdl.model2.expression.Visitor;

import java.util.ArrayList;

/**
 * Merges the bool expression by inlining nodes which also represent a bool expression.
 */
public class MergeAssignments implements Optimization {
    private HDLCircuit circuit;
    private ArrayList<HDLNode> nodes;

    @Override
    public void optimize(HDLCircuit circuit) {
        this.circuit = circuit;
        this.nodes = circuit.getNodes();

        boolean wasOptimization;
        do {
            wasOptimization = false;
            outer:
            for (int i = 0; i < nodes.size(); i++) {
                HDLNode n1 = nodes.get(i);
                if (n1 instanceof HDLNodeAssignment) {
                    HDLNodeAssignment host = (HDLNodeAssignment) n1;
                    for (HDLPort p : host.getInputs()) {
                        HDLNode n2 = searchCreator(p.getNet());
                        if (n2 != null && n2 instanceof HDLNodeAssignment) {
                            HDLNodeAssignment include = (HDLNodeAssignment) n2;
                            if (include.getOutputs().size() == 1 && include.getOutput().getNet().getInputs().size() == 1) {
                                if (allowedToReplaceNet(host.getExpression(), include.getOutput().getNet())) {
                                    nodes.set(i, merge(host, include));
                                    nodes.remove(n2);
                                    wasOptimization = true;
                                    break outer;
                                }
                            }
                        }
                    }
                }
            }
        } while (wasOptimization);
    }

    private boolean allowedToReplaceNet(Expression expression, HDLNet net) {
        final CheckVarRangeVisitor visitor = new CheckVarRangeVisitor(net);
        expression.traverse(visitor);
        return visitor.ok;
    }

    private HDLNodeAssignment merge(HDLNodeAssignment host, HDLNodeAssignment include) {
        final Expression expression = host.getExpression();
        final HDLNet obsoleteNet = include.getOutput().getNet();
        expression.replace(obsoleteNet, include.getExpression());

        HDLNodeAssignment node = new HDLNodeAssignment("merged expression",
                null, name -> host.getOutput().getBits());
        node.setExpression(expression);

        circuit.removeNet(obsoleteNet);

        node.addPort(host.getOutput());
        for (HDLPort i : host.getInputs())
            if (i.getNet() != obsoleteNet)
                node.addPort(i);

        for (HDLPort i : include.getInputs())
            if (!node.hasInput(i))
                node.addPort(i);
            else
                i.getNet().remove(i);

        return node;
    }

    private HDLNode searchCreator(HDLNet net) {
        for (HDLNode n : nodes)
            for (HDLPort p : n.getOutputs())
                if (p.getNet() == net)
                    return n;
        return null;
    }

    private static final class CheckVarRangeVisitor implements Visitor {
        private final HDLNet net;
        private boolean ok;

        private CheckVarRangeVisitor(HDLNet net) {
            this.net = net;
            ok = true;
        }

        @Override
        public void visit(Expression expression) {
            if (expression instanceof ExprVarRange) {
                ExprVarRange evr = (ExprVarRange) expression;
                if (evr.getNet() == net)
                    ok = false;
            }
        }
    }
}

/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

import de.neemann.digital.core.wiring.Splitter;
import de.neemann.digital.hdl.model2.expression.ExprVar;
import de.neemann.digital.hdl.model2.expression.Expression;
import de.neemann.digital.hdl.model2.expression.Visitor;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * The Many to One splitter.
 * A Many to Many splitter is build with a ManyToOne and a OneToMany splitter.
 */
public class HDLNodeSplitterManyToOne extends HDLNode implements Iterable<HDLNodeSplitterManyToOne.SplitterAssignment> {
    private final ArrayList<SplitterAssignment> outputs;

    /**
     * Creates a new instance
     *
     * @param node       the original splitter node
     * @param inputSplit input splitting
     */
    HDLNodeSplitterManyToOne(HDLNode node, Splitter.Ports inputSplit) {
        super(node.getElementName(), node.getElementAttributes(), null);

        outputs = new ArrayList<>();
        int i = 0;
        for (Splitter.Port p : inputSplit) {
            outputs.add(new SplitterAssignment(
                    p.getPos() + p.getBits() - 1,
                    p.getPos(),
                    new ExprVar(node.getInputs().get(i).getNet())));
            i++;
        }

        for (HDLPort p : node.getInputs())
            addPort(p);
        for (HDLPort p : node.getOutputs())
            addPort(p);
    }

    /**
     * @return the targets signal name
     */
    public String getTargetSignal() {
        HDLNet net = getOutput().getNet();
        if (net == null)
            return null;
        return net.getName();
    }

    @Override
    public void print(CodePrinter out) throws IOException {
        super.print(out);
        int i = 0;
        HDLPort o = getOutput();
        for (SplitterAssignment sp : this) {
            out.print(o.getNet().getName());
            outputs.get(i++).print(out);
            out.println();
        }
    }

    @Override
    public void replaceNetByExpression(HDLNet net, Expression expression) {
        for (SplitterAssignment p : this)
            p.replace(net, expression);
    }

    @Override
    public <V extends Visitor> V traverseExpressions(V visitor) {
        for (SplitterAssignment p : this)
            p.traverseExpressions(visitor);
        return visitor;
    }

    @Override
    public Iterator<SplitterAssignment> iterator() {
        return outputs.iterator();
    }

    /**
     * The splitter assignment
     */
    public final static class SplitterAssignment implements Printable {
        private final int msb;
        private final int lsb;
        private Expression expression;

        private SplitterAssignment(int msb, int lsb, Expression expression) {
            this.msb = msb;
            this.lsb = lsb;
            this.expression = expression;
        }

        @Override
        public void print(CodePrinter out) throws IOException {
            out.print("(").print(msb).print("-").print(lsb).print(")").print(" := ");
            expression.print(out);
        }

        /**
         * @return the msb of the assignment
         */
        public int getMsb() {
            return msb;
        }

        /**
         * @return the lsb of the assignment
         */
        public int getLsb() {
            return lsb;
        }

        /**
         * @return the expression to assign
         */
        public Expression getExpression() {
            return expression;
        }

        private void replace(HDLNet net, Expression expr) {
            if (Expression.isVar(expression, net))
                expression = expr;
            else
                expression.replace(net, expr);
        }

        private void traverseExpressions(Visitor visitor) {
            expression.traverse(visitor);
        }
    }
}

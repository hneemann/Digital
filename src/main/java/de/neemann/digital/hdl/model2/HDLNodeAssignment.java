/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;


import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.hdl.model2.expression.ExprVar;
import de.neemann.digital.hdl.model2.expression.Expression;
import de.neemann.digital.hdl.model2.expression.ExpressionOptimizer;
import de.neemann.digital.hdl.model2.expression.Visitor;
import de.neemann.digital.hdl.printer.CodePrinter;

import java.io.IOException;

/**
 * A node which represents a simple expression
 */
public class HDLNodeAssignment extends HDLNode {
    private Expression expression;

    /**
     * Creates a new instace
     *
     * @param elementName       the elements name
     * @param elementAttributes the attributes
     * @param bitProvider       the bit provider which provides the outputs bit width
     */
    public HDLNodeAssignment(String elementName, ElementAttributes elementAttributes, HDLModel.BitProvider bitProvider) {
        super(elementName, elementAttributes, bitProvider);
    }

    /**
     * Sets the expression tu use
     *
     * @param expression the expression
     */
    public void setExpression(Expression expression) {
        this.expression = expression;
    }

    /**
     * @return the expression
     */
    public Expression getExpression() {
        return expression;
    }

    @Override
    public void print(CodePrinter out) throws IOException {
        super.print(out);
        getOutput().getNet().print(out);
        out.print(" := ");
        expression.print(out);
        out.println();
    }

    @Override
    public void replaceNet(HDLNet oldNet, HDLNet newNet) {
        super.replaceNet(oldNet, newNet);
        expression.replace(oldNet, new ExprVar(newNet));
    }

    @Override
    public <V extends Visitor> V traverseExpressions(V visitor) {
        expression.traverse(visitor);
        return visitor;
    }

    @Override
    public void replaceNetByExpression(HDLNet net, Expression expression) {
        expression.replace(net, expression);
    }

    /**
     * @return the target net of this expression.
     */
    public HDLNet getTargetNet() {
        return getOutput().getNet();
    }

    /**
     * Optimizes the expression
     *
     * @param eo the optimizer
     */
    public void optimize(ExpressionOptimizer eo) {
        expression = eo.optimize(expression);
    }
}

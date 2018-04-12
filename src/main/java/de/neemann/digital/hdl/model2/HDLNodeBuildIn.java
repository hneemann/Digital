/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.hdl.model2.expression.ExprVar;
import de.neemann.digital.hdl.model2.expression.Expression;
import de.neemann.digital.hdl.model2.expression.Visitor;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A node which represents a build-in component
 */
public class HDLNodeBuildIn extends HDLNode implements Iterable<HDLNodeBuildIn.InputAssignment> {
    private ArrayList<InputAssignment> inputAssignement;

    /**
     * Creates e new instance
     *
     * @param elementName       the instances name
     * @param elementAttributes the attributes
     * @param bitProvider       the bit provider which provides the outputs bit width
     */
    public HDLNodeBuildIn(String elementName, ElementAttributes elementAttributes, HDLModel.BitProvider bitProvider) {
        super(elementName, elementAttributes, bitProvider);
        inputAssignement = new ArrayList<>();
    }

    @Override
    public void replaceNetByExpression(HDLNet net, Expression expression) {
        for (InputAssignment ia : inputAssignement)
            ia.replaceNetByExpression(net, expression);
    }

    @Override
    public <V extends Visitor> V traverseExpressions(V visitor) {
        for (InputAssignment ia : this)
            ia.traverseExpressions(visitor);
        return visitor;
    }

    HDLNode createExpressions() {
        for (HDLPort in : getInputs())
            inputAssignement.add(new InputAssignment(in.getName(), new ExprVar(in.getNet())));

        return this;
    }

    @Override
    public Iterator<InputAssignment> iterator() {
        if (getInputs().size() != inputAssignement.size())
            throw new RuntimeException("no expressions created for " + getElementName());

        return inputAssignement.iterator();
    }

    @Override
    public void rename(HDLModel.Renaming renaming) {
        super.rename(renaming);
        for (InputAssignment in : inputAssignement)
            in.rename(renaming);
    }

    /**
     * A port assignment.
     * Connects a port to an expression
     */
    public static final class InputAssignment {
        private String name;
        private Expression expression;

        private InputAssignment(String name, Expression expression) {
            this.name = name;
            this.expression = expression;
        }

        /**
         * @return the targtet signal name
         */
        public String getTargetName() {
            return name;
        }

        /**
         * @return the expression to assign
         */
        public Expression getExpression() {
            return expression;
        }

        private void replaceNetByExpression(HDLNet net, Expression expr) {
            if (Expression.isVar(expression, net))
                expression = expr;
            else
                expr.replace(net, expr);
        }

        private void rename(HDLModel.Renaming renaming) {
            name = renaming.checkName(name);
        }

        private  <V extends Visitor> void traverseExpressions(V visitor) {
            expression.traverse(visitor);
        }
    }
}

package de.neemann.digital.draw.builder;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Not;
import de.neemann.digital.analyse.expression.Operation;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.core.basic.And;
import de.neemann.digital.core.basic.Or;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.Main;

import javax.swing.*;
import java.util.ArrayList;

/**
 * @author hneemann
 */
public class Builder {

    private final Expression expression;
    private ShapeFactory shapeFactory;

    public Builder(Expression expression, ShapeFactory shapeFactory) {
        this.expression = expression;
        this.shapeFactory = shapeFactory;
    }

    public Circuit createCircuit() {
        Fragment fr = createFragment(expression);
        fr.setPos(new Vector(0, 0));
        fr.doLayout();
        Circuit circuit = new Circuit();
        fr.addToCircuit(new Vector(0, 0), circuit);
        return circuit;
    }

    private Fragment createFragment(Expression expression) {
        if (expression instanceof Operation) {
            Operation op = (Operation) expression;
            ArrayList<Fragment> frags = new ArrayList<>();
            for (Expression exp : op.getExpressions())
                frags.add(createFragment(exp));

            if (op instanceof Operation.And)
                return new FragmentExpression(frags, new FragmentVisualElement(And.DESCRIPTION, frags.size(), shapeFactory));
            else if (op instanceof Operation.Or)
                return new FragmentExpression(frags, new FragmentVisualElement(Or.DESCRIPTION, frags.size(), shapeFactory));
            else
                throw new RuntimeException("nyi");
        } else if (expression instanceof Not) {
            Not n = (Not) expression;
            ArrayList<Fragment> frags = new ArrayList<>();
            frags.add(createFragment(n.getExpression()));
            return new FragmentExpression(frags, new FragmentVisualElement(de.neemann.digital.core.basic.Not.DESCRIPTION, frags.size(), shapeFactory));
        } else if (expression instanceof Variable) {
            return new FragmentVariable(((Variable) expression));
        } else
            throw new RuntimeException("nyi");
    }

    public static void main(String[] args) {
        Variable a = new Variable("A");
        Variable b = new Variable("B");
        Variable c = new Variable("C");
        Expression e = Operation.or(Not.not(Operation.and(a, Not.not(b))), Operation.and(a, Not.not(c)), Operation.and(b, Not.not(c)));
        Builder builder = new Builder(e, new ShapeFactory(new ElementLibrary()));

        Circuit circuit = builder.createCircuit();
        SwingUtilities.invokeLater(() -> new Main(null, circuit).setVisible(true));
    }
}

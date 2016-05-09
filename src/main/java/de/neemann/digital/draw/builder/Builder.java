package de.neemann.digital.draw.builder;

import de.neemann.digital.analyse.expression.*;
import de.neemann.digital.core.basic.And;
import de.neemann.digital.core.basic.Or;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.Main;

import javax.swing.*;
import java.util.ArrayList;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * @author hneemann
 */
public class Builder {

    private final Circuit circuit;
    private final VariableVisitor variableVisitor;
    private final ShapeFactory shapeFactory;
    private int pos;

    public Builder(ShapeFactory shapeFactory) {
        this.shapeFactory = shapeFactory;
        circuit = new Circuit();
        variableVisitor = new VariableVisitor();
    }

    public Builder addCircuit(String name, Expression expression) {
        Fragment fr = createFragment(expression);

        fr = new FragmentExpression(fr, new FragmentVisualElement(Out.DESCRIPTION, shapeFactory).setAttr(Keys.LABEL, name));

        fr.setPos(new Vector(0, 0));
        Box b = fr.doLayout();

        fr.addToCircuit(new Vector(0, pos), circuit);
        pos += b.getHeight() + SIZE;

        expression.traverse(variableVisitor);

        return this;
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
            return new FragmentExpression(createFragment(n.getExpression()), new FragmentVisualElement(de.neemann.digital.core.basic.Not.DESCRIPTION, shapeFactory));
        } else if (expression instanceof Variable) {
            return new FragmentVariable(((Variable) expression));
        } else
            throw new RuntimeException("nyi");
    }

    public Circuit getCircuit() {
        return circuit;
    }

    public static void main(String[] args) {
        Variable a = new Variable("A");
        Variable b = new Variable("B");
        Variable c = new Variable("C");
        Expression y = Operation.or(Not.not(Operation.and(a, Not.not(b), c)), Operation.and(Not.not(a), c), Operation.and(b, Not.not(c)));
        Expression y1 = Operation.or(Not.not(Operation.and(a, Not.not(b), c)), Operation.and(Not.not(a), c), Operation.and(b, Not.not(c)), Operation.and(b, Not.not(c)));

        Expression l = Operation.and(y, y1, a);

        Builder builder = new Builder(new ShapeFactory(new ElementLibrary()));

        Circuit circuit = builder
                .addCircuit("L", l)
                .addCircuit("Y", y)
                .getCircuit();
        SwingUtilities.invokeLater(() -> new Main(null, circuit).setVisible(true));
    }

}

package de.neemann.digital.draw.builder;

import de.neemann.digital.analyse.expression.*;
import de.neemann.digital.core.basic.And;
import de.neemann.digital.core.basic.NAnd;
import de.neemann.digital.core.basic.NOr;
import de.neemann.digital.core.basic.Or;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.Rotation;
import de.neemann.digital.core.io.Const;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.Main;
import de.neemann.digital.lang.Lang;

import javax.swing.*;
import java.util.ArrayList;
import java.util.HashMap;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;
import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * Builder to create a circuit from an expression
 *
 * @author hneemann
 */
public class Builder {

    private final Circuit circuit;
    private final VariableVisitor variableVisitor;
    private final ShapeFactory shapeFactory;
    private int pos;
    private ArrayList<FragmentVariable> fragmentVariables;

    /**
     * Creates a new builder
     *
     * @param shapeFactory ShapeFactory used ti set to the created VisualElements
     */
    public Builder(ShapeFactory shapeFactory) {
        this.shapeFactory = shapeFactory;
        circuit = new Circuit();
        variableVisitor = new VariableVisitor();
        fragmentVariables = new ArrayList<>();
    }

    /**
     * Adds an expression to the circuit
     *
     * @param name       the output name
     * @param expression the expression
     * @return this for chained calls
     * @throws BuilderException BuilderException
     */
    public Builder addExpression(String name, Expression expression) throws BuilderException {
        Fragment fr = createFragment(expression);

        fr = new FragmentExpression(fr, new FragmentVisualElement(Out.DESCRIPTION, shapeFactory).setAttr(Keys.LABEL, name));

        fr.setPos(new Vector(0, 0));
        Box b = fr.doLayout();

        fr.addToCircuit(new Vector(0, pos), circuit);
        pos += b.getHeight() + SIZE * 2;

        expression.traverse(variableVisitor);

        return this;
    }

    private Fragment createFragment(Expression expression) throws BuilderException {
        if (expression instanceof Operation) {
            Operation op = (Operation) expression;
            ArrayList<Fragment> frags = getOperationFragments(op);
            if (op instanceof Operation.And)
                return new FragmentExpression(frags, new FragmentVisualElement(And.DESCRIPTION, frags.size(), shapeFactory));
            else if (op instanceof Operation.Or)
                return new FragmentExpression(frags, new FragmentVisualElement(Or.DESCRIPTION, frags.size(), shapeFactory));
            else
                throw new BuilderException(Lang.get("err_builder_operationNotSupported", op.getClass().getSimpleName()));
        } else if (expression instanceof Not) {
            Not n = (Not) expression;
            if (n.getExpression() instanceof Variable) {
                FragmentVariable fragmentVariable = new FragmentVariable((Variable) n.getExpression(), true);
                fragmentVariables.add(fragmentVariable);
                return fragmentVariable;
            } else if (n.getExpression() instanceof Operation.And) {
                ArrayList<Fragment> frags = getOperationFragments((Operation) n.getExpression());
                return new FragmentExpression(frags, new FragmentVisualElement(NAnd.DESCRIPTION, frags.size(), shapeFactory));
            } else if (n.getExpression() instanceof Operation.Or) {
                ArrayList<Fragment> frags = getOperationFragments((Operation) n.getExpression());
                return new FragmentExpression(frags, new FragmentVisualElement(NOr.DESCRIPTION, frags.size(), shapeFactory));
            }
            return new FragmentExpression(createFragment(n.getExpression()), new FragmentVisualElement(de.neemann.digital.core.basic.Not.DESCRIPTION, shapeFactory));
        } else if (expression instanceof Variable) {
            FragmentVariable fragmentVariable = new FragmentVariable((Variable) expression, false);
            fragmentVariables.add(fragmentVariable);
            return fragmentVariable;
        } else if (expression instanceof Constant) {
            int val = ((Constant) expression).getValue() ? 1 : 0;
            return new FragmentVisualElement(Const.DESCRIPTION, shapeFactory).setAttr(Keys.VALUE, val);
        } else
            throw new BuilderException(Lang.get("err_builder_exprNotSupported", expression.getClass().getSimpleName()));
    }

    private ArrayList<Fragment> getOperationFragments(Operation op) throws BuilderException {
        ArrayList<Fragment> frags = new ArrayList<>();
        for (Expression exp : op.getExpressions())
            frags.add(createFragment(exp));
        return frags;
    }

    private void createInputBus() {
        HashMap<String, Integer> varPos = new HashMap<>();
        int dx = -variableVisitor.getVariables().size() * SIZE * 2;
        pos -= SIZE;
        for (Variable v : variableVisitor.getVariables()) {
            VisualElement visualElement = new VisualElement(In.DESCRIPTION.getName()).setShapeFactory(shapeFactory);
            visualElement.getElementAttributes()
                    .set(Keys.ROTATE, new Rotation(3))
                    .set(Keys.LABEL, v.getIdentifier());
            visualElement.setPos(new Vector(dx, -SIZE * 5));
            circuit.add(visualElement);

            circuit.add(new Wire(new Vector(dx, -SIZE * 5), new Vector(dx, pos)));

            if (isNotNeeded(v.getIdentifier())) {
                visualElement = new VisualElement(de.neemann.digital.core.basic.Not.DESCRIPTION.getName()).setShapeFactory(shapeFactory);
                visualElement.getElementAttributes()
                        .set(Keys.ROTATE, new Rotation(3));
                visualElement.setPos(new Vector(dx + SIZE, -SIZE * 3));
                circuit.add(visualElement);

                circuit.add(new Wire(new Vector(dx, -SIZE * 4), new Vector(dx + SIZE, -SIZE * 4)));
                circuit.add(new Wire(new Vector(dx + SIZE, -SIZE * 3), new Vector(dx + SIZE, -SIZE * 4)));
                circuit.add(new Wire(new Vector(dx + SIZE, -SIZE), new Vector(dx + SIZE, pos)));
            }

            varPos.put(v.getIdentifier(), dx);
            dx += SIZE * 2;
        }

        for (FragmentVariable f : fragmentVariables) {
            Vector p = f.getCircuitPos();
            int in = varPos.get(f.getVariable().getIdentifier());
            if (f.isInvert()) in += SIZE;
            circuit.add(new Wire(p, new Vector(in, p.y)));
        }
    }

    private boolean isNotNeeded(String identifier) {
        for (FragmentVariable fv : fragmentVariables)
            if (fv.isInvert() && fv.getVariable().getIdentifier().equals(identifier))
                return true;

        return false;
    }

    /**
     * Creates the circuit
     *
     * @return the circuit
     */
    public Circuit createCircuit() {
        createInputBus();
        circuit.setNotModified();
        return circuit;
    }

    /**
     * Only used for manual tests
     *
     * @param args args
     * @throws BuilderException BuilderException
     */
    public static void main(String[] args) throws BuilderException {

        Variable a = new Variable("a");
        Variable b = new Variable("b");
        Variable c = new Variable("c");
        Variable d = new Variable("d");

        Expression y0 = or(a, b);
        Expression y1 = and(a, b, b, not(or(not(and(c, not(b))), and(c, d))));
        Expression y2 = or(and(a, not(b)), and(c, d));
        Expression y3 = or(and(a, b), and(c, d), and(c, d));
        Expression y4 = or(and(not(a), b), and(b, c), and(c, d), and(b, c));
        Expression y5 = or(and(a, b), and(b, not(c)), and(c, d), and(b, c), and(b, c));
        Expression y6 = or(and(a, b), and(b, c), and(c, not(d)), and(b, c), and(b, c), and(b, c));

        Circuit circuit = new Builder(new ShapeFactory(new ElementLibrary()))
                .addExpression("Y_0", y0)
                .addExpression("Y_1", y1)
//                .addExpression("Y_2", y2)
//                .addExpression("Y_3", y3)
//                .addExpression("Y_4", y4)
//                .addExpression("Y_5", y5)
                .addExpression("Y_6", y6)
                .createCircuit();

        SwingUtilities.invokeLater(() -> new Main(null, circuit).setVisible(true));
    }


}

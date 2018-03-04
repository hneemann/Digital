/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.circuit;

import de.neemann.digital.analyse.DetermineJKStateMachine;
import de.neemann.digital.analyse.ModelAnalyserInfo;
import de.neemann.digital.analyse.expression.*;
import de.neemann.digital.analyse.expression.Not;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.builder.BuilderException;
import de.neemann.digital.builder.BuilderInterface;
import de.neemann.digital.core.basic.*;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.Rotation;
import de.neemann.digital.core.flipflops.FlipflopD;
import de.neemann.digital.core.flipflops.FlipflopJK;
import de.neemann.digital.core.io.Const;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.core.wiring.Splitter;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Tunnel;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.lang.Lang;

import java.util.*;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * Builder to create a circuit from a set of expressions.
 * Is also able to create a finite state machine.
 */
public class CircuitBuilder implements BuilderInterface<CircuitBuilder> {

    private final VariableVisitor variableVisitor;
    private final ShapeFactory shapeFactory;
    private final ArrayList<FragmentVariable> fragmentVariables;
    private final ArrayList<Fragment> fragments;
    private final HashMap<String, FragmentVisualElement> combinatorialOutputs;
    private final ArrayList<Variable> sequentialVars;
    private final ArrayList<FragmentVisualElement> flipflops;
    private final boolean useJKff;
    private final ArrayList<Variable> desiredVarOrdering;
    private int pos;
    private HashSet<String> varsToNet;
    private ModelAnalyserInfo mis;

    /**
     * Creates a new builder.
     * Creates a state machine with D flip-flops
     *
     * @param shapeFactory ShapeFactory which is set to the created VisualElements
     */
    public CircuitBuilder(ShapeFactory shapeFactory) {
        this(shapeFactory, false);
    }

    /**
     * Creates a new builder.
     *
     * @param shapeFactory ShapeFactory which is set to the created VisualElements
     * @param useJKff      true if JK flip-flops should be used to create state machines instead of D flip-flops.
     */
    public CircuitBuilder(ShapeFactory shapeFactory, boolean useJKff) {
        this(shapeFactory, useJKff, null);
    }

    /**
     * Creates a new builder.
     *
     * @param shapeFactory ShapeFactory which is set to the created VisualElements
     * @param useJKff      true if JK flip-flops should be used to create state machines instead of D flip-flops.
     * @param varOrdering  the desired ordering of the variables, There may be more variables than required. Maybe null.
     */
    public CircuitBuilder(ShapeFactory shapeFactory, boolean useJKff, ArrayList<Variable> varOrdering) {
        this.shapeFactory = shapeFactory;
        this.useJKff = useJKff;
        desiredVarOrdering = varOrdering;
        variableVisitor = new VariableVisitor();
        fragmentVariables = new ArrayList<>();
        fragments = new ArrayList<>();
        flipflops = new ArrayList<>();
        combinatorialOutputs = new HashMap<>();
        sequentialVars = new ArrayList<>();
        varsToNet = new HashSet<>();
    }

    /**
     * Adds an expression to the circuit
     *
     * @param name       the output name
     * @param expression the expression
     * @return this for chained calls
     * @throws BuilderException BuilderException
     */
    @Override
    public CircuitBuilder addCombinatorial(String name, Expression expression) throws BuilderException {
        if (expression instanceof NamedExpression) {
            name = ((NamedExpression) expression).getName();
            expression = ((NamedExpression) expression).getExpression();
        }

        Fragment fr = createFragment(expression);

        final FragmentVisualElement frag = new FragmentVisualElement(Out.DESCRIPTION, shapeFactory).setAttr(Keys.LABEL, name);
        checkPinNumber(frag.getVisualElement());

        combinatorialOutputs.put(name, frag);

        fragments.add(new FragmentExpression(fr, frag));
        expression.traverse(variableVisitor);
        return this;
    }

    /**
     * Add a transition function of a state machine
     *
     * @param name       name of the state
     * @param expression the expression describing next state
     * @return this for chained calls
     * @throws BuilderException BuilderException
     */
    @Override
    public CircuitBuilder addSequential(String name, Expression expression) throws BuilderException {
        boolean useDff = true;
        if (useJKff) {
            try {
                DetermineJKStateMachine jk = new DetermineJKStateMachine(name, expression);
                useDff = jk.isDFF();
                if (!useDff) {
                    boolean isJequalK = new Equals(jk.getSimplifiedJ(), jk.getSimplifiedK()).isEqual();
                    if (isJequalK) {
                        Fragment frJ = createFragment(jk.getSimplifiedJ());
                        FragmentVisualElement ff = new FragmentVisualElement(FlipflopJK.DESCRIPTION, shapeFactory).ignoreInput(1).setAttr(Keys.LABEL, name);
                        flipflops.add(ff);
                        FragmentSameInValue fsv = new FragmentSameInValue(ff);
                        FragmentExpression fe = new FragmentExpression(fsv, new FragmentVisualElement(Tunnel.DESCRIPTION, shapeFactory).setAttr(Keys.NETNAME, name));
                        fragments.add(new FragmentExpression(frJ, fe));
                    } else {
                        Fragment frJ = createFragment(jk.getSimplifiedJ());
                        Fragment frK = createFragment(jk.getSimplifiedK());
                        FragmentVisualElement ff = new FragmentVisualElement(FlipflopJK.DESCRIPTION, shapeFactory).ignoreInput(1).setAttr(Keys.LABEL, name);
                        flipflops.add(ff);
                        FragmentExpression fe = new FragmentExpression(ff, new FragmentVisualElement(Tunnel.DESCRIPTION, shapeFactory).setAttr(Keys.NETNAME, name));
                        fragments.add(new FragmentExpression(Arrays.asList(frJ, frK), fe));
                    }
                }
            } catch (ExpressionException | FormatterException e) {
                throw new BuilderException(e.getMessage());
            }
        }
        if (useDff) {
            Fragment fr = createFragment(expression);
            FragmentVisualElement ff = new FragmentVisualElement(FlipflopD.DESCRIPTION, shapeFactory).setAttr(Keys.LABEL, name);
            flipflops.add(ff);
            FragmentExpression fe = new FragmentExpression(ff, new FragmentVisualElement(Tunnel.DESCRIPTION, shapeFactory).setAttr(Keys.NETNAME, name));
            fragments.add(new FragmentExpression(fr, fe));
        }
        expression.traverse(variableVisitor);
        sequentialVars.add(new Variable(name));
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
            else if (op instanceof Operation.XOr)
                return new FragmentExpression(frags, new FragmentVisualElement(XOr.DESCRIPTION, frags.size(), shapeFactory));
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
            } else if (n.getExpression() instanceof Operation.XOr) {
                ArrayList<Fragment> frags = getOperationFragments((Operation) n.getExpression());
                return new FragmentExpression(frags, new FragmentVisualElement(XNOr.DESCRIPTION, frags.size(), shapeFactory));
            }
            return new FragmentExpression(createFragment(n.getExpression()), new FragmentVisualElement(de.neemann.digital.core.basic.Not.DESCRIPTION, shapeFactory));
        } else if (expression instanceof Variable) {
            FragmentVariable fragmentVariable = new FragmentVariable((Variable) expression, false);
            fragmentVariables.add(fragmentVariable);
            return fragmentVariable;
        } else if (expression instanceof Constant) {
            long val = 0;
            if (((Constant) expression).getValue())
                val = 1;
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

    private void createInputBus(Collection<Variable> inputs, Circuit circuit) {
        HashMap<String, Integer> varPos = new HashMap<>();
        int dx = -inputs.size() * SIZE * 2;
        pos -= SIZE;
        for (Variable v : inputs) {
            VisualElement visualElement;
            if (sequentialVars.contains(v) || varsToNet.contains(v.getIdentifier())) {
                visualElement = new VisualElement(Tunnel.DESCRIPTION.getName()).setShapeFactory(shapeFactory);
                visualElement.getElementAttributes()
                        .set(Keys.ROTATE, new Rotation(1))
                        .set(Keys.NETNAME, v.getIdentifier());
            } else {
                visualElement = new VisualElement(In.DESCRIPTION.getName()).setShapeFactory(shapeFactory);
                visualElement.getElementAttributes()
                        .set(Keys.ROTATE, new Rotation(3))
                        .set(Keys.LABEL, v.getIdentifier());
                checkPinNumber(visualElement);
            }
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

    private void addFragmentToCircuit(Fragment fr, Circuit circuit) {
        fr.setPos(new Vector(0, 0));
        Box b = fr.doLayout();

        fr.addToCircuit(new Vector(0, pos), circuit);
        pos += b.getHeight() + SIZE * 2;
    }

    /**
     * Creates the circuit
     *
     * @return the circuit
     */
    public Circuit createCircuit() {
        // determine maximum width
        int maxWidth = 0;
        for (Fragment f : fragments) {
            Box b = f.doLayout();
            if (maxWidth < b.getWidth()) maxWidth = b.getWidth();
        }
        // add space for clock wire!
        if (!sequentialVars.isEmpty())
            maxWidth += SIZE * 2;

        // set width to fragments
        for (Fragment f : fragments) {
            if (f instanceof FragmentExpression)
                ((FragmentExpression) f).setWidth(maxWidth);
        }

        Circuit circuit = new Circuit();

        int outSplitterY = 0;
        if (mis != null)
            outSplitterY = checkForOutputBus(maxWidth + SIZE * 15, circuit);

        // add fragments to circuit
        for (Fragment f : fragments)
            addFragmentToCircuit(f, circuit);

        // order bus variables
        Collection<Variable> variables = variableVisitor.getVariables();
        if (desiredVarOrdering != null)
            variables = order(variables, desiredVarOrdering);
        if (!sequentialVars.isEmpty())
            variables = order(variables, sequentialVars);

        if (mis != null)
            checkForInputBus(variables, -(variables.size() + 5) * SIZE * 2, circuit);

        createInputBus(variables, circuit);

        // add clock
        if (!flipflops.isEmpty())
            addClockToFlipFlops(circuit);

        if (combinatorialOutputs.isEmpty())
            addNetConnections(circuit, maxWidth + SIZE * 17, outSplitterY);

        circuit.setModified(false);
        return circuit;
    }

    private void checkForInputBus(Collection<Variable> variables, int splitterXPos, Circuit circuit) {
        StringBuilder pinString = new StringBuilder();
        int y = 0;
        for (Map.Entry<String, ArrayList<String>> e : mis.getInputBusMap().entrySet()) {
            pinString.setLength(0);
            int found = 0;
            final ArrayList<String> inputs = e.getValue();
            for (String n : inputs) {
                if (variables.contains(new Variable(n))) {
                    found++;
                    String p = mis.getPins().get(n);
                    if (p != null) {
                        if (pinString.length() != 0)
                            pinString.append(",");
                        pinString.append(p);
                    }
                }
            }
            if (found == inputs.size()) {
                varsToNet.addAll(inputs);

                circuit.add(new VisualElement(Splitter.DESCRIPTION.getName())
                        .setAttribute(Keys.INPUT_SPLIT, "" + inputs.size())
                        .setAttribute(Keys.OUTPUT_SPLIT, "1*" + inputs.size())
                        .setPos(new Vector(splitterXPos, y))
                        .setShapeFactory(shapeFactory));
                circuit.add(new VisualElement(In.DESCRIPTION.getName())
                        .setAttribute(Keys.LABEL, e.getKey())
                        .setAttribute(Keys.BITS, inputs.size())
                        .setAttribute(Keys.PINNUMBER, pinString.toString())
                        .setPos(new Vector(splitterXPos - 2 * SIZE, y))
                        .setShapeFactory(shapeFactory));
                circuit.add(new Wire(
                        new Vector(splitterXPos - 2 * SIZE, y),
                        new Vector(splitterXPos, y)
                ));

                for (int i = 0; i < inputs.size(); i++) {
                    circuit.add(new VisualElement(Tunnel.DESCRIPTION.getName())
                            .setAttribute(Keys.NETNAME, inputs.get(i))
                            .setPos(new Vector(splitterXPos + 2 * SIZE, y + i * SIZE))
                            .setShapeFactory(shapeFactory));
                    circuit.add(new Wire(
                            new Vector(splitterXPos + SIZE, y + i * SIZE),
                            new Vector(splitterXPos + 2 * SIZE, y + i * SIZE)
                    ));
                }

                y += (inputs.size() + 2) * SIZE;
            }
        }
    }

    private int checkForOutputBus(int splitterXPos, Circuit circuit) {
        StringBuilder pinString = new StringBuilder();
        int y = 0;
        for (Map.Entry<String, ArrayList<String>> e : mis.getOutputBusMap().entrySet()) {
            pinString.setLength(0);
            int found = 0;
            final ArrayList<String> outputs = e.getValue();
            for (String n : outputs) {
                if (combinatorialOutputs.containsKey(n)) {
                    found++;
                    String p = mis.getPins().get(n);
                    if (p != null) {
                        if (pinString.length() != 0)
                            pinString.append(",");
                        pinString.append(p);
                    }
                }
            }
            if (found == outputs.size()) {

                circuit.add(new VisualElement(Splitter.DESCRIPTION.getName())
                        .setAttribute(Keys.OUTPUT_SPLIT, "" + outputs.size())
                        .setAttribute(Keys.INPUT_SPLIT, "1*" + outputs.size())
                        .setPos(new Vector(splitterXPos, y))
                        .setShapeFactory(shapeFactory));
                circuit.add(new VisualElement(Out.DESCRIPTION.getName())
                        .setAttribute(Keys.LABEL, e.getKey())
                        .setAttribute(Keys.BITS, outputs.size())
                        .setAttribute(Keys.PINNUMBER, pinString.toString())
                        .setPos(new Vector(splitterXPos + 3 * SIZE, y))
                        .setShapeFactory(shapeFactory));
                circuit.add(new Wire(
                        new Vector(splitterXPos + 3 * SIZE, y),
                        new Vector(splitterXPos + SIZE, y)
                ));

                for (int i = 0; i < outputs.size(); i++) {
                    circuit.add(new VisualElement(Tunnel.DESCRIPTION.getName())
                            .setAttribute(Keys.NETNAME, outputs.get(i))
                            .setRotation(2)
                            .setPos(new Vector(splitterXPos - SIZE, y + i * SIZE))
                            .setShapeFactory(shapeFactory));
                    circuit.add(new Wire(
                            new Vector(splitterXPos - SIZE, y + i * SIZE),
                            new Vector(splitterXPos, y + i * SIZE)
                    ));

                    FragmentVisualElement frag = combinatorialOutputs.get(outputs.get(i));
                    frag.setVisualElement(new VisualElement(Tunnel.DESCRIPTION.getName())
                            .setShapeFactory(shapeFactory)
                            .setAttribute(Keys.NETNAME, outputs.get(i)));
                }

                y += (outputs.size() + 2) * SIZE;
            }
        }
        return y;
    }


    /**
     * Move the lastItems to the end of the variables list.
     * Items which are not in lastItems are placed at the beginning of the result list
     *
     * @param variables the variables to order
     * @param lastItems the items to be placed at the end of the list
     * @return the ordered list
     */
    private static <T> ArrayList<T> order(Collection<T> variables, ArrayList<T> lastItems) {
        ArrayList<T> vars = new ArrayList<>(variables);
        for (T seq : lastItems)
            if (vars.contains(seq)) {
                vars.remove(seq); // move to end
                vars.add(seq);
            }
        return vars;
    }

    private void addClockToFlipFlops(Circuit circuit) {
        int x = Integer.MAX_VALUE;
        int yMin = Integer.MAX_VALUE;
        int yMax = Integer.MIN_VALUE;
        for (FragmentVisualElement ff : flipflops) {
            Vector p = ff.getVisualElement().getPos();
            if (p.x < x) x = p.x;
            if (p.y < yMin) yMin = p.y;
            if (p.y > yMax) yMax = p.y;
        }
        x -= SIZE;
        if (useJKff) x -= SIZE;

        int yPos = yMin - SIZE * 3;
        if (useJKff) yPos = -SIZE;

        circuit.add(new Wire(new Vector(x, yPos), new Vector(x, yMax + SIZE)));

        for (FragmentVisualElement ff : flipflops) {
            Vector p = ff.getVisualElement().getPos();
            circuit.add(new Wire(new Vector(x, p.y + SIZE), new Vector(p.x, p.y + SIZE)));
        }

        VisualElement clock = new VisualElement(Clock.DESCRIPTION.getName())
                .setShapeFactory(shapeFactory)
                .setPos(new Vector(x, yPos));
        clock.getElementAttributes()
                .set(Keys.LABEL, "C")
                .set(Keys.ROTATE, new Rotation(3))
                .set(Keys.FREQUENCY, 2)
                .set(Keys.RUN_AT_REAL_TIME, true);
        circuit.add(clock);
    }

    private void addNetConnections(Circuit circuit, int xPos, int y) {
        for (Variable name : sequentialVars) {
            String oName = name.getIdentifier();
            if (oName.endsWith("n")) {
                oName = oName.substring(0, oName.length() - 1);
                if (oName.endsWith("_") || oName.endsWith("^")) oName = oName.substring(0, oName.length() - 1);
            }
            if (!combinatorialOutputs.containsKey(oName)) {
                VisualElement t = new VisualElement(Tunnel.DESCRIPTION.getName()).setShapeFactory(shapeFactory);
                t.getElementAttributes().set(Keys.NETNAME, name.getIdentifier());
                t.setPos(new Vector(xPos, y));
                t.setRotation(2);
                circuit.add(t);
                VisualElement o = new VisualElement(Out.DESCRIPTION.getName()).setShapeFactory(shapeFactory);
                o.getElementAttributes().set(Keys.LABEL, oName);
                o.setPos(new Vector(xPos + SIZE, y));
                checkPinNumber(o);
                circuit.add(o);
                circuit.add(new Wire(new Vector(xPos, y), new Vector(xPos + SIZE, y)));
                y += SIZE * 2;
            }
        }
    }

    private void checkPinNumber(VisualElement pin) {
        if (mis != null) {
            String name = pin.getElementAttributes().getLabel();
            String num = mis.getPins().get(name);
            if (num != null && num.length() > 0) {
                pin.getElementAttributes().set(Keys.PINNUMBER, num);
            }
        }
    }

    /**
     * Sets the infos obtained from the model
     *
     * @param modelAnalyserInfo the model analyzer infos
     * @return this for chained calls
     */
    public CircuitBuilder setModelAnalyzerInfo(ModelAnalyserInfo modelAnalyserInfo) {
        mis = modelAnalyserInfo;
        return this;
    }
}

/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.basic.*;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.core.io.Const;
import de.neemann.digital.core.io.DipSwitch;
import de.neemann.digital.core.io.Ground;
import de.neemann.digital.core.io.VDD;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.hdl.model2.clock.HDLClockIntegrator;
import de.neemann.digital.hdl.model2.expression.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * The context of creating nodes and circuits.
 * Ensures that every circuit is only processed one time.
 */
public class HDLModel implements Iterable<HDLCircuit> {
    private ElementLibrary elementLibrary;
    private HashMap<Circuit, HDLCircuit> circuitMap;
    private HDLCircuit main;
    private Renaming renaming;

    /**
     * Creates a new instance
     *
     * @param elementLibrary the element library
     */
    public HDLModel(ElementLibrary elementLibrary) {
        this.elementLibrary = elementLibrary;
        circuitMap = new HashMap<>();
    }

    /**
     * Creates a isolated node
     *
     * @param v      the VisualElement of the node
     * @param parent the parrents circuit
     * @return the node
     * @throws HDLException HDLException
     */
    public HDLNode createNode(VisualElement v, HDLCircuit parent) throws HDLException {
        try {
            ElementTypeDescription td = elementLibrary.getElementType(v.getElementName());
            if (td instanceof ElementLibrary.ElementTypeDescriptionCustom) {
                ElementLibrary.ElementTypeDescriptionCustom tdc = (ElementLibrary.ElementTypeDescriptionCustom) td;

                HDLCircuit c = circuitMap.get(tdc.getCircuit());
                if (c == null) {
                    c = new HDLCircuit(tdc.getCircuit(), v.getElementName(), this);
                    circuitMap.put(tdc.getCircuit(), c);
                }

                return addInputsOutputs(
                        new HDLNodeCustom(v.getElementName(), v.getElementAttributes(), c),
                        v, parent).createExpressions();

            } else if (v.equalsDescription(Const.DESCRIPTION)) {
                final HDLNodeAssignment node = createExpression(v, parent, td);
                node.setExpression(new ExprConstant(node.getElementAttributes().get(Keys.VALUE), node.getOutput().getBits()));
                return node;
            } else if (v.equalsDescription(DipSwitch.DESCRIPTION)) {
                final HDLNodeAssignment node = createExpression(v, parent, td);
                node.setExpression(new ExprConstant(node.getElementAttributes().get(Keys.DIP_DEFAULT) ? 1 : 0, node.getOutput().getBits()));
                return node;
            } else if (v.equalsDescription(Ground.DESCRIPTION)) {
                final HDLNodeAssignment node = createExpression(v, parent, td);
                node.setExpression(new ExprConstant(0, node.getOutput().getBits()));
                return node;
            } else if (v.equalsDescription(VDD.DESCRIPTION)) {
                final HDLNodeAssignment node = createExpression(v, parent, td);
                node.setExpression(new ExprConstant(-1, node.getOutput().getBits()));
                return node;
            } else if (v.equalsDescription(Not.DESCRIPTION)) {
                final HDLNodeAssignment node = createExpression(v, parent, td);
                node.setExpression(new ExprNot(new ExprVar(node.getInputs().get(0).getNet())));
                return node;
            } else if (v.equalsDescription(Or.DESCRIPTION)) {
                final HDLNodeAssignment node = createExpression(v, parent, td);
                node.setExpression(createOperation(node.getInputs(), ExprOperate.Operation.OR));
                return node;
            } else if (v.equalsDescription(And.DESCRIPTION)) {
                final HDLNodeAssignment node = createExpression(v, parent, td);
                node.setExpression(createOperation(node.getInputs(), ExprOperate.Operation.AND));
                return node;
            } else if (v.equalsDescription(XOr.DESCRIPTION)) {
                final HDLNodeAssignment node = createExpression(v, parent, td);
                node.setExpression(createOperation(node.getInputs(), ExprOperate.Operation.XOR));
                return node;
            } else if (v.equalsDescription(NOr.DESCRIPTION)) {
                final HDLNodeAssignment node = createExpression(v, parent, td);
                node.setExpression(new ExprNot(createOperation(node.getInputs(), ExprOperate.Operation.OR)));
                return node;
            } else if (v.equalsDescription(NAnd.DESCRIPTION)) {
                final HDLNodeAssignment node = createExpression(v, parent, td);
                node.setExpression(new ExprNot(createOperation(node.getInputs(), ExprOperate.Operation.AND)));
                return node;
            } else if (v.equalsDescription(XNOr.DESCRIPTION)) {
                final HDLNodeAssignment node = createExpression(v, parent, td);
                node.setExpression(new ExprNot(createOperation(node.getInputs(), ExprOperate.Operation.XOR)));
                return node;
            } else
                return addInputsOutputs(
                        new HDLNodeBuildIn(v.getElementName(),
                                v.getElementAttributes(),
                                new ObservableValuesBitsProvider(
                                        td.createElement(v.getElementAttributes()).getOutputs())),
                        v, parent).createExpressions();


        } catch (ElementNotFoundException | PinException | NodeException e) {
            throw new HDLException("error creating node", e);
        }
    }

    private Expression createOperation(ArrayList<HDLPort> inputs, ExprOperate.Operation op) {
        ArrayList<Expression> list = new ArrayList<>();
        for (HDLPort p : inputs) {
            list.add(new ExprVar(p.getNet()));
        }
        return new ExprOperate(op, list);
    }

    private HDLNodeAssignment createExpression(VisualElement v, HDLCircuit parent, ElementTypeDescription td) throws HDLException, PinException {
        return addInputsOutputs(new HDLNodeAssignment(v.getElementName(),
                        v.getElementAttributes(),
                        new ObservableValuesBitsProvider(
                                td.createElement(v.getElementAttributes()).getOutputs())),
                v, parent);
    }

    private <N extends HDLNode> N addInputsOutputs(N node, VisualElement v, HDLCircuit c) throws HDLException {
        for (Pin p : v.getPins()) {
            HDLNet net = c.getNetOfPin(p);
            if (p.getDirection().equals(PinDescription.Direction.input))
                node.addPort(new HDLPort(p.getName(), net, HDLPort.Direction.IN, 0));
            else
                node.addPort(new HDLPort(p.getName(), net, HDLPort.Direction.OUT, node.getBits(p.getName())));
        }
        return node;
    }


    @Override
    public Iterator<HDLCircuit> iterator() {
        return circuitMap.values().iterator();
    }

    /**
     * Analyses the given circuit
     *
     * @param circuit         the circuit
     * @param clockIntegrator the clock integrator. Meybe null
     * @return this for chained calls
     * @throws PinException  PinException
     * @throws HDLException  HDLException
     * @throws NodeException NodeException
     */
    public HDLModel create(Circuit circuit, HDLClockIntegrator clockIntegrator) throws PinException, HDLException, NodeException {
        main = new HDLCircuit(circuit, "main", this, clockIntegrator);
        circuitMap.put(circuit, main);
        return this;
    }

    /**
     * Renames the signals in this model
     *
     * @param renaming the renaming algorithm
     * @throws HDLException HDLException
     */
    public void renameLabels(Renaming renaming) throws HDLException {
        this.renaming = new RenameSingleCheck(renaming);
        for (HDLCircuit c : circuitMap.values())
            c.rename(this.renaming);
    }

    /**
     * @return the used renaming
     */
    public Renaming getRenaming() {
        return renaming;
    }

    /**
     * The renaming algorithm
     */
    public interface Renaming {
        /**
         * If the given name is a valid you can return the name unchanged.
         * If not, return an appropriate name.
         *
         * @param name the original name
         * @return the modified name
         */
        String checkName(String name);
    }

    static final class RenameSingleCheck implements Renaming {
        private final Renaming parent;
        private final HashMap<String, String> map;

        private RenameSingleCheck(Renaming parent) {
            this.parent = parent;
            map = new HashMap<>();
        }

        @Override
        public String checkName(String name) {
            String n = map.get(name);
            if (n == null) {
                n = parent.checkName(name);
                map.put(name, n);
            }
            return n;
        }
    }

    /**
     * @return the main node
     */
    public HDLCircuit getMain() {
        return main;
    }

    /**
     * The bit provider interface
     */
    public interface BitProvider {
        /**
         * Returns the number of bits of the signal with the given name
         *
         * @param name the signal name
         * @return the number of bits
         */
        int getBits(String name);
    }

    private static final class ObservableValuesBitsProvider implements BitProvider {
        private final ObservableValues values;

        private ObservableValuesBitsProvider(ObservableValues values) {
            this.values = values;
        }

        @Override
        public int getBits(String name) {
            return values.get(name).getBits();
        }
    }
}

/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.circuit;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.core.flipflops.FlipflopD;
import de.neemann.digital.core.flipflops.FlipflopJK;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.graphics.GraphicMinMax;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.shapes.ShapeFactory;

import java.util.ArrayList;
import java.util.List;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * A fragment describing a VisualElement
 */
public class FragmentVisualElement implements Fragment {

    private ArrayList<Vector> inputs;
    private ArrayList<Vector> outputs;
    private VisualElement visualElement;
    private Vector pos;

    /**
     * Creates a new instance
     *
     * @param description  the elements description
     * @param shapeFactory the shapeFactory to use
     */
    public FragmentVisualElement(ElementTypeDescription description, ShapeFactory shapeFactory) {
        this(description, 1, shapeFactory);
    }

    /**
     * Creates a new instance
     *
     * @param description  the elements description
     * @param inputCount   number of inputs
     * @param shapeFactory the shapeFactory to use
     */
    public FragmentVisualElement(ElementTypeDescription description, int inputCount, ShapeFactory shapeFactory) {
        visualElement = new VisualElement(description.getName()).setShapeFactory(shapeFactory);
        visualElement.getElementAttributes().set(Keys.INPUT_COUNT, inputCount);
    }

    private void checkInOutPresent() {
        if (inputs == null || outputs == null) {
            Pins pins = visualElement.getShape().getPins();
            inputs = new ArrayList<>();
            outputs = new ArrayList<>();
            for (Pin p : pins) {
                if (p.getDirection().equals(PinDescription.Direction.input))
                    inputs.add(p.getPos());
                else
                    outputs.add(p.getPos());
            }
        }
    }

    /**
     * Used to ignore the c input at a JK flipflop
     *
     * @param n number of pin to ignore
     * @return this for call chaining
     */
    public FragmentVisualElement ignoreInput(int n) {
        checkInOutPresent();
        inputs.remove(n);
        return this;
    }

    /**
     * Sets an attribute to this VisualElement
     *
     * @param key     the key
     * @param value   the value
     * @param <VALUE> the type of the value
     * @return this for call chaining
     */
    public <VALUE> FragmentVisualElement setAttr(Key<VALUE> key, VALUE value) {
        visualElement.getElementAttributes().set(key, value);
        return this;
    }

    @Override
    public Box doLayout() {
        checkInOutPresent();
        GraphicMinMax mm = new GraphicMinMax();
        for (Vector p : inputs)
            mm.check(p);
        for (Vector p : outputs)
            mm.check(p);
        Vector delta = mm.getMax().sub(mm.getMin());
        if (visualElement.equalsDescription(FlipflopJK.DESCRIPTION)
                || visualElement.equalsDescription(FlipflopD.DESCRIPTION))
            return new Box(delta.x, delta.y + SIZE);   // Space for label
        else
            return new Box(delta.x, delta.y);
    }

    @Override
    public void setPos(Vector pos) {
        this.pos = pos;
    }

    @Override
    public void addToCircuit(Vector offset, Circuit circuit) {
        visualElement.setPos(pos.add(offset));
        circuit.add(visualElement);
    }

    @Override
    public List<Vector> getInputs() {
        checkInOutPresent();
        return Vector.add(inputs, pos);
    }

    @Override
    public List<Vector> getOutputs() {
        checkInOutPresent();
        return Vector.add(outputs, pos);
    }

    /**
     * @return the VisualElement contained in this fragment
     */
    public VisualElement getVisualElement() {
        return visualElement;
    }

    /**
     * Sets the visual element
     *
     * @param visualElement the visual element to set
     */
    public void setVisualElement(VisualElement visualElement) {
        this.visualElement = visualElement;
    }

    @Override
    public <V extends FragmentVisitor> V traverse(V v) {
        v.visit(this);
        return v;
    }
}

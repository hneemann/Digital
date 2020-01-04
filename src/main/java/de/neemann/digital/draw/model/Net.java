/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.model;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.core.wiring.bus.DataBus;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.lang.Lang;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Contains all pins which are connected together.
 * Is created and filled by the ModelDescription constructor.
 * After creation all the ObservableValues belonging to the outputs are set.
 */
public class Net {

    private final HashSet<Vector> points;
    private final ArrayList<Pin> pins;
    private final ArrayList<Wire> wires;
    private final HashSet<String> labelSet;
    private File origin;
    private VisualElement visualElement; // only used to create better error messages

    /**
     * Creates a copy of the given net
     *
     * @param toCopy        the net to copy
     * @param visualElement the containing visual element, only used to create better error messages
     */
    public Net(Net toCopy, VisualElement visualElement) {
        points = toCopy.points;  // no deep copy of points necessary
        this.visualElement = visualElement;
        wires = null;            // wires not needed
        pins = new ArrayList<>(toCopy.pins); // Pins are changed so create a deep copy
        labelSet = new HashSet<>(toCopy.labelSet); //ToDo copy necessary?
        origin = toCopy.origin;
    }

    /**
     * Creates a net containing a single wire
     *
     * @param w the wire
     */
    public Net(Wire w) {
        points = new HashSet<>();
        points.add(w.p1);
        points.add(w.p2);
        pins = new ArrayList<>();
        wires = new ArrayList<>();
        wires.add(w);
        labelSet = new HashSet<>();
    }

    /**
     * Creates a single point net
     *
     * @param v the vector containing the points coordinates
     */
    public Net(Vector v) {
        points = new HashSet<>();
        points.add(v);
        pins = new ArrayList<>();
        wires = null;
        labelSet = new HashSet<>();
    }

    /**
     * Tries to add the given wire to this net
     *
     * @param wire the wire
     * @return true if the given wire is connected to one of the old wires.
     */
    public Vector tryMerge(Wire wire) {
        if (points.contains(wire.p1)) {
            wires.add(wire);
            points.add(wire.p2);
            return wire.p2;
        }
        if (points.contains(wire.p2)) {
            wires.add(wire);
            points.add(wire.p1);
            return wire.p1;
        }
        return null;
    }

    /**
     * Checks if the given position is part of this net
     *
     * @param vector the position
     * @return true if vector matches a wire end point
     */
    public boolean contains(Vector vector) {
        return points.contains(vector);
    }

    /**
     * Add all wires of the given net to this net
     *
     * @param changedNet the net to add
     */
    void addAllPointsFrom(Net changedNet) {
        points.addAll(changedNet.points);
        wires.addAll(changedNet.wires);
        labelSet.addAll(changedNet.labelSet);
    }

    /**
     * Add a pin to this net
     *
     * @param pin the pin to add
     */
    public void add(Pin pin) {
        pins.add(pin);
    }

    /**
     * Add all given pins to the net
     *
     * @param p the pins
     */
    public void addAll(Collection<Pin> p) {
        pins.addAll(p);
    }

    /**
     * Add all given pins to the net
     *
     * @param otherNet the other net
     */
    public void addNet(Net otherNet) {
        pins.addAll(otherNet.getPins());
        if (wires != null && otherNet.getWires() != null)
            wires.addAll(otherNet.getWires());
        labelSet.addAll(otherNet.labelSet);
    }

    /**
     * Do the interconnection.
     * All inputs and outputs in the net are connected together.
     * If there is no output an exception is thrown.
     * If there is one single output, all input {@link ObservableValue}s are set to this output
     * If there is more than one output, a {@link DataBus} is created.
     * <p>
     * At the end all wires get a reference to the {@link ObservableValue} the represent
     *
     * @param m           the model is needed to create the {@link DataBus}
     * @param attachWires if true, the values are attached to the wires
     * @throws PinException PinException
     */
    public void interconnect(Model m, boolean attachWires) throws PinException {
        ArrayList<Pin> inputs = new ArrayList<>();
        ArrayList<Pin> outputs = new ArrayList<>();
        for (Pin p : pins) {
            if (p.getDirection() == Pin.Direction.input)
                inputs.add(p);
            else
                outputs.add(p);
        }

        if (outputs.size() == 0)
            throw new PinException(Lang.get("err_noOutConnectedToWire", this.toString()), this);

        ObservableValue value = null;
        if (outputs.size() == 1 && outputs.get(0).getPullResistor() == PinDescription.PullResistor.none) {
            value = outputs.get(0).getValue();
        } else {
            value = new DataBus(this, m, outputs).getReadableOutput();
        }

        if (value == null)
            throw new PinException(Lang.get("err_output_N_notDefined", outputs.get(0)), this);

        for (Pin i : inputs)
            i.setValue(value);

        for (Pin o : outputs)  // set also the reader for bidirectional pins
            o.setReaderValue(value);

        if (wires != null && attachWires)
            for (Wire w : wires)
                w.setValue(value);
    }

    /**
     * @return the wires building this net
     */
    public ArrayList<Wire> getWires() {
        return wires;
    }

    /**
     * @param p the pin
     * @return true if the given pin belongs to this net
     */
    public boolean containsPin(Pin p) {
        return pins.contains(p);
    }

    /**
     * @return all the pins of this net
     */
    public ArrayList<Pin> getPins() {
        return pins;
    }

    /**
     * Removes a pin from the net
     *
     * @param p the pin to remove
     * @throws PinException is thrown if pin is not present
     */
    public void removePin(Pin p) throws PinException {
        if (!pins.remove(p))
            throw new PinException(Lang.get("err_pinNotPresent"), this);
    }

    /**
     * Adds a label this this net
     *
     * @param label the label to add
     */
    public void addLabel(String label) {
        labelSet.add(label);
    }

    /**
     * Returns true if the given net has at least one same net label.
     *
     * @param net the other net
     * @return true if same net
     */
    public boolean matchesLabel(Net net) {
        for (String l : labelSet) {
            if (net.labelSet.contains(l))
                return true;
        }
        return false;
    }

    @Override
    public String toString() {
        if (labelSet.isEmpty())
            return pins.toString();
        else
            return labelSet + "/" + pins;
    }

    /**
     * Sets the origin of this net
     *
     * @param origin the origin
     */
    public void setOrigin(File origin) {
        this.origin = origin;
    }

    /**
     * @return the origin of this net
     */
    public File getOrigin() {
        return origin;
    }

    /**
     * @return the containing visual element
     */
    public VisualElement getVisualElement() {
        return visualElement;
    }

    /**
     * @return the set of labels attached to this net
     */
    public HashSet<String> getLabels() {
        return labelSet;
    }
}

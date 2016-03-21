package de.neemann.digital.gui.draw.model;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.gui.draw.elements.Pin;
import de.neemann.digital.gui.draw.elements.PinException;
import de.neemann.digital.gui.draw.elements.Wire;
import de.neemann.digital.gui.draw.graphics.Vector;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author hneemann
 */
public class Net {

    private final HashSet<Vector> points;
    private final ArrayList<Pin> pins;
    private final ArrayList<Wire> wires;

    public Net(Wire w) {
        points = new HashSet<>();
        points.add(w.p1);
        points.add(w.p2);
        pins = new ArrayList<>();
        wires = new ArrayList<>();
        wires.add(w);
    }

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

    public boolean contains(Vector vector) {
        return points.contains(vector);
    }

    public void addAllPointsFrom(Net changedNet) {
        points.addAll(changedNet.points);
        wires.addAll(changedNet.wires);
    }

    public void add(Pin pin) {
        pins.add(pin);
    }

    public void interconnect() throws PinException {
        ArrayList<Pin> inputs = new ArrayList<>();
        ArrayList<Pin> outputs = new ArrayList<>();
        for (Pin p : pins) {
            if (p.getDirection() == Pin.Direction.input)
                inputs.add(p);
            else
                outputs.add(p);
        }

        if (outputs.size() == 0)
            throw new PinException("No output connected to wire!", this);

        if (outputs.size() > 1)
            throw new PinException("Multiple outputs not supported yet!", this);

        ObservableValue value = outputs.get(0).getValue();
        if (value == null)
            throw new PinException("Output " + outputs.get(0) + " not defined!", this);

        for (Pin i : inputs)
            i.setValue(value);

        for (Wire w : wires)
            w.setValue(value);
    }

    public void setHighLight(boolean highLight) {
        for (Wire w : wires)
            w.setHighLight(highLight);
    }

    public boolean containsValue(ObservableValue v) {
        for (Pin p : pins)
            if (p.getValue() == v)
                return true;
        return false;
    }
}

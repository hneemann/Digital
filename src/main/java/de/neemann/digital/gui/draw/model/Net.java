package de.neemann.digital.gui.draw.model;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.parts.Pin;
import de.neemann.digital.gui.draw.parts.PinException;
import de.neemann.digital.gui.draw.parts.Wire;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * @author hneemann
 */
public class Net {

    private final HashSet<Vector> points;
    private final ArrayList<Pin> pins;

    public Net(Wire w) {
        points = new HashSet<>();
        points.add(w.p1);
        points.add(w.p2);
        pins = new ArrayList<>();
    }

    public Vector tryMerge(Wire wire) {
        if (points.contains(wire.p1)) {
            points.add(wire.p2);
            return wire.p2;
        }
        if (points.contains(wire.p2)) {
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
            throw new PinException("only inputs connected!");

        if (outputs.size() > 1)
            throw new PinException("multiple outputs not supported yet!");

        ObservableValue value = outputs.get(0).getValue();
        if (value == null)
            throw new PinException("output " + outputs.get(0) + " not defined!");

        for (Pin i : inputs) {
            i.setValue(value);
        }
    }
}

package de.neemann.digital.draw.model;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.wiring.DataBus;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

/**
 * Contains all pins which are connected tigether.
 * Is created and filled by the ModelDescription constructor.
 * After creation all the ObservableValues belonging to the outputs are set.
 *
 * @author hneemann
 */
public class Net {

    private final HashSet<Vector> points;
    private final ArrayList<Pin> pins;
    private final ArrayList<Wire> wires;

    public Net(Net toCopy) {
        points = toCopy.points;  // no deep copy of points necessary
        wires = null;            // wires not needed
        pins = new ArrayList<>(toCopy.pins); // Pins are changed so create a deep copy
    }

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

    public void addAll(Collection<Pin> p) {
        pins.addAll(p);
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
            throw new PinException(Lang.get("err_onOutConnectedToWire"), this);

        ObservableValue value = null;
        if (outputs.size() == 1) {
            value = outputs.get(0).getValue();
        } else {
            value = new DataBus(this, outputs).getReadeableOutput();
        }

        if (value == null)
            throw new PinException(Lang.get("err_output_N_notDefined", outputs.get(0)), this);

        for (Pin i : inputs)
            i.setValue(value);

        for (Pin o : outputs)  // set also the reader for bidirectional pins
            o.setReaderValue(value);

        if (wires != null)
            for (Wire w : wires)
                w.setValue(value);
    }

    public void setHighLight(boolean highLight) {
        if (wires != null)
            for (Wire w : wires)
                w.setHighLight(highLight);
    }

    public boolean containsValue(ObservableValue v) {
        for (Pin p : pins)
            if (p.getValue() == v)
                return true;
        return false;
    }

    public boolean containsPin(Pin p) {
        return pins.contains(p);
    }

    public ArrayList<Pin> getPins() {
        return pins;
    }

    public void removePin(Pin p) throws PinException {
        if (!pins.remove(p))
            throw new PinException(Lang.get("err_pinNotPresent"), this);
    }
}

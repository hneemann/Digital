package de.neemann.digital.gui.draw.elements;

import de.neemann.digital.core.ObservableValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author hneemann
 */
public class Pins implements Iterable<Pin> {

    private final HashMap<String, Pin> inputs;
    private final HashMap<String, Pin> outputs;
    private final ArrayList<Pin> allPins;

    public Pins() {
        inputs = new HashMap<>();
        outputs = new HashMap<>();
        allPins = new ArrayList<>();
    }

    public Pins add(Pin pin) {
        if (pin.getDirection() == Pin.Direction.input)
            inputs.put(pin.getName(), pin);
        else
            outputs.put(pin.getName(), pin);
        allPins.add(pin);
        return this;
    }

    @Override
    public Iterator<Pin> iterator() {
        return allPins.iterator();
    }

    public void setOutputs(ObservableValue[] outs) throws PinException {
        for (ObservableValue o : outs) {
            Pin pin = outputs.get(o.getName());
            if (pin == null)
                throw new PinException("pin " + o.getName() + " unknown!");
            pin.setValue(o);
        }
    }

    public HashMap<String, Pin> getInputs() {
        return inputs;
    }

    public boolean containsValue(ObservableValue v) {
        for (Pin p : allPins)
            if (p.getValue() == v)
                return true;
        return false;
    }

    public int size() {
        return allPins.size();
    }

    public Pin get(int index) {
        return allPins.get(index);
    }
}

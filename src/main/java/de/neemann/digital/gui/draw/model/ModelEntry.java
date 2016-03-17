package de.neemann.digital.gui.draw.model;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Part;
import de.neemann.digital.gui.draw.parts.Pin;
import de.neemann.digital.gui.draw.parts.PinException;
import de.neemann.digital.gui.draw.parts.Pins;

import java.util.HashMap;

/**
 * @author hneemann
 */
public class ModelEntry {
    private final Part part;
    private final Pins pins;
    private final String[] names;

    public ModelEntry(Part part, Pins pins, String[] names) {
        this.part = part;
        this.pins = pins;
        this.names = names;
    }

    public void applyInputs() throws PinException, NodeException {
        HashMap<String, Pin> ins = pins.getInputs();

        if (names.length > 0) {
            ObservableValue[] inputs = new ObservableValue[names.length];
            for (int i = 0; i < names.length; i++) {
                Pin pin = ins.get(names[i]);
                if (pin == null)
                    throw new PinException("pin " + names[i] + " not found!");

                ObservableValue value = pin.getValue();
                if (value == null)
                    throw new PinException("no value set for " + names[i] + "!");

                inputs[i] = value;
            }
            part.setInputs(inputs);
        }
    }

    public Part getPart() {
        return part;
    }
}

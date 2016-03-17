package de.neemann.digital.gui.draw.model;

import de.neemann.digital.core.*;
import de.neemann.digital.gui.draw.parts.*;

import java.util.HashMap;

/**
 * @author hneemann
 */
public class ModelEntry {
    private final Part part;
    private final Pins pins;
    private final String[] names;
    private final VisualPart visualPart;

    public ModelEntry(Part part, Pins pins, VisualPart visualPart) {
        this.part = part;
        this.pins = pins;
        this.visualPart = visualPart;
        this.names = visualPart.getPartDescription().getInputNames();
    }

    public void applyInputs(Listener listener, Model model) throws PinException, NodeException {
        HashMap<String, Pin> ins = pins.getInputs();

        ObservableValue[] inputs = new ObservableValue[names.length];
        if (names.length > 0) {
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
        visualPart.setState(new State(inputs, part.getOutputs()), listener, model);
    }

    public Part getPart() {
        return part;
    }
}

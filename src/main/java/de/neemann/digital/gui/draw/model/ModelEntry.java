package de.neemann.digital.gui.draw.model;

import de.neemann.digital.core.Listener;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.part.Part;
import de.neemann.digital.gui.draw.parts.*;

import java.util.HashMap;

/**
 * @author hneemann
 */
public class ModelEntry {
    private final Part part;
    private final Pins pins;
    private final String[] inputNames;
    private final VisualPart visualPart;

    public ModelEntry(Part part, Pins pins, VisualPart visualPart, String[] inputNames) {
        this.part = part;
        this.pins = pins;
        this.visualPart = visualPart;
        this.inputNames = inputNames;
    }

    public void applyInputs(Listener listener, Model model) throws PinException, NodeException {
        HashMap<String, Pin> ins = pins.getInputs();

        ObservableValue[] inputs = new ObservableValue[inputNames.length];
        if (inputNames.length > 0) {
            for (int i = 0; i < inputNames.length; i++) {
                Pin pin = ins.get(inputNames[i]);
                if (pin == null)
                    throw new PinException("pin " + inputNames[i] + " not found!");

                ObservableValue value = pin.getValue();
                if (value == null)
                    throw new PinException("no value set for " + inputNames[i] + "!");

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

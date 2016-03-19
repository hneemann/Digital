package de.neemann.digital.gui.draw.model;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;
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
    private IOState ioState;

    public ModelEntry(Part part, Pins pins, VisualPart visualPart, String[] inputNames) {
        this.part = part;
        this.pins = pins;
        this.visualPart = visualPart;
        this.inputNames = inputNames;
    }

    /**
     * Sets the Inputs of the part contained in thes entry
     *
     * @throws PinException
     * @throws NodeException
     */
    public void applyInputs() throws PinException, NodeException {
        HashMap<String, Pin> ins = pins.getInputs();

        ObservableValue[] inputs = new ObservableValue[inputNames.length];
        if (inputNames.length > 0) {
            for (int i = 0; i < inputNames.length; i++) {
                Pin pin = ins.get(inputNames[i]);
                if (pin == null)
                    throw new PinException("pin '" + inputNames[i] + "' at " + visualPart + " not found!");

                ObservableValue value = pin.getValue();
                if (value == null)
                    throw new PinException("no value set for '" + inputNames[i] + "' at " + visualPart + "!");

                inputs[i] = value;
            }
            part.setInputs(inputs);
        }
        ioState = new IOState(inputs, part.getOutputs());
    }

    public void connectToGui(Observer guiObserver) {
        if (ioState == null)
            throw new RuntimeException("call applyInputs before connectToGui");
        visualPart.setState(ioState, guiObserver);
    }

    public Part getPart() {
        return part;
    }
}

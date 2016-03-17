package de.neemann.digital.core.io;

import de.neemann.digital.core.*;
import de.neemann.digital.gui.draw.shapes.OutputShape;

/**
 * @author hneemann
 */
public class Out implements Part {

    private ObservableValue value;

    public Out() {
    }

    public static PartDescription createFactory(int bits) {
        return new PartDescription(new OutputShape(bits), () -> new Out(), "in");
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        value = inputs[0];
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[0];
    }

    @Override
    public void registerNodes(Model model) {
    }

    public ObservableValue getValue() {
        return value;
    }
}

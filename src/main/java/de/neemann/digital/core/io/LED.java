package de.neemann.digital.core.io;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.part.AttributeKey;
import de.neemann.digital.core.part.Part;
import de.neemann.digital.core.part.PartAttributes;
import de.neemann.digital.core.part.PartTypeDescription;

/**
 * @author hneemann
 */
public class LED implements Part {

    public static final PartTypeDescription DESCRIPTION = new PartTypeDescription(LED.class, "in")
            .addAttribute(AttributeKey.Label)
            .addAttribute(AttributeKey.Color);

    public LED(PartAttributes attributes) {
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[0];
    }

    @Override
    public void registerNodes(Model model) {
    }
}

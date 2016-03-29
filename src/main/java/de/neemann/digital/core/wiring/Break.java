package de.neemann.digital.core.wiring;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementTypeDescription;

/**
 * @author hneemann
 */
public class Break implements Element {

    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Break.class, "brk")
            .addAttribute(AttributeKey.Rotate)
            .addAttribute(AttributeKey.Label)
            .addAttribute(AttributeKey.Cycles);


    private ObservableValue input;

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        input = inputs[0].checkBits(1, null);
    }

    public ObservableValue getBreakInput() {
        return input;
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[0];
    }

    @Override
    public void registerNodes(Model model) {
        model.addBreak(this);
    }

}

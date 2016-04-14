package de.neemann.digital.core.io;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * @author hneemann
 */
public class Probe implements Element {


    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription("Probe", Probe.class, input("in"))
            .addAttribute(AttributeKey.Rotate)
            .addAttribute(AttributeKey.Label);

    private final String label;
    private ObservableValue value;

    public Probe(ElementAttributes attributes) {
        label = attributes.get(AttributeKey.Label);
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
        model.addSignal(label, value);
    }

}

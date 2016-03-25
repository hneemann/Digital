package de.neemann.digital.core.io;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;

/**
 * @author hneemann
 */
public class Out implements Element {

    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Out.class, "in")
            .addAttribute(AttributeKey.Bits)
            .addAttribute(AttributeKey.Label);

    public static final ElementTypeDescription PROBEDESCRIPTION = new ElementTypeDescription("Probe", Out.class, "in")
            .addAttribute(AttributeKey.Bits)
            .addAttribute(AttributeKey.Label);

    public static final ElementTypeDescription LEDDESCRIPTION = new ElementTypeDescription("LED", Out.class, "in")
            .addAttribute(AttributeKey.Label)
            .addAttribute(AttributeKey.Color);

    private final int bits;
    private ObservableValue value;

    public Out(ElementAttributes attributes) {
        bits = attributes.getBits();
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        value = inputs[0].checkBits(bits, null);
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

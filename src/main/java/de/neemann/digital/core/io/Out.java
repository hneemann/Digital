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

    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(Out.class, "in")
            .addAttribute(AttributeKey.Rotate)
            .addAttribute(AttributeKey.Bits)
            .addAttribute(AttributeKey.Label);

    public static final ElementTypeDescription LEDDESCRIPTION
            = new ElementTypeDescription("LED", Out.class, "in")
            .addAttribute(AttributeKey.Rotate)
            .addAttribute(AttributeKey.Label)
            .addAttribute(AttributeKey.Color);

    public static final ElementTypeDescription SEVENDESCRIPTION
            = new ElementTypeDescription("Seven-Seg", Out.class, "a", "b", "c", "d", "e", "f", "g", "dp")
            .addAttribute(AttributeKey.Label)
            .addAttribute(AttributeKey.Color);

    public static final ElementTypeDescription SEVENHEXDESCRIPTION
            = new ElementTypeDescription("Seven-Seg-Hex",
            attributes -> {
                return new Out(4);
            }, "d")
            .addAttribute(AttributeKey.Label)
            .addAttribute(AttributeKey.Color);

    private final int bits;
    private final String label;
    private ObservableValue value;

    public Out(ElementAttributes attributes) {
        bits = attributes.getBits();
        label = attributes.get(AttributeKey.Label);
    }

    public Out(int bits) {
        this.bits = bits;
        label = null;
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
        model.addSignal(label, value);
    }

    public ObservableValue getValue() {
        return value;
    }
}

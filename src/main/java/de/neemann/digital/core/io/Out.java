package de.neemann.digital.core.io;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.*;

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

    public static final ElementTypeDescription SEVENDESCRIPTION = new ElementTypeDescription("Seven-Seg", Out.class, "a", "b", "c", "d", "e", "f", "g", "dp")
            .addAttribute(AttributeKey.Label)
            .addAttribute(AttributeKey.Color);

    public static final ElementTypeDescription SEVENHEXDESCRIPTION = new ElementTypeDescription("Seven-Seg-Hex", new ElementFactory() {
        @Override
        public Element create(ElementAttributes attributes) {
            return new Out(4);
        }
    }, "d")
            .addAttribute(AttributeKey.Label)
            .addAttribute(AttributeKey.Color);

    private final int bits;
    private ObservableValue value;

    public Out(ElementAttributes attributes) {
        bits = attributes.getBits();
    }

    public Out(int bits) {
        this.bits = bits;
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

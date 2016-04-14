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
public class Out implements Element {

    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(Out.class, input("in")) {
        @Override
        public String getDescription(ElementAttributes elementAttributes) {
            return elementAttributes.get(AttributeKey.Description);
        }
    }
            .addAttribute(AttributeKey.Rotate)
            .addAttribute(AttributeKey.Bits)
            .addAttribute(AttributeKey.Label)
            .addAttribute(AttributeKey.Description);

    public static final ElementTypeDescription LEDDESCRIPTION
            = new ElementTypeDescription("LED", Out.class, input("in"))
            .addAttribute(AttributeKey.Rotate)
            .addAttribute(AttributeKey.Label)
            .addAttribute(AttributeKey.Color);

    public static final ElementTypeDescription SEVENDESCRIPTION
            = new ElementTypeDescription("Seven-Seg",
            attributes -> {
                return new Out(1, 1, 1, 1, 1, 1, 1, 1);
            },
            input("a"), input("b"), input("c"), input("d"), input("e"), input("f"), input("g"), input("dp"))
            .addAttribute(AttributeKey.Label)
            .addAttribute(AttributeKey.Color);

    public static final ElementTypeDescription SEVENHEXDESCRIPTION
            = new ElementTypeDescription("Seven-Seg-Hex",
            attributes -> {
                return new Out(4, 1);
            }, input("d"), input("dp"))
            .addAttribute(AttributeKey.Label)
            .addAttribute(AttributeKey.Color);

    private final int[] bits;
    private final String label;
    private ObservableValue value;

    public Out(ElementAttributes attributes) {
        bits = new int[]{attributes.getBits()};
        label = attributes.get(AttributeKey.Label);
    }

    public Out(int... bits) {
        this.bits = bits;
        label = null;
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        if (inputs.length != bits.length)
            throw new NodeException("wrong input count");
        value = inputs[0].checkBits(bits[0], null);
        for (int i = 1; i < bits.length; i++)
            inputs[i].checkBits(bits[i], null);
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

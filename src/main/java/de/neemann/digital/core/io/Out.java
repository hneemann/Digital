package de.neemann.digital.core.io;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * The different outputs
 *
 * @author hneemann
 */
public class Out implements Element {

    /**
     * The Input description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(Out.class, input("in")) {
        @Override
        public String getDescription(ElementAttributes elementAttributes) {
            return elementAttributes.get(Keys.DESCRIPTION);
        }
    }
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.DESCRIPTION);

    /**
     * The LED description
     */
    public static final ElementTypeDescription LEDDESCRIPTION
            = new ElementTypeDescription("LED", Out.class, input("in"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.COLOR);

    /**
     * The seven segment display description
     */
    public static final ElementTypeDescription SEVENDESCRIPTION
            = new ElementTypeDescription("Seven-Seg",
            attributes -> {
                return new Out(1, 1, 1, 1, 1, 1, 1, 1);
            },
            input("a"), input("b"), input("c"), input("d"), input("e"), input("f"), input("g"), input("dp"))
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.COLOR);

    /**
     * The seven segment hex display description
     */
    public static final ElementTypeDescription SEVENHEXDESCRIPTION
            = new ElementTypeDescription("Seven-Seg-Hex",
            attributes -> {
                return new Out(4, 1);
            }, input("d"), input("dp"))
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.COLOR);

    private final int[] bits;
    private final String label;
    private ObservableValue value;

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Out(ElementAttributes attributes) {
        bits = new int[]{attributes.getBits()};
        label = attributes.getCleanLabel();
    }

    /**
     * Creates a new instance
     *
     * @param bits the bitcount of the different inputs
     */
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
        model.addOutput(label, value);
    }
}

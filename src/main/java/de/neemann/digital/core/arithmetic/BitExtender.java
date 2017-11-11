package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.lang.Lang;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * Sign extension component
 */
public class BitExtender implements Element {

    /**
     * Description of the sign extend component.
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(BitExtender.class, input("in"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.OUTPUT_BITS);

    private final ObservableValue out;
    private final int outBits;

    /**
     * creates a new instance
     *
     * @param attr the components attributes
     */
    public BitExtender(ElementAttributes attr) {
        outBits = attr.get(Keys.OUTPUT_BITS);
        out = new ObservableValue("out", outBits).setPinDescription(DESCRIPTION);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        final ObservableValue in = inputs.get(0);
        final int inBits = in.getBits();
        if (inBits >= outBits)
            throw new NodeException(Lang.get("err_notMoreOutBitsThanInBits"));

        final long signMask = 1L << (inBits - 1);
        final long extendMask = ~((1L << inBits) - 1);

        in.addObserver(() -> {
            long inValue = in.getValue();
            if ((inValue & signMask) == 0)
                out.setValue(inValue);
            else
                out.setValue(inValue | extendMask);
        }).hasChanged();
    }

    @Override
    public ObservableValues getOutputs() throws PinException {
        return out.asList();
    }

    @Override
    public void registerNodes(Model model) {
        // has no nodes! Is just wiring
    }
}

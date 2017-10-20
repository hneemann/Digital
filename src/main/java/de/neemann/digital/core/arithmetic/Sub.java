package de.neemann.digital.core.arithmetic;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * @author hneemann
 */
public class Sub extends Add {

    /**
     * The subtractors description
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(Sub.class, input("a"), input("b"), input("c_i"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.BITS);

    /**
     * Creates a new instance
     *
     * @param attributes the attributes
     */
    public Sub(ElementAttributes attributes) {
        super(attributes);
        getOutputs().get(0).setPinDescription(DESCRIPTION);
        getOutputs().get(1).setPinDescription(DESCRIPTION);
    }

    @Override
    protected long calc(long a, long b, long c) {
        return a - b - c;
    }
}

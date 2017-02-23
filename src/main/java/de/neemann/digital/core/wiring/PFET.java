package de.neemann.digital.core.wiring;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * P-Channel MOS FET
 * Created by hneemann on 22.02.17.
 */
public class PFET extends Relay {
    /**
     * The switch description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(PFET.class, input("G"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL);

    /**
     * Create a new instance
     *
     * @param attr the attributes
     */
    public PFET(ElementAttributes attr) {
        super(attr);
        getOutput1().setPinDescription(DESCRIPTION);
        getOutput2().setPinDescription(DESCRIPTION);
    }

    @Override
    protected boolean getClosed(boolean inState, boolean inHighZ, ObservableValue in1, ObservableValue in2) {
        if (inHighZ || in1.isHighZ())
            return false;

        return !inState && in1.getBool();
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        super.setInputs(inputs);
        inputs.get(1).addObserverToValue(this);
    }

}

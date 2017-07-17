package de.neemann.digital.core.io;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.lang.Lang;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * Enforces a power supply
 * Created by hneemann on 15.07.17.
 */
public class PowerSupply implements Element {

    /**
     * Enforces a power supply
     */
    public static final ElementTypeDescription DESCRIPTION
            = new ElementTypeDescription(PowerSupply.class, input("VDD"), input("GND"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.LABEL);

    /**
     * Creates a new instance
     *
     * @param attributes attributes
     */
    public PowerSupply(ElementAttributes attributes) {
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        check(inputs.get(0).checkBits(1, null, 0), 1);
        check(inputs.get(1).checkBits(1, null, 1), 0);
    }

    private void check(ObservableValue val, int expected) throws NodeException {
        if (!val.isConstant() || val.getValue() != expected)
            throw new NodeException(Lang.get("err_errorInPowerSupply"), val);
    }

    @Override
    public ObservableValues getOutputs() throws PinException {
        return ObservableValues.EMPTY_LIST;
    }

    @Override
    public void registerNodes(Model model) {
    }
}

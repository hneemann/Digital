package de.neemann.digital.core.wiring;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A simple relay.
 * Created by hneemann on 22.02.17.
 */
public class Relay extends Switch {

    /**
     * The switch description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(Relay.class, input("in"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BITS)
            .addAttribute(Keys.LABEL)
            .addAttribute(Keys.RELAY_NORMALLY_CLOSED);


    private ObservableValue input;
    private final boolean invers;

    /**
     * Create a new instance
     *
     * @param attr the attributes
     */
    public Relay(ElementAttributes attr) {
        super(attr, attr.get(Keys.RELAY_NORMALLY_CLOSED));
        invers = attr.get(Keys.RELAY_NORMALLY_CLOSED);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        input = inputs.get(0).checkBits(1, null).addObserverToValue(() -> setClosed(input.getBool() ^ invers));
        super.setInputs(new ObservableValues(inputs.get(1), inputs.get(2)));
    }

}

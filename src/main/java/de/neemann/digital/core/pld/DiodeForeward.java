package de.neemann.digital.core.pld;

import de.neemann.digital.core.*;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

import static de.neemann.digital.core.element.PinInfo.input;

/**
 * A diode needed to pull a wire to VDD.
 * Used to build a wired OR.
 */
public class DiodeForeward implements Element, Observer {

    /**
     * The diodes description
     */
    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription(DiodeForeward.class, input("in"))
            .addAttribute(Keys.ROTATE)
            .addAttribute(Keys.BLOWN);

    private final ObservableValue output;
    private final boolean blown;
    private ObservableValue input;

    /**
     * Creates a new instance
     *
     * @param attr the elements attributes
     */
    public DiodeForeward(ElementAttributes attr) {
        output = new ObservableValue("out", 1, true);
        blown = attr.get(Keys.BLOWN);
        if (blown)
            output.set(1, true);
    }

    @Override
    public void setInputs(ObservableValues inputs) throws NodeException {
        input = inputs.get(0).addObserverToValue(this).checkBits(1, null);
    }

    @Override
    public ObservableValues getOutputs() {
        return output.asList();
    }

    @Override
    public void registerNodes(Model model) {
        // its just a wire and has no delay, so it is'nt a node
    }

    @Override
    public void hasChanged() {
        if (!blown) {
            if (input.isHighZ()) {
                output.set(0, true);
            } else {
                final boolean in = input.getBool();
                setOutValue(output, in);
            }
        }
    }

    /**
     * Sets the value to the output
     *
     * @param output the output to write to
     * @param in     the input value
     */
    protected void setOutValue(ObservableValue output, boolean in) {
        output.set(in ? 1 : 0, !in);
    }

    @Override
    public void init(Model model) throws NodeException {
        hasChanged();
    }
}

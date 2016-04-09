package de.neemann.digital.core.wiring;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.lang.Lang;

/**
 * @author hneemann
 */
public class Clock implements Element {

    public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription("Clock", Clock.class)
            .addAttribute(AttributeKey.Rotate)
            .addAttribute(AttributeKey.Label)
            .addAttribute(AttributeKey.Frequency);

    private final ObservableValue output;
    private final int frequency;
    private final String label;

    public Clock(ElementAttributes attributes) {
        output = new ObservableValue("C", 1);
        int f = attributes.get(AttributeKey.Frequency);
        if (f < 1) f = 1;
        frequency = f;
        label = attributes.get(AttributeKey.Label);
    }

    @Override
    public void setInputs(ObservableValue... inputs) throws NodeException {
        throw new NodeException(Lang.get("err_noInputsAvailable"));
    }

    @Override
    public ObservableValue[] getOutputs() {
        return new ObservableValue[]{output};
    }

    @Override
    public void registerNodes(Model model) {
        model.addClock(this);
        model.addSignal(label, output);
    }

    public ObservableValue getClockOutput() {
        return output;
    }

    public int getFrequency() {
        return frequency;
    }

}

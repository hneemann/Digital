package de.neemann.digital.core.wiring.bus;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.PinDescription;

import java.util.Arrays;
import java.util.List;

/**
 * Created by hneemann on 22.02.17.
 */
public class SingleBusHandler extends AbstractBusHandler {
    private final ObservableValue output;
    private final PinDescription.PullResistor resistor;
    private final List<ObservableValue> inputs;

    public SingleBusHandler(BusModelStateObserver obs, ObservableValue output, PinDescription.PullResistor resistor, ObservableValue... inputs) {
        super(obs);
        this.output = output;
        this.resistor = resistor;
        this.inputs = Arrays.asList(inputs);
    }

    @Override
    public List<ObservableValue> getInputs() {
        return inputs;
    }

    @Override
    public PinDescription.PullResistor getResistor() {
        return resistor;
    }

    @Override
    public void set(long value, boolean highz) {
        output.set(value, highz);
    }
}

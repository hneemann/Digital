package de.neemann.digital.core.wiring.bus;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.PinDescription;

import java.util.Arrays;
import java.util.List;

/**
 * The SingleBusHandler is used for a simple isolated net.
 * Created by hneemann on 22.02.17.
 */
public final class SingleBusHandler extends AbstractBusHandler {
    private final ObservableValue output;
    private final PinDescription.PullResistor resistor;
    private final List<ObservableValue> inputs;

    /**
     * Creates a new instance
     *
     * @param obs      the Observer used ti detect a burn condition
     * @param output   the outputs connected to this net
     * @param resistor the resistor of this net
     * @param inputs   the outputs connected to this net are the inputs of the nets state
     */
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

    @Override
    public ObservableValues getAllValues() {
        return new ObservableValues.Builder().add(getInputs()).add(output).build();
    }
}

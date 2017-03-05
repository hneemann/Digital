package de.neemann.digital.core.wiring.bus;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.PinDescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The ConnectedBusHandler calculates the state of net, which is formed by the single nets
 * connected by a closed switch.
 * Created by hneemann on 22.02.17.
 */
public final class ConnectedBusHandler extends AbstractBusHandler {
    private PinDescription.PullResistor resistor = PinDescription.PullResistor.none;
    private ArrayList<CommonBusValue> values;
    private ArrayList<ObservableValue> inputs;

    /**
     * Creates a new instance
     *
     * @param obs The observer needed to check the burn condition
     */
    public ConnectedBusHandler(BusModelStateObserver obs) {
        super(obs);
        values = new ArrayList<>();
        inputs = new ArrayList<>();
    }

    /**
     * Adds a net to the common unified net
     *
     * @param net the net to add
     */
    public void addNet(CommonBusValue net) {
        values.add(net);
        inputs.addAll(Arrays.asList(net.getInputs()));

        if (!net.getResistor().equals(PinDescription.PullResistor.none)) {
            if (resistor.equals(PinDescription.PullResistor.none)) {
                resistor = net.getResistor();
            } else {
                if (!resistor.equals(net.getResistor())) {
                    // set error condition
                    resistor = PinDescription.PullResistor.both;
                }
            }
        }

        net.setHandler(this);
    }

    /**
     * Adds all nets in the given {@link ConnectedBusHandler}.
     *
     * @param h2 the {@link ConnectedBusHandler}
     */
    public void addNet(ConnectedBusHandler h2) {
        for (CommonBusValue cbv : h2.values)
            addNet(cbv);
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
        for (ObservableValue val : values)
            val.set(value, highz);
    }

    /**
     * @return all the nets connected by this handler.
     */
    public ArrayList<CommonBusValue> getValues() {
        return values;
    }
}

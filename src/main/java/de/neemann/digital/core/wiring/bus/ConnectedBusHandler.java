package de.neemann.digital.core.wiring.bus;

import de.neemann.digital.core.BurnException;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.PinDescription;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by hneemann on 22.02.17.
 */
public class ConnectedBusHandler extends AbstractBusHandler {
    private PinDescription.PullResistor resistor = PinDescription.PullResistor.none;
    private ArrayList<CommonBusValue> values;
    private ArrayList<ObservableValue> inputs;

    public ConnectedBusHandler(BusModelStateObserver obs) {
        super(obs);
        values = new ArrayList<>();
        inputs=new ArrayList<>();
    }

    public void addNet(CommonBusValue net) {
        values.add(net);
        inputs.addAll(Arrays.asList(net.getInputs()));

        if (!net.getResistor().equals(PinDescription.PullResistor.none)) {
            if (resistor.equals(PinDescription.PullResistor.none)) {
                resistor=net.getResistor();
            } else {
                if (!resistor.equals(net.getResistor())) {
                    // ToDo different resistors!
                    throw new BurnException(inputs);
                }
            }
        }

        net.setHandler(this);
    }

    public void addNet(ConnectedBusHandler h2) {
        for (CommonBusValue cbv:h2.values)
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

    public ArrayList<CommonBusValue> getValues() {
        return values;
    }
}

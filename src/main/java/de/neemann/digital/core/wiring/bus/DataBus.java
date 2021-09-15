/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.wiring.bus;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.model.Net;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;

/**
 * Handles the creation of a data bus.
 * Is needed to connect multiple outputs which can become high Z.
 * If one of the output becomes low Z, this value is returned by the {@link ObservableValue}
 * created by this bus. If more than one output becomes low Z and the values are not equal then
 * a {@link de.neemann.digital.core.BurnException} is thrown after the models step is completed.
 * During the calculation of of a single step a temporary burn condition is allowed.
 */
public class DataBus {
    private final CommonBusValue commonBusValue;

    /**
     * Creates a new data bus
     *
     * @param net     the net
     * @param model   the model
     * @param outputs the outputs building the net
     * @throws PinException PinException
     */
    public DataBus(Net net, Model model, ArrayList<Pin> outputs) throws PinException {
        this(net, model, createArray(outputs));
    }

    private static ObservableValue[] createArray(ArrayList<Pin> outputs) {
        ObservableValue[] o = new ObservableValue[outputs.size()];
        for (int i = 0; i < outputs.size(); i++)
            o[i] = outputs.get(i).getValue();
        return o;
    }

    /**
     * Creates a new data bus
     *
     * @param net     the net
     * @param model   the model
     * @param outputs the outputs building the net
     * @throws PinException PinException
     */
    public DataBus(Net net, Model model, ObservableValue... outputs) throws PinException {
        int bits = 0;
        PinDescription.PullResistor resistor = PinDescription.PullResistor.none;
        for (ObservableValue o : outputs) {
            int b = o.getBits();
            if (bits == 0) bits = b;
            else {
                if (bits != b)
                    throw new PinException(Lang.get("err_notAllOutputsSameBits"), net);
            }
//            if (!o.supportsHighZ())
//                throw new PinException(Lang.get("err_notAllOutputsSupportHighZ"), net);

            switch (o.getPullResistor()) {
                case pullDown:
                    if (resistor == PinDescription.PullResistor.pullUp)
                        throw new PinException(Lang.get("err_pullUpAndDownNotAllowed"), net);
                    resistor = PinDescription.PullResistor.pullDown;
                    break;
                case pullUp:
                    if (resistor == PinDescription.PullResistor.pullDown)
                        throw new PinException(Lang.get("err_pullUpAndDownNotAllowed"), net);
                    resistor = PinDescription.PullResistor.pullUp;
                    break;
            }
        }

        BusModelStateObserver obs = model.getOrCreateObserver(BusModelStateObserver.class, BusModelStateObserver::new);

        commonBusValue = new CommonBusValue(bits, obs, resistor, outputs, net == null ? null : net.getOrigin());
        for (ObservableValue p : outputs)
            p.addObserverToValue(commonBusValue);
        commonBusValue.hasChanged();
    }

    /**
     * Returns the readable ObservableValue for this bus
     *
     * @return the readable ObservableValue
     */
    public ObservableValue getReadableOutput() {
        return commonBusValue;
    }

}

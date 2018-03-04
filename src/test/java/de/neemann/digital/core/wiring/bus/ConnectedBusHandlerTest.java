/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.wiring.bus;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.element.PinDescription;
import junit.framework.TestCase;

/**
 */
public class ConnectedBusHandlerTest extends TestCase {

    public void testNets() {
        BusModelStateObserver obs = new BusModelStateObserver();
        ConnectedBusHandler cbh = new ConnectedBusHandler(obs);

        cbh.addNet(new CommonBusValue(1, obs, PinDescription.PullResistor.none, new ObservableValue[]{}));
        assertEquals(PinDescription.PullResistor.none, cbh.getResistor());
        cbh.addNet(new CommonBusValue(1, obs, PinDescription.PullResistor.pullUp, new ObservableValue[]{}));
        assertEquals(PinDescription.PullResistor.pullUp, cbh.getResistor());
        cbh.addNet(new CommonBusValue(1, obs, PinDescription.PullResistor.pullDown, new ObservableValue[]{}));
        assertEquals(PinDescription.PullResistor.both, cbh.getResistor());
        cbh.addNet(new CommonBusValue(1, obs, PinDescription.PullResistor.pullDown, new ObservableValue[]{}));
        assertEquals(PinDescription.PullResistor.both, cbh.getResistor());
        cbh.addNet(new CommonBusValue(1, obs, PinDescription.PullResistor.pullUp, new ObservableValue[]{}));
        assertEquals(PinDescription.PullResistor.both, cbh.getResistor());
        cbh.addNet(new CommonBusValue(1, obs, PinDescription.PullResistor.none, new ObservableValue[]{}));
        assertEquals(PinDescription.PullResistor.both, cbh.getResistor());
    }

}

/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.wiring.bus;

import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.ModelStateObserverTyped;
import de.neemann.digital.core.switching.Switch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Checks if a temporary burn condition is still present after the step is completed.
 * If so, an exception is thrown.
 * Handles also the reconfiguration of the nets if a switch has changed.
 */
public final class BusModelStateObserver implements ModelStateObserverTyped {
    private final ArrayList<AbstractBusHandler> busList;
    private final HashSet<Switch.RealSwitch> closedSwitches;
    private int version;

    BusModelStateObserver() {
        busList = new ArrayList<>();
        closedSwitches = new HashSet<>();
    }

    @Override
    public void handleEvent(ModelEvent event) {
        if (event == ModelEvent.STEP && !busList.isEmpty()) {
            for (AbstractBusHandler bus : busList) {
                bus.checkBurn();
            }
            busList.clear();
            version++;
        }
    }

    @Override
    public ModelEvent[] getEvents() {
        return new ModelEvent[]{ModelEvent.STEP};
    }

    /**
     * @return the version used to avoid double additions of nets in a burn condition
     */
    public int getVersion() {
        return version;
    }

    /**
     * Adds a net in a burn condition
     *
     * @param commonBusValue the value in burn condition
     */
    public void addCheck(AbstractBusHandler commonBusValue) {
        busList.add(commonBusValue);
    }

    /**
     * Closes or opens a switch.
     * Is used to reconfigure a net with switches
     *
     * @param realSwitch the switch
     * @param closed     true if switch is closed
     */
    public void setClosed(Switch.RealSwitch realSwitch, boolean closed) {
        if (closed) {
            closedSwitches.add(realSwitch);
        } else {
            closedSwitches.remove(realSwitch);
            realSwitch.getInput1().resetHandler();
            realSwitch.getInput2().resetHandler();
        }
        reconfigureNets();
    }

    /**
     * Reconfiguration of the nets.
     * If a switch is closed the nets on both contacts of the switch are connected to a single
     * common net, After that the state of the new merged nets are updated.
     * Needs to be called every time a switch has changed.
     */
    private void reconfigureNets() {
        busList.removeIf(abstractBusHandler -> abstractBusHandler instanceof ConnectedBusHandler);
        HashMap<CommonBusValue, ConnectedBusHandler> netMap = new HashMap<>();
        ArrayList<ConnectedBusHandler> createdHandlers = new ArrayList<>();
        for (Switch.RealSwitch s : closedSwitches) {
            ConnectedBusHandler h1 = netMap.get(s.getInput1());
            ConnectedBusHandler h2 = netMap.get(s.getInput2());
            if (h1 == null) {
                if (h2 == null) {
                    ConnectedBusHandler h = new ConnectedBusHandler(this);
                    createdHandlers.add(h);
                    h.addNet(s.getInput1());
                    h.addNet(s.getInput2());
                    netMap.put(s.getInput1(), h);
                    netMap.put(s.getInput2(), h);
                } else {
                    h2.addNet(s.getInput1());
                    netMap.put(s.getInput1(), h2);
                }
            } else {
                if (h2 == null) {
                    h1.addNet(s.getInput2());
                    netMap.put(s.getInput2(), h1);
                } else {
                    if (h1 != h2) {
                        // merge the nets
                        h1.addNet(h2);
                        for (CommonBusValue v : h2.getValues())
                            netMap.put(v, h1);
                        createdHandlers.remove(h2);
                    }
                }
            }
        }
        for (ConnectedBusHandler h : createdHandlers)
            h.recalculate();
    }
}

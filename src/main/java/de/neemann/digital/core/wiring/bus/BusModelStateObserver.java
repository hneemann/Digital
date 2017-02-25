package de.neemann.digital.core.wiring.bus;

import de.neemann.digital.core.ModelEvent;
import de.neemann.digital.core.ModelStateObserver;
import de.neemann.digital.core.wiring.Switch;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Checks if a temporary burn condition is still present after the step is completed.
 * If so an exception is thrown.
 * Handles also the switches
 */
public final class BusModelStateObserver implements ModelStateObserver {
    private final ArrayList<AbstractBusHandler> busList;
    private int version;
    private HashSet<Switch.RealSwitch> closedSwitches;

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
                    }
                }
            }
        }
        for (ConnectedBusHandler h : createdHandlers)
            h.recalculate();
    }
}

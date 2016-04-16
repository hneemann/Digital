package de.neemann.digital.draw.model;

import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Wire;
import de.neemann.digital.draw.graphics.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Holds all the nets in a circuit
 *
 * @author hneemann
 */
public class NetList implements Iterable<Net> {

    private final ArrayList<Net> netList;

    /**
     * Creates a net list from the given wires
     *
     * @param wires the wires
     */
    public NetList(List<Wire> wires) {
        netList = new ArrayList<>();
        for (Wire w : wires)
            add(w);
    }

    /**
     * Creates a copy of the given net list
     *
     * @param toCopy the net list to copy
     */
    public NetList(NetList toCopy) {
        netList = new ArrayList<>();
        for (Net net : toCopy)
            netList.add(new Net(net));
    }

    /**
     * Adds a complete net list to this net list
     *
     * @param netList the net list to add
     */
    public void add(NetList netList) {
        this.netList.addAll(netList.netList);
    }

    /**
     * Adds a pin to tis net list
     *
     * @param pin the pin to add
     */
    public void add(Pin pin) {
        for (Net net : netList)
            if (net.contains(pin.getPos()))
                net.add(pin);
    }

    private void add(Wire w) {
        for (Net net : netList) {
            Vector added = net.tryMerge(w);
            if (added != null) {
                netChanged(net, added);
                return;
            }
        }
        netList.add(new Net(w));
    }

    private void netChanged(Net changedNet, Vector added) {
        for (Net n : netList) {
            if (n != changedNet) {
                if (n.contains(added)) {
                    n.addAllPointsFrom(changedNet);
                    netList.remove(changedNet);
                    return;
                }
            }
        }
    }

    /**
     * @return the number of nets in this net list
     */
    public int size() {
        return netList.size();
    }

    @Override
    public Iterator<Net> iterator() {
        return netList.iterator();
    }

    /**
     * Returns the net of the given pin
     *
     * @param p the pin
     * @return the net or null if not found
     */
    public Net getNetOfPin(Pin p) {
        for (Net n : netList)
            if (n.containsPin(p))
                return n;
        return null;
    }

    /**
     * Removes a net from this net list
     *
     * @param childNet the net to remove
     */
    public void remove(Net childNet) {
        netList.remove(childNet);
    }
}

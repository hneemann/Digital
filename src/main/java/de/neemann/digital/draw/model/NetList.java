/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.model;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.*;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Holds all the nets in a circuit
 */
public class NetList implements Iterable<Net> {

    private final ArrayList<Net> netList;

    /**
     * Creates a net list from the given circuit
     *
     * @param circuit the circuit
     * @throws PinException PinException
     */
    public NetList(Circuit circuit) throws PinException {
        netList = new ArrayList<>();
        for (Wire w : circuit.getWires())
            add(w);


        // handles a direct pin overlap by adding a single point net
        HashSet<Vector> allPinPositions = new HashSet<>();
        HashSet<Vector> directConnection = new HashSet<>();
        for (VisualElement ve : circuit.getElements())
            for (Pin p : ve.getPins()) {
                Vector v = p.getPos();
                if (allPinPositions.contains(v))
                    directConnection.add(v);
                else
                    allPinPositions.add(v);
            }

        for (Vector v : directConnection)
            if (getNetOfPos(v) == null)
                netList.add(new Net(v));


        boolean hasLabel = false;
        for (VisualElement ve : circuit.getElements())
            if (ve.equalsDescription(Tunnel.DESCRIPTION)) {
                String label = ve.getElementAttributes().get(Keys.NETNAME).trim();
                if (!label.isEmpty()) {
                    Vector pos = ve.getPos();
                    Net found = null;
                    for (Net n : netList)
                        if (n.contains(pos))
                            found = n;

                    if (found == null) {
                        final PinException e = new PinException(Lang.get("err_labelNotConnectedToNet_N", label), ve);
                        e.setOrigin(circuit.getOrigin());
                        throw e;
                    }

                    found.addLabel(label);
                    hasLabel = true;
                }
            }

        if (hasLabel)
            mergeLabels();

        for (Net n : netList)
            n.setOrigin(circuit.getOrigin());
    }

    //modification of loop variable j is intended!
    //CHECKSTYLE.OFF: ModifiedControlVariable
    private void mergeLabels() {
        boolean wasMerge;
        do {
            wasMerge = false;
            for (int i = 0; i < netList.size() - 1; i++)
                for (int j = i + 1; j < netList.size(); j++)
                    if (netList.get(i).matchesLabel(netList.get(j))) {
                        netList.get(i).addAllPointsFrom(netList.get(j));
                        netList.remove(j);
                        j--;
                        wasMerge = true;
                    }
        } while (wasMerge);
    }
    //CHECKSTYLE.ON: ModifiedControlVariable

    /**
     * Creates a copy of the given net list
     *
     * @param toCopy        the net list to copy
     * @param visualElement the containing visual element, only used to create better error messages
     */
    public NetList(NetList toCopy, VisualElement visualElement) {
        netList = new ArrayList<>();
        for (Net net : toCopy)
            netList.add(new Net(net, visualElement));
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
     * Adds a pin to this net list
     *
     * @param pin the pin to add
     */
    public void add(Pin pin) {
        for (Net net : netList)
            if (net.contains(pin.getPos())) {
                net.add(pin);
                return;
            }
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
     * Returns the net of the given position
     *
     * @param pos the position
     * @return the net
     */
    public Net getNetOfPos(Vector pos) {
        for (Net n : netList)
            if (n.contains(pos))
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

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Holds all the nets in a circuit
 */
public class NetList implements Iterable<Net> {

    private final ArrayList<Net> netList;
    private HashMap<Vector, Net> posMap;
    private HashMap<Pin, Net> pinMap;

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

        HashMap<Vector, Net> allNetPositions = getAllNetPositions();
        for (Vector v : directConnection)
            if (!allNetPositions.containsKey(v)) {
                Net net = new Net(v);
                netList.add(net);
                allNetPositions.put(v, net);
            }


        boolean hasLabel = false;
        for (VisualElement ve : circuit.getElements())
            if (ve.equalsDescription(Tunnel.DESCRIPTION)) {
                String label = ve.getElementAttributes().get(Keys.NETNAME).trim();
                if (!label.isEmpty()) {
                    Net found = allNetPositions.get(ve.getPos());
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

    private void mergeLabels() {
        ArrayList<Net> oldNetList = new ArrayList<>(netList);
        netList.clear();

        HashMap<String, Net> map = new HashMap<>();
        for (Net n : oldNetList) {
            HashSet<String> labels = n.getLabels();
            switch (labels.size()) {
                case 0:
                    netList.add(n);
                    break;
                case 1:
                    String label = labels.iterator().next();
                    Net net = map.get(label);
                    if (net == null) {
                        netList.add(n);
                        map.put(label, n);
                    } else {
                        net.addAllPointsFrom(n);
                        for (String l : n.getLabels())
                            map.put(l, net);
                    }
                    break;
                default:
                    for (String la : new ArrayList<>(labels)) {
                        net = map.get(la);
                        if (net != null) {
                            n.addAllPointsFrom(net);
                            netList.remove(net);
                        }
                    }
                    netList.add(n);
                    for (String l : n.getLabels())
                        map.put(l, n);
            }
        }
    }

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
     * Adds a complete net list to this net list.
     * Used during custom component connection.
     *
     * @param netList the net list to add
     */
    public void add(NetList netList) {
        this.netList.addAll(netList.netList);
        if (pinMap != null)
            pinMap.putAll(netList.pinMap);
    }

    /**
     * Adds a pin to this net list.
     * Used only during model creation.
     *
     * @param pin the pin to add
     */
    public void add(Pin pin) {
        if (posMap == null)
            posMap = getAllNetPositions();

        Net net = posMap.get(pin.getPos());
        if (net != null)
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
     * Returns the net of the given pin.
     * Used during custom component connection.
     *
     * @param p the pin
     * @return the net or null if not found
     */
    public Net getNetOfPin(Pin p) {
        if (pinMap == null) {
            pinMap = new HashMap<>();
            for (Net n : netList)
                n.addPinsTo(pinMap);
        }
        return pinMap.get(p);
    }

    /**
     * Returns the net of the given position.
     * Not used during model formation
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

    private HashMap<Vector, Net> getAllNetPositions() {
        HashMap<Vector, Net> map = new HashMap<>();
        for (Net n : netList)
            n.addPointsTo(map);
        return map;
    }

    /**
     * Removes a net from this net list.
     * Used during custom component connection.
     *
     * @param childNet the net to remove
     */
    public void remove(Net childNet) {
        netList.remove(childNet);
        for (Pin p : childNet.getPins())
            if (pinMap.get(p) == childNet)
                pinMap.remove(p);
    }
}

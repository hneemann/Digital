package de.neemann.digital.gui.draw.model;

import de.neemann.digital.gui.draw.elements.Pin;
import de.neemann.digital.gui.draw.elements.Wire;
import de.neemann.digital.gui.draw.graphics.Vector;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author hneemann
 */
public class NetList implements Iterable<Net> {

    public final ArrayList<Net> netList;

    public NetList(List<Wire> wires) {
        netList = new ArrayList<>();
        for (Wire w : wires)
            add(w);
    }

    public void add(NetList netList) {
        this.netList.addAll(netList.netList);
    }

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

    public int size() {
        return netList.size();
    }

    @Override
    public Iterator<Net> iterator() {
        return netList.iterator();
    }

    public Net getNetOfPin(Pin p) {
        for (Net n : netList)
            if (n.containsPin(p))
                return n;
        return null;
    }

    public void remove(Net childNet) {
        netList.remove(childNet);
    }
}

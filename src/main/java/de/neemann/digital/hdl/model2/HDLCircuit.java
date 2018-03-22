/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.basic.Not;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.io.PowerSupply;
import de.neemann.digital.core.io.Probe;
import de.neemann.digital.core.pld.PullDown;
import de.neemann.digital.core.pld.PullUp;
import de.neemann.digital.core.wiring.Break;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.*;
import de.neemann.digital.draw.model.InverterConfig;
import de.neemann.digital.draw.model.Net;
import de.neemann.digital.draw.model.NetList;
import de.neemann.digital.gui.components.data.DummyElement;
import de.neemann.digital.testing.TestCaseElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class HDLCircuit implements Iterable<HDLNode>, HDLContext.BitProvider {
    private final String elementName;
    private final ArrayList<HDLPort> outputs;
    private final ArrayList<HDLPort> inputs;
    private NetList netList;
    private ArrayList<HDLNode> nodes;
    private HashMap<Net, HDLNet> nets;
    private int netNumber;

    public HDLCircuit(Circuit circuit, String elementName, HDLContext c) throws PinException, HDLException, NodeException {
        this.elementName = elementName;
        inputs = new ArrayList<>();
        outputs = new ArrayList<>();

        nodes = new ArrayList<>();
        nets = new HashMap<>();
        netList = new NetList(circuit);
        try {
            for (VisualElement v : circuit.getElements()) {
                if (v.equalsDescription(In.DESCRIPTION) || v.equalsDescription(Clock.DESCRIPTION))
                    addInput(new HDLPort(
                            v.getElementAttributes().getCleanLabel(),
                            getNetOfPin(v.getPins().get(0)),
                            HDLPort.Direction.OUT,
                            v.getElementAttributes().getBits())
                            .setPinNumber(v.getElementAttributes().get(Keys.PINNUMBER)));
                else if (v.equalsDescription(Out.DESCRIPTION))
                    addOutput(new HDLPort(
                            v.getElementAttributes().getCleanLabel(),
                            getNetOfPin(v.getPins().get(0)),
                            HDLPort.Direction.IN,
                            v.getElementAttributes().getBits())
                            .setPinNumber(v.getElementAttributes().get(Keys.PINNUMBER)));
                else if (isRealElement(v))
                    addNode(v, c);
            }
        } catch (HDLException e) {
            throw new HDLException("error parsing " + circuit.getOrigin(), e);
        }

        netList = null;

        for (HDLNet n : nets.values())
            n.fixBits();


        ArrayList<HDLNode> newNodes = new ArrayList<>();

        // fix inverted inputs
        for (HDLNode n : nodes) {
            InverterConfig iv = n.getElementAttributes().get(Keys.INVERTER_CONFIG);
            if (!iv.isEmpty()) {
                for (HDLPort p : n.getInputs())
                    if (iv.contains(p.getName()))
                        newNodes.add(createNot(p));
            }
        }

        nodes.addAll(newNodes);
    }

    private HDLNode createNot(HDLPort p) throws HDLException, NodeException, PinException {
        final ElementAttributes attr = new ElementAttributes().setBits(p.getBits());
        HDLNode n = new HDLNode(Not.DESCRIPTION.getName(), attr, name -> p.getBits());
        HDLNet outNet = new HDLNet(p.getName() + "_invert");
        HDLNet inNet = p.getNet();
        inNet.remove(p);

        n.addInput(new HDLPort(Not.DESCRIPTION.getInputDescription(attr).get(0).getName(), inNet, HDLPort.Direction.IN, p.getBits()));
        n.addOutput(new HDLPort(Not.DESCRIPTION.getOutputDescriptions(attr).get(0).getName(), outNet, HDLPort.Direction.OUT, p.getBits()));

        p.setNet(outNet);

        return n;
    }

    private void addOutput(HDLPort port) {
        outputs.add(port);
    }

    private void addInput(HDLPort port) {
        inputs.add(port);
    }

    private boolean isRealElement(VisualElement v) {
        return !v.equalsDescription(Tunnel.DESCRIPTION)
                && !v.equalsDescription(Break.DESCRIPTION)
                && !v.equalsDescription(PullDown.DESCRIPTION)
                && !v.equalsDescription(PullUp.DESCRIPTION)
                && !v.equalsDescription(Probe.DESCRIPTION)
                && !v.equalsDescription(PowerSupply.DESCRIPTION)
                && !v.equalsDescription(DummyElement.TEXTDESCRIPTION)
                && !v.equalsDescription(DummyElement.DATADESCRIPTION)
                && !v.equalsDescription(TestCaseElement.TESTCASEDESCRIPTION);
    }

    private HDLNet getNetOfPin(Pin pin) {
        Net n = netList.getNetOfPos(pin.getPos());
        if (n == null)
            return null;

        return nets.computeIfAbsent(n, net -> new HDLNet(createNetName(net)));
    }

    private String createNetName(Net net) {
        final HashSet<String> labels = net.getLabels();
        if (labels.size() == 1)
            return labels.iterator().next();
        else
            return Integer.toString(netNumber++);
    }

    private void addNode(VisualElement v, HDLContext c) throws HDLException {
        final HDLNode node = c.createNode(v);
        for (Pin p : v.getPins()) {
            HDLNet net = getNetOfPin(p);
            if (p.getDirection().equals(PinDescription.Direction.input))
                node.addInput(new HDLPort(p.getName(), net, HDLPort.Direction.IN, 0));
            else
                node.addOutput(new HDLPort(p.getName(), net, HDLPort.Direction.OUT, node.getBits(p.getName())));
        }
        nodes.add(node);
    }

    @Override
    public Iterator<HDLNode> iterator() {
        return nodes.iterator();
    }

    @Override
    public int getBits(String name) {
        for (HDLPort o : outputs)
            if (o.getName().equals(name))
                return o.getBits();
        return 0;
    }

    public String getElementName() {
        return elementName;
    }

    public ArrayList<HDLPort> getOutputs() {
        return outputs;
    }

    public ArrayList<HDLPort> getInputs() {
        return inputs;
    }

    public void traverse(HDLVisitor visitor) {
        for (HDLNode n : nodes)
            n.traverse(visitor);
    }

    @Override
    public String toString() {
        return "HDLCircuit{elementName='" + elementName + "'}";
    }
}

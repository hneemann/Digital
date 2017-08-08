package de.neemann.digital.hdl.model;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.basic.Not;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.*;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.model.InverterConfig;
import de.neemann.digital.draw.model.Net;
import de.neemann.digital.draw.model.NetList;
import de.neemann.digital.lang.Lang;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Hierarchical model of the circuit
 */
public class HDLModel implements HDLInterface, Iterable<HDLNode> {

    private final HashMap<VisualElement, HDLNode> nodeMap;
    private final Ports ports;
    private final Collection<Signal> signals;
    private int signalNumber;

    /**
     * Creates a new model
     *
     * @param circuit the circuit
     * @param library the library to use
     * @throws PinException             PinException
     * @throws HDLException             HDLException
     * @throws ElementNotFoundException ElementNotFoundException
     * @throws NodeException            NodeException
     */
    public HDLModel(Circuit circuit, ElementLibrary library) throws PinException, HDLException, ElementNotFoundException, NodeException {
        ports = new Ports();
        nodeMap = new HashMap<>();
        NetList nets = new NetList(circuit);
        HashMap<Net, Signal> signalMap = new HashMap<>();

        for (VisualElement v : circuit.getElements()) {
            if (v.equalsDescription(Clock.DESCRIPTION))
                addPort(v, nets, Port.Direction.in, 1, signalMap);
            if (v.equalsDescription(In.DESCRIPTION))
                addPort(v, nets, Port.Direction.in, v.getElementAttributes().getBits(), signalMap);
            else if (v.equalsDescription(Out.DESCRIPTION))
                addPort(v, nets, Port.Direction.out, v.getElementAttributes().getBits(), signalMap);
            else
                addNode(v, library);
        }

        HashMap<VisualElement, HDLNode> inverterNodes = new HashMap<>();
        HashMap<String, Signal> invertedSignals = new HashMap<>();

        for (HDLNode node : nodeMap.values()) {
            VisualElement ve = node.getVisualElement();
            InverterConfig inverterConfig = ve.getElementAttributes().get(Keys.INVERTER_CONFIG);
            Pins pins = ve.getPins();
            for (Pin p : pins) {
                Net n = nets.getNetOfPos(p.getPos());
                if (n == null)
                    throw new HDLException(Lang.get("err_netOfPin_N_notFound", p.getName()));
                final Signal s = signalMap.computeIfAbsent(n, Net -> new Signal("S" + (signalNumber++)));

                if (inverterConfig.contains(p.getName())) {
                    String invName = s.getName() + "_Neg";
                    Signal sNeg = invertedSignals.computeIfAbsent(invName, Net -> new Signal(invName));
                    s.copyBitsTo(sNeg);
                    VisualElement vi = new VisualElement(Not.DESCRIPTION.getName());
                    HDLNode negNode = new HDLNode(vi, library);
                    Ports negPorts = negNode.getPorts();
                    negPorts.get(0).ensure(Port.Direction.out).setSignal(sNeg);
                    negPorts.get(1).ensure(Port.Direction.in).setSignal(s);
                    inverterNodes.put(vi, negNode);
                    node.setPinToSignal(p, sNeg);
                } else
                    node.setPinToSignal(p, s);
            }
        }
        nodeMap.putAll(inverterNodes);

        for (Signal s : signalMap.values())
            s.checkBits();

        for (Signal s : invertedSignals.values())
            s.checkBits();

        if (invertedSignals.isEmpty())
            signals = signalMap.values();
        else {
            ArrayList<Signal> sigs = new ArrayList<>();
            sigs.addAll(signalMap.values());
            sigs.addAll(invertedSignals.values());
            signals = sigs;
        }
    }

    private void addNode(VisualElement v, ElementLibrary library) throws ElementNotFoundException, PinException, NodeException {
        nodeMap.put(v, new HDLNode(v, library));
    }

    private void addPort(VisualElement out, NetList nets, Port.Direction direction, int bits, HashMap<Net, Signal> signalMap) {
        String name = out.getElementAttributes().getCleanLabel();
        Port port = new Port(name, direction);
        port.setBits(bits);
        Net n = nets.getNetOfPos(out.getPins().get(0).getPos());
        signalMap.computeIfAbsent(n, Net -> new Signal(name).setIsPort()).addPort(port);
        ports.add(port);
    }

    @Override
    public Ports getPorts() {
        return ports;
    }

    @Override
    public Iterator<HDLNode> iterator() {
        return nodeMap.values().iterator();
    }

    /**
     * @return the number of nodes
     */
    public int size() {
        return nodeMap.size();
    }

    /**
     * @return the used signals
     */
    public Collection<Signal> getSignals() {
        return signals;
    }
}

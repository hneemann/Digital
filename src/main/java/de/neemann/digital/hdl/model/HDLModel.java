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

import java.io.File;
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
    private final File origin;
    private int signalNumber;
    private String name;

    /**
     * Creates a new model
     *
     * @param circuit   the circuit
     * @param library   the library to use
     * @param modelList here are the embedded models are stored
     * @throws PinException             PinException
     * @throws HDLException             HDLException
     * @throws ElementNotFoundException ElementNotFoundException
     * @throws NodeException            NodeException
     */
    public HDLModel(Circuit circuit, ElementLibrary library, ModelList modelList) throws PinException, HDLException, ElementNotFoundException, NodeException {
        origin = circuit.getOrigin();
        try {
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
                    addNode(v, library, modelList);
            }

            HashMap<VisualElement, HDLNode> inverterNodes = new HashMap<>();
            HashMap<String, Signal> invertedSignals = new HashMap<>();

            for (HDLNode node : nodeMap.values()) {
                VisualElement ve = node.getVisualElement();
                InverterConfig inverterConfig = ve.getElementAttributes().get(Keys.INVERTER_CONFIG);
                Pins pins = ve.getPins();
                for (Pin p : pins) {
                    Net n = nets.getNetOfPos(p.getPos());
                    if (n != null) {
                        final Signal s = signalMap.computeIfAbsent(n, Net -> new Signal("S" + (signalNumber++)));

                        if (inverterConfig.contains(p.getName())) {
                            String invName = s.getName() + "_Neg";
                            Signal sNeg = invertedSignals.computeIfAbsent(invName, Net -> {
                                Signal sNegL = new Signal(invName);
                                s.copyBitsTo(sNegL);

                                VisualElement vi = new VisualElement(Not.DESCRIPTION.getName());
                                try {
                                    HDLNode negNode = new HDLNode(vi, library, modelList);
                                    Ports negPorts = negNode.getPorts();
                                    negPorts.get(0).ensure(Port.Direction.out).setSignal(sNegL);
                                    s.addPort(negPorts.get(1).ensure(Port.Direction.in));
                                    inverterNodes.put(vi, negNode);
                                } catch (ElementNotFoundException | NodeException | HDLException | PinException e) {
                                    throw new RuntimeException(e);
                                }

                                return sNegL;
                            });
                            node.setPinToSignal(p, sNeg);
                        } else
                            node.setPinToSignal(p, s);
                    }
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
        } catch (HDLException | PinException | NodeException e) {
            e.setOrigin(origin);
            throw e;
        }
    }

    private void addNode(VisualElement v, ElementLibrary library, ModelList modelList) throws ElementNotFoundException, PinException, NodeException, HDLException {
        if (!v.equalsDescription(Tunnel.DESCRIPTION))
            nodeMap.put(v, new HDLNode(v, library, modelList));
    }

    private void addPort(VisualElement out, NetList nets, Port.Direction direction, int bits, HashMap<Net, Signal> signalMap) {
        String name = out.getElementAttributes().getCleanLabel();
        Port port = new Port(name, direction);
        port.setBits(bits);
        Net n = nets.getNetOfPos(out.getPins().get(0).getPos());
        signalMap.computeIfAbsent(n, Net -> new Signal(Port.PREFIX + name).setIsPort()).addPort(port);
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

    /**
     * @return the origin of this model
     */
    public File getOrigin() {
        return origin;
    }

    /**
     * The number of bits in i'th output
     *
     * @param i the number of the output
     * @return the number of bits
     */
    public int getOutputBits(int i) {
        return ports.getOutputs().get(i).getBits();
    }

    /**
     * Setd the name of the model
     *
     * @param name the name of the model
     * @return this for chained calls
     */
    public HDLModel setName(String name) {
        this.name = name.replace('.', '_').replace('/', '_').replace('\\', '_');
        return this;
    }

    /**
     * @return the name of the model
     */
    public String getName() {
        return name;
    }
}

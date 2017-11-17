package de.neemann.digital.hdl.model;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.basic.Not;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.io.PowerSupply;
import de.neemann.digital.core.io.Probe;
import de.neemann.digital.core.pld.PullDown;
import de.neemann.digital.core.pld.PullUp;
import de.neemann.digital.core.wiring.Break;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.*;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.model.InverterConfig;
import de.neemann.digital.draw.model.Net;
import de.neemann.digital.draw.model.NetList;
import de.neemann.digital.gui.components.data.DummyElement;
import de.neemann.digital.testing.TestCaseElement;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Hierarchical model of the circuit
 */
public class HDLModel implements HDLInterface, Iterable<HDLNode> {

    private final ArrayList<HDLNode> nodeList;
    private final Ports ports;
    private final ArrayList<Signal> signals;
    private final File origin;
    private final String description;
    private int signalNumber;
    private String name;
    private ArrayList<HDLClock> clocks;

    /**
     * Creates a new root model
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
        this(circuit, library, modelList, true);
    }

    /**
     * Creates a new model
     *
     * @param circuit   the circuit
     * @param library   the library to use
     * @param modelList here are the embedded models are stored
     * @param isRoot    true if this is the root circuit
     * @throws PinException             PinException
     * @throws HDLException             HDLException
     * @throws ElementNotFoundException ElementNotFoundException
     * @throws NodeException            NodeException
     */
    public HDLModel(Circuit circuit, ElementLibrary library, ModelList modelList, boolean isRoot) throws PinException, HDLException, ElementNotFoundException, NodeException {
        origin = circuit.getOrigin();
        description = circuit.getAttributes().get(Keys.DESCRIPTION);
        try {
            ports = new Ports();
            nodeList = new ArrayList<>();
            NetList nets = new NetList(circuit);
            HashMap<Net, Signal> signalMap = new HashMap<>();
            signals = new ArrayList<>();

            for (VisualElement v : circuit.getElements()) {
                if (v.equalsDescription(Clock.DESCRIPTION)) {
                    if (isRoot) {
                        Port cl = addPort(v, nets, Port.Direction.in, 1, signalMap);
                        if (clocks == null)
                            clocks = new ArrayList<>();
                        clocks.add(new HDLClock(cl, v.getElementAttributes().get(Keys.FREQUENCY)));
                    } else
                        addPort(v, nets, Port.Direction.in, v.getElementAttributes().getBits(), signalMap);
                } else if (v.equalsDescription(In.DESCRIPTION))
                    addPort(v, nets, Port.Direction.in, v.getElementAttributes().getBits(), signalMap);
                else if (v.equalsDescription(Out.DESCRIPTION))
                    addPort(v, nets, Port.Direction.out, v.getElementAttributes().getBits(), signalMap);
                else
                    addNode(v, library, modelList);
            }

            for (HDLNode node : nodeList) {
                VisualElement ve = node.getVisualElement();
                Pins pins = ve.getPins();
                for (Pin p : pins) {
                    Net n = nets.getNetOfPos(p.getPos());
                    if (n != null) {
                        final Signal s = signalMap.computeIfAbsent(n, Net -> createSignal());
                        node.setPinToSignal(p, s);
                    }
                }
            }
            for (Signal s : signalMap.values())
                s.checkBits();

            HashMap<String, Signal> negSignals = new HashMap<>();
            ArrayList<HDLNode> negNodes = new ArrayList<>();

            // handle the inverters at the components inputs
            for (HDLNode node : nodeList) {
                InverterConfig ic = node.get(Keys.INVERTER_CONFIG);
                if (!ic.isEmpty()) {
                    for (Port p : node.getPorts().getInputs()) {
                        if (ic.contains(p.getOrigName())) {
                            String negName = p.getSignal().getName() + "_Neg";
                            Signal nSig = negSignals.get(negName);
                            if (nSig == null) {
                                nSig = new Signal(negName);
                                nSig.setBits(p.getBits());
                                negSignals.put(negName, nSig);
                                signals.add(nSig);
                                VisualElement vi = new VisualElement(Not.DESCRIPTION.getName()).setAttribute(Keys.BITS, p.getBits());
                                HDLNode n = new HDLNode(vi, library, modelList);
                                n.getPorts().getInputs().get(0).setBits(p.getBits());
                                p.getSignal().addPort(n.getPorts().getInputs().get(0));
                                nSig.addPort(n.getPorts().getOutputs().get(0));
                                negNodes.add(n);
                            }
                            nSig.addPort(p);
                        }
                    }
                }
            }
            nodeList.addAll(negNodes);

        } catch (HDLException | PinException | NodeException e) {
            e.setOrigin(origin);
            throw e;
        }
    }

    private void addNode(VisualElement v, ElementLibrary library, ModelList modelList) throws ElementNotFoundException, PinException, NodeException, HDLException {
        if (!v.equalsDescription(Tunnel.DESCRIPTION)
                && !v.equalsDescription(Break.DESCRIPTION)
                && !v.equalsDescription(PullDown.DESCRIPTION)
                && !v.equalsDescription(PullUp.DESCRIPTION)
                && !v.equalsDescription(Probe.DESCRIPTION)
                && !v.equalsDescription(PowerSupply.DESCRIPTION)
                && !v.equalsDescription(DummyElement.TEXTDESCRIPTION)
                && !v.equalsDescription(DummyElement.DATADESCRIPTION)
                && !v.equalsDescription(TestCaseElement.TESTCASEDESCRIPTION))
            nodeList.add(new HDLNode(v, library, modelList));
    }

    /**
     * Adds a node to the model
     *
     * @param node the node to add
     */
    public void addNode(HDLNode node) {
        nodeList.add(node);
    }

    private Port addPort(VisualElement out, NetList nets, Port.Direction direction, int bits, HashMap<Net, Signal> signalMap) throws HDLException {
        String name = out.getElementAttributes().getCleanLabel();
        Port port = new Port(name, direction, out.getElementAttributes().get(Keys.DESCRIPTION));
        port.setPinNumber(out.getElementAttributes().get(Keys.PINNUMBER));
        port.setBits(bits);
        Net n = nets.getNetOfPos(out.getPins().get(0).getPos());
        signalMap.computeIfAbsent(n, Net -> {
            if (direction == Port.Direction.out) {
                return createSignal();
            } else {
                Signal s = new Signal(port.getName()).setIsPort(direction);
                signals.add(s);
                return s;
            }
        }).addPort(port);
        ports.add(port);
        return port;
    }

    /**
     * @return the description of this model
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return the used clocks or null if no clock is present
     */
    public ArrayList<HDLClock> getClocks() {
        return clocks;
    }

    @Override
    public Ports getPorts() {
        return ports;
    }

    @Override
    public Iterator<HDLNode> iterator() {
        return nodeList.iterator();
    }

    /**
     * @return the number of nodes
     */
    public int size() {
        return nodeList.size();
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
     * @throws HDLException if name is not valid
     */
    public HDLModel setName(String name) throws HDLException {
        this.name = Port.getHDLName(name.replace('.', '_').replace('/', '_').replace('\\', '_'));
        return this;
    }

    /**
     * @return the name of the model
     */
    public String getName() {
        return name;
    }

    /**
     * Creates a new signal.
     *
     * @return the new signal
     */
    public Signal createSignal() {
        Signal s = new Signal(signalNumber++);
        signals.add(s);
        return s;
    }

}

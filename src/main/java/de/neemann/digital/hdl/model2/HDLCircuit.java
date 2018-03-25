/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2;

import de.neemann.digital.core.BitsException;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.basic.Not;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.io.PowerSupply;
import de.neemann.digital.core.io.Probe;
import de.neemann.digital.core.pld.PullDown;
import de.neemann.digital.core.pld.PullUp;
import de.neemann.digital.core.wiring.Break;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.core.wiring.Splitter;
import de.neemann.digital.draw.elements.*;
import de.neemann.digital.draw.model.InverterConfig;
import de.neemann.digital.draw.model.Net;
import de.neemann.digital.draw.model.NetList;
import de.neemann.digital.gui.components.data.DummyElement;
import de.neemann.digital.hdl.model2.clock.ClockInfo;
import de.neemann.digital.hdl.model2.clock.HDLClockIntegrator;
import de.neemann.digital.hdl.model2.expression.ExprNot;
import de.neemann.digital.hdl.model2.expression.ExprVar;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.testing.TestCaseElement;

import java.io.IOException;
import java.util.*;

/**
 * The representation of a circuit
 */
public class HDLCircuit implements Iterable<HDLNode>, HDLModel.BitProvider, Printable {
    private final String elementName;
    private final ArrayList<HDLPort> outputs;
    private final ArrayList<HDLPort> inputs;
    private final ArrayList<HDLNet> listOfNets;
    private ArrayList<HDLPort> ports;
    private NetList netList;
    private ArrayList<HDLNode> nodes;
    private HashMap<Net, HDLNet> nets;
    private String hdlEntityName;

    /**
     * Creates a new instance
     *
     * @param circuit     the circuit
     * @param elementName the name of the circuit
     * @param c           the context to create the circuits
     * @throws PinException  PinException
     * @throws HDLException  HDLException
     * @throws NodeException NodeException
     */
    HDLCircuit(Circuit circuit, String elementName, HDLModel c) throws PinException, HDLException, NodeException {
        this(circuit, elementName, c, null);
    }

    /**
     * Creates a new instance.
     *
     * @param circuit         the circuit
     * @param elementName     the name of the circuit
     * @param c               the context to create the circuits
     * @param clockIntegrator the clock integrator
     * @throws PinException  PinException
     * @throws HDLException  HDLException
     * @throws NodeException NodeException
     */
    public HDLCircuit(Circuit circuit, String elementName, HDLModel c, HDLClockIntegrator clockIntegrator) throws PinException, HDLException, NodeException {
        this.elementName = elementName;

        if (elementName.toLowerCase().endsWith(".dig"))
            hdlEntityName = elementName.substring(0, elementName.length() - 4);
        else
            hdlEntityName = elementName;

        inputs = new ArrayList<>();
        outputs = new ArrayList<>();

        nodes = new ArrayList<>();
        nets = new HashMap<>();
        listOfNets = new ArrayList<>();
        netList = new NetList(circuit);

        ArrayList<ClockInfo> clocks = new ArrayList<>();

        try {
            for (VisualElement v : circuit.getElements()) {
                if (v.equalsDescription(In.DESCRIPTION) || v.equalsDescription(Clock.DESCRIPTION)) {
                    final HDLPort port = new HDLPort(
                            v.getElementAttributes().getCleanLabel(),
                            getNetOfPin(v.getPins().get(0)),
                            HDLPort.Direction.OUT,  // from inside the node this is an output because it defines a value
                            v.getElementAttributes().getBits())
                            .setPinNumber(v.getElementAttributes().get(Keys.PINNUMBER));
                    addInput(port);
                    if (v.equalsDescription(Clock.DESCRIPTION))
                        clocks.add(new ClockInfo(port, v.getElementAttributes().get(Keys.FREQUENCY)));
                } else if (v.equalsDescription(Out.DESCRIPTION))
                    addOutput(new HDLPort(
                            v.getElementAttributes().getCleanLabel(),
                            getNetOfPin(v.getPins().get(0)),
                            HDLPort.Direction.IN,  // from inside the node this is an input because it reads the value to output
                            v.getElementAttributes().getBits())
                            .setPinNumber(v.getElementAttributes().get(Keys.PINNUMBER)));
                else if (v.equalsDescription(Splitter.DESCRIPTION))
                    handleSplitter(c.createNode(v, this));
                else if (isRealElement(v))
                    nodes.add(c.createNode(v, this));
            }
        } catch (HDLException e) {
            throw new HDLException("error parsing " + circuit.getOrigin(), e);
        }

        netList = null;
        nets = null;

        if (clockIntegrator != null && !clocks.isEmpty())
            clockIntegrator.integrateClocks(this, clocks);

        for (HDLNet n : listOfNets)
            n.fixBits();

        // fix inverted inputs
        ArrayList<HDLNode> newNodes = new ArrayList<>();
        for (HDLNode n : nodes) {
            InverterConfig iv = n.getElementAttributes().get(Keys.INVERTER_CONFIG);
            if (!iv.isEmpty()) {
                for (HDLPort p : n.getInputs())
                    if (iv.contains(p.getName()))
                        newNodes.add(createNot(p, n));
            }
        }
        nodes.addAll(newNodes);

        for (HDLPort i : inputs)
            if (i.getNet() != null)
                i.getNet().setIsInput(i.getName());

        for (HDLPort o : outputs)
            if (o.getNet().needsVariable())
                o.getNet().setIsOutput(o.getName(), o.getNet().getInputs().size() == 1);

        nodes = new NodeSorter(inputs, nodes).sort();
    }

    private void handleSplitter(HDLNode node) throws BitsException, HDLException {
        Splitter.Ports inputSplit = new Splitter.Ports(node.getElementAttributes().get(Keys.INPUT_SPLIT));
        Splitter.Ports outputSplit = new Splitter.Ports(node.getElementAttributes().get(Keys.OUTPUT_SPLIT));
        if (node.getInputs().size() == 1) {
            nodes.add(new HDLNodeSplitterOneToMany(node, outputSplit));
            return;
        }
        if (node.getOutputs().size() == 1 && node.getOutput().getBits() == inputSplit.getBits()) {
            nodes.add(new HDLNodeSplitterManyToOne(node, inputSplit));
            return;
        }

        int bits = inputSplit.getBits();
        HDLNet net = new HDLNet(null);
        listOfNets.add(net);
        HDLPort left = new HDLPort("single", net, HDLPort.Direction.OUT, bits);
        HDLPort right = new HDLPort("single", net, HDLPort.Direction.IN, bits);

        HDLNodeSplitterManyToOne manyToOne = new HDLNodeSplitterManyToOne(node, inputSplit);
        HDLNodeSplitterOneToMany oneToMany = new HDLNodeSplitterOneToMany(node, outputSplit);

        manyToOne.getOutputs().clear();
        manyToOne.addPort(left);

        oneToMany.getInputs().clear();
        oneToMany.addPort(right);

        nodes.add(manyToOne);
        nodes.add(oneToMany);
    }

    private HDLNode createNot(HDLPort p, HDLNode node) throws HDLException, NodeException, PinException {
        final ElementAttributes attr = new ElementAttributes().setBits(p.getBits());
        HDLNodeExpression n = new HDLNodeExpression(Not.DESCRIPTION.getName(), attr, name -> p.getBits());
        HDLNet outNet = new HDLNet(null);
        listOfNets.add(outNet);
        HDLNet inNet = p.getNet();
        inNet.remove(p);

        n.addPort(new HDLPort(Not.DESCRIPTION.getInputDescription(attr).get(0).getName(), inNet, HDLPort.Direction.IN, p.getBits()));
        n.addPort(new HDLPort(Not.DESCRIPTION.getOutputDescriptions(attr).get(0).getName(), outNet, HDLPort.Direction.OUT, p.getBits()));

        p.setNet(outNet);
        node.replaceNet(inNet, outNet);

        n.setExpression(new ExprNot(new ExprVar(n.getInputs().get(0).getNet())));

        return n;
    }

    private void addOutput(HDLPort port) {
        outputs.add(port);
        ports = null;
    }

    private void addInput(HDLPort port) {
        inputs.add(port);
        ports = null;
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

    HDLNet getNetOfPin(Pin pin) {
        Net n = netList.getNetOfPos(pin.getPos());
        if (n == null)
            return null;

        return nets.computeIfAbsent(n, net -> {
            final HDLNet hdlNet = new HDLNet(createNetName(net));
            listOfNets.add(hdlNet);
            return hdlNet;
        });
    }

    private String createNetName(Net net) {
        final HashSet<String> labels = net.getLabels();
        if (labels.size() == 1)
            return labels.iterator().next();
        else
            return null;
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

    /**
     * @return the elements name
     */
    public String getElementName() {
        return elementName;
    }

    /**
     * @return the circuits outputs
     */
    public ArrayList<HDLPort> getOutputs() {
        return outputs;
    }

    /**
     * @return the circuits inputs
     */
    public ArrayList<HDLPort> getInputs() {
        return inputs;
    }

    /**
     * @return a list containing input and output ports
     */
    public ArrayList<HDLPort> getPorts() {
        if (ports == null) {
            ports = new ArrayList<>();
            ports.addAll(inputs);
            ports.addAll(outputs);
        }
        return ports;
    }

    @Override
    public String toString() {
        return "HDLCircuit{elementName='" + elementName + "'}";
    }

    /**
     * Merges logical operations if possible
     *
     * @return this for chained calls
     */
    public HDLCircuit mergeOperations() {
        nodes = new OperationMerger(nodes, this).merge();
        return this;
    }

    /**
     * Name the unnamed nets.
     *
     * @param netNaming the net naming algorithm
     * @return this for chained calls
     */
    public HDLCircuit nameNets(NetNaming netNaming) {
        for (HDLNet n : listOfNets)
            if (n.getName() == null)
                n.setName(netNaming.createName(n));
        return this;
    }

    /**
     * Names the nets with the default naming algorithm
     *
     * @return this for chained calls
     */
    public HDLCircuit nameNets() {
        return nameNets(new DefaultNetNaming());
    }


    @Override
    public void print(CodePrinter out) throws IOException {
        out.print("circuit ").println(elementName).inc();
        out.print("in");
        printList(out, inputs);
        out.print("out");
        printList(out, outputs);
        out.print("sig");
        printList(out, listOfNets);

        out.println();
        for (HDLNode n : nodes) {
            out.print("node ").println(n.getElementName()).inc();
            n.print(out);
            out.dec();
        }
        out.println();
        for (HDLPort p : outputs) {
            final HDLNet net = p.getNet();
            if (net.needsVariable() || net.isInput()) {
                p.print(out);
                out.print(" := ");
                net.print(out);
                out.println();
            }
        }

        out.dec().print("end circuit ").println(elementName);
    }

    private void printList(CodePrinter out, Collection<? extends Printable> ports) throws IOException {
        boolean first = true;
        for (Printable p : ports) {
            if (first) {
                first = false;
                out.print("(");
            } else
                out.print(", ");
            p.print(out);
        }
        if (first)
            out.print("(");
        out.println(")");
    }

    private void printList(CodePrinter out, ArrayList<HDLNet> nets) throws IOException {
        boolean first = true;
        for (HDLNet net : nets) {
            if (net.needsVariable()) {
                if (first) {
                    first = false;
                    out.print("(");
                } else
                    out.print(", ");
                net.print(out);
            }
        }
        if (first)
            out.print("(");
        out.println(")");
    }

    /**
     * Removed an obsolete net
     *
     * @param net the net to remove
     */
    public void removeNet(HDLNet net) {
        listOfNets.remove(net);
    }

    /**
     * @return the list of nets
     */
    public ArrayList<HDLNet> getNets() {
        return listOfNets;
    }

    /**
     * Renames the names in this model to satisfy constrains of the final target language.
     *
     * @param renaming the renaming algorithm
     */
    public void rename(HDLModel.Renaming renaming) {
        for (HDLPort p : outputs)
            p.rename(renaming);
        for (HDLPort p : inputs)
            p.rename(renaming);
        for (HDLNet p : listOfNets)
            p.rename(renaming);

        for (HDLNode n : nodes)
            n.rename(renaming);

        hdlEntityName = renaming.checkName(hdlEntityName);
    }

    /**
     * The entity name which should be used in the target language.
     *
     * @return the name
     */
    public String getHdlEntityName() {
        return hdlEntityName;
    }

    /**
     * Integrates a clock node.
     *
     * @param clock     the clock port
     * @param clockNode the new clock node
     * @throws HDLException HDLException
     */
    public void integrateClockNode(HDLPort clock, HDLNodeBuildIn clockNode) throws HDLException {
        HDLNet outNet = clock.getNet();
        HDLNet inNet = new HDLNet(null);
        outNet.resetOutput();
        clock.setNet(inNet);
        listOfNets.add(inNet);

        clockNode
                .addPort(new HDLPort("cout", outNet, HDLPort.Direction.OUT, 1))
                .addPort(new HDLPort("cin", inNet, HDLPort.Direction.IN, 1));

        nodes.add(clockNode);
    }

    /**
     * The net naming algorithm
     */
    public interface NetNaming {
        /**
         * Returns a nem for the given net
         *
         * @param n the net to name
         * @return the name to use
         */
        String createName(HDLNet n);
    }

    private class DefaultNetNaming implements NetNaming {
        private int num = 0;

        @Override
        public String createName(HDLNet n) {
            String name;
            do {
                name = "s" + (num++);
            } while (isDuplicate(name));
            return name;
        }

        private boolean isDuplicate(String name) {
            for (HDLPort p : inputs)
                if (p.getName().equalsIgnoreCase(name))
                    return true;
            for (HDLPort p : outputs)
                if (p.getName().equalsIgnoreCase(name))
                    return true;
            return false;
        }
    }
}

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
import de.neemann.digital.core.wiring.Break;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.core.wiring.Splitter;
import de.neemann.digital.draw.elements.*;
import de.neemann.digital.draw.library.GenericCode;
import de.neemann.digital.draw.library.GenericInitCode;
import de.neemann.digital.draw.model.Net;
import de.neemann.digital.draw.model.NetList;
import de.neemann.digital.gui.components.data.DummyElement;
import de.neemann.digital.gui.components.graphics.VGA;
import de.neemann.digital.hdl.model2.clock.ClockInfo;
import de.neemann.digital.hdl.model2.clock.HDLClockIntegrator;
import de.neemann.digital.hdl.model2.expression.ExprNot;
import de.neemann.digital.hdl.model2.expression.ExprVar;
import de.neemann.digital.hdl.model2.expression.Expression;
import de.neemann.digital.hdl.model2.optimizations.*;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestCaseElement;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * The representation of a circuit
 */
public class HDLCircuit implements Iterable<HDLNode>, HDLModel.BitProvider, Printable {
    private final String elementName;
    private final int depth;
    private final ArrayList<HDLPort> outputs;
    private final ArrayList<HDLPort> inputs;
    private final ArrayList<HDLNet> listOfNets;
    private final String description;
    private final File origin;
    private final ArrayList<HDLNode> nodes;
    private final boolean skipHDL;
    private ArrayList<HDLPort> ports;
    private NetList netList;
    private HashMap<Net, HDLNet> nets;
    private String hdlEntityName;

    /**
     * Creates a new instance
     *
     * @param circuit     the circuit
     * @param elementName the name of the circuit
     * @param c           the context to create the circuits
     * @param depth       the depth of this circuit in the circuits hierarchy
     * @throws PinException  PinException
     * @throws HDLException  HDLException
     * @throws NodeException NodeException
     */
    HDLCircuit(Circuit circuit, String elementName, HDLModel c, int depth) throws PinException, HDLException, NodeException {
        this(circuit, elementName, c, depth, null);
    }

    /**
     * Creates a new instance.
     *
     * @param circuit         the circuit
     * @param elementName     the name of the circuit
     * @param c               the context to create the circuits
     * @param depth           the depth of this circuit in the circuits hierarchy
     * @param clockIntegrator the clock integrator
     * @throws PinException  PinException
     * @throws HDLException  HDLException
     * @throws NodeException NodeException
     */
    public HDLCircuit(Circuit circuit, String elementName, HDLModel c, int depth, HDLClockIntegrator clockIntegrator) throws PinException, HDLException, NodeException {
        this.elementName = elementName;
        this.depth = depth;

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
        description = Lang.evalMultilingualContent(circuit.getAttributes().get(Keys.DESCRIPTION));
        this.origin = circuit.getOrigin();

        this.skipHDL = circuit.getAttributes().get(Keys.SKIP_HDL);

        ArrayList<ClockInfo> clocks = new ArrayList<>();

        try {
            for (VisualElement v : circuit.getElements()) {
                if (v.equalsDescription(In.DESCRIPTION) || v.equalsDescription(Clock.DESCRIPTION)) {
                    final HDLPort port = new HDLPort(
                            v.getElementAttributes().getLabel(),
                            getNetOfPin(v.getPins().get(0)),
                            HDLPort.Direction.OUT,  // from inside the node this is an output because it defines a value
                            v.getElementAttributes().getBits())
                            .setPinNumber(v.getElementAttributes().get(Keys.PINNUMBER))
                            .setDescription(Lang.evalMultilingualContent(v.getElementAttributes().get(Keys.DESCRIPTION)));
                    if (v.equalsDescription(Clock.DESCRIPTION)) {
                        clocks.add(new ClockInfo(port, v.getElementAttributes().get(Keys.FREQUENCY)));
                        port.setIsClock();
                    }
                    addInput(port);
                } else if (v.equalsDescription(Out.DESCRIPTION))
                    addOutput(new HDLPort(
                            v.getElementAttributes().getLabel(),
                            getNetOfPin(v.getPins().get(0)),
                            HDLPort.Direction.IN,  // from inside the node this is an input because it reads the value to output
                            v.getElementAttributes().getBits())
                            .setPinNumber(v.getElementAttributes().get(Keys.PINNUMBER))
                            .setDescription(Lang.evalMultilingualContent(v.getElementAttributes().get(Keys.DESCRIPTION))));
                else if (v.equalsDescription(Splitter.DESCRIPTION))
                    handleSplitter(c.createNode(v, this));
                else if (isRealElement(v))
                    nodes.add(c.createNode(v, this));
            }
        } catch (HDLException e) {
            throw new HDLException(Lang.get("err_errorAnalysingCircuit_N", circuit.getOrigin()), e);
        }

        netList = null;
        nets = null;

        if (clockIntegrator != null && !clocks.isEmpty())
            clockIntegrator.integrateClocks(this, clocks);

        for (HDLNet n : listOfNets)
            n.fixBits();

        for (HDLNet n : listOfNets)
            n.checkPinControlUsage();

        for (HDLPort i : inputs)
            if (i.getNet() != null) {
                i.getNet().setIsInput(i.getName());
                if (i.getNet().isInOutNet())
                    i.setInOut();
            }

        for (HDLPort o : outputs) {
            if (o.getNet().needsVariable())
                o.getNet().setIsOutput(o.getName(), o.getNet().getInputs().size() == 1);
            if (o.getNet().isInOutNet())
                o.setInOut();
        }

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

    HDLNet createNot(HDLNet inNet) throws HDLException, NodeException, PinException {
        int bits = 1;
        final ElementAttributes attr = new ElementAttributes().setBits(bits);
        HDLNodeAssignment n = new HDLNodeAssignment(Not.DESCRIPTION.getName(), attr, name -> bits);
        HDLNet outNet = new HDLNet(null);
        listOfNets.add(outNet);

        HDLPort notOut = new HDLPort(Not.DESCRIPTION.getOutputDescriptions(attr).get(0).getName(), outNet, HDLPort.Direction.OUT, 0);
        n.addPort(notOut);
        n.addPort(new HDLPort(Not.DESCRIPTION.getInputDescription(attr).get(0).getName(), inNet, HDLPort.Direction.IN, 0) {
            @Override
            public void setBits(int bits) {
                super.setBits(bits);
                notOut.setBits(bits);
            }
        });

        n.setExpression(new ExprNot(new ExprVar(inNet)));

        nodes.add(n);

        return outNet;
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
                && !v.equalsDescription(Probe.DESCRIPTION)
                && !v.equalsDescription(VGA.DESCRIPTION)
                && !v.equalsDescription(PowerSupply.DESCRIPTION)
                && !v.equalsDescription(DummyElement.TEXTDESCRIPTION)
                && !v.equalsDescription(DummyElement.DATADESCRIPTION)
                && !v.equalsDescription(DummyElement.RECTDESCRIPTION)
                && !v.equalsDescription(TestCaseElement.DESCRIPTION)
                && !v.equalsDescription(GenericInitCode.DESCRIPTION)
                && !v.equalsDescription(GenericCode.DESCRIPTION);
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
     * Name the unnamed nets.
     *
     * @param netNaming the net naming algorithm
     * @return this for chained calls
     */
    public HDLCircuit nameUnnamedSignals(NetNaming netNaming) {
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
    public HDLCircuit nameUnnamedSignals() {
        return nameUnnamedSignals(new DefaultNetNaming());
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
     * @throws HDLException HDLException
     */
    public void rename(HDLModel.Renaming renaming) throws HDLException {
        try {
            for (HDLPort p : outputs)
                p.rename(renaming);
            for (HDLPort p : inputs)
                p.rename(renaming);
            for (HDLNet p : listOfNets)
                p.rename(renaming);

            for (HDLNode n : nodes)
                n.rename(renaming);

            hdlEntityName = renaming.checkName(hdlEntityName);

            checkUnique(getPorts());
            checkUnique(listOfNets);
        } catch (HDLException e) {
            e.setOrigin(origin);
            throw e;
        }
    }

    private void checkUnique(Collection<? extends HasName> names) throws HDLException {
        HashSet<String> set = new HashSet<>();
        for (HasName hn : names) {
            String name = hn.getName();
            if (set.contains(name))
                throw new HDLException(Lang.get("err_namesAreNotUnique_N", name));
            else
                set.add(name);
        }
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
     * @return the description of this circuit
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return true if the circuit has a description
     */
    public boolean hasDescription() {
        return description != null && description.trim().length() > 0;
    }

    /**
     * @return true if HDL export should be skipped for this circuit
     */
    public boolean shouldSkipHDLExport() {
        return this.skipHDL;
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

        if (outNet == null)
            throw new HDLException(Lang.get("err_clockIsNotUsed"));

        outNet.resetOutput();
        clock.setNet(inNet);
        listOfNets.add(inNet);

        clockNode
                .addPort(new HDLPort("cout", outNet, HDLPort.Direction.OUT, 1))
                .addPort(new HDLPort("cin", inNet, HDLPort.Direction.IN, 1));

        clockNode.createExpressions();

        nodes.add(clockNode);
    }

    /**
     * @return the list of nodes
     */
    public ArrayList<HDLNode> getNodes() {
        return nodes;
    }

    /**
     * Applies the given optimization to this circuit
     *
     * @param optimization the optimization
     * @return this for chained calls
     * @throws HDLException HDLException
     */
    public HDLCircuit apply(Optimization optimization) throws HDLException {
        try {
            optimization.optimize(this);
        } catch (HDLException e) {
            e.setOrigin(origin);
            throw e;
        }
        return this;
    }

    /**
     * Applies the default optimizations to the model.
     * Should be sufficient for VHDL and Verilog.
     *
     * @return this for chained calls
     * @throws HDLException HDLException
     */
    public HDLCircuit applyDefaultOptimizations() throws HDLException {
        apply(new ReplaceOneToMany());
        apply(new MergeAssignments());
        apply(new OptimizeExpressions(new ExprNot.OptimizeNotNot()));
        apply(new InlineManyToOne());
        apply(new RemoveConstantSignals());
        apply(new MergeConstants());  // under certain circumstances there are still constants
        apply(new NameConstantSignals());
        apply(new NodeSorterExpressionBased());
        return nameUnnamedSignals();
    }

    /**
     * Called to replace a net by an expression
     *
     * @param net        the net to replace
     * @param expression the expression to use instead
     */
    public void replaceNetByExpression(HDLNet net, Expression expression) {
        for (HDLNode n : nodes)
            n.replaceNetByExpression(net, expression);
    }

    /**
     * @return the origin of this HDLCircuit
     */
    public File getOrigin() {
        return origin;
    }

    /**
     * @return the depth of this circuit in the circuits hierarchy
     */
    public int getDepth() {
        return depth;
    }

    /**
     * The net naming algorithm
     */
    public interface NetNaming {
        /**
         * Returns a name for the given net
         *
         * @param n the net to name
         * @return the name to use
         */
        String createName(HDLNet n);
    }

    private final class DefaultNetNaming implements NetNaming {
        private final HashSet<String> map;
        private int num = 0;

        private DefaultNetNaming() {
            this.map = new HashSet<>();
            for (HDLPort p : inputs)
                map.add(p.getName().toLowerCase());
            for (HDLPort p : outputs)
                map.add(p.getName().toLowerCase());
            for (HDLNet n : listOfNets)
                if (n.getName() != null)
                    map.add(n.getName().toLowerCase());
        }

        @Override
        public String createName(HDLNet n) {
            String name;
            do {
                name = "s" + (num++);
            } while (isDuplicate(name));
            return name;
        }

        private boolean isDuplicate(String name) {
            return map.contains(name.toLowerCase());
        }
    }
}

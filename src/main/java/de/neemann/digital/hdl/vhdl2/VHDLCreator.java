/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl2;

import de.neemann.digital.core.Bits;
import de.neemann.digital.core.wiring.Splitter;
import de.neemann.digital.hdl.hgs.HGSEvalException;
import de.neemann.digital.hdl.model2.*;
import de.neemann.digital.hdl.model2.expression.*;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.vhdl2.entities.VHDLEntity;

import java.io.IOException;
import java.util.HashSet;

/**
 * Create the vhdl output
 */
public class VHDLCreator {
    private final CodePrinter out;
    private final VHDLLibrary library;
    private HashSet<String> customPrinted;

    /**
     * Creates a new instance
     *
     * @param out   the output stream
     */
    VHDLCreator(CodePrinter out) {
        this.out = out;
        library = new VHDLLibrary();
        customPrinted = new HashSet<>();
    }

    /**
     * Returns the vhdl type name
     *
     * @param bits the number of bits
     * @return the types name
     */
    public static String getType(int bits) {
        if (bits == 1)
            return "std_logic";
        else
            return "std_logic_vector(" + (bits - 1) + " downto 0)";
    }

    /**
     * Creates a vhdl value
     *
     * @param val  the value
     * @param bits the bit number
     * @return the value as vhdl code
     */
    public static String value(long val, int bits) {
        String s = Long.toBinaryString(val & Bits.mask(bits));
        while (s.length() < bits)
            s = "0" + s;

        if (bits > 1)
            s = "\"" + s + "\"";
        else
            s = "'" + s + "'";

        return s;
    }

    private void printNodeBuiltIn(HDLNodeBuildIn node) throws HDLException, IOException, HGSEvalException {
        VHDLEntity entity = library.getEntity(node);
        String hdlEntityName = entity.print(out, node);
        node.setHdlEntityName(hdlEntityName);
    }

    private void printNodeCustom(HDLNodeCustom node) throws HDLException, IOException, HGSEvalException {
        if (!customPrinted.contains(node.getElementName()))
            printHDLCircuit(node.getCircuit());
    }

    /**
     * Prints the given circuit to the output.
     * Also all needed entities are printed.
     *
     * @param circuit the circuit to print
     * @throws IOException      IOException
     * @throws HDLException     HDLException
     * @throws HGSEvalException HGSEvalException
     */
    public void printHDLCircuit(HDLCircuit circuit) throws IOException, HDLException, HGSEvalException {
        // at first print all used entities to maintain the correct order
        for (HDLNode node : circuit)
            if (node instanceof HDLNodeBuildIn)
                printNodeBuiltIn((HDLNodeBuildIn) node);
            else if (node instanceof HDLNodeCustom)
                printNodeCustom((HDLNodeCustom) node);

        // after that print this entity

        out.println()
                .println("LIBRARY ieee;")
                .println("USE ieee.std_logic_1164.all;")
                .println("USE ieee.numeric_std.all;")
                .println();

        out.print("entity ").print(circuit.getHdlEntityName()).println(" is").inc();
        writePorts(out, circuit);
        out.dec();
        out.print("end ").print(circuit.getHdlEntityName()).println(";");
        out.println();
        out.print("architecture Behavioral of " + circuit.getHdlEntityName()).println(" is").inc();

        for (HDLNet net : circuit.getNets())
            if (net.needsVariable())
                out.print("signal ").print(net.getName()).print(": ").print(getType(net.getBits())).println(";");


        out.dec().println("begin").inc();

        int num = 0;
        for (HDLNode node : circuit)
            if (node instanceof HDLNodeExpression)
                printExpression((HDLNodeExpression) node);
            else if (node instanceof HDLNodeBuildIn)
                printEntityInstantiation(node, num++);
            else if (node instanceof HDLNodeCustom)
                printEntityInstantiation(node, num++);
            else if (node instanceof HDLNodeSplitterOneToMany)
                printOneToMany((HDLNodeSplitterOneToMany) node);
            else if (node instanceof HDLNodeSplitterManyToOne)
                printManyToOne((HDLNodeSplitterManyToOne) node);
            else
                throw new HDLException("Not yet implemented: " + node.getClass().getSimpleName());

        for (HDLPort p : circuit.getOutputs()) {
            final HDLNet net = p.getNet();
            if (net.needsVariable() || net.isInput())
                out.print(p.getName()).print(" <= ").print(p.getNet().getName()).println(";");
        }

        out.dec().println("end Behavioral;");
    }

    /**
     * Writes the ports of the given circuit
     *
     * @param out     the stream to write to
     * @param circuit the circuit
     * @throws IOException IOException
     */
    public static void writePorts(CodePrinter out, HDLCircuit circuit) throws IOException {
        out.println("port (").inc();

        Separator sep = new Separator(";\n");

        for (HDLPort i : circuit.getInputs()) {
            sep.check(out);
            out.print(i.getName()).print(": in ").print(getType(i.getBits()));
        }
        for (HDLPort o : circuit.getOutputs()) {
            sep.check(out);
            out.print(o.getName()).print(": out ").print(getType(o.getBits()));
        }
        out.println(");").dec();
    }

    private void printManyToOne(HDLNodeSplitterManyToOne node) throws IOException {
        String target = node.getTargetSignal();
        Splitter.Ports is = node.getInputSplit();
        int i = 0;
        for (HDLPort in : node.getInputs()) {
            Splitter.Port sp = is.getPort(i++);
            out.print(target).print("(");
            if (in.getBits() == 1)
                out.print(sp.getPos());
            else
                out.print(sp.getPos() + sp.getBits() - 1).print(" downto ").print(sp.getPos());
            out.print(") <= ").print(in.getNet().getName()).println(";");
        }
    }

    private void printOneToMany(HDLNodeSplitterOneToMany node) throws IOException {
        String source = node.getSourceSignal();
        Splitter.Ports is = node.getOutputSplit();
        int i = 0;
        for (HDLPort outPort : node.getOutputs()) {
            Splitter.Port sp = is.getPort(i++);
            if (outPort.getNet() != null) {
                out.print(outPort.getNet().getName()).print(" <= ").print(source).print("(");
                if (outPort.getBits() == 1)
                    out.print(sp.getPos());
                else
                    out.print(sp.getPos() + sp.getBits() - 1).print(" downto ").print(sp.getPos());
                out.println(");");
            }
        }
    }

    private void printEntityInstantiation(HDLNode node, int num) throws IOException, HDLException {
        String entityName = node.getHdlEntityName();

        out.print("gate").print(num).print(": entity work.").println(entityName).inc();
        if (!(node instanceof HDLNodeCustom))
            library.getEntity(node).writeGenericMap(out, node);
        out.println("port map (").inc();
        Separator sep = new Separator(",\n");
        for (HDLPort i : node.getInputs())
            if (i.getNet() != null) {
                sep.check(out);
                out.print(i.getName()).print(" => ").print(i.getNet().getName());
            }

        for (HDLPort o : node.getOutputs())
            if (o.getNet() != null) {
                sep.check(out);
                out.print(o.getName()).print(" => ").print(o.getNet().getName());
            }
        out.println(");").dec().dec();
    }

    private void printExpression(HDLNodeExpression node) throws IOException, HDLException {
        out.print(node.getTargetSignal()).print(" <= ");
        printExpression(node.getExpression());
        out.println(";");
    }

    private void printExpression(Expression expression) throws IOException, HDLException {
        if (expression instanceof ExprVar)
            out.print(((ExprVar) expression).getNet().getName());
        else if (expression instanceof ExprConstant) {
            final ExprConstant constant = (ExprConstant) expression;
            out.print(value(constant.getValue(), constant.getBits()));
        } else if (expression instanceof ExprNot) {
            out.print("NOT ");
            printExpression(((ExprNot) expression).getExpression());
        } else if (expression instanceof ExprOperate) {
            out.print("(");
            boolean first = true;
            final ExprOperate operate = (ExprOperate) expression;

            String op;
            switch (operate.getOperation()) {
                case OR:
                    op = " OR ";
                    break;
                case AND:
                    op = " AND ";
                    break;
                case XOR:
                    op = " XOR ";
                    break;
                default:
                    throw new HDLException("unknown operation " + operate.getOperation());
            }
            for (Expression exp : operate.getOperands()) {
                if (first)
                    first = false;
                else
                    out.print(op);
                printExpression(exp);
            }
            out.print(")");
        }
    }
}

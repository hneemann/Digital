/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl2;

import de.neemann.digital.core.Bits;
import de.neemann.digital.core.wiring.Splitter;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.hdl.hgs.HGSEvalException;
import de.neemann.digital.hdl.model2.*;
import de.neemann.digital.hdl.model2.expression.*;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.vhdl2.entities.VHDLEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

/**
 * Create the vhdl output.
 * Used to print a optimized {@link de.neemann.digital.hdl.model2.HDLModel} as VHDL
 * code to a {@link CodePrinter} instance.
 */
public class VHDLCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(VHDLCreator.class);
    private static final String ZEROS = "0000000000000000000000000000000000000000000000000000000000000000";
    private final CodePrinter out;
    private final VHDLLibrary library;
    private HashSet<String> customPrinted;

    /**
     * Creates a new instance
     *
     * @param out the output stream
     */
    VHDLCreator(CodePrinter out, ElementLibrary lib) {
        this.out = out;
        library = new VHDLLibrary(lib);
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
     * @param con the constant
     * @return the value as vhdl code
     */
    public static String value(ExprConstant con) {
        return value(con.getValue(), con.getBits());
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
        int missing = bits - s.length();
        if (missing > 0)
            s = ZEROS.substring(0, missing) + s;

        if (bits > 1)
            s = "\"" + s + "\"";
        else
            s = "'" + s + "'";

        return s;
    }

    private void printNodeBuiltIn(HDLNodeBuildIn node, File root) throws HDLException, IOException, HGSEvalException {
        VHDLEntity entity = library.getEntity(node);
        String hdlEntityName = entity.print(out, node, root);
        node.setHdlEntityName(hdlEntityName);
    }

    private void printNodeCustom(HDLNodeCustom node, File root) throws HDLException, IOException, HGSEvalException {
        if (!customPrinted.contains(node.getElementName())) {
            printHDLCircuit(node.getCircuit(), root);
            customPrinted.add(node.getElementName());
        }
    }

    /**
     * Prints the given circuit to the output.
     * Also all needed entities are printed.
     *
     * @param circuit the circuit to print
     * @param root    the projects main folder
     * @throws IOException      IOException
     * @throws HDLException     HDLException
     * @throws HGSEvalException HGSEvalException
     */
    public void printHDLCircuit(HDLCircuit circuit, File root) throws IOException, HDLException, HGSEvalException {
        // skip the HDL export and any dependant circuits
        if (circuit.shouldSkipHDLExport()) {
            return;
        }

        // at first print all used entities to maintain the correct order
        for (HDLNode node : circuit)
            if (node instanceof HDLNodeCustom)
                printNodeCustom((HDLNodeCustom) node, root);
            else if (node instanceof HDLNodeBuildIn)
                printNodeBuiltIn((HDLNodeBuildIn) node, root);

        LOGGER.info("export " + circuit.getElementName());

        // after that print this entity
        out.println()
                .println("LIBRARY ieee;")
                .println("USE ieee.std_logic_1164.all;")
                .println("USE ieee.numeric_std.all;")
                .println();

        if (circuit.hasDescription())
            out.printComment("-- ", circuit.getDescription());

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
            if (node instanceof HDLNodeAssignment)
                printExpression((HDLNodeAssignment) node);
            else if (node instanceof HDLNodeBuildIn)
                printEntityInstantiation((HDLNodeBuildIn) node, num++, root);
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

        Separator sep = new Separator(out, ";\n");

        for (HDLPort i : circuit.getInputs()) {
            sep.check();
            out.print(i.getName()).print(": ").print(getDir(i.getDirection(), "in")).print(" ").print(getType(i.getBits()));
            if (i.hasDescription()) sep.setLineFinalizer(ou -> ou.printComment(" -- ", i.getDescription()));
        }
        for (HDLPort o : circuit.getOutputs()) {
            sep.check();
            out.print(o.getName()).print(": ").print(getDir(o.getDirection(), "out")).print(" ").print(getType(o.getBits()));
            if (o.hasDescription()) sep.setLineFinalizer(ou -> ou.printComment(" -- ", o.getDescription()));
        }
        sep.close();
        out.println(");").dec();
    }

    private static String getDir(HDLPort.Direction direction, String def) {
        if (direction == HDLPort.Direction.INOUT)
            return "inout";
        return def;
    }

    private void printManyToOne(HDLNodeSplitterManyToOne node) throws IOException, HDLException {
        String target = node.getTargetSignal();

        if (target != null) {
            for (HDLNodeSplitterManyToOne.SplitterAssignment in : node) {
                out.print(target).print("(");
                if (in.getLsb() == in.getMsb())
                    out.print(in.getLsb());
                else
                    out.print(in.getMsb()).print(" downto ").print(in.getLsb());
                out.print(") <= ");
                printExpression(in.getExpression());
                out.println(";");
            }
        }
    }

    /**
     * After ReplaceOneToMany optimization there are no such nodes in the model!
     */
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

    private void printEntityInstantiation(HDLNodeBuildIn node, int num, File root) throws IOException, HDLException {
        String entityName = node.getHdlEntityName();

        out.print("gate").print(num).print(": entity work.").print(entityName);

        final String label = node.getElementAttributes().getLabel();
        if (label != null && label.length() > 0)
            out.print(" -- ").print(label.replace('\n', ' '));

        out.println().inc();
        if (!(node instanceof HDLNodeCustom))
            library.getEntity(node).writeGenericMap(out, node, root);
        out.println("port map (").inc();
        Separator sep = new Separator(out, ",\n");
        for (HDLNodeBuildIn.InputAssignment i : node) {
            sep.check();
            out.print(i.getTargetName()).print(" => ");
            printExpression(i.getExpression());
        }

        for (HDLPort o : node.getOutputs())
            if (o.getNet() != null) {
                sep.check();
                out.print(o.getName()).print(" => ").print(o.getNet().getName());
            }
        for (HDLPort o : node.getInOutputs())
            if (o.getNet() != null) {
                sep.check();
                out.print(o.getName()).print(" => ").print(o.getNet().getName());
            }
        out.println(");").dec().dec();
    }

    private void printExpression(HDLNodeAssignment node) throws IOException, HDLException {
        if (node.getTargetNet() != null) {
            out.print(node.getTargetNet().getName()).print(" <= ");
            printExpression(node.getExpression());
            out.println(";");
        }
    }

    private void printExpression(Expression expression) throws IOException, HDLException {
        if (expression instanceof ExprVar)
            out.print(((ExprVar) expression).getNet().getName());
        else if (expression instanceof ExprVarRange) {
            final ExprVarRange evr = (ExprVarRange) expression;
            out.print(evr.getNet().getName()).print("(");
            if (evr.getMsb() == evr.getLsb())
                out.print(evr.getMsb());
            else
                out.print(evr.getMsb()).print(" downto ").print(evr.getLsb());
            out.print(")");
        } else if (expression instanceof ExprConstant) {
            final ExprConstant constant = (ExprConstant) expression;
            out.print(value(constant));
        } else if (expression instanceof ExprNot) {
            out.print("NOT ");
            Expression inner = ((ExprNot) expression).getExpression();
            if (inner instanceof ExprNot) { // Quartus does not like a NOT NOT
                out.print("(");
                printExpression(inner);
                out.print(")");
            } else
                printExpression(inner);
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
        } else
            throw new HDLException("expression type " + expression.getClass().getSimpleName() + " unknown");
    }
}

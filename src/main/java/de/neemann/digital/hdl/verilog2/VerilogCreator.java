/*
 * Copyright (c) 2018 Ivan Deras.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog2;

import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.hdl.vhdl2.*;
import de.neemann.digital.core.Bits;
import de.neemann.digital.core.wiring.Splitter;
import de.neemann.digital.hdl.hgs.HGSEvalException;
import de.neemann.digital.hdl.model2.*;
import de.neemann.digital.hdl.model2.expression.*;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.verilog2.lib.VerilogElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;

/**
 * Create the verilog output
 */
public class VerilogCreator {
    private static final Logger LOGGER = LoggerFactory.getLogger(VerilogCreator.class);
    private final CodePrinter out;
    private final VerilogLibrary library;
    private final HashSet<String> customPrinted;

    /**
     * Creates a new instance
     *
     * @param out the output stream
     */
    VerilogCreator(CodePrinter out, ElementLibrary lib) {
        this.out = out;
        library = new VerilogLibrary(lib);
        customPrinted = new HashSet<>();
    }

    /**
     * Returns the verilog bit range
     *
     * @param bits the number of bits
     * @return the bit range
     */
    public static String getRange(int bits) {
        if (bits == 1)
            return "";
        else
            return "[" + (bits - 1) + ":0]";
    }

    /**
     * Returns the verilog type for a signal
     *
     * @param def  the signal type (input or output) used if dir is not "inout"
     * @param dir  used to check if direction is "inout"
     * @param bits the number of bits
     * @return the verilog signal type
     */
    public static String getType(HDLPort.Direction def, HDLPort.Direction dir, int bits) {
        String result;
        if (dir == HDLPort.Direction.INOUT)
            result = "inout";
        else
            result = (def == HDLPort.Direction.IN) ? "input" : "output";

        if (bits > 1) {
            result += " [" + (bits - 1) + ":0]";
        }

        return result;
    }

    /**
     * Creates a verilog value
     *
     * @param con the constant
     * @return the value as vhdl code
     */
    public static String value(ExprConstant con) {
        return value(con.getValue(), con.getBits());
    }

    /**
     * Creates a verilog value
     *
     * @param val  the value
     * @param bits the bit number
     * @return the value as vhdl code
     */
    public static String value(long val, int bits) {
        String s = Long.toBinaryString(val & Bits.mask(bits));

        return (bits + "'b" + s);
    }

    private void printNodeBuiltIn(HDLNodeBuildIn node, File root) throws HDLException, IOException, HGSEvalException {
        VerilogElement elem = library.getVerilogElement(node);
        String hdlEntityName = elem.print(out, node, root);
        node.setHdlEntityName(hdlEntityName);
    }

    private void printNodeCustom(HDLNodeCustom node, File root) throws HDLException, IOException, HGSEvalException {
        if (!customPrinted.contains(node.getElementName())) {
            printHDLCircuit(node.getCircuit(), node.getHdlEntityName(), root);
            customPrinted.add(node.getElementName());
        }
    }

    /**
     * Prints the given circuit to the output.
     * Also all needed entities are printed.
     *
     * @param circuit    the circuit to print
     * @param moduleName the module name
     * @param root       the projects main folder
     * @throws IOException      IOException
     * @throws HDLException     HDLException
     * @throws HGSEvalException HGSEvalException
     */
    public void printHDLCircuit(HDLCircuit circuit, String moduleName, File root) throws IOException, HDLException, HGSEvalException {
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

        LOGGER.info("export " + moduleName);

        out.println();
        if (circuit.hasDescription())
            out.printComment("// ", circuit.getDescription());

        out.print("module ").print(moduleName).println(" (").inc();
        writePorts(out, circuit);
        out.dec();
        out.println().println(");");

        out.inc();
        for (HDLNet net : circuit.getNets()) {
            if (net.needsVariable()) {
                String range = "";

                if (net.getBits() > 1) {
                    range += " [" + (net.getBits() - 1) + ":0]";
                }
                out.print("wire").print(range).print(" ").print(net.getName()).println(";");
            }
        }

        int num = 0;
        for (HDLNode node : circuit)
            if (node instanceof HDLNodeAssignment)
                printExpression((HDLNodeAssignment) node);
            else if (node instanceof HDLNodeBuildIn)
                printModuleInstantiation((HDLNodeBuildIn) node, num++, root);
            else if (node instanceof HDLNodeSplitterOneToMany)
                printOneToMany((HDLNodeSplitterOneToMany) node);
            else if (node instanceof HDLNodeSplitterManyToOne)
                printManyToOne((HDLNodeSplitterManyToOne) node);
            else
                throw new HDLException("Not yet implemented: " + node.getClass().getSimpleName());

        for (HDLPort p : circuit.getOutputs()) {
            final HDLNet net = p.getNet();
            if (net.needsVariable() || net.isInput())
                out.print("assign ").print(p.getName()).print(" = ").print(p.getNet().getName()).println(";");
        }

        out.dec().println("endmodule");
    }

    /**
     * Writes the ports of the given circuit
     *
     * @param out     the stream to write to
     * @param circuit the circuit
     * @throws IOException IOException
     */
    public static void writePorts(CodePrinter out, HDLCircuit circuit) throws IOException {
        Separator sep = new Separator(out, ",\n");

        for (HDLPort i : circuit.getInputs()) {
            sep.check();
            out.print(getType(HDLPort.Direction.IN, i.getDirection(), i.getBits())).print(" ").print(i.getName());
            if (i.hasDescription()) sep.setLineFinalizer(ou -> ou.printComment(" // ", i.getDescription()));
        }
        for (HDLPort o : circuit.getOutputs()) {
            sep.check();
            out.print(getType(HDLPort.Direction.OUT, o.getDirection(), o.getBits())).print(" ").print(o.getName());
            if (o.hasDescription()) sep.setLineFinalizer(ou -> ou.printComment(" // ", o.getDescription()));
        }
        sep.close();
    }

    private void printManyToOne(HDLNodeSplitterManyToOne node) throws IOException, HDLException {
        String target = node.getTargetSignal();
        if (target != null) {
            for (HDLNodeSplitterManyToOne.SplitterAssignment in : node) {
                out.print("assign ").print(target).print("[");
                if (in.getLsb() == in.getMsb())
                    out.print(in.getLsb());
                else
                    out.print(in.getMsb()).print(":").print(in.getLsb());
                out.print("] = ");
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

    private void printModuleInstantiation(HDLNodeBuildIn node, int num, File root) throws IOException, HDLException {
        String entityName = node.getHdlEntityName();

        final String label = node.getElementAttributes().getLabel();
        if (label != null && label.length() > 0)
            out.print("// ").println(label.replace('\n', ' '));

        out.print(entityName).print(" ");

        if (!(node instanceof HDLNodeCustom)) {
            library.getVerilogElement(node).writeGenericMap(out, node, root);
        }

        // entityName can have an space at the end if the identifier is escaped
        String instanceName = entityName.trim() + "_i" + num;

        out.print(instanceName + " ")
                .println("(");

        out.inc();
        Separator sep = new Separator(out, ",\n");
        for (HDLNodeBuildIn.InputAssignment i : node) {
            sep.check();
            out.print(".").print(i.getTargetName()).print("( ");
            printExpression(i.getExpression());
            out.print(" )");
        }

        for (HDLPort o : node.getOutputs())
            if (o.getNet() != null) {
                sep.check();
                out.print(".").print(o.getName()).print("( ").print(o.getNet().getName()).print(" )");
            }

        for (HDLPort o : node.getInOutputs())
            if (o.getNet() != null) {
                sep.check();
                out.print(".").print(o.getName()).print("( ").print(o.getNet().getName()).print(" )");
            }
        out.dec();
        out.println().println(");");
    }

    private void printExpression(HDLNodeAssignment node) throws IOException, HDLException {
        if (node.getTargetNet() != null) {
            out.print("assign ").print(node.getTargetNet().getName()).print(" = ");
            printExpression(node.getExpression());
            out.println(";");
        }
    }

    private void printExpression(Expression expression) throws IOException, HDLException {
        if (expression instanceof ExprVar)
            out.print(((ExprVar) expression).getNet().getName());
        else if (expression instanceof ExprVarRange) {
            final ExprVarRange evr = (ExprVarRange) expression;
            out.print(evr.getNet().getName()).print("[");
            if (evr.getMsb() == evr.getLsb())
                out.print(evr.getMsb());
            else
                out.print(evr.getMsb()).print(":").print(evr.getLsb());
            out.print("]");
        } else if (expression instanceof ExprConstant) {
            final ExprConstant constant = (ExprConstant) expression;
            out.print(value(constant));
        } else if (expression instanceof ExprNot) {
            out.print("~ ");
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
                    op = " | ";
                    break;
                case AND:
                    op = " & ";
                    break;
                case XOR:
                    op = " ^ ";
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

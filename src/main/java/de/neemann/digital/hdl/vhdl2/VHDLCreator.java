/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl2;

import de.neemann.digital.hdl.hgs.HGSEvalException;
import de.neemann.digital.hdl.model2.*;
import de.neemann.digital.hdl.model2.expression.*;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.vhdl2.entities.VHDLEntity;

import java.io.IOException;
import java.util.HashSet;

public class VHDLCreator {
    private final CodePrinter out;
    private final VHDLLibrary library;
    private HashSet<String> customPrinted;

    public VHDLCreator(CodePrinter out, HDLModel model) {
        this.out = out;
        library = new VHDLLibrary(model);
        customPrinted = new HashSet<>();
    }

    private String getType(int bits) {
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
    public static String value(long val, long bits) {
        String s = Long.toBinaryString(val);
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
        String specializedName = entity.print(out, node);
        node.setSpecializedName(specializedName);
    }

    private void printNodeCustom(HDLNodeCustom node) throws HDLException, IOException, HGSEvalException {
        if (!customPrinted.contains(node.getElementName()))
            printHDLCircuit(node.getCircuit());
    }

    public void printHDLCircuit(HDLCircuit circuit) throws IOException, HDLException, HGSEvalException {
        // at first print all used entities to maintain the correct order
        for (HDLNode node : circuit)
            if (node instanceof HDLNodeBuildIn)
                printNodeBuiltIn((HDLNodeBuildIn) node);
            else if (node instanceof HDLNodeCustom)
                printNodeCustom((HDLNodeCustom) node);

        // after that print this entity

        out
                .println("LIBRARY ieee;")
                .println("USE ieee.std_logic_1164.all;")
                .println("USE ieee.numeric_std.all;")
                .println();

        out.print("entity ").print(circuit.getSpecializedName()).println(" is").inc();
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
        out.dec();
        out.print("end ").print(circuit.getSpecializedName()).println(";");
        out.println();
        out.print("architecture Behavioral of " + circuit.getSpecializedName()).println(" is").inc();

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

    private void printManyToOne(HDLNodeSplitterManyToOne node) {

    }

    private void printOneToMany(HDLNodeSplitterOneToMany node) {

    }

    private void printEntityInstantiation(HDLNode node, int num) throws IOException, HDLException {
        String entityName = node.getSpecializedName();

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
                    op = " or ";
                    break;
                case AND:
                    op = " and ";
                    break;
                case XOR:
                    op = " xor ";
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

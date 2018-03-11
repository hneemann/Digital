/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog;

import de.neemann.digital.hdl.vhdl.*;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.data.Value;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.model.HDLModel;
import de.neemann.digital.hdl.model.Port;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestCaseDescription;
import de.neemann.digital.testing.TestCaseElement;
import de.neemann.digital.testing.TestingDataException;
import de.neemann.digital.testing.parser.Context;
import de.neemann.digital.testing.parser.LineListener;
import de.neemann.digital.testing.parser.ParserException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static de.neemann.digital.testing.TestCaseElement.TESTDATA;

/**
 * Creates a test bench for a model.
 * The needed test date is taken from the test cases in the circuit
 */
public class VerilogTestBenchCreator {
    private final ArrayList<ElementAttributes> testCases;
    private final HDLModel model;
    private final ArrayList<File> testFileWritten;

    /**
     * Creates a new instance
     *
     * @param circuit the circuit
     * @param model   the model
     */
    public VerilogTestBenchCreator(Circuit circuit, HDLModel model) {
        this.model = model;
        testCases = new ArrayList<>();
        for (VisualElement ve : circuit.getElements())
            if (ve.equalsDescription(TestCaseElement.TESTCASEDESCRIPTION))
                testCases.add(ve.getElementAttributes());
        testFileWritten = new ArrayList<>();
    }

    /**
     * Writes the test benches
     *
     * @param file the original verilog file
     * @throws IOException  IOException
     * @throws HDLException HDLException
     */
    public void write(File file) throws IOException, HDLException {
        String filename = file.getName();
        int p = filename.indexOf('.');
        if (p > 0)
            filename = filename.substring(0, p);

        for (ElementAttributes tc : testCases) {
            String testName = tc.getCleanLabel();
            if (testName.length() > 0)
                testName = filename + "_" + testName + "_tb";
            else
                testName = filename + "_tb";

            testName = Port.getHDLName(testName);

            File f = new File(file.getParentFile(), testName + ".v");
            testFileWritten.add(f);
            try (CodePrinter out = new CodePrinter(f)) {
                try {
                    writeTestBench(out, model.getName(), testName, tc);
                } catch (TestingDataException | ParserException | RuntimeException e) {
                    throw new HDLException(Lang.get("err_vhdlErrorWritingTestBench"), e);
                }
            }
        }
    }

    /**
     * @return returns the files which are written
     */
    public ArrayList<File> getTestFileWritten() {
        return testFileWritten;
    }

    private void writeTestBench(CodePrinter out, String moduleName, String testName, ElementAttributes tc) throws IOException, HDLException, TestingDataException, ParserException {
        out.print("//  A testbench for ").println(testName);
        out.println("`timescale 1us/1ns").println();
        out.print("module ").print(testName).println(";");

        // Write local port declaration
        for (Port p : model.getPorts()) {
            out.print("  ").print(getSignalDeclarationCode(p)).println(";");
        }

        out.println();
        out.print(moduleName).print(" ").print(moduleName).print("0 (").println();
        out.inc();

        Separator comma = new Separator(",\n");

        for (Port p : model.getPorts()) {
            comma.check(out);
            out.print(".").print(p.getName()).print("(").print(p.getName()).print(")");
        }
        out.dec().println().print(");").println().println();

        out.println("initial begin").println().inc();

        TestCaseDescription testdata = tc.get(TESTDATA);

        ArrayList<Port> dataOrder = new ArrayList<>();
        for (String name : testdata.getNames()) {
            boolean found = false;
            for (Port p : model.getPorts())
                if (p.getOrigName().equals(name)) {
                    dataOrder.add(p);
                    found = true;
                    break;
                }
            if (!found)
                throw new TestingDataException(Lang.get("err_testSignal_N_notFound", name));
        }

        LineListener parent = new LineListenerVerilog(out, dataOrder);
        testdata.getLines().emitLines(parent, new Context());

        out.println().println("$display(\"All tests passed.\");");

        out.dec().println("end");
        out.println("endmodule");
    }

    private String getSignalDeclarationCode(Port p) throws HDLException {
        String declCode;

        switch (p.getDirection()) {
            case out: declCode = "wire "; break;
            case in: declCode = "reg "; break;
            default:
                declCode = "/* Invalid port */";
        }

        if (p.getBits() > 1)
            declCode += "[" + Integer.toString(p.getBits() - 1) + ":0] ";

        declCode += p.getName();

        return declCode;
    }

    private static final class LineListenerVerilog implements LineListener {
        private final CodePrinter out;
        private final ArrayList<Port> dataOrder;
        private int line = 0;

        private LineListenerVerilog(CodePrinter out, ArrayList<Port> dataOrder) {
            this.out = out;
            this.dataOrder = dataOrder;
        }

        @Override
        public void add(Value[] values) {
            try {
                boolean containsClock = false;
                for (Value v : values)
                    if (v.getType() == Value.Type.CLOCK)
                        containsClock = true;
                if (containsClock) {
                    writeValues(values, true, 0);
                    writeValues(values, true, 1);
                }
                writeValues(values, false, 0);
            } catch (IOException | HDLException e) {
                throw new RuntimeException(e);
            }
        }

        private void writeValues(Value[] values, boolean isClock, int clock) throws IOException, HDLException {

            for (int i = 0; i < values.length; i++) {
                Port p = dataOrder.get(i);

                if (p.getDirection() == Port.Direction.in) {
                    if (values[i].getType() == Value.Type.CLOCK) {
                        out.print(p.getName()).print(" = ").print(clock).println(";");
                    } else {
                        String hexVal = trimHex(values[i].toString(), p.getBits());
                        String valStr = p.getBits() + "'h" + hexVal;

                        out.print(p.getName()).print(" = ").print(valStr).println(";");
                    }
                }
            }
            out.println("#10;").println();

            if (!isClock) {
                for (int i = 0; i < values.length; i++) {
                    Port p = dataOrder.get(i);

                    // Don't generate don't care values
                    if (values[i].getType() == Value.Type.DONTCARE) {
                        continue;
                    }
                    if (p.getDirection() == Port.Direction.out) {
                        String hexVal = trimHex(values[i].toString(), p.getBits());
                        String valStr = p.getBits() + "'h" + hexVal;

                        out.print("if (").print(p.getName()).print(" !== ").print(valStr).println(") begin");
                        out.inc();
                        out.print("$display(\"")
                           .print(line).print(": ")
                           .print(p.getName()).print(": Assert failed. Expected %h, found %h\", ")
                           .print(valStr).print(", ").print(p.getName()).print(");").println();
                        out.println("$finish;");
                        out.dec().println("end");
                    }
                }
                line++;
            }
        }

        private String trimHex(String hex, int bits) {
            if (hex.startsWith("0x")) {
                hex = hex.substring(2);
            }

            int digitCount = (bits / 4) + ((bits % 4 != 0)? 1 : 0);

            if (hex.length() > digitCount) {
                int pos = hex.length() - digitCount;

                hex = hex.substring(pos);
            }

            return hex;
        }
    }
}

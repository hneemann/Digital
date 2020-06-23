/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog2;

import de.neemann.digital.hdl.vhdl2.*;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.data.Value;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.hdl.model2.HDLCircuit;
import de.neemann.digital.hdl.model2.HDLException;
import de.neemann.digital.hdl.model2.HDLModel;
import de.neemann.digital.hdl.model2.HDLPort;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.printer.CodePrinterStr;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestCaseDescription;
import de.neemann.digital.testing.TestingDataException;
import de.neemann.digital.testing.parser.Context;
import de.neemann.digital.testing.parser.LineListener;
import de.neemann.digital.testing.parser.ParserException;
import de.neemann.digital.testing.parser.TestRow;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import static de.neemann.digital.testing.TestCaseElement.TESTDATA;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Creates a test bench for a model.
 * The needed test date is taken from the test cases in the circuit
 */
public class VerilogTestBenchCreator {
    private final ArrayList<ElementAttributes> testCases;
    private final HDLCircuit main;
    private final String topModuleName;
    private final HDLModel.Renaming renaming;
    private final ArrayList<File> testFileWritten;

    /**
     * Creates a new instance
     *
     * @param circuit       the circuit
     * @param model         the model
     * @param topModuleName the name of the module under test
     */
    public VerilogTestBenchCreator(Circuit circuit, HDLModel model, String topModuleName) {
        this.main = model.getMain();
        this.topModuleName = topModuleName;
        testCases = new ArrayList<>();
        for (VisualElement ve : circuit.getTestCases())
            testCases.add(ve.getElementAttributes());
        testFileWritten = new ArrayList<>();
        renaming = model.getRenaming();
    }

    /**
     * Writes the test benches
     *
     * @param file the original verilog file
     * @return this for chained calls
     * @throws IOException  IOException
     * @throws HDLException HDLException
     */
    public VerilogTestBenchCreator write(File file) throws IOException, HDLException {
        String filename = file.getName();
        int p = filename.indexOf('.');
        if (p > 0)
            filename = filename.substring(0, p);

        for (ElementAttributes tc : testCases) {
            String testName = tc.getLabel();
            if (testName.length() > 0)
                testName = filename + "_" + testName + "_tb";
            else
                testName = filename + "_tb";

            //testName = HDLPort.getHDLName(testName);

            File f = new File(file.getParentFile(), testName + ".v");
            testFileWritten.add(f);
            try (CodePrinter out = new CodePrinter(f)) {
                try {
                    writeTestBench(out, topModuleName, testName, tc);
                } catch (RuntimeException e) {
                    throw new HDLException(Lang.get("err_vhdlErrorWritingTestBench"), e);
                } catch (TestingDataException | ParserException ex) {
                    Logger.getLogger(VerilogTestBenchCreator.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return this;
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
        out.inc();

        for (HDLPort p : main.getPorts()) {
            out.print("  ").print(getSignalDeclarationCode(p)).println(";");
        }

        out.println();
        out.print(moduleName).print(" ").print(moduleName).print("0 (").println();
        out.inc();

        Separator comma = new Separator(out, ",\n");

        for (HDLPort p : main.getPorts()) {
            comma.check();
            out.print(".").print(p.getName()).print("(").print(p.getName()).print(")");
        }
        out.dec().println().print(");").println().println();

        TestCaseDescription testdata = tc.get(TESTDATA);

        ArrayList<HDLPort> dataOrder = new ArrayList<>();
        ArrayList<HDLPort> inputsInOrder = new ArrayList<>();
        ArrayList<HDLPort> outputsInOrder = new ArrayList<>();
        for (String name : testdata.getNames()) {
            String saveName = renaming.checkName(name);
            boolean found = false;
            for (HDLPort p : main.getPorts()) {
                if (p.getName().equals(saveName)) {
                    dataOrder.add(p);

                    if (p.getDirection() == HDLPort.Direction.OUT) {
                        inputsInOrder.add(p);
                    } else {
                        outputsInOrder.add(p);
                    }

                    found = true;
                    break;
                }
            }
            if (!found)
                throw new TestingDataException(Lang.get("err_testSignal_N_notFound", name));
        }

        int rowBits = 0;
        for (HDLPort p : dataOrder) {
            rowBits += p.getBits();
        }

        CodePrinterStr outTmp = new CodePrinterStr();

        outTmp.inc();
        LineListener parent = new LineListenerVerilog(outTmp, dataOrder, rowBits);
        testdata.getLines().emitLines(parent, new Context());
        int lineCount = ((LineListenerVerilog) parent).getLineCount();

        String patternRange1 = rowBits == 1 ? "" : String.format("[%d:0] ", rowBits - 1);
        String patternRange2 = lineCount == 1 ? "" : String.format("[0:%d]", lineCount - 1);

        out.inc();
        out.print("reg ").print(patternRange1).print("patterns").print(patternRange2).println(";");

        String loopVar = "i";
        int lv = 0;
        while (loopVarExists(loopVar, main.getPorts()))
            loopVar = "i" + (lv++);

        out.print("integer ").print(loopVar).println(";");

        out.println().println("initial begin");
        out.println(outTmp.toString());

        out.inc();
        out.print(String.format("for (%1$s = 0; %1$s < %2$d; %1$s = %1$s + 1)\n", loopVar, lineCount));
        out.println("begin").inc();

        int rangeStart = rowBits - 1;
        for (HDLPort p : inputsInOrder) {
            int rangeEnd = rangeStart - p.getBits() + 1;
            String rangeStr = (rangeStart != rangeEnd) ? ("[" + rangeStart + ":" + rangeEnd + "]") : ("[" + rangeStart + "]");

            out.print(p.getName()).print(" = patterns[").print(loopVar).print("]").print(rangeStr).println(";");
            rangeStart -= p.getBits();
        }
        out.println("#10;");

        for (HDLPort p : outputsInOrder) {
            String dontCareValue = (p.getBits()) + "'hx";
            int rangeEnd = rangeStart - p.getBits() + 1;
            String rangeStr = (rangeStart != rangeEnd) ? ("[" + rangeStart + ":" + rangeEnd + "]") : ("[" + rangeStart + "]");

            out.print("if (patterns[").print(loopVar).print("]").print(rangeStr).print(" !== ").print(dontCareValue).println(")")
                    .println("begin");
            out.inc();
            out.print("if (").print(p.getName()).print(" !== patterns[").print(loopVar).print("]").print(rangeStr).println(")")
                    .println("begin");
            out.inc();
            out.print("$display(\"%d:")
                    .print(p.getName()).print(": (assertion error). Expected %h, found %h\", ")
                    .print(loopVar).print(", ").print("patterns[").print(loopVar).print("]").print(rangeStr).print(", ")
                    .print(p.getName()).print(");")
                    .println();
            out.println("$finish;");
            out.dec().println("end");
            out.dec().println("end");

            rangeStart -= p.getBits();
        }
        out.dec();
        out.println("end");

        out.println().println("$display(\"All tests passed.\");");

        out.dec().println("end");
        out.println("endmodule");
    }

    private boolean loopVarExists(String loopVar, ArrayList<HDLPort> ports) {
        for (HDLPort p : ports)
            if (p.getName().equals(loopVar))
                return true;
        return false;
    }

    private String getSignalDeclarationCode(HDLPort p) throws HDLException {
        String declCode;

        switch (p.getDirection()) {
            case IN:
                declCode = "wire ";
                break;
            case OUT:
                declCode = "reg ";
                break;
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
        private final ArrayList<HDLPort> dataOrder;
        private final int rowBits;
        private int rowIndex;

        private LineListenerVerilog(CodePrinter out, ArrayList<HDLPort> dataOrder, int rowBits) {
            this.out = out;
            this.dataOrder = dataOrder;
            this.rowBits = rowBits;
            rowIndex = 0;
        }

        @Override
        public void add(TestRow row) {
            try {
                boolean containsClock = false;
                for (Value v : row.getValues())
                    if (v.getType() == Value.Type.CLOCK)
                        containsClock = true;
                if (containsClock) {
                    writeValues(row.getValues(), true, 0);
                    writeValues(row.getValues(), true, 1);
                }
                writeValues(row.getValues(), false, 0);
            } catch (IOException | HDLException e) {
                throw new RuntimeException(e);
            }
        }

        /**
         * Returns the number of lines emitted
         *
         * @return the number of lines
         */
        public int getLineCount() {
            return rowIndex;
        }

        private void writeValues(Value[] values, boolean isClock, int clock) throws IOException, HDLException {
            out.print("patterns[").print(rowIndex).print("] = ").print(rowBits).print("'b");

            for (int i = 0; i < values.length; i++) {
                HDLPort p = dataOrder.get(i);

                if (p.getDirection() == HDLPort.Direction.OUT) {
                    if (values[i].getType() == Value.Type.CLOCK) {
                        out.print(clock);
                    } else {
                        out.print(toBinaryString(values[i], p.getBits()));
                    }
                    out.print("_");
                }
            }

            Separator sep = new Separator(out, "_");

            for (int i = 0; i < values.length; i++) {
                HDLPort p = dataOrder.get(i);

                if (p.getDirection() == HDLPort.Direction.IN) {
                    sep.check();

                    if (isClock) {
                        out.print(toBinaryString(0, Value.Type.DONTCARE, p.getBits()));
                    } else {
                        out.print(toBinaryString(values[i], p.getBits()));
                    }
                }
            }

            out.println(";");

            rowIndex++;
        }

        private String toBinaryString(Value v, int bits) {
            return toBinaryString(v.getValue(), v.getType(), bits);
        }

        private String toBinaryString(long val, Value.Type type, int bits) {
            String binStr = "";
            char fillCh = '0';

            switch (type) {
                case DONTCARE:
                    fillCh = 'x';
                    break;
                case HIGHZ:
                    fillCh = 'z';
                    break;
                default:
                    long mask = (bits < 64) ? ((1L << bits) - 1) : 0xffffffffffffffffL;
                    binStr = Long.toBinaryString(val & mask);
            }

            StringBuilder sb = new StringBuilder();
            if (binStr.length() < bits) {
                int diff = bits - binStr.length();

                for (int i = 0; i < diff; i++) {
                    sb.append(fillCh);
                }
            }
            sb.append(binStr);

            return sb.toString();
        }
    }
}

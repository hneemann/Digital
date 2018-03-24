/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl2;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.data.Value;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.hdl.model2.HDLCircuit;
import de.neemann.digital.hdl.model2.HDLException;
import de.neemann.digital.hdl.model2.HDLPort;
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
public class VHDLTestBenchCreator {
    private final ArrayList<ElementAttributes> testCases;
    private final HDLCircuit main;
    private ArrayList<File> testFileWritten;

    /**
     * Creates a new instance
     *
     * @param circuit the circuit
     * @param main    the model
     */
    public VHDLTestBenchCreator(Circuit circuit, HDLCircuit main) {
        this.main = main;
        testCases = new ArrayList<>();
        for (VisualElement ve : circuit.getElements())
            if (ve.equalsDescription(TestCaseElement.TESTCASEDESCRIPTION))
                testCases.add(ve.getElementAttributes());
        testFileWritten = new ArrayList<>();
    }

    /**
     * Writes the test benches
     *
     * @param file the original vhdl file
     * @return this for chained calls
     * @throws IOException  IOException
     * @throws HDLException HDLException
     */
    public VHDLTestBenchCreator write(File file) throws IOException, HDLException {
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

            File f = new File(file.getParentFile(), testName + ".vhdl");
            testFileWritten.add(f);
            try (CodePrinter out = new CodePrinter(f)) {
                try {
                    writeTestBench(out, testName, tc);
                } catch (TestingDataException | ParserException | RuntimeException e) {
                    throw new HDLException(Lang.get("err_vhdlErrorWritingTestBench"), e);
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

    private void writeTestBench(CodePrinter out, String testName, ElementAttributes tc) throws IOException, HDLException, TestingDataException, ParserException {
        out.print("--  A testbench for ").println(testName);
        out.println("LIBRARY ieee;");
        out.println("USE ieee.std_logic_1164.all;");
        out.println("USE ieee.numeric_std.all;");
        out.println();
        out.print("entity ").print(testName).println(" is");
        out.print("end ").print(testName).println(";");
        out.println();
        out.print("architecture behav of ").print(testName).println(" is").inc();
        out.println("component main").inc();
        VHDLCreator.writePorts(out, main);
        out.dec().println("end component;");
        out.println();
        for (HDLPort p : main.getInputs())
            out.print("signal ").print(p.getName()).print(" : ").print(VHDLCreator.getType(p.getBits())).println(";");
        for (HDLPort p : main.getOutputs())
            out.print("signal ").print(p.getName()).print(" : ").print(VHDLCreator.getType(p.getBits())).println(";");
        out.dec().println("begin").inc();

        out.println("main_0 : main port map (").inc();
        Separator comma = new Separator(",\n");
        for (HDLPort p : main.getInputs()) {
            comma.check(out);
            out.print(p.getName() + " => " + p.getName());
        }
        for (HDLPort p : main.getOutputs()) {
            comma.check(out);
            out.print(p.getName() + " => " + p.getName());
        }
        out.println(" );").dec();

        out.println("process").inc();

        TestCaseDescription testdata = tc.get(TESTDATA);

        ArrayList<HDLPort> dataOrder = new ArrayList<>();
        out.println("type pattern_type is record").inc();
        for (String name : testdata.getNames()) {
            boolean found = false;
            for (HDLPort p : main.getPorts()) {
                if (p.getName().equals(name)) {
                    out.print(p.getName()).print(" : ").print(VHDLCreator.getType(p.getBits())).println(";");
                    dataOrder.add(p);
                    found = true;
                    break;
                }
            }
            if (!found)
                throw new TestingDataException(Lang.get("err_testSignal_N_notFound", name));
        }
        out.dec().println("end record;");

        out.println("type pattern_array is array (natural range <>) of pattern_type;");
        out.println("constant patterns : pattern_array := (").inc();

        LineListener parent = new LineListenerVHDL(out, dataOrder);
        testdata.getLines().emitLines(parent, new Context());

        out.println(");").dec();

        out.dec().println("begin").inc();
        out.println("for i in patterns'range loop").inc();

        for (HDLPort p : main.getInputs())
            out.print(p.getName()).print(" <= patterns(i).").print(p.getName()).println(";");
        out.println("wait for 10 ns;");
        for (HDLPort p : main.getOutputs()) {
            out.print("assert std_match(").print(p.getName()).print(", patterns(i).").print(p.getName()).print(")");
            out.print(" OR (")
                    .print(p.getName())
                    .print(" = ")
                    .print(getSimpleValue(p.getBits(), 'Z'))
                    .print(" AND patterns(i).").print(p.getName()).print(" = ")
                    .print(getSimpleValue(p.getBits(), 'Z'))
                    .print(")").eol();
            out.inc().print("report \"wrong value for ").print(p.getName()).println(" i=\" & integer'image(i) severity error;").dec();
        }

        out.dec().println("end loop;");
        out.println("wait;");
        out.dec().println("end process;");
        out.dec().println("end behav;");
    }

    private static String getSimpleValue(int bits, char c) {
        if (bits == 1)
            return "'" + c + "'";

        StringBuilder sb = new StringBuilder("\"");
        for (int i = 0; i < bits; i++)
            sb.append('z');

        return sb.append('"').toString();
    }

    /*
    private static void writeCharValue(CodePrinter out, char c, int bits) throws IOException {
        if (bits > 1) {
            out.print("\"");
            for (int i = 0; i < bits; i++)
                out.print(c);
            out.print("\"");
        } else
            out.print("'").print(c).print("'");
    }*/

    private static final class LineListenerVHDL implements LineListener {
        private final CodePrinter out;
        private final ArrayList<HDLPort> dataOrder;
        private final Separator lineSep;
        private int line = 0;

        private LineListenerVHDL(CodePrinter out, ArrayList<HDLPort> dataOrder) {
            this.out = out;
            this.dataOrder = dataOrder;
            lineSep = new Separator("") {
                @Override
                public String getSeperator() {
                    return ", -- i=" + (line++) + "\n";
                }
            };
        }

        @Override
        public void add(Value[] values) {
            try {
                boolean containsClock = false;
                for (Value v : values)
                    if (v.getType() == Value.Type.CLOCK)
                        containsClock = true;
                if (containsClock) {
                    lineSep.check(out);
                    writeValues(values, true, 0);
                    lineSep.check(out);
                    writeValues(values, true, 1);
                }
                lineSep.check(out);
                writeValues(values, false, 0);
            } catch (IOException | HDLException e) {
                throw new RuntimeException(e);
            }
        }

        private void writeValues(Value[] values, boolean isClock, int clock) throws IOException, HDLException {
            out.print("(");
            Separator sep = new Separator(", ");
            for (int i = 0; i < values.length; i++) {
                sep.check(out);
                Value val = values[i];
                int bits = dataOrder.get(i).getBits();
                switch (val.getType()) {
                    case NORMAL:
                        if (isClock && dataOrder.get(i).getDirection() == HDLPort.Direction.OUT)
                            out.print(getSimpleValue(bits, '-'));
                        else
                            out.print(VHDLCreator.value(val.getValue(), bits));
                        break;
                    case DONTCARE:
                        out.print(getSimpleValue(bits, '-'));
                        break;
                    case HIGHZ:
                        out.print(getSimpleValue(bits, 'Z'));
                        break;
                    case CLOCK:
                        out.print("'").print(clock).print("'");
                        break;
                    default:
                        throw new RuntimeException(Lang.get("err_vhdlValuesOfType_N_notAllowed", val.getType()));
                }
            }
            out.print(")");
        }
    }
}

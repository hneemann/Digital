/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli;

import de.neemann.digital.cli.cli.Argument;
import de.neemann.digital.cli.cli.BasicCommand;
import de.neemann.digital.cli.cli.CLIException;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestCaseDescription;
import de.neemann.digital.testing.TestCaseElement;
import de.neemann.digital.testing.TestExecutor;
import de.neemann.digital.testing.TestingDataException;
import de.neemann.digital.testing.parser.ParserException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Tester used from the command line
 */
public class CommandLineTester {

    private final CircuitLoader circuitLoader;
    private PrintStream out = System.out;
    private ArrayList<TestCase> testCases;
    private int testsPassed;

    /**
     * Creates a new instance.
     *
     * @param file the file to test
     * @throws IOException IOException
     */
    public CommandLineTester(File file) throws IOException {
        circuitLoader = new CircuitLoader(file);
    }

    /**
     * Sets the printer to use
     *
     * @param out the {@link PrintStream}
     * @return this for chained calls
     */
    public CommandLineTester setOutputs(PrintStream out) {
        this.out = out;
        return this;
    }

    /**
     * Uses the test cases from the given file
     *
     * @param file the file containing the test cases
     * @return this for chained calls
     * @throws IOException IOException
     */
    public CommandLineTester useTestCasesFrom(File file) throws IOException {
        Circuit c = Circuit.loadCircuit(file, circuitLoader.getShapeFactory());
        testCases = getTestCasesFrom(c);
        return this;
    }

    private ArrayList<TestCase> getTestCasesFrom(Circuit circuit) {
        ArrayList<TestCase> tsl = new ArrayList<>();
        for (VisualElement el : circuit.getElements())
            if (el.equalsDescription(TestCaseElement.TESTCASEDESCRIPTION))
                tsl.add(new TestCase(
                        el.getElementAttributes().get(TestCaseElement.TESTDATA),
                        el.getElementAttributes().getLabel()));
        return tsl;
    }

    /**
     * Executes test test
     *
     * @return the number of failed test cases
     */
    public int execute() {
        if (testCases == null)
            testCases = getTestCasesFrom(circuitLoader.getCircuit());

        int errorCount = 0;

        if (testCases.isEmpty()) {
            out.println("no test cases given");
            errorCount++;
        } else {
            for (TestCase t : testCases) {
                String label = t.getLabel();
                if (label.isEmpty())
                    label = "unnamed";

                try {
                    Model model = circuitLoader.createModel();
                    TestExecutor te = new TestExecutor(t.getTestCaseDescription()).create(model);

                    if (te.allPassed()) {
                        out.println(label + ": passed");
                        testsPassed++;
                    } else {
                        out.println(label + ": failed");
                        errorCount++;
                    }
                } catch (ParserException | PinException | ElementNotFoundException | TestingDataException | NodeException e) {
                    out.println(label + ": " + e.getMessage());
                    errorCount++;
                }
            }
        }
        return errorCount;
    }

    /**
     * @return the number of passed tests
     */
    public int getTestsPassed() {
        return testsPassed;
    }

    private static final class TestCase {
        private final TestCaseDescription testCaseDescription;
        private final String label;

        private TestCase(TestCaseDescription testCaseDescription, String label) {
            this.testCaseDescription = testCaseDescription;
            this.label = label;
        }

        private TestCaseDescription getTestCaseDescription() {
            return testCaseDescription;
        }

        private String getLabel() {
            return label;
        }
    }

    /**
     * The test command
     */
    public static class TestCommand extends BasicCommand {
        private final Argument<String> circ;
        private final Argument<String> tests;
        private int testsPassed;

        /**
         * Creates a new CLI command
         */
        public TestCommand() {
            super("test");
            circ = addArgument(new Argument<>("circ", "", false));
            tests = addArgument(new Argument<>("tests", "", true));
        }

        @Override
        protected void execute() throws CLIException {
            try {
                CommandLineTester clt = new CommandLineTester(new File(circ.get()));
                if (tests.isSet())
                    clt.useTestCasesFrom(new File(tests.get()));
                int errors = clt.execute();
                testsPassed = clt.getTestsPassed();
                if (errors > 0)
                    throw new CLIException(Lang.get("cli_thereAreTestFailures"), errors).hideHelp();
            } catch (IOException e) {
                throw new CLIException(Lang.get("cli_errorExecutingTests"), e);
            }
        }

        /**
         * @return the number of tests passed
         */
        public int getTestsPassed() {
            return testsPassed;
        }
    }
}

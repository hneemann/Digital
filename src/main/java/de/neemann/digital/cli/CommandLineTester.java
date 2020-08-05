/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli;

import de.neemann.digital.cli.cli.Argument;
import de.neemann.digital.cli.cli.BasicCommand;
import de.neemann.digital.cli.cli.CLIException;
import de.neemann.digital.core.ErrorDetector;
import de.neemann.digital.core.Model;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.testing.TestCaseDescription;
import de.neemann.digital.testing.TestCaseElement;
import de.neemann.digital.testing.TestExecutor;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Tester used from the command line
 */
public class CommandLineTester {

    private final CircuitLoader circuitLoader;
    private ArrayList<TestCase> testCases;
    private int testsPassed;
    private boolean allowMissingInputs;

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
        for (VisualElement el : circuit.getTestCases())
            tsl.add(new TestCase(
                    el.getElementAttributes().get(TestCaseElement.TESTDATA),
                    el.getElementAttributes().getLabel()));
        return tsl;
    }

    /**
     * Executes test test
     *
     * @param out Stream to output messages
     * @return the number of failed test cases
     */
    public int execute(PrintStream out) {
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
                    ErrorDetector errorDetector = new ErrorDetector();
                    model.addObserver(errorDetector);
                    TestExecutor te = new TestExecutor(t.getTestCaseDescription())
                            .setAllowMissingInputs(allowMissingInputs)
                            .create(model);

                    if (te.allPassed()) {
                        out.println(label + ": passed");
                        testsPassed++;
                    } else {
                        String message = label + ": failed";
                        if (te.isErrorOccurred())
                            message += " due to an error";
                        else
                            message += " (" + te.failedPercent() + "%)";
                        out.println(message);
                        errorCount++;
                    }
                    errorDetector.check();

                } catch (Exception e) {
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

    private CommandLineTester setAllowMissingInputs(boolean allowMissingInputs) {
        this.allowMissingInputs = allowMissingInputs;
        return this;
    }

    /**
     * The test command
     */
    public static class TestCommand extends BasicCommand {
        private final Argument<String> circ;
        private final Argument<String> tests;
        private final Argument<Boolean> allowMissingInputs;
        private int testsPassed;

        /**
         * Creates a new CLI command
         */
        public TestCommand() {
            super("test");
            circ = addArgument(new Argument<>("circ", "", false));
            tests = addArgument(new Argument<>("tests", "", true));
            allowMissingInputs = addArgument(new Argument<>("allowMissingInputs", false, true));
        }

        @Override
        protected void execute() throws CLIException {
            try {
                CommandLineTester clt = new CommandLineTester(new File(circ.get())).setAllowMissingInputs(allowMissingInputs.get());
                if (tests.isSet())
                    clt.useTestCasesFrom(new File(tests.get()));
                int errors = clt.execute(System.out);
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

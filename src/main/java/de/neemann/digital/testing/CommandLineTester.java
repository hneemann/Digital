/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.testing.parser.ParserException;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;

/**
 * Tester used from the command line
 */
public class CommandLineTester {

    private final ElementLibrary library;
    private final ShapeFactory shapeFactory;
    private final Circuit circuit;
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
        library = new ElementLibrary();
        shapeFactory = new ShapeFactory(library);
        circuit = Circuit.loadCircuit(file, shapeFactory);
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
        Circuit c = Circuit.loadCircuit(file, shapeFactory);
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
            testCases = getTestCasesFrom(circuit);

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
                    Model model = new ModelCreator(circuit, library).createModel(false);
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
     * Entry point of the command line tester.
     *
     * @param args the program arguments
     * @throws IOException IOException
     */
    public static void main(String[] args) throws IOException {
        if (args.length == 0) {
            System.err.println("no command line arguments given!\n");
            System.err.println("usage:\n");
            System.err.println("java -cp Digital.jar " + CommandLineTester.class.getName() + " [dig file to test] [[optional dig file with test cases]]");
            System.exit(1);
        }

        CommandLineTester clt = new CommandLineTester(new File(args[0]));
        if (args.length > 1)
            clt.useTestCasesFrom(new File(args[1]));
        int errors = clt.execute();
        System.exit(errors);
    }
}

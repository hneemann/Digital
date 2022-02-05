/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.parser.ParseException;
import de.neemann.digital.analyse.parser.Parser;
import de.neemann.digital.builder.BuilderException;
import de.neemann.digital.builder.circuit.CircuitBuilder;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.data.ValueTable;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.shapes.ShapeFactory;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.ArrayList;

public class TestResultTest extends TestCase {

    private Model getModel(String func) throws IOException, ParseException, BuilderException, PinException, NodeException, ElementNotFoundException {
        ArrayList<Expression> exp = new Parser(func).parse();
        ElementLibrary library = new ElementLibrary();
        CircuitBuilder cb = new CircuitBuilder(new ShapeFactory(library));
        cb.addCombinatorial("Y", exp.get(0));
        Circuit circ = cb.createCircuit();
        Model model = new ModelCreator(circ, library).createModel(false);
        model.init();
        return model;
    }

    public void testResultOk() throws Exception {
        Model model = getModel("A^B");
        TestCaseDescription data = new TestCaseDescription(
                "A B Y\n"
                        + "0 0 0\n"
                        + "0 1 1\n"
                        + "1 0 1\n"
                        + "1 1 0\n");
        TestResult tr = new TestExecutor(data, model).execute();
        assertEquals(4, tr.getValueTable().getRows());
        assertTrue(tr.allPassed());
    }

    public void testResultError() throws Exception {
        Model model = getModel("A+B");
        TestCaseDescription data = new TestCaseDescription(
                "A B Y\n"
                        + "0 0 0\n"
                        + "0 1 1\n"
                        + "1 0 1\n"
                        + "1 1 0\n");
        TestResult tr = new TestExecutor(data, model).execute();
        ValueTable valueTable = tr.getValueTable();
        assertEquals(4, valueTable.getRows());
        assertFalse(tr.allPassed());
        assertEquals(true, ((MatchedValue) valueTable.getValue(0, 2)).isPassed());
        assertEquals(true, ((MatchedValue) valueTable.getValue(1, 2)).isPassed());
        assertEquals(true, ((MatchedValue) valueTable.getValue(2, 2)).isPassed());
        assertEquals(false, ((MatchedValue) valueTable.getValue(3, 2)).isPassed());

        assertEquals(25, tr.failedPercent());
    }

    public void testResultDontCare() throws Exception {
        Model model = getModel("A+B");
        TestCaseDescription data = new TestCaseDescription(
                "A B Y\n"
                        + "0 0 0\n"
                        + "0 1 1\n"
                        + "1 0 1\n"
                        + "1 1 x\n");
        TestResult tr = new TestExecutor(data, model).execute();
        ValueTable valueTable = tr.getValueTable();
        assertEquals(4, valueTable.getRows());
        assertTrue(tr.allPassed());
    }

    public void testResultDontCare2() throws Exception {
        Model model = getModel("A+B");
        TestCaseDescription data = new TestCaseDescription(
                "A B Y\n"
                        + "0 0 x\n"
                        + "0 1 1\n"
                        + "1 0 1\n"
                        + "1 1 1\n");
        TestResult tr = new TestExecutor(data, model).execute();
        ValueTable valueTable = tr.getValueTable();
        assertEquals(4, valueTable.getRows());
        assertTrue(tr.allPassed());
    }

    public void testResultDontCareInput() throws Exception {
        Model model = getModel("A*0+B");
        TestCaseDescription data = new TestCaseDescription(
                "A B Y\n"
                        + "x 0 0\n"
                        + "x 1 1\n");
        TestResult tr = new TestExecutor(data, model).execute();
        ValueTable valueTable = tr.getValueTable();
        assertEquals(4, valueTable.getRows());
        assertTrue(tr.allPassed());
    }

    public void testResultDontCareInput2() throws Exception {
        Model model = getModel("A*0+B*0+C");
        TestCaseDescription data = new TestCaseDescription(
                "A B C Y\n"
                        + "x x 0 0\n"
                        + "x x 1 1\n");
        TestResult tr = new TestExecutor(data, model).execute();
        ValueTable valueTable = tr.getValueTable();
        assertEquals(8, valueTable.getRows());
        assertTrue(tr.allPassed());
    }

}

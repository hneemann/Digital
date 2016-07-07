package de.neemann.digital.gui.components.test;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.parser.ParseException;
import de.neemann.digital.analyse.parser.Parser;
import de.neemann.digital.builder.BuilderException;
import de.neemann.digital.builder.circuit.CircuitBuilder;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.shapes.ShapeFactory;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.ArrayList;

/**
 * @author hneemann
 */
public class TestResultTest extends TestCase {

    private Model getModel(String func) throws IOException, ParseException, BuilderException, PinException, NodeException {
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
        TestData data = new TestData(
                "A B Y\n"
                        + "0 0 0\n"
                        + "0 1 1\n"
                        + "1 0 1\n"
                        + "1 1 0\n");
        TestResult tr = new TestResult(data).create(model);
        assertTrue(tr.allPassed());
    }

    public void testResultError() throws Exception {
        Model model = getModel("A+B");
        TestData data = new TestData(
                "A B Y\n"
                        + "0 0 0\n"
                        + "0 1 1\n"
                        + "1 0 1\n"
                        + "1 1 0\n");
        TestResult tr = new TestResult(data).create(model);
        assertFalse(tr.allPassed());
        assertEquals(true, ((MatchedValue)tr.getValue(0,2)).isPassed());
        assertEquals(true, ((MatchedValue)tr.getValue(1,2)).isPassed());
        assertEquals(true, ((MatchedValue)tr.getValue(2,2)).isPassed());
        assertEquals(false, ((MatchedValue)tr.getValue(3,2)).isPassed());
    }

    public void testResultErrorDC() throws Exception {
        Model model = getModel("A+B");
        TestData data = new TestData(
                "A B Y\n"
                        + "0 0 0\n"
                        + "0 1 1\n"
                        + "1 0 1\n"
                        + "1 1 x\n");
        TestResult tr = new TestResult(data).create(model);
        assertTrue(tr.allPassed());
    }

}
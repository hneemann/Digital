/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Signal;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.memory.DataField;
import de.neemann.digital.core.memory.RAMSinglePort;
import de.neemann.digital.core.memory.ROM;
import de.neemann.digital.data.Value;
import de.neemann.digital.testing.TestingDataException;
import junit.framework.TestCase;

import java.io.IOException;

/**
 * Created by Helmut.Neemann on 02.12.2016.
 */
public class ParserTest extends TestCase {

    public void testOk() throws IOException, ParserException {
        Parser parser = new Parser("A B\n0 1\n1 0\nX x").parse();
        LineCollector td = new LineCollector(parser);

        assertEquals(2, td.getNames().size());

        assertEquals(3, td.getLines().size());


        assertEquals(0, td.getLines().get(0).getValue(0).getValue());
        assertEquals(Value.Type.NORMAL, td.getLines().get(0).getValue(0).getType());

        assertEquals(1, td.getLines().get(0).getValue(1).getValue());
        assertEquals(Value.Type.NORMAL, td.getLines().get(0).getValue(1).getType());

        assertEquals(1, td.getLines().get(1).getValue(0).getValue());
        assertEquals(Value.Type.NORMAL, td.getLines().get(1).getValue(0).getType());

        assertEquals(0, td.getLines().get(1).getValue(1).getValue());
        assertEquals(Value.Type.NORMAL, td.getLines().get(1).getValue(1).getType());

        assertEquals(Value.Type.DONTCARE, td.getLines().get(2).getValue(0).getType());
        assertEquals(Value.Type.DONTCARE, td.getLines().get(2).getValue(1).getType());
    }

    public void testHex() throws IOException, ParserException {
        checkValueParser("A B\n0 0xff", 255);
    }

    public void testBin() throws IOException, ParserException {
        checkValueParser("A B\n0 0b11111111", 255);
    }

    public void testFixed() throws IOException, ParserException {
        checkValueParser("A B\n0 0.5:4", 8);
    }

    public void testFloat() throws IOException, ParserException {
        checkValueParser("A B\n0 0.5", Float.floatToIntBits(0.5f));
    }

    public void testDouble() throws IOException, ParserException {
        checkValueParser("A B\n0 0.5d", Double.doubleToLongBits(0.5));
    }

    void checkValueParser(String s, long val) throws IOException, ParserException {
        Parser parser = new Parser(s).parse();
        LineCollector td = new LineCollector(parser);
        assertEquals(2, td.getNames().size());

        assertEquals(1, td.getLines().size());

        assertEquals(0, td.getLines().get(0).getValue(0).getValue());
        assertEquals(Value.Type.NORMAL, td.getLines().get(0).getValue(0).getType());

        assertEquals(val, td.getLines().get(0).getValue(1).getValue());
        assertEquals(Value.Type.NORMAL, td.getLines().get(0).getValue(1).getType());
    }

    public void testMissingValue() throws IOException {
        try {
            new Parser("A B\n0 0\n1").parse().getLines().emitLines(values -> {
            }, new Context());
            fail();
        } catch (ParserException e) {
            assertTrue(true);
        }
    }

    public void testInvalidValue() throws IOException {
        try {
            new Parser("A B\n0 0\n1 u").parse();
            fail();
        } catch (ParserException e) {
            assertTrue(true);
        }
    }

    public void testClock() throws Exception {
        Parser parser = new Parser("A B\nC 1\nC 0").parse();
        LineCollector td = new LineCollector(parser);

        assertEquals(2, td.getNames().size());
        assertEquals(2, td.getLines().size());

        assertEquals(Value.Type.CLOCK, td.getLines().get(0).getValue(0).getType());
        assertEquals(1, td.getLines().get(0).getValue(1).getValue());
        assertEquals(Value.Type.CLOCK, td.getLines().get(1).getValue(0).getType());
        assertEquals(0, td.getLines().get(1).getValue(1).getValue());
    }

    public void testFor() throws IOException, ParserException {
        Parser parser = new Parser("A B\nrepeat(10) C (n*2)\n").parse();
        LineCollector td = new LineCollector(parser);

        assertEquals(2, td.getNames().size());
        assertEquals(10, td.getLines().size());

        for (int i = 0; i < 10; i++) {
            assertEquals(Value.Type.CLOCK, td.getLines().get(i).getValue(0).getType());
            assertEquals(i * 2, td.getLines().get(i).getValue(1).getValue());
        }
    }

    public void testForBits() throws IOException, ParserException {
        Parser parser = new Parser("A B C D \nrepeat(8) X bits(3,n)\n").parse();
        LineCollector td = new LineCollector(parser);
        assertEquals(4, td.getNames().size());
        assertEquals(8, td.getLines().size());

        for (int i = 0; i < 8; i++) {
            assertEquals(Value.Type.DONTCARE, td.getLines().get(i).getValue(0).getType());
            assertEquals((i >> 2) & 1, td.getLines().get(i).getValue(1).getValue());
            assertEquals((i >> 1) & 1, td.getLines().get(i).getValue(2).getValue());
            assertEquals(i & 1, td.getLines().get(i).getValue(3).getValue());
        }
    }

    public void testComment() throws IOException, ParserException {
        Parser parser = new Parser("#test\nA B\n1 1").parse();
        LineCollector td = new LineCollector(parser);
        assertEquals(2, td.getNames().size());
        assertEquals(1, td.getLines().size());
    }

    public void testHeader() throws IOException, ParserException {
        Parser parser = new Parser("A   B     C  D\n1 1 1 1").parse();
        LineCollector td = new LineCollector(parser);
        assertEquals(4, td.getNames().size());
        assertEquals(1, td.getLines().size());
    }

    public void testHeaderTabs() throws IOException, ParserException {
        Parser parser = new Parser("A\tB\tC \t D\n1\t1\t1\t1").parse();
        LineCollector td = new LineCollector(parser);
        assertEquals(4, td.getNames().size());
        assertEquals("A", td.getNames().get(0));
        assertEquals("B", td.getNames().get(1));
        assertEquals("C", td.getNames().get(2));
        assertEquals("D", td.getNames().get(3));
        assertEquals(1, td.getLines().size());
        assertEquals(4, td.getLines().get(0).getValues().length);
    }

    public void testEmptyLines() throws IOException, ParserException {
        Parser parser = new Parser("A_i B_i C_i-1 C_i S_i\n" +
                " 0   0   0     0   0\n" +
                " 0   0   1     0   1\n" +
                " 0   1   0     0   1\n\n" +
                " 0   1   1     1   0\n" +
                " 1   0   0     0   1\n\n" +
                " 1   0   1     1   0\n" +
                " 1   1   0     1   0\n" +
                " 1   1   1     1   1\n").parse();
        LineCollector td = new LineCollector(parser);

        assertEquals(5, td.getNames().size());
        assertEquals(8, td.getLines().size());

        assertEquals("A_i", td.getNames().get(0));
        assertEquals("B_i", td.getNames().get(1));
        assertEquals("C_i-1", td.getNames().get(2));
    }

    public void testBUG1() throws IOException, ParserException {
        Parser parser = new Parser("C_i-1 A B    C   S\n" +
                "repeat(1<<16) 0 (n>>8) (n&255) ((n>>8)*(n&255)) 0").parse();
        LineCollector td = new LineCollector(parser);
        assertEquals(5, td.getNames().size());
        assertEquals(1 << 16, td.getLines().size());
    }

    public void test_GitHub_49() throws IOException, ParserException {
        Parser parser = new Parser("A B Y\n" +
                "1 1 1\n#test").parse();
        LineCollector td = new LineCollector(parser);
        assertEquals(3, td.getNames().size());
        assertEquals(1, td.getLines().size());
    }

    public void test_modelSetVar() throws IOException, ParserException {
        Model model = new Model();
        model.addOutput(new Signal("A", new ObservableValue("A", 3).setValue(2)));
        model.addOutput(new Signal("B", new ObservableValue("B", 3).setValue(3)));
        Parser parser = new Parser("A B Y\n" +
                "let a=A+B;\n" +
                "1 1 1\n#test").parse();
        Context context = new Context().setModel(model);
        new LineCollector(parser, context);
        assertEquals(5, context.getVar("a"));
    }
}

/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.Gal16v8;

import de.neemann.digital.analyse.*;
import de.neemann.digital.analyse.expression.Constant;
import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.analyse.expression.modify.ExpressionModifier;
import de.neemann.digital.builder.PinMapException;
import de.neemann.digital.core.BacktrackException;
import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.draw.model.ModelCreator;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.gui.components.table.BuilderExpressionCreator;
import de.neemann.digital.gui.components.table.ExpressionCreator;
import de.neemann.digital.gui.components.table.ExpressionListenerStore;
import de.neemann.digital.integration.Resources;
import junit.framework.TestCase;

import java.io.*;
import java.util.Date;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;

/**
 * CUPL builder tests
 */
public class Gal16V8CuplExporterTest extends TestCase {

    public void testCUPLExporter() throws Exception {
        Variable y0 = new Variable("Y_0");
        Variable y1 = new Variable("Y_1");

        // counter
        Expression y0s = not(y0);
        Expression y1s = or(and(not(y0), y1), and(y0, not(y1)));

        CuplExporter ce = new CuplExporter("user", null)
                .setProjectName("test");
        ce.getPinMapping().parseString("Y_0=12;Y_1=13;A=14");
        ce.getBuilder()
                .addSequential("Y_0", y0s)
                .addSequential("Y_1", y1s)
                .addCombinatorial("A", and(y0, y1));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ce.writeTo(baos);

        assertEquals("Name     test ;\r\n" +
                "PartNo   00 ;\r\n" +
                "Date     unknownDate ;\r\n" +
                "Revision 01 ;\r\n" +
                "Designer user ;\r\n" +
                "Company  unknown ;\r\n" +
                "Assembly None ;\r\n" +
                "Location unknown ;\r\n" +
                "Device   g16v8a ;\r\n" +
                "\r\n" +
                "/* inputs */\r\n" +
                "PIN 1 = CLK;\r\n" +
                "\r\n" +
                "/* outputs */\r\n" +
                "PIN 12 = Y_0;\r\n" +
                "PIN 13 = Y_1;\r\n" +
                "PIN 14 = A;\r\n" +
                "\r\n" +
                "/* sequential logic */\r\n" +
                "Y_0.D = !Y_0;\r\n" +
                "Y_1.D = (!Y_0 & Y_1) # (Y_0 & !Y_1);\r\n" +
                "\r\n" +
                "/* combinatorial logic */\r\n" +
                "A = Y_0 & Y_1;\r\n", baos.toString());
    }

    public void testCUPLExporterConst() throws Exception {
        CuplExporter ce = new CuplExporter("user", null)
                .setProjectName("test");
        ce.getPinMapping().parseString("A=14;B=15");
        ce.getBuilder()
                .addCombinatorial("A", Constant.ONE)
                .addCombinatorial("B", Constant.ZERO);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ce.writeTo(baos);

        assertEquals("Name     test ;\r\n" +
                "PartNo   00 ;\r\n" +
                "Date     unknownDate ;\r\n" +
                "Revision 01 ;\r\n" +
                "Designer user ;\r\n" +
                "Company  unknown ;\r\n" +
                "Assembly None ;\r\n" +
                "Location unknown ;\r\n" +
                "Device   g16v8a ;\r\n" +
                "\r\n" +
                "/* inputs */\r\n" +
                "\r\n" +
                "/* outputs */\r\n" +
                "PIN 14 = A;\r\n" +
                "PIN 15 = B;\r\n" +
                "\r\n" +
                "/* combinatorial logic */\r\n" +
                "A = 'b'1;\r\n" +
                "B = 'b'0;\r\n", baos.toString());
    }


    public void testCUPLBuilderInvalidVars() throws Exception {
        Variable y0 = new Variable("D");  // D is not allowed in CUPL

        Expression y0s = not(y0);

        try {
            new CuplExporter("user", new Date(0))
                    .setProjectName("test")
                    .getBuilder()
                    .addSequential("Y_0", y0s);

            fail();
        } catch (RuntimeException e) {
            assertTrue(true);
        }
    }

    public void testCUPLBuilderInvalidVars2() throws Exception {
        Variable y0 = new Variable("Y_0");  // D is not allowed in CUPL

        Expression y0s = not(y0);

        try {
            new CuplExporter("user", new Date(0))
                    .setProjectName("test")
                    .getBuilder()
                    .addCombinatorial("D", y0s);

            fail();
        } catch (RuntimeException e) {
            assertTrue(true);
        }
    }

    public void testCuplWorkFlow() throws IOException, ElementNotFoundException, PinException, NodeException, AnalyseException, BacktrackException, PinMapException, ExpressionException, FormatterException {
        String cupl = createCupl("dig/GAL/Medwedew.dig");

        assertEquals("Name     test ;\r\n" +
                "PartNo   00 ;\r\n" +
                "Date     unknownDate ;\r\n" +
                "Revision 01 ;\r\n" +
                "Designer nn ;\r\n" +
                "Company  unknown ;\r\n" +
                "Assembly None ;\r\n" +
                "Location unknown ;\r\n" +
                "Device   g16v8a ;\r\n" +
                "\r\n" +
                "/* inputs */\r\n" +
                "PIN 1 = CLK;\r\n" +
                "\r\n" +
                "/* outputs */\r\n" +
                "PIN 15 = Q_3n;\r\n" +
                "PIN 16 = Q_2n;\r\n" +
                "PIN 17 = Q_1n;\r\n" +
                "PIN 18 = Q_0n;\r\n" +
                "\r\n" +
                "/* sequential logic */\r\n" +
                "Q_0n.D = !Q_0n;\r\n" +
                "Q_1n.D = (Q_0n & !Q_1n) # (!Q_0n & Q_1n);\r\n" +
                "Q_2n.D = (Q_0n & Q_1n & !Q_2n) # (!Q_0n & Q_2n) # (!Q_1n & Q_2n);\r\n" +
                "Q_3n.D = (Q_0n & Q_1n & Q_2n & !Q_3n) # (!Q_0n & Q_3n) # (!Q_1n & Q_3n) # (!Q_2n & Q_3n);\r\n", cupl);
    }

    public void testCuplWorkFlowPassThrough() throws IOException, ElementNotFoundException, PinException, NodeException, AnalyseException, BacktrackException, PinMapException, ExpressionException, FormatterException {
        String cupl = createCupl("dig/GAL/PassThrough.dig");

        assertEquals("Name     test ;\r\n" +
                "PartNo   00 ;\r\n" +
                "Date     unknownDate ;\r\n" +
                "Revision 01 ;\r\n" +
                "Designer nn ;\r\n" +
                "Company  unknown ;\r\n" +
                "Assembly None ;\r\n" +
                "Location unknown ;\r\n" +
                "Device   g16v8a ;\r\n" +
                "\r\n" +
                "/* inputs */\r\n" +
                "PIN 1 = CLK;\r\n" +
                "PIN 3 = A;\r\n" +
                "\r\n" +
                "/* outputs */\r\n" +
                "PIN 16 = Yn;\r\n" +
                "PIN 15 = X;\r\n" +
                "\r\n" +
                "/* sequential logic */\r\n" +
                "Yn.D = A;\r\n" +
                "\r\n" +
                "/* combinatorial logic */\r\n" +
                "X = A;\r\n", cupl);
    }


    private String createCupl(String filename) throws IOException, PinException, NodeException, ElementNotFoundException, BacktrackException, AnalyseException, ExpressionException, FormatterException, PinMapException {
        File f = new File(Resources.getRoot(), filename);
        ElementLibrary library = new ElementLibrary();
        Circuit c = Circuit.loadCircuit(f, new ShapeFactory(library));
        Model model = new ModelCreator(c, new SubstituteLibrary(library)).createModel(false);
        TruthTable t = new ModelAnalyser(model).analyse();

        ExpressionListenerStore expressions = new ExpressionListenerStore(null);
        new ExpressionCreator(t).create(expressions);

        CuplExporter cuplExporter = new CuplExporter("nn", null);
        cuplExporter.setProjectName("test");
        final ModelAnalyserInfo modelAnalyzerInfo = t.getModelAnalyzerInfo();
        if (modelAnalyzerInfo != null)
            cuplExporter.getPinMapping().addAll(modelAnalyzerInfo.getPins());
        new BuilderExpressionCreator(cuplExporter.getBuilder(), ExpressionModifier.IDENTITY).create(expressions);

        StringWriter str = new StringWriter();
        cuplExporter.writeTo(str);
        return str.toString();
    }

}

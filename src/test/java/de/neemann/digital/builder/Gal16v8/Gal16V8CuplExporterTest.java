/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.Gal16v8;

import de.neemann.digital.analyse.expression.Constant;
import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Variable;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.util.Date;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;

/**
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

}

/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.Gal22v10;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Variable;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;

/**
 */
public class Gal22V10CuplExporterTest extends TestCase {

    public void testCUPLExporter() throws Exception {
        Variable y0 = new Variable("Y_0");
        Variable y1 = new Variable("Y_1");

        // counter
        Expression y0s = not(y0);
        Expression y1s = or(and(not(y0), y1), and(y0, not(y1)));

        Gal22v10CuplExporter ce = new Gal22v10CuplExporter("user", null);
        ce.getPinMapping().parseString("Y_0=14;Y_1=15;A=16");
        ce.setProjectName("test");
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
                "Device   g22v10 ;\r\n" +
                "\r\n" +
                "ar = 'b'0 ;\r\n" +
                "sp = 'b'0 ;\r\n" +
                "\r\n" +
                "/* inputs */\r\n" +
                "PIN 1 = CLK;\r\n" +
                "\r\n" +
                "/* outputs */\r\n" +
                "PIN 14 = Y_0;\r\n" +
                "PIN 15 = Y_1;\r\n" +
                "PIN 16 = A;\r\n" +
                "\r\n" +
                "/* sequential logic */\r\n" +
                "Y_0.D = !Y_0;\r\n" +
                "Y_0.ar = ar ;\r\n" +
                "Y_0.sp = sp ;\r\n" +
                "Y_1.D = (!Y_0 & Y_1) # (Y_0 & !Y_1);\r\n" +
                "Y_1.ar = ar ;\r\n" +
                "Y_1.sp = sp ;\r\n" +
                "\r\n" +
                "/* combinatorial logic */\r\n" +
                "A = Y_0 & Y_1;\r\n", baos.toString());
    }

}

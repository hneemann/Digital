/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.ATF150x;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Variable;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;

/**
 * Created by helmut.neemann on 20.12.2016.
 */
public class ATF1502CuplExporterTest extends TestCase {

    public void testCUPLExporter() throws Exception {
        Variable y0 = new Variable("Y_0");
        Variable y1 = new Variable("Y_1");

        // counter
        Expression y0s = not(y0);
        Expression y1s = or(and(not(y0), y1), and(y0, not(y1)));

        ATF150xCuplExporter ce = ATFDevice.ATF1502PLCC44.getCuplExporter("user", null);
        ce.getPinMapping().parseString("Y_0=4;Y_1=5;A=6");
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
                "Device   f1502ispplcc44 ;\r\n" +
                "\r\n" +
                "ar = 'b'0 ;\r\n" +
                "\r\n" +
                "/* inputs */\r\n" +
                "PIN 43 = CLK;\r\n" +
                "\r\n" +
                "/* outputs */\r\n" +
                "PIN 4 = Y_0;\r\n" +
                "PIN 5 = Y_1;\r\n" +
                "PIN 6 = A;\r\n" +
                "\r\n" +
                "/* sequential logic */\r\n" +
                "Y_0.D = !Y_0;\r\n" +
                "Y_0.ck = CLK ;\r\n" +
                "Y_0.ar = ar ;\r\n" +
                "Y_1.D = (!Y_0 & Y_1) # (Y_0 & !Y_1);\r\n" +
                "Y_1.ck = CLK ;\r\n" +
                "Y_1.ar = ar ;\r\n" +
                "\r\n" +
                "/* combinatorial logic */\r\n" +
                "A = Y_0 & Y_1;\r\n", baos.toString());
    }

}

/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.printer;

import junit.framework.TestCase;

import java.io.IOException;


public class CodePrinterStrTest extends TestCase {

    public void testSimple() throws IOException {
        assertEquals("test", new CodePrinterStr().print("test").toString());
        assertEquals("test\n", new CodePrinterStr().println("test").toString());
        assertEquals("test\n  test\n", new CodePrinterStr().println("test").inc().println("test").toString());
        assertEquals("test\n  test\ntest\n", new CodePrinterStr()
                .println("test")
                .inc()
                .println("test")
                .dec()
                .println("test")
                .toString());

        assertEquals("test;\n" +
                " -- Hello\n" +
                " -- World\n" +
                " -- Long text\n" +
                "test",new CodePrinterStr()
                .println("test;")
                .printComment(" -- ","Hello\nWorld\nLong text").print("test").toString());
        assertEquals("test; -- Hello\n" +
                "      -- World\n" +
                "      -- Long text\n" +
                "test",new CodePrinterStr()
                .print("test;")
                .printComment(" -- ","Hello\nWorld\nLong text").print("test").toString());
    }

}

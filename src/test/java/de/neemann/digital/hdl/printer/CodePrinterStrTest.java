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
    }

}
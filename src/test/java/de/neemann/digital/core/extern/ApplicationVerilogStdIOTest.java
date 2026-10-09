/*
 * Copyright (c) 2019 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.extern.handler.ProcessInterface;
import de.neemann.digital.hdl.hgs.Context;
import junit.framework.TestCase;
import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.EnvironmentVariables;

import java.io.File;
import java.lang.reflect.Field;
import java.util.Map;

public class ApplicationVerilogStdIOTest extends TestCase {


    @Rule
    public final EnvironmentVariables environmentVariables = new EnvironmentVariables();
    private class TestApp extends ApplicationVerilogStdIO {

        @Override
        public ProcessInterface start(String label, String code, PortDefinition inputs, PortDefinition outputs, File root) {
            return null;
        }

        @Override
        public boolean checkSupported() {
            return false;
        }

        @Override
        public String checkCode(String label, String code, PortDefinition inputs, PortDefinition outputs, File root) {
            return null;
        }
    }


    public void testEnsureConsistencyDirect() {
        ElementAttributes attr = extractParameters("module test (\n" +
                "  input a,\n" +
                "  input b,\n" +
                "  output y );\n" +
                "  assign y = a & b;\n" +
                "endmodule");

        assertEquals("test", attr.getLabel());
        assertEquals("a,b", attr.get(Keys.EXTERNAL_INPUTS));
        assertEquals("y", attr.get(Keys.EXTERNAL_OUTPUTS));
    }

    public void testEnsureConsistencyDirect2() {
        ElementAttributes attr = extractParameters("module test(input[4:0] a,b,output[4:0] y);\n" +
                "  assign y = a & b;\n" +
                "endmodule");

        assertEquals("test", attr.getLabel());
        assertEquals("a:5,b:5", attr.get(Keys.EXTERNAL_INPUTS));
        assertEquals("y:5", attr.get(Keys.EXTERNAL_OUTPUTS));
    }

    public void testEnsureConsistencyDirect3() {
        ElementAttributes attr = extractParameters("module test (\n" +
                "  input a,b,\n" +
                "  output y );\n" +
                "  assign y = a & b;\n" +
                "endmodule");

        assertEquals("test", attr.getLabel());
        assertEquals("a,b", attr.get(Keys.EXTERNAL_INPUTS));
        assertEquals("y", attr.get(Keys.EXTERNAL_OUTPUTS));
    }

    public void testEnsureConsistencyIndirect() {
        ElementAttributes attr = extractParameters("module test(a,b,y);\n" +
                "  input a,b;\n" +
                "  output y;\n" +
                "  assign y = a & b;\n" +
                "endmodule");

        assertEquals("test", attr.getLabel());
        assertEquals("a,b", attr.get(Keys.EXTERNAL_INPUTS));
        assertEquals("y", attr.get(Keys.EXTERNAL_OUTPUTS));
    }

    public void testEnsureConsistencyIndirect2() {
        ElementAttributes attr = extractParameters("module test(a,b,y);\n" +
                "  input[4:0] a,b;\n" +
                "  output[4:0] y;\n" +
                "  assign y = a & b;\n" +
                "endmodule");

        assertEquals("test", attr.getLabel());
        assertEquals("a:5,b:5", attr.get(Keys.EXTERNAL_INPUTS));
        assertEquals("y:5", attr.get(Keys.EXTERNAL_OUTPUTS));
    }

    @Test
    public void testReplaceEnvVars() {
        String path = "d:\\verilogcode\\lib";
        String pathAfterReplace = ApplicationVerilogStdIO.replaceEnvVars(path);
        assertEquals("d:\\\\verilogcode\\\\lib",pathAfterReplace);

        path = "/home/verilogcode/lib";
        pathAfterReplace = ApplicationVerilogStdIO.replaceEnvVars(path);
        assertEquals(path,pathAfterReplace);

//        path = "${VERILOG_LIB_PATH}";
//        environmentVariables.set("VERILOG_LIB_PATH","/home/verilogcode/lib");
//        pathAfterReplace = ApplicationVerilogStdIO.replaceEnvVars(path);
//        assertEquals("/home/verilogcode/lib",pathAfterReplace);
//        environmentVariables.clear("VERILOG_LIB_PATH");

    }

    private ElementAttributes extractParameters(String code) {
        TestApp ta = new TestApp();
        ElementAttributes attr = new ElementAttributes();
        attr.set(Keys.EXTERNAL_CODE, code);
        assertTrue(ta.ensureConsistency(attr, null));
        return attr;
    }

}
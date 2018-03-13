/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.io.IOException;

public class VerilogGeneratorTest extends TestCase {

    public void testNegSimple() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/negSimple.dig");
        String sourceCode = new VerilogGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals("module negSimple (\n" +
                     "    input PORT_A,\n" +
                     "    output PORT_Y\n" +
                     ");\n" +
                     "  assign PORT_Y = ~PORT_A;\n" +
                     "endmodule", TestHelper.removeCommentLines(sourceCode));
    }

    public void testXor() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/xor.dig");
        String sourceCode = new VerilogGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals("module xorDig (\n" +
                     "    input PORT_A,\n" +
                     "    input PORT_B,\n" +
                     "    output PORT_Y\n" +
                     ");\n" +
                     "  assign PORT_Y = ~PORT_A & PORT_B | PORT_A & ~PORT_B;\n" +
                     "endmodule", TestHelper.removeCommentLines(sourceCode));
    }

    public void testXorNeg() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/xorNeg.dig");
        String sourceCode = new VerilogGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals("module xorNeg (\n" +
                     "    input PORT_A,\n" +
                     "    input PORT_B,\n" +
                     "    output PORT_Y\n" +
                     ");\n" +
                     "  assign PORT_Y = ~PORT_A & PORT_B | PORT_A & ~PORT_B;\n" +
                     "endmodule", TestHelper.removeCommentLines(sourceCode));
    }

    public void testNeg() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/neg.dig");
        String sourceCode = new VerilogGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals("module neg (\n" +
                     "    input PORT_A,\n" +
                     "    output PORT_Y\n" +
                     ");\n" +
                     "  wire PORT_A_Neg;\n" +
                     "  assign PORT_A_Neg = ~PORT_A;\n" +
                     "  assign PORT_Y = PORT_A_Neg | PORT_A_Neg;\n" +
                     "endmodule", TestHelper.removeCommentLines(sourceCode));
    }

    public void testXorNegBus() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/xorNegBus.dig");
        String sourceCode = new VerilogGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals("module xorNegBus (\n" +
                     "    input [1:0] PORT_A,\n" +
                     "    input [1:0] PORT_B,\n" +
                     "    output [1:0] PORT_Y\n" +
                     ");\n" +
                     "  assign PORT_Y = ~PORT_A & PORT_B | PORT_A & ~PORT_B;\n" +
                     "endmodule", TestHelper.removeCommentLines(sourceCode));
    }

    public void testIdent() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/ident.dig");
        String sourceCode = new VerilogGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals("module ident (\n" +
                     "    input PORT_A,\n" +
                     "    output PORT_Y,\n" +
                     "    output PORT_Z\n" +
                     ");\n" +
                     "  assign PORT_Y = PORT_A;\n" +
                     "  assign PORT_Z = PORT_A;\n" +
                     "endmodule", TestHelper.removeCommentLines(sourceCode));
    }

    public void testNestedAnd() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/nestedAnd.dig");
        String sourceCode = new VerilogGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals("module nestedAnd (\n" +
                     "    input PORT_A,\n" +
                     "    input PORT_B,\n" +
                     "    output PORT_C\n" +
                     ");\n" +
                     "  wire S0;\n" +
                     "  and_dig and_dig0 (\n" +
                     "    .PORT_A(PORT_A),\n" +
                     "    .PORT_B(PORT_B),\n" +
                     "    .PORT_Out(S0)\n" +
                     "  );\n" +
                     "  assign PORT_C = S0;\n" +
                     "endmodule\n" +
                     "module and_dig (\n" +
                     "    input PORT_A,\n" +
                     "    input PORT_B,\n" +
                     "    output PORT_Out\n" +
                     ");\n" +
                     "  assign PORT_Out = PORT_A & PORT_B;\n" +
                     "endmodule", TestHelper.removeCommentLines(sourceCode));
    }

    public void testReadOutput() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/readOutput.dig");
        String sourceCode = new VerilogGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals("module readOutput (\n" +
                     "    input PORT_A,\n" +
                     "    output PORT_Y,\n" +
                     "    output PORT_Z,\n" +
                     "    input PORT_B\n" +
                     ");\n" +
                     "  wire S0;\n" +
                     "  assign S0 = PORT_A & PORT_B;\n" +
                     "  assign PORT_Y = S0;\n" +
                     "  assign PORT_Z = ~S0;\n" +
                     "endmodule", TestHelper.removeCommentLines(sourceCode));
    }


}

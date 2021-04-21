/*
 * Copyright (c) 2018 Ivan Deras.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.verilog2;

import de.neemann.digital.hdl.model2.HDLCircuit;
import de.neemann.digital.hdl.model2.HDLModel;
import de.neemann.digital.hdl.printer.CodePrinterStr;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

public class DescriptionTest extends TestCase {

    public void testDescription() throws Exception {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/model2/naming.dig");
        HDLCircuit circuit = new HDLCircuit(
                br.getCircuit(),
                "main"
                , new HDLModel(br.getLibrary()),
                0, null)
                .applyDefaultOptimizations();
        CodePrinterStr out = new CodePrinterStr();
        new VerilogCreator(out, br.getLibrary()).printHDLCircuit(circuit, "naming", null);

        assertEquals( "\n"
                    + "// Simple test circuit\n"
                    + "// used to test comments.\n"
                    + "module naming (\n"
                    + "  input S0, // First input\n"
                    + "            // This is a far longer text.\n"
                    + "  input S1, // Second input\n"
                    + "  output S2, // first output\n"
                    + "  output S3 // second output\n"
                    + "            // also with a longer text\n"
                    + "\n"
                    + ");\n"
                    + "  wire s4;\n"
                    + "  assign s4 = ~ (S0 | S1);\n"
                    + "  assign S2 = (S0 ^ s4);\n"
                    + "  assign S3 = (s4 ^ S1);\n"
                    + "endmodule\n", out.toString());
    }
}

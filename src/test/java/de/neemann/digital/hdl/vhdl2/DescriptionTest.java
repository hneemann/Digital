/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl2;

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
        new VHDLCreator(out, br.getLibrary()).printHDLCircuit(circuit);

        assertEquals("\n" +
                "LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "\n" +
                "-- Simple test circuit\n" +
                "-- used to test comments.\n" +
                "entity main is\n" +
                "  port (\n" +
                "    S0: in std_logic; -- First input\n" +
                "                      -- This is a far longer text.\n" +
                "    S1: in std_logic; -- Second input\n" +
                "    S2: out std_logic; -- first output\n" +
                "    S3: out std_logic -- second output\n" +
                "                      -- also with a longer text\n" +
                "    );\n" +
                "end main;\n" +
                "\n" +
                "architecture Behavioral of main is\n" +
                "  signal s4: std_logic;\n" +
                "begin\n" +
                "  s4 <= NOT (S0 OR S1);\n" +
                "  S2 <= (S0 XOR s4);\n" +
                "  S3 <= (s4 XOR S1);\n" +
                "end Behavioral;\n", out.toString());
    }
}

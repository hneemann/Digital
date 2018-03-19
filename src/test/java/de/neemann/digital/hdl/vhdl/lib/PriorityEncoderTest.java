/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.hdl.vhdl.TestHelper;
import de.neemann.digital.hdl.vhdl.VHDLGenerator;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.io.IOException;

public class PriorityEncoderTest extends TestCase {

    public void testSimple() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/test/vhdl/priorityEncoder.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals(
                "LIBRARY ieee;\n" +
                        "USE ieee.std_logic_1164.all;\n" +
                        "USE ieee.numeric_std.all;\n" +
                        "entity main is\n" +
                        "  port (\n" +
                        "    PORT_A0: in std_logic;\n" +
                        "    PORT_A1: in std_logic;\n" +
                        "    PORT_A2: in std_logic;\n" +
                        "    PORT_A3: in std_logic;\n" +
                        "    PORT_A4: in std_logic;\n" +
                        "    PORT_A5: in std_logic;\n" +
                        "    PORT_A6: in std_logic;\n" +
                        "    PORT_A7: in std_logic;\n" +
                        "    PORT_any: out std_logic;\n" +
                        "    PORT_num: out std_logic_vector (2 downto 0) );\n" +
                        "end main;\n" +
                        "architecture main_arch of main is\n" +
                        "  component PRIORITY_GATE_3\n" +
                        "    port (\n" +
                        "      PORT_num: out std_logic_vector (2 downto 0);\n" +
                        "      PORT_any: out std_logic;\n" +
                        "      PORT_in0: in std_logic;\n" +
                        "      PORT_in1: in std_logic;\n" +
                        "      PORT_in2: in std_logic;\n" +
                        "      PORT_in3: in std_logic;\n" +
                        "      PORT_in4: in std_logic;\n" +
                        "      PORT_in5: in std_logic;\n" +
                        "      PORT_in6: in std_logic;\n" +
                        "      PORT_in7: in std_logic );\n" +
                        "  end component;\n" +
                        "  signal S0: std_logic;\n" +
                        "  signal S1: std_logic_vector (2 downto 0);\n" +
                        "begin\n" +
                        "  gate0 : PRIORITY_GATE_3\n" +
                        "    port map (\n" +
                        "      PORT_num => S1,\n" +
                        "      PORT_any => S0,\n" +
                        "      PORT_in0 => PORT_A0,\n" +
                        "      PORT_in1 => PORT_A1,\n" +
                        "      PORT_in2 => PORT_A2,\n" +
                        "      PORT_in3 => PORT_A3,\n" +
                        "      PORT_in4 => PORT_A4,\n" +
                        "      PORT_in5 => PORT_A5,\n" +
                        "      PORT_in6 => PORT_A6,\n" +
                        "      PORT_in7 => PORT_A7 );\n" +
                        "  PORT_any <= S0;\n" +
                        "  PORT_num <= S1;\n" +
                        "end main_arch;\n" +
                        "LIBRARY ieee;\n" +
                        "USE ieee.std_logic_1164.all;\n" +
                        "entity PRIORITY_GATE_3 is\n" +
                        "  port (\n" +
                        "    PORT_num: out std_logic_vector (2 downto 0);\n" +
                        "    PORT_any: out std_logic;\n" +
                        "    PORT_in0: in std_logic;\n" +
                        "    PORT_in1: in std_logic;\n" +
                        "    PORT_in2: in std_logic;\n" +
                        "    PORT_in3: in std_logic;\n" +
                        "    PORT_in4: in std_logic;\n" +
                        "    PORT_in5: in std_logic;\n" +
                        "    PORT_in6: in std_logic;\n" +
                        "    PORT_in7: in std_logic );\n" +
                        "end PRIORITY_GATE_3;\n" +
                        "architecture PRIORITY_GATE_3_arch of PRIORITY_GATE_3 is\n" +
                        "begin\n" +
                        "  PORT_num <=\n" +
                        "    \"111\" when PORT_in7 = '1' else\n" +
                        "    \"110\" when PORT_in6 = '1' else\n" +
                        "    \"101\" when PORT_in5 = '1' else\n" +
                        "    \"100\" when PORT_in4 = '1' else\n" +
                        "    \"011\" when PORT_in3 = '1' else\n" +
                        "    \"010\" when PORT_in2 = '1' else\n" +
                        "    \"001\" when PORT_in1 = '1' else\n" +
                        "    \"000\" ;\n" +
                        "  PORT_any <= PORT_in0 OR PORT_in1 OR PORT_in2 OR PORT_in3 OR PORT_in4 OR PORT_in5 OR PORT_in6 OR PORT_in7;\n" +
                        "end PRIORITY_GATE_3_arch;", TestHelper.removeCommentLines(vhdl));
    }

}

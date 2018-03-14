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

public class BitSelectorTest extends TestCase {

    public void testSimple() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/test/vhdl/BitSelect.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals(
                "LIBRARY ieee;\n" +
                        "USE ieee.std_logic_1164.all;\n" +
                        "USE ieee.numeric_std.all;\n" +
                        "entity main is\n" +
                        "  port (\n" +
                        "    PORT_A: in std_logic_vector (3 downto 0);\n" +
                        "    PORT_sel: in std_logic_vector (1 downto 0);\n" +
                        "    PORT_Y: out std_logic );\n" +
                        "end main;\n" +
                        "architecture main_arch of main is\n" +
                        "  component BIT_SEL_2\n" +
                        "    port (\n" +
                        "      PORT_out: out std_logic;\n" +
                        "      PORT_in: in std_logic_vector (3 downto 0);\n" +
                        "      PORT_sel: in std_logic_vector (1 downto 0) );\n" +
                        "  end component;\n" +
                        "  signal S0: std_logic;\n" +
                        "begin\n" +
                        "  gate0 : BIT_SEL_2\n" +
                        "    port map (\n" +
                        "      PORT_out => S0,\n" +
                        "      PORT_in => PORT_A,\n" +
                        "      PORT_sel => PORT_sel );\n" +
                        "  PORT_Y <= S0;\n" +
                        "end main_arch;\n" +
                        "LIBRARY ieee;\n" +
                        "USE ieee.std_logic_1164.all;\n" +
                        "entity BIT_SEL_2 is\n" +
                        "  port (\n" +
                        "    PORT_out: out std_logic;\n" +
                        "    PORT_in: in std_logic_vector (3 downto 0);\n" +
                        "    PORT_sel: in std_logic_vector (1 downto 0) );\n" +
                        "end BIT_SEL_2;\n" +
                        "architecture BIT_SEL_2_arch of BIT_SEL_2 is\n" +
                        "begin\n" +
                        "  with PORT_sel select\n" +
                        "    PORT_out <=\n" +
                        "      PORT_in(0) when \"00\",\n" +
                        "      PORT_in(1) when \"01\",\n" +
                        "      PORT_in(2) when \"10\",\n" +
                        "      PORT_in(3) when \"11\",\n" +
                        "      '0' when others;\n" +
                        "end BIT_SEL_2_arch;", TestHelper.removeCommentLines(vhdl));
    }

    public void testSimple2() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/test/vhdl/BitSelect2.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals(
                "LIBRARY ieee;\n" +
                        "USE ieee.std_logic_1164.all;\n" +
                        "USE ieee.numeric_std.all;\n" +
                        "entity main is\n" +
                        "  port (\n" +
                        "    PORT_A: in std_logic_vector (15 downto 0);\n" +
                        "    PORT_sel: in std_logic_vector (3 downto 0);\n" +
                        "    PORT_Y: out std_logic );\n" +
                        "end main;\n" +
                        "architecture main_arch of main is\n" +
                        "  component BIT_SEL_4\n" +
                        "    port (\n" +
                        "      PORT_out: out std_logic;\n" +
                        "      PORT_in: in std_logic_vector (15 downto 0);\n" +
                        "      PORT_sel: in std_logic_vector (3 downto 0) );\n" +
                        "  end component;\n" +
                        "  signal S0: std_logic;\n" +
                        "begin\n" +
                        "  gate0 : BIT_SEL_4\n" +
                        "    port map (\n" +
                        "      PORT_out => S0,\n" +
                        "      PORT_in => PORT_A,\n" +
                        "      PORT_sel => PORT_sel );\n" +
                        "  PORT_Y <= S0;\n" +
                        "end main_arch;\n" +
                        "LIBRARY ieee;\n" +
                        "USE ieee.std_logic_1164.all;\n" +
                        "entity BIT_SEL_4 is\n" +
                        "  port (\n" +
                        "    PORT_out: out std_logic;\n" +
                        "    PORT_in: in std_logic_vector (15 downto 0);\n" +
                        "    PORT_sel: in std_logic_vector (3 downto 0) );\n" +
                        "end BIT_SEL_4;\n" +
                        "architecture BIT_SEL_4_arch of BIT_SEL_4 is\n" +
                        "begin\n" +
                        "  with PORT_sel select\n" +
                        "    PORT_out <=\n" +
                        "      PORT_in(0) when \"0000\",\n" +
                        "      PORT_in(1) when \"0001\",\n" +
                        "      PORT_in(2) when \"0010\",\n" +
                        "      PORT_in(3) when \"0011\",\n" +
                        "      PORT_in(4) when \"0100\",\n" +
                        "      PORT_in(5) when \"0101\",\n" +
                        "      PORT_in(6) when \"0110\",\n" +
                        "      PORT_in(7) when \"0111\",\n" +
                        "      PORT_in(8) when \"1000\",\n" +
                        "      PORT_in(9) when \"1001\",\n" +
                        "      PORT_in(10) when \"1010\",\n" +
                        "      PORT_in(11) when \"1011\",\n" +
                        "      PORT_in(12) when \"1100\",\n" +
                        "      PORT_in(13) when \"1101\",\n" +
                        "      PORT_in(14) when \"1110\",\n" +
                        "      PORT_in(15) when \"1111\",\n" +
                        "      '0' when others;\n" +
                        "end BIT_SEL_4_arch;", TestHelper.removeCommentLines(vhdl));
    }

}

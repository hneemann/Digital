/*
 * Copyright (c) 2018 Helmut Neemann.
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

public class ROMTest extends TestCase {

    public void testSimple() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/test/vhdl/rom.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals(
                "LIBRARY ieee;\n" +
                        "USE ieee.std_logic_1164.all;\n" +
                        "USE ieee.numeric_std.all;\n" +
                        "entity main is\n" +
                        "  port (\n" +
                        "    PORT_O: out std_logic_vector (7 downto 0);\n" +
                        "    PORT_A: in std_logic_vector (3 downto 0);\n" +
                        "    PORT_en: in std_logic );\n" +
                        "end main;\n" +
                        "architecture main_arch of main is\n" +
                        "  component DIG_ROM_ROM\n" +
                        "    port (\n" +
                        "      PORT_D: out std_logic_vector (7 downto 0);\n" +
                        "      PORT_A: in std_logic_vector (3 downto 0);\n" +
                        "      PORT_sel: in std_logic );\n" +
                        "  end component;\n" +
                        "  signal S0: std_logic_vector (7 downto 0);\n" +
                        "begin\n" +
                        "  gate0 : DIG_ROM_ROM\n" +
                        "    port map (\n" +
                        "      PORT_D => S0,\n" +
                        "      PORT_A => PORT_A,\n" +
                        "      PORT_sel => PORT_en );\n" +
                        "  PORT_O <= S0;\n" +
                        "end main_arch;\n" +
                        "LIBRARY ieee;\n" +
                        "USE ieee.std_logic_1164.all;\n" +
                        "use IEEE.NUMERIC_STD.ALL;\n" +
                        "entity DIG_ROM_ROM is\n" +
                        "  port (\n" +
                        "    PORT_D: out std_logic_vector (7 downto 0);\n" +
                        "    PORT_A: in std_logic_vector (3 downto 0);\n" +
                        "    PORT_sel: in std_logic );\n" +
                        "end DIG_ROM_ROM;\n" +
                        "architecture DIG_ROM_ROM_arch of DIG_ROM_ROM is\n" +
                        "  type mem is array ( 0 to 13) of std_logic_vector (7 downto 0);\n" +
                        "  constant my_Rom : mem := (\n" +
                        "    \"00000011\", \"00000001\", \"00000000\", \"00000101\", \"00000111\", \"00000000\", \n" +
                        "    \"00000000\", \"00000000\", \"00000000\", \"00000111\", \"00000000\", \"00000000\", \n" +
                        "    \"00000000\", \"11111111\");\n" +
                        "begin\n" +
                        "  process (PORT_A, PORT_sel)\n" +
                        "  begin\n" +
                        "    if PORT_sel='0' then\n" +
                        "      PORT_D <= (others => 'Z');\n" +
                        "    elsif PORT_A > \"1101\" then\n" +
                        "      PORT_D <= (others => '0');\n" +
                        "    else\n" +
                        "      PORT_D <= my_rom(to_integer(unsigned(PORT_A)));\n" +
                        "    end if;\n" +
                        "  end process;\n" +
                        "end DIG_ROM_ROM_arch;", TestHelper.removeCommentLines(vhdl));
    }

}

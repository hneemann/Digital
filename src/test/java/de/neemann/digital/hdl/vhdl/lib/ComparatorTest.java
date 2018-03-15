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

public class ComparatorTest extends TestCase {

    public void testSimple() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/test/vhdl/comp.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals(
                "LIBRARY ieee;\n" +
                        "USE ieee.std_logic_1164.all;\n" +
                        "USE ieee.numeric_std.all;\n" +
                        "entity main is\n" +
                        "  port (\n" +
                        "    PORT_a: in std_logic_vector (3 downto 0);\n" +
                        "    PORT_b: in std_logic_vector (3 downto 0);\n" +
                        "    PORT_eq: out std_logic;\n" +
                        "    PORT_gr: out std_logic;\n" +
                        "    PORT_le: out std_logic );\n" +
                        "end main;\n" +
                        "architecture main_arch of main is\n" +
                        "  component COMP_GATE_UNSIGNED\n" +
                        "    generic ( Bits : integer );\n" +
                        "    port (\n" +
                        "      PORT_gr: out std_logic;\n" +
                        "      PORT_eq: out std_logic;\n" +
                        "      PORT_le: out std_logic;\n" +
                        "      PORT_a: in std_logic_vector ((Bits-1) downto 0);\n" +
                        "      PORT_b: in std_logic_vector ((Bits-1) downto 0) );\n" +
                        "  end component;\n" +
                        "  signal S0: std_logic;\n" +
                        "  signal S1: std_logic;\n" +
                        "  signal S2: std_logic;\n" +
                        "begin\n" +
                        "  gate0 : COMP_GATE_UNSIGNED\n" +
                        "    generic map (\n" +
                        "      Bits => 4)\n" +
                        "    port map (\n" +
                        "      PORT_gr => S1,\n" +
                        "      PORT_eq => S0,\n" +
                        "      PORT_le => S2,\n" +
                        "      PORT_a => PORT_a,\n" +
                        "      PORT_b => PORT_b );\n" +
                        "  PORT_eq <= S0;\n" +
                        "  PORT_gr <= S1;\n" +
                        "  PORT_le <= S2;\n" +
                        "end main_arch;\n" +
                        "LIBRARY ieee;\n" +
                        "USE ieee.std_logic_1164.all;\n" +
                        "entity COMP_GATE_UNSIGNED is\n" +
                        "  generic ( Bits : integer );\n" +
                        "  port (\n" +
                        "    PORT_gr: out std_logic;\n" +
                        "    PORT_eq: out std_logic;\n" +
                        "    PORT_le: out std_logic;\n" +
                        "    PORT_a: in std_logic_vector ((Bits-1) downto 0);\n" +
                        "    PORT_b: in std_logic_vector ((Bits-1) downto 0) );\n" +
                        "end COMP_GATE_UNSIGNED;\n" +
                        "architecture COMP_GATE_UNSIGNED_arch of COMP_GATE_UNSIGNED is\n" +
                        "begin\n" +
                        "  process(PORT_a,PORT_b)\n" +
                        "  begin\n" +
                        "    if (PORT_a > PORT_b ) then\n" +
                        "      PORT_le <= '0';\n" +
                        "      PORT_eq <= '0';\n" +
                        "      PORT_gr <= '1';\n" +
                        "    elsif (PORT_a < PORT_b) then\n" +
                        "      PORT_le <= '1';\n" +
                        "      PORT_eq <= '0';\n" +
                        "      PORT_gr <= '0';\n" +
                        "    else\n" +
                        "      PORT_le <= '0';\n" +
                        "      PORT_eq <= '1';\n" +
                        "      PORT_gr <= '0';\n" +
                        "    end if;\n" +
                        "  end process;\n" +
                        "end COMP_GATE_UNSIGNED_arch;", TestHelper.removeCommentLines(vhdl));
    }

    public void testSimpleSig() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/test/vhdl/compSig.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals(
                "LIBRARY ieee;\n" +
                        "USE ieee.std_logic_1164.all;\n" +
                        "USE ieee.numeric_std.all;\n" +
                        "entity main is\n" +
                        "  port (\n" +
                        "    PORT_a: in std_logic_vector (3 downto 0);\n" +
                        "    PORT_b: in std_logic_vector (3 downto 0);\n" +
                        "    PORT_eq: out std_logic;\n" +
                        "    PORT_gr: out std_logic;\n" +
                        "    PORT_le: out std_logic );\n" +
                        "end main;\n" +
                        "architecture main_arch of main is\n" +
                        "  component COMP_GATE_SIGNED\n" +
                        "    generic ( Bits : integer );\n" +
                        "    port (\n" +
                        "      PORT_gr: out std_logic;\n" +
                        "      PORT_eq: out std_logic;\n" +
                        "      PORT_le: out std_logic;\n" +
                        "      PORT_a: in std_logic_vector ((Bits-1) downto 0);\n" +
                        "      PORT_b: in std_logic_vector ((Bits-1) downto 0) );\n" +
                        "  end component;\n" +
                        "  signal S0: std_logic;\n" +
                        "  signal S1: std_logic;\n" +
                        "  signal S2: std_logic;\n" +
                        "begin\n" +
                        "  gate0 : COMP_GATE_SIGNED\n" +
                        "    generic map (\n" +
                        "      Bits => 4)\n" +
                        "    port map (\n" +
                        "      PORT_gr => S1,\n" +
                        "      PORT_eq => S0,\n" +
                        "      PORT_le => S2,\n" +
                        "      PORT_a => PORT_a,\n" +
                        "      PORT_b => PORT_b );\n" +
                        "  PORT_eq <= S0;\n" +
                        "  PORT_gr <= S1;\n" +
                        "  PORT_le <= S2;\n" +
                        "end main_arch;\n" +
                        "LIBRARY ieee;\n" +
                        "USE ieee.std_logic_1164.all;\n" +
                        "USE ieee.numeric_std.all;\n" +
                        "entity COMP_GATE_SIGNED is\n" +
                        "  generic ( Bits : integer );\n" +
                        "  port (\n" +
                        "    PORT_gr: out std_logic;\n" +
                        "    PORT_eq: out std_logic;\n" +
                        "    PORT_le: out std_logic;\n" +
                        "    PORT_a: in std_logic_vector ((Bits-1) downto 0);\n" +
                        "    PORT_b: in std_logic_vector ((Bits-1) downto 0) );\n" +
                        "end COMP_GATE_SIGNED;\n" +
                        "architecture COMP_GATE_SIGNED_arch of COMP_GATE_SIGNED is\n" +
                        "begin\n" +
                        "  process(PORT_a,PORT_b)\n" +
                        "  begin\n" +
                        "    if (signed(PORT_a) > signed(PORT_b)) then\n" +
                        "      PORT_le <= '0';\n" +
                        "      PORT_eq <= '0';\n" +
                        "      PORT_gr <= '1';\n" +
                        "    elsif (signed(PORT_a) < signed(PORT_b)) then\n" +
                        "      PORT_le <= '1';\n" +
                        "      PORT_eq <= '0';\n" +
                        "      PORT_gr <= '0';\n" +
                        "    else\n" +
                        "      PORT_le <= '0';\n" +
                        "      PORT_eq <= '1';\n" +
                        "      PORT_gr <= '0';\n" +
                        "    end if;\n" +
                        "  end process;\n" +
                        "end COMP_GATE_SIGNED_arch;", TestHelper.removeCommentLines(vhdl));
    }

}

package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.hdl.vhdl.TestHelper;
import de.neemann.digital.hdl.vhdl.VHDLGenerator;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.io.IOException;

public class MultiplexerTest extends TestCase {

    public void testSimple() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/mux.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals("LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "entity main is\n" +
                "  port (\n" +
                "    PORT_A: in std_logic;\n" +
                "    PORT_B: in std_logic;\n" +
                "    PORT_C: out std_logic;\n" +
                "    PORT_Sel: in std_logic );\n" +
                "end main;\n" +
                "architecture main_arch of main is\n" +
                "  component MUX_GATE_1\n" +
                "    port (\n" +
                "      PORT_out: out std_logic;\n" +
                "      PORT_sel: in std_logic;\n" +
                "      PORT_in_0: in std_logic;\n" +
                "      PORT_in_1: in std_logic );\n" +
                "  end component;\n" +
                "  signal S0: std_logic;\n" +
                "begin\n" +
                "  gate0 : MUX_GATE_1\n" +
                "    port map (\n" +
                "      PORT_out => S0,\n" +
                "      PORT_sel => PORT_Sel,\n" +
                "      PORT_in_0 => PORT_A,\n" +
                "      PORT_in_1 => PORT_B );\n" +
                "  PORT_C <= S0;\n" +
                "end main_arch;\n" +
                "LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "entity MUX_GATE_1 is\n" +
                "  port (\n" +
                "    PORT_out: out std_logic;\n" +
                "    PORT_sel: in std_logic;\n" +
                "    PORT_in_0: in std_logic;\n" +
                "    PORT_in_1: in std_logic );\n" +
                "end MUX_GATE_1;\n" +
                "architecture MUX_GATE_1_arch of MUX_GATE_1 is\n" +
                "begin\n" +
                "  with PORT_sel select\n" +
                "    PORT_out <=\n" +
                "      PORT_in_0 when '0',\n" +
                "      PORT_in_1 when '1',\n" +
                "      '0' when others;\n" +
                "end MUX_GATE_1_arch;", TestHelper.removeCommentLines(vhdl));
    }

    public void testSimple2() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/mux2.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals("LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "entity main is\n" +
                "  port (\n" +
                "    PORT_A: in std_logic;\n" +
                "    PORT_B: in std_logic;\n" +
                "    PORT_Y: out std_logic;\n" +
                "    PORT_Sel: in std_logic_vector (1 downto 0);\n" +
                "    PORT_C: in std_logic;\n" +
                "    PORT_D: in std_logic );\n" +
                "end main;\n" +
                "architecture main_arch of main is\n" +
                "  component MUX_GATE_2\n" +
                "    port (\n" +
                "      PORT_out: out std_logic;\n" +
                "      PORT_sel: in std_logic_vector (1 downto 0);\n" +
                "      PORT_in_0: in std_logic;\n" +
                "      PORT_in_1: in std_logic;\n" +
                "      PORT_in_2: in std_logic;\n" +
                "      PORT_in_3: in std_logic );\n" +
                "  end component;\n" +
                "  signal S0: std_logic;\n" +
                "begin\n" +
                "  gate0 : MUX_GATE_2\n" +
                "    port map (\n" +
                "      PORT_out => S0,\n" +
                "      PORT_sel => PORT_Sel,\n" +
                "      PORT_in_0 => PORT_A,\n" +
                "      PORT_in_1 => PORT_B,\n" +
                "      PORT_in_2 => PORT_C,\n" +
                "      PORT_in_3 => PORT_D );\n" +
                "  PORT_Y <= S0;\n" +
                "end main_arch;\n" +
                "LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "entity MUX_GATE_2 is\n" +
                "  port (\n" +
                "    PORT_out: out std_logic;\n" +
                "    PORT_sel: in std_logic_vector (1 downto 0);\n" +
                "    PORT_in_0: in std_logic;\n" +
                "    PORT_in_1: in std_logic;\n" +
                "    PORT_in_2: in std_logic;\n" +
                "    PORT_in_3: in std_logic );\n" +
                "end MUX_GATE_2;\n" +
                "architecture MUX_GATE_2_arch of MUX_GATE_2 is\n" +
                "begin\n" +
                "  with PORT_sel select\n" +
                "    PORT_out <=\n" +
                "      PORT_in_0 when \"00\",\n" +
                "      PORT_in_1 when \"01\",\n" +
                "      PORT_in_2 when \"10\",\n" +
                "      PORT_in_3 when \"11\",\n" +
                "      '0' when others;\n" +
                "end MUX_GATE_2_arch;", TestHelper.removeCommentLines(vhdl));
    }

    public void testSimple3() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/mux3.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals("LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "entity main is\n" +
                "  port (\n" +
                "    PORT_A: in std_logic_vector (3 downto 0);\n" +
                "    PORT_B: in std_logic_vector (3 downto 0);\n" +
                "    PORT_Y: out std_logic_vector (3 downto 0);\n" +
                "    PORT_Sel: in std_logic_vector (1 downto 0);\n" +
                "    PORT_C: in std_logic_vector (3 downto 0);\n" +
                "    PORT_D: in std_logic_vector (3 downto 0) );\n" +
                "end main;\n" +
                "architecture main_arch of main is\n" +
                "  component MUX_GATE_BUS_2\n" +
                "    generic ( bitCount : integer );\n" +
                "    port (\n" +
                "      PORT_sel: in std_logic_vector (1 downto 0);\n" +
                "      PORT_out: out std_logic_vector ( (bitCount-1)  downto 0);\n" +
                "      PORT_in_0: in std_logic_vector ( (bitCount-1)  downto 0);\n" +
                "      PORT_in_1: in std_logic_vector ( (bitCount-1)  downto 0);\n" +
                "      PORT_in_2: in std_logic_vector ( (bitCount-1)  downto 0);\n" +
                "      PORT_in_3: in std_logic_vector ( (bitCount-1)  downto 0) );\n" +
                "  end component;\n" +
                "  signal S0: std_logic_vector (3 downto 0);\n" +
                "begin\n" +
                "  gate0 : MUX_GATE_BUS_2\n" +
                "    generic map ( bitCount => 4)\n" +
                "    port map (\n" +
                "      PORT_out => S0,\n" +
                "      PORT_sel => PORT_Sel,\n" +
                "      PORT_in_0 => PORT_A,\n" +
                "      PORT_in_1 => PORT_B,\n" +
                "      PORT_in_2 => PORT_C,\n" +
                "      PORT_in_3 => PORT_D );\n" +
                "  PORT_Y <= S0;\n" +
                "end main_arch;\n" +
                "LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "entity MUX_GATE_BUS_2 is\n" +
                "  generic ( bitCount : integer );\n" +
                "  port (\n" +
                "    PORT_sel: in std_logic_vector (1 downto 0);\n" +
                "    PORT_out: out std_logic_vector ( (bitCount-1)  downto 0);\n" +
                "    PORT_in_0: in std_logic_vector ( (bitCount-1)  downto 0);\n" +
                "    PORT_in_1: in std_logic_vector ( (bitCount-1)  downto 0);\n" +
                "    PORT_in_2: in std_logic_vector ( (bitCount-1)  downto 0);\n" +
                "    PORT_in_3: in std_logic_vector ( (bitCount-1)  downto 0) );\n" +
                "end MUX_GATE_BUS_2;\n" +
                "architecture MUX_GATE_BUS_2_arch of MUX_GATE_BUS_2 is\n" +
                "begin\n" +
                "  with PORT_sel select\n" +
                "    PORT_out <=\n" +
                "      PORT_in_0 when \"00\",\n" +
                "      PORT_in_1 when \"01\",\n" +
                "      PORT_in_2 when \"10\",\n" +
                "      PORT_in_3 when \"11\",\n" +
                "      (others => '0') when others;\n" +
                "end MUX_GATE_BUS_2_arch;", TestHelper.removeCommentLines(vhdl));
    }

}

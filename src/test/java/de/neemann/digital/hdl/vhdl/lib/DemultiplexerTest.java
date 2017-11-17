package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.hdl.vhdl.TestHelper;
import de.neemann.digital.hdl.vhdl.VHDLGenerator;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.io.IOException;

public class DemultiplexerTest extends TestCase {

    public void testSimple() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/demux.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals("LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "entity main is\n" +
                "  port (\n" +
                "    PORT_S: in std_logic;\n" +
                "    PORT_D: in std_logic;\n" +
                "    PORT_Y: out std_logic;\n" +
                "    PORT_Z: out std_logic );\n" +
                "end main;\n" +
                "architecture main_arch of main is\n" +
                "  component DEMUX_GATE_1\n" +
                "    port (\n" +
                "      PORT_out_0: out std_logic;\n" +
                "      PORT_out_1: out std_logic;\n" +
                "      PORT_sel: in std_logic;\n" +
                "      PORT_in: in std_logic );\n" +
                "  end component;\n" +
                "  signal S0: std_logic;\n" +
                "  signal S1: std_logic;\n" +
                "begin\n" +
                "  gate0 : DEMUX_GATE_1\n" +
                "    port map (\n" +
                "      PORT_out_0 => S0,\n" +
                "      PORT_out_1 => S1,\n" +
                "      PORT_sel => PORT_S,\n" +
                "      PORT_in => PORT_D );\n" +
                "  PORT_Y <= S0;\n" +
                "  PORT_Z <= S1;\n" +
                "end main_arch;\n" +
                "LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "entity DEMUX_GATE_1 is\n" +
                "  port (\n" +
                "    PORT_out_0: out std_logic;\n" +
                "    PORT_out_1: out std_logic;\n" +
                "    PORT_sel: in std_logic;\n" +
                "    PORT_in: in std_logic );\n" +
                "end DEMUX_GATE_1;\n" +
                "architecture DEMUX_GATE_1_arch of DEMUX_GATE_1 is\n" +
                "begin\n" +
                "    PORT_out_0 <= PORT_in when PORT_sel = '0' else '0';\n" +
                "    PORT_out_1 <= PORT_in when PORT_sel = '1' else '0';\n" +
                "end DEMUX_GATE_1_arch;", TestHelper.removeCommentLines(vhdl));
    }

    public void testSimple2() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/demux2.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals("LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "entity main is\n" +
                "  port (\n" +
                "    PORT_S: in std_logic;\n" +
                "    PORT_D: in std_logic_vector (7 downto 0);\n" +
                "    PORT_Y: out std_logic_vector (7 downto 0);\n" +
                "    PORT_Z: out std_logic_vector (7 downto 0) );\n" +
                "end main;\n" +
                "architecture main_arch of main is\n" +
                "  component DEMUX_GATE_BUS_1\n" +
                "    generic ( bitCount : integer );\n" +
                "    port (\n" +
                "      PORT_sel: in std_logic;\n" +
                "      PORT_in: in std_logic_vector ( (bitCount-1)  downto 0);\n" +
                "      PORT_out_0: out std_logic_vector ( (bitCount-1)  downto 0);\n" +
                "      PORT_out_1: out std_logic_vector ( (bitCount-1)  downto 0) );\n" +
                "  end component;\n" +
                "  signal S0: std_logic_vector (7 downto 0);\n" +
                "  signal S1: std_logic_vector (7 downto 0);\n" +
                "begin\n" +
                "  gate0 : DEMUX_GATE_BUS_1\n" +
                "    generic map ( bitCount => 8)\n" +
                "    port map (\n" +
                "      PORT_out_0 => S0,\n" +
                "      PORT_out_1 => S1,\n" +
                "      PORT_sel => PORT_S,\n" +
                "      PORT_in => PORT_D );\n" +
                "  PORT_Y <= S0;\n" +
                "  PORT_Z <= S1;\n" +
                "end main_arch;\n" +
                "LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "entity DEMUX_GATE_BUS_1 is\n" +
                "  generic ( bitCount : integer );\n" +
                "  port (\n" +
                "    PORT_sel: in std_logic;\n" +
                "    PORT_in: in std_logic_vector ( (bitCount-1)  downto 0);\n" +
                "    PORT_out_0: out std_logic_vector ( (bitCount-1)  downto 0);\n" +
                "    PORT_out_1: out std_logic_vector ( (bitCount-1)  downto 0) );\n" +
                "end DEMUX_GATE_BUS_1;\n" +
                "architecture DEMUX_GATE_BUS_1_arch of DEMUX_GATE_BUS_1 is\n" +
                "begin\n" +
                "    PORT_out_0 <= PORT_in when PORT_sel = '0' else (others => '0');\n" +
                "    PORT_out_1 <= PORT_in when PORT_sel = '1' else (others => '0');\n" +
                "end DEMUX_GATE_BUS_1_arch;",TestHelper.removeCommentLines(vhdl));
    }

    public void testSimple3() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/demux3.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals("LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "entity main is\n" +
                "  port (\n" +
                "    PORT_S: in std_logic_vector (1 downto 0);\n" +
                "    PORT_D: in std_logic_vector (7 downto 0);\n" +
                "    PORT_Y: out std_logic_vector (7 downto 0);\n" +
                "    PORT_Z: out std_logic_vector (7 downto 0);\n" +
                "    PORT_U: out std_logic_vector (7 downto 0);\n" +
                "    PORT_V: out std_logic_vector (7 downto 0) );\n" +
                "end main;\n" +
                "architecture main_arch of main is\n" +
                "  component DEMUX_GATE_BUS_2\n" +
                "    generic ( bitCount : integer );\n" +
                "    port (\n" +
                "      PORT_sel: in std_logic_vector (1 downto 0);\n" +
                "      PORT_in: in std_logic_vector ( (bitCount-1)  downto 0);\n" +
                "      PORT_out_0: out std_logic_vector ( (bitCount-1)  downto 0);\n" +
                "      PORT_out_1: out std_logic_vector ( (bitCount-1)  downto 0);\n" +
                "      PORT_out_2: out std_logic_vector ( (bitCount-1)  downto 0);\n" +
                "      PORT_out_3: out std_logic_vector ( (bitCount-1)  downto 0) );\n" +
                "  end component;\n" +
                "  signal S0: std_logic_vector (7 downto 0);\n" +
                "  signal S1: std_logic_vector (7 downto 0);\n" +
                "  signal S2: std_logic_vector (7 downto 0);\n" +
                "  signal S3: std_logic_vector (7 downto 0);\n" +
                "begin\n" +
                "  gate0 : DEMUX_GATE_BUS_2\n" +
                "    generic map ( bitCount => 8)\n" +
                "    port map (\n" +
                "      PORT_out_0 => S0,\n" +
                "      PORT_out_1 => S1,\n" +
                "      PORT_out_2 => S2,\n" +
                "      PORT_out_3 => S3,\n" +
                "      PORT_sel => PORT_S,\n" +
                "      PORT_in => PORT_D );\n" +
                "  PORT_Y <= S0;\n" +
                "  PORT_Z <= S1;\n" +
                "  PORT_U <= S2;\n" +
                "  PORT_V <= S3;\n" +
                "end main_arch;\n" +
                "LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "entity DEMUX_GATE_BUS_2 is\n" +
                "  generic ( bitCount : integer );\n" +
                "  port (\n" +
                "    PORT_sel: in std_logic_vector (1 downto 0);\n" +
                "    PORT_in: in std_logic_vector ( (bitCount-1)  downto 0);\n" +
                "    PORT_out_0: out std_logic_vector ( (bitCount-1)  downto 0);\n" +
                "    PORT_out_1: out std_logic_vector ( (bitCount-1)  downto 0);\n" +
                "    PORT_out_2: out std_logic_vector ( (bitCount-1)  downto 0);\n" +
                "    PORT_out_3: out std_logic_vector ( (bitCount-1)  downto 0) );\n" +
                "end DEMUX_GATE_BUS_2;\n" +
                "architecture DEMUX_GATE_BUS_2_arch of DEMUX_GATE_BUS_2 is\n" +
                "begin\n" +
                "    PORT_out_0 <= PORT_in when PORT_sel = \"00\" else (others => '0');\n" +
                "    PORT_out_1 <= PORT_in when PORT_sel = \"01\" else (others => '0');\n" +
                "    PORT_out_2 <= PORT_in when PORT_sel = \"10\" else (others => '0');\n" +
                "    PORT_out_3 <= PORT_in when PORT_sel = \"11\" else (others => '0');\n" +
                "end DEMUX_GATE_BUS_2_arch;", TestHelper.removeCommentLines(vhdl));
    }

}

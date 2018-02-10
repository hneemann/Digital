package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.hdl.vhdl.TestHelper;
import de.neemann.digital.hdl.vhdl.VHDLGenerator;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.io.IOException;

public class SplitterTest extends TestCase {

    public void testSimple() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/splitter.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals("LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "entity main is\n" +
                "  port (\n" +
                "    PORT_A: in std_logic_vector (3 downto 0);\n" +
                "    PORT_B: in std_logic_vector (3 downto 0);\n" +
                "    PORT_Y: out std_logic_vector (2 downto 0);\n" +
                "    PORT_Z: out std_logic_vector (4 downto 0) );\n" +
                "end main;\n" +
                "architecture main_arch of main is\n" +
                "  signal S0: std_logic_vector (2 downto 0);\n" +
                "  signal S1: std_logic_vector (4 downto 0);\n" +
                "  signal S2: std_logic_vector (7 downto 0);\n" +
                "begin\n" +
                "  S2(3 downto 0) <= PORT_A;\n" +
                "  S2(7 downto 4) <= PORT_B;\n" +
                "  S0 <= S2(2 downto 0);\n" +
                "  S1 <= S2(7 downto 3);\n" +
                "  PORT_Y <= S0;\n" +
                "  PORT_Z <= S1;\n" +
                "end main_arch;", TestHelper.removeCommentLines(vhdl));
    }

    public void testSimple2() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/splitter2.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals("LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "entity main is\n" +
                "  port (\n" +
                "    PORT_inst: in std_logic_vector (15 downto 0);\n" +
                "    PORT_9SD: out std_logic_vector (15 downto 0) );\n" +
                "end main;\n" +
                "architecture main_arch of main is\n" +
                "  signal S0: std_logic_vector (15 downto 0);\n" +
                "  signal S1: std_logic_vector (7 downto 0);\n" +
                "  signal S2: std_logic;\n" +
                "  signal S3: std_logic_vector (15 downto 0);\n" +
                "  signal S4: std_logic_vector (15 downto 0);\n" +
                "begin\n" +
                "  S3(15 downto 0) <= PORT_inst;\n" +
                "  S1 <= S3(7 downto 0);\n" +
                "  S2 <= S3(8);\n" +
                "  S4(7 downto 0) <= S1;\n" +
                "  S4(8) <= S2;\n" +
                "  S4(9) <= S2;\n" +
                "  S4(10) <= S2;\n" +
                "  S4(11) <= S2;\n" +
                "  S4(12) <= S2;\n" +
                "  S4(13) <= S2;\n" +
                "  S4(14) <= S2;\n" +
                "  S4(15) <= S2;\n" +
                "  S0 <= S4(15 downto 0);\n" +
                "  PORT_9SD <= S0;\n" +
                "end main_arch;", TestHelper.removeCommentLines(vhdl));
    }

    public void testSimple3() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/splitter3.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals("LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "entity main is\n" +
                "  port (\n" +
                "    PORT_A: in std_logic_vector (3 downto 0);\n" +
                "    PORT_Y: out std_logic_vector (7 downto 0);\n" +
                "    PORT_Z: out std_logic_vector (7 downto 0);\n" +
                "    PORT_B: in std_logic_vector (3 downto 0) );\n" +
                "end main;\n" +
                "architecture main_arch of main is\n" +
                "  component NOT_GATE_BUS\n" +
                "    generic ( bitCount : integer );\n" +
                "    port (\n" +
                "      PORT_out: out std_logic_vector ((bitCount-1) downto 0);\n" +
                "      PORT_in: in std_logic_vector ((bitCount-1) downto 0) );\n" +
                "  end component;\n" +
                "  signal S0: std_logic_vector (7 downto 0);\n" +
                "  signal S1: std_logic_vector (7 downto 0);\n" +
                "  signal S2: std_logic_vector (7 downto 0);\n" +
                "begin\n" +
                "  S2(3 downto 0) <= PORT_A;\n" +
                "  S2(7 downto 4) <= PORT_B;\n" +
                "  S0 <= S2(7 downto 0);\n" +
                "  gate0 : NOT_GATE_BUS\n" +
                "    generic map ( bitCount => 8)\n" +
                "    port map (\n" +
                "      PORT_out => S1,\n" +
                "      PORT_in => S0 );\n" +
                "  PORT_Y <= S0;\n" +
                "  PORT_Z <= S1;\n" +
                "end main_arch;\n" +
                "LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "entity NOT_GATE_BUS is\n" +
                "  generic ( bitCount : integer );\n" +
                "  port (\n" +
                "    PORT_out: out std_logic_vector ((bitCount-1) downto 0);\n" +
                "    PORT_in: in std_logic_vector ((bitCount-1) downto 0) );\n" +
                "end NOT_GATE_BUS;\n" +
                "architecture NOT_GATE_BUS_arch of NOT_GATE_BUS is\n" +
                "begin\n" +
                "  PORT_out <=  NOT( PORT_in );\n" +
                "end NOT_GATE_BUS_arch;", TestHelper.removeCommentLines(vhdl));
    }

}

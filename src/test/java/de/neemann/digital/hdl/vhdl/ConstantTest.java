package de.neemann.digital.hdl.vhdl;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.io.IOException;

public class ConstantTest extends TestCase {

    public void testSimple() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/const.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals("LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "entity main is\n" +
                "  port (\n" +
                "    PORT_A: in std_logic;\n" +
                "    PORT_Y: out std_logic );\n" +
                "end main;\n" +
                "architecture main_arch of main is\n" +
                "  component XOR_GATE_2\n" +
                "    port (\n" +
                "      PORT_out: out std_logic;\n" +
                "      PORT_In_1: in std_logic;\n" +
                "      PORT_In_2: in std_logic );\n" +
                "  end component;\n" +
                "  signal S0: std_logic;\n" +
                "  signal S1: std_logic;\n" +
                "begin\n" +
                "  S1 <= '0';\n" +
                "  gate0 : XOR_GATE_2\n" +
                "    port map (\n" +
                "      PORT_out => S0,\n" +
                "      PORT_In_1 => PORT_A,\n" +
                "      PORT_In_2 => S1 );\n" +
                "  PORT_Y <= S0;\n" +
                "end main_arch;\n" +
                "LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "entity XOR_GATE_2 is\n" +
                "  port (\n" +
                "    PORT_out: out std_logic;\n" +
                "    PORT_In_1: in std_logic;\n" +
                "    PORT_In_2: in std_logic );\n" +
                "end XOR_GATE_2;\n" +
                "architecture XOR_GATE_2_arch of XOR_GATE_2 is\n" +
                "begin\n" +
                "  PORT_out <= PORT_In_1 XOR PORT_In_2;\n" +
                "end XOR_GATE_2_arch;", TestHelper.removeCommentLines(vhdl));
    }

    public void testSimple2() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/const2.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals("LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "entity main is\n" +
                "  port (\n" +
                "    PORT_A: in std_logic_vector (2 downto 0);\n" +
                "    PORT_Y: out std_logic_vector (2 downto 0) );\n" +
                "end main;\n" +
                "architecture main_arch of main is\n" +
                "  component XOR_GATE_BUS_2\n" +
                "    generic ( bitCount : integer );\n" +
                "    port (\n" +
                "      PORT_out: out std_logic_vector ((bitCount-1) downto 0);\n" +
                "      PORT_In_1: in std_logic_vector ((bitCount-1) downto 0);\n" +
                "      PORT_In_2: in std_logic_vector ((bitCount-1) downto 0) );\n" +
                "  end component;\n" +
                "  signal S0: std_logic_vector (2 downto 0);\n" +
                "  signal S1: std_logic_vector (2 downto 0);\n" +
                "begin\n" +
                "  S1 <= \"000\";\n" +
                "  gate0 : XOR_GATE_BUS_2\n" +
                "    generic map ( bitCount => 3)\n" +
                "    port map (\n" +
                "      PORT_out => S0,\n" +
                "      PORT_In_1 => PORT_A,\n" +
                "      PORT_In_2 => S1 );\n" +
                "  PORT_Y <= S0;\n" +
                "end main_arch;\n" +
                "LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "entity XOR_GATE_BUS_2 is\n" +
                "  generic ( bitCount : integer );\n" +
                "  port (\n" +
                "    PORT_out: out std_logic_vector ((bitCount-1) downto 0);\n" +
                "    PORT_In_1: in std_logic_vector ((bitCount-1) downto 0);\n" +
                "    PORT_In_2: in std_logic_vector ((bitCount-1) downto 0) );\n" +
                "end XOR_GATE_BUS_2;\n" +
                "architecture XOR_GATE_BUS_2_arch of XOR_GATE_BUS_2 is\n" +
                "begin\n" +
                "  PORT_out <= PORT_In_1 XOR PORT_In_2;\n" +
                "end XOR_GATE_BUS_2_arch;", TestHelper.removeCommentLines(vhdl));
    }

    public void testSimple3() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/const3.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals("LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "entity main is\n" +
                "  port (\n" +
                "    PORT_Y: out std_logic_vector (2 downto 0) );\n" +
                "end main;\n" +
                "architecture main_arch of main is\n" +
                "  signal S0: std_logic_vector (2 downto 0);\n" +
                "begin\n" +
                "  S0 <= \"000\";\n" +
                "  PORT_Y <= S0;\n" +
                "end main_arch;", TestHelper.removeCommentLines(vhdl));
    }

    /*
    public void testPullUp() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/constPullUp.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals("-- auto generated by Digital\n" +
                "\n" +
                "LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "\n" +
                "entity main is\n" +
                "  port (\n" +
                "    PORT_Y: out std_logic_vector (2 downto 0) );\n" +
                "end main;\n" +
                "\n" +
                "architecture main_arch of main is\n" +
                "\n" +
                "begin\n" +
                "  PORT_Y <= \"HHH\";\n" +
                "end main_arch;\n" +
                "\n" +
                "-- library components\n", vhdl);
    }

    public void testPullDown() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/constPullDown.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals("-- auto generated by Digital\n" +
                "\n" +
                "LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "\n" +
                "entity main is\n" +
                "  port (\n" +
                "    PORT_Y: out std_logic_vector (2 downto 0) );\n" +
                "end main;\n" +
                "\n" +
                "architecture main_arch of main is\n" +
                "\n" +
                "begin\n" +
                "  PORT_Y <= \"LLL\";\n" +
                "end main_arch;\n" +
                "\n" +
                "-- library components\n", vhdl);
    }*/

}
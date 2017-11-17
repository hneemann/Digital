package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.hdl.vhdl.TestHelper;
import de.neemann.digital.hdl.vhdl.VHDLGenerator;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.io.IOException;

public class DecoderVHDLTest extends TestCase {

    public void testSimple() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/decoder.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals("LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "entity main is\n" +
                "  port (\n" +
                "    PORT_A: out std_logic;\n" +
                "    PORT_Sel: in std_logic;\n" +
                "    PORT_B: out std_logic );\n" +
                "end main;\n" +
                "architecture main_arch of main is\n" +
                "  component MUX_GATE_1\n" +
                "    port (\n" +
                "      PORT_out_0: out std_logic;\n" +
                "      PORT_out_1: out std_logic;\n" +
                "      PORT_sel: in std_logic );\n" +
                "  end component;\n" +
                "  signal S0: std_logic;\n" +
                "  signal S1: std_logic;\n" +
                "begin\n" +
                "  gate0 : MUX_GATE_1\n" +
                "    port map (\n" +
                "      PORT_out_0 => S0,\n" +
                "      PORT_out_1 => S1,\n" +
                "      PORT_sel => PORT_Sel );\n" +
                "  PORT_A <= S0;\n" +
                "  PORT_B <= S1;\n" +
                "end main_arch;\n" +
                "LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "entity MUX_GATE_1 is\n" +
                "  port (\n" +
                "    PORT_out_0: out std_logic;\n" +
                "    PORT_out_1: out std_logic;\n" +
                "    PORT_sel: in std_logic );\n" +
                "end MUX_GATE_1;\n" +
                "architecture MUX_GATE_1_arch of MUX_GATE_1 is\n" +
                "begin\n" +
                "  PORT_out_0 <= '1' when PORT_sel = '0' else '0';\n" +
                "  PORT_out_1 <= '1' when PORT_sel = '1' else '0';\n" +
                "end MUX_GATE_1_arch;", TestHelper.removeCommentLines(vhdl));
    }

    public void testSimple2() throws IOException, ElementNotFoundException, PinException, NodeException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/decoder2.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();
        assertEquals("LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "entity main is\n" +
                "  port (\n" +
                "    PORT_A: out std_logic;\n" +
                "    PORT_Sel: in std_logic_vector (1 downto 0);\n" +
                "    PORT_B: out std_logic;\n" +
                "    PORT_C: out std_logic;\n" +
                "    PORT_D: out std_logic );\n" +
                "end main;\n" +
                "architecture main_arch of main is\n" +
                "  component MUX_GATE_2\n" +
                "    port (\n" +
                "      PORT_out_0: out std_logic;\n" +
                "      PORT_out_1: out std_logic;\n" +
                "      PORT_out_2: out std_logic;\n" +
                "      PORT_out_3: out std_logic;\n" +
                "      PORT_sel: in std_logic_vector (1 downto 0) );\n" +
                "  end component;\n" +
                "  signal S0: std_logic;\n" +
                "  signal S1: std_logic;\n" +
                "  signal S2: std_logic;\n" +
                "  signal S3: std_logic;\n" +
                "begin\n" +
                "  gate0 : MUX_GATE_2\n" +
                "    port map (\n" +
                "      PORT_out_0 => S0,\n" +
                "      PORT_out_1 => S1,\n" +
                "      PORT_out_2 => S2,\n" +
                "      PORT_out_3 => S3,\n" +
                "      PORT_sel => PORT_Sel );\n" +
                "  PORT_A <= S0;\n" +
                "  PORT_B <= S1;\n" +
                "  PORT_C <= S2;\n" +
                "  PORT_D <= S3;\n" +
                "end main_arch;\n" +
                "LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "entity MUX_GATE_2 is\n" +
                "  port (\n" +
                "    PORT_out_0: out std_logic;\n" +
                "    PORT_out_1: out std_logic;\n" +
                "    PORT_out_2: out std_logic;\n" +
                "    PORT_out_3: out std_logic;\n" +
                "    PORT_sel: in std_logic_vector (1 downto 0) );\n" +
                "end MUX_GATE_2;\n" +
                "architecture MUX_GATE_2_arch of MUX_GATE_2 is\n" +
                "begin\n" +
                "  PORT_out_0 <= '1' when PORT_sel = \"00\" else '0';\n" +
                "  PORT_out_1 <= '1' when PORT_sel = \"01\" else '0';\n" +
                "  PORT_out_2 <= '1' when PORT_sel = \"10\" else '0';\n" +
                "  PORT_out_3 <= '1' when PORT_sel = \"11\" else '0';\n" +
                "end MUX_GATE_2_arch;", TestHelper.removeCommentLines(vhdl));
    }


}
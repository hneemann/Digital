package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.hdl.model.HDLException;
import de.neemann.digital.hdl.vhdl.TestHelper;
import de.neemann.digital.hdl.vhdl.VHDLGenerator;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.io.IOException;

public class ClockTest extends TestCase {

    public void testClock() throws PinException, NodeException, ElementNotFoundException, IOException, HDLException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/Clock.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();

        assertEquals("LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "entity main is\n" +
                "  port (\n" +
                "    PORT_Y0: out std_logic;\n" +
                "    PORT_Clk: in std_logic );\n" +
                "end main;\n" +
                "architecture main_arch of main is\n" +
                "  component NOT_GATE\n" +
                "    port (\n" +
                "      PORT_out: out std_logic;\n" +
                "      PORT_in: in std_logic );\n" +
                "  end component;\n" +
                "  component DIG_simpleClockDivider\n" +
                "      generic (\n" +
                "        maxCounter : integer );\n" +
                "      port (\n" +
                "        PORT_out: out std_logic;\n" +
                "        PORT_in: in std_logic );\n" +
                "  end component;\n" +
                "  signal S0: std_logic;\n" +
                "  signal S1: std_logic;\n" +
                "begin\n" +
                "  gate0 : NOT_GATE\n" +
                "    port map (\n" +
                "      PORT_out => S0,\n" +
                "      PORT_in => S1 );\n" +
                "  gate1 : DIG_simpleClockDivider\n" +
                "    generic map (\n" +
                "      maxCounter => 2500000 )\n" +
                "    port map (\n" +
                "      PORT_in => PORT_Clk,\n" +
                "      PORT_out => S1 );\n" +
                "  PORT_Y0 <= S0;\n" +
                "end main_arch;\n" +
                "LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "entity NOT_GATE is\n" +
                "  port (\n" +
                "    PORT_out: out std_logic;\n" +
                "    PORT_in: in std_logic );\n" +
                "end NOT_GATE;\n" +
                "architecture NOT_GATE_arch of NOT_GATE is\n" +
                "begin\n" +
                "  PORT_out <=  NOT( PORT_in );\n" +
                "end NOT_GATE_arch;\n" +
                "LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "USE ieee.std_logic_unsigned.all;\n" +
                "entity DIG_simpleClockDivider is\n" +
                "    generic (\n" +
                "      maxCounter : integer );\n" +
                "    port (\n" +
                "      PORT_out: out std_logic;\n" +
                "      PORT_in: in std_logic );\n" +
                "end DIG_simpleClockDivider;\n" +
                "architecture DIG_simpleClockDivider_arch of DIG_simpleClockDivider is\n" +
                "  signal counter: integer range 0 to maxCounter := 0;\n" +
                "  signal state: std_logic;\n" +
                "begin\n" +
                "  process (PORT_in)\n" +
                "  begin\n" +
                "    if rising_edge(PORT_in) then\n" +
                "       if counter = maxCounter then\n" +
                "          counter <= 0;\n" +
                "          state <= NOT (state);\n" +
                "       else\n" +
                "          counter <= counter+1;\n" +
                "       end if;\n" +
                "    end if;\n" +
                "  end process;\n" +
                "  PORT_out <= state;\n" +
                "end DIG_simpleClockDivider_arch;", TestHelper.removeCommentLines(vhdl));
    }


    public void testClock2() throws PinException, NodeException, ElementNotFoundException, IOException, HDLException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/Clock2.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();

        assertEquals("LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "entity main is\n" +
                "  port (\n" +
                "    PORT_Y0: out std_logic;\n" +
                "    PORT_Clk: in std_logic;\n" +
                "    PORT_A: in std_logic );\n" +
                "end main;\n" +
                "architecture main_arch of main is\n" +
                "  component XOR_GATE_2\n" +
                "    port (\n" +
                "      PORT_out: out std_logic;\n" +
                "      PORT_In_1: in std_logic;\n" +
                "      PORT_In_2: in std_logic );\n" +
                "  end component;\n" +
                "  component DIG_simpleClockDivider\n" +
                "      generic (\n" +
                "        maxCounter : integer );\n" +
                "      port (\n" +
                "        PORT_out: out std_logic;\n" +
                "        PORT_in: in std_logic );\n" +
                "  end component;\n" +
                "  signal S0: std_logic;\n" +
                "  signal S1: std_logic;\n" +
                "begin\n" +
                "  gate0 : XOR_GATE_2\n" +
                "    port map (\n" +
                "      PORT_out => S0,\n" +
                "      PORT_In_1 => PORT_A,\n" +
                "      PORT_In_2 => S1 );\n" +
                "  gate1 : DIG_simpleClockDivider\n" +
                "    generic map (\n" +
                "      maxCounter => 2500000 )\n" +
                "    port map (\n" +
                "      PORT_in => PORT_Clk,\n" +
                "      PORT_out => S1 );\n" +
                "  PORT_Y0 <= S0;\n" +
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
                "end XOR_GATE_2_arch;\n" +
                "LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "USE ieee.std_logic_unsigned.all;\n" +
                "entity DIG_simpleClockDivider is\n" +
                "    generic (\n" +
                "      maxCounter : integer );\n" +
                "    port (\n" +
                "      PORT_out: out std_logic;\n" +
                "      PORT_in: in std_logic );\n" +
                "end DIG_simpleClockDivider;\n" +
                "architecture DIG_simpleClockDivider_arch of DIG_simpleClockDivider is\n" +
                "  signal counter: integer range 0 to maxCounter := 0;\n" +
                "  signal state: std_logic;\n" +
                "begin\n" +
                "  process (PORT_in)\n" +
                "  begin\n" +
                "    if rising_edge(PORT_in) then\n" +
                "       if counter = maxCounter then\n" +
                "          counter <= 0;\n" +
                "          state <= NOT (state);\n" +
                "       else\n" +
                "          counter <= counter+1;\n" +
                "       end if;\n" +
                "    end if;\n" +
                "  end process;\n" +
                "  PORT_out <= state;\n" +
                "end DIG_simpleClockDivider_arch;", TestHelper.removeCommentLines(vhdl));
    }


    // If frequency is high, the clock divider is omitted.
    public void testClock3() throws PinException, NodeException, ElementNotFoundException, IOException, HDLException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/Clock3.dig");
        String vhdl = new VHDLGenerator(br.getLibrary()).export(br.getCircuit()).toString();

        assertEquals("LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "entity main is\n" +
                "  port (\n" +
                "    PORT_OV: out std_logic;\n" +
                "    PORT_C: in std_logic );\n" +
                "end main;\n" +
                "architecture main_arch of main is\n" +
                "  component NOT_GATE\n" +
                "    port (\n" +
                "      PORT_out: out std_logic;\n" +
                "      PORT_in: in std_logic );\n" +
                "  end component;\n" +
                "  signal S0: std_logic;\n" +
                "begin\n" +
                "  gate0 : NOT_GATE\n" +
                "    port map (\n" +
                "      PORT_out => S0,\n" +
                "      PORT_in => PORT_C );\n" +
                "  PORT_OV <= S0;\n" +
                "end main_arch;\n" +
                "LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "entity NOT_GATE is\n" +
                "  port (\n" +
                "    PORT_out: out std_logic;\n" +
                "    PORT_in: in std_logic );\n" +
                "end NOT_GATE;\n" +
                "architecture NOT_GATE_arch of NOT_GATE is\n" +
                "begin\n" +
                "  PORT_out <=  NOT( PORT_in );\n" +
                "end NOT_GATE_arch;", TestHelper.removeCommentLines(vhdl));
    }


}

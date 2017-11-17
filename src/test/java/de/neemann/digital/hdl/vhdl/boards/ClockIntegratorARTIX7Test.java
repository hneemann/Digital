package de.neemann.digital.hdl.vhdl.boards;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.hdl.vhdl.TestHelper;
import de.neemann.digital.hdl.vhdl.VHDLGenerator;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.io.IOException;

public class ClockIntegratorARTIX7Test extends TestCase {

    public void testArtix7() throws PinException, NodeException, ElementNotFoundException, IOException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/ArtixClock.dig");
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
                "  component DIG_MMCME2_BASE\n" +
                "      generic (\n" +
                "        D_PARAM : integer;\n" +
                "        M_PARAM : real;\n" +
                "        DIV_PARAM : real;\n" +
                "        PERIOD_PARAM: real);\n" +
                "      port (\n" +
                "        PORT_in: in std_logic;\n" +
                "        PORT_out: out std_logic );\n" +
                "  end component;\n" +
                "  signal S0: std_logic;\n" +
                "  signal S1: std_logic;\n" +
                "begin\n" +
                "  gate0 : NOT_GATE\n" +
                "    port map (\n" +
                "      PORT_out => S0,\n" +
                "      PORT_in => S1 );\n" +
                "  gate1 : DIG_MMCME2_BASE\n" +
                "    generic map (\n" +
                "      D_PARAM => 1,\n" +
                "      M_PARAM => 12.0,\n" +
                "      DIV_PARAM => 120.0,\n" +
                "      PERIOD_PARAM => 10.0 )\n" +
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
                "Library UNISIM;\n" +
                "use UNISIM.vcomponents.all;\n" +
                "entity DIG_MMCME2_BASE is\n" +
                "    generic (\n" +
                "      D_PARAM : integer;\n" +
                "      M_PARAM : real;\n" +
                "      DIV_PARAM : real;\n" +
                "      PERIOD_PARAM: real);\n" +
                "    port (\n" +
                "      PORT_in: in std_logic;\n" +
                "      PORT_out: out std_logic );\n" +
                "end DIG_MMCME2_BASE;\n" +
                "architecture DIG_MMCME2_BASE_arch of DIG_MMCME2_BASE is\n" +
                "    signal DEV_NULL: std_logic;\n" +
                "    signal feedback: std_logic;\n" +
                "begin\n" +
                "DEV_NULL <= '0';\n" +
                "MMCME2_BASE_inst : MMCME2_BASE\n" +
                "generic map (\n" +
                "BANDWIDTH => \"OPTIMIZED\", -- Jitter programming (OPTIMIZED, HIGH, LOW)\n" +
                "CLKFBOUT_MULT_F => M_PARAM,\n" +
                "DIVCLK_DIVIDE => D_PARAM,\n" +
                "CLKFBOUT_PHASE => 0.0,\n" +
                "CLKIN1_PERIOD => PERIOD_PARAM,\n" +
                "CLKOUT1_DIVIDE => 1,\n" +
                "CLKOUT2_DIVIDE => 1,\n" +
                "CLKOUT3_DIVIDE => 1,\n" +
                "CLKOUT4_DIVIDE => 1,\n" +
                "CLKOUT5_DIVIDE => 1,\n" +
                "CLKOUT6_DIVIDE => 1,\n" +
                "CLKOUT0_DIVIDE_F => DIV_PARAM,\n" +
                "CLKOUT0_DUTY_CYCLE => 0.5,\n" +
                "CLKOUT1_DUTY_CYCLE => 0.5,\n" +
                "CLKOUT2_DUTY_CYCLE => 0.5,\n" +
                "CLKOUT3_DUTY_CYCLE => 0.5,\n" +
                "CLKOUT4_DUTY_CYCLE => 0.5,\n" +
                "CLKOUT5_DUTY_CYCLE => 0.5,\n" +
                "CLKOUT6_DUTY_CYCLE => 0.5,\n" +
                "CLKOUT0_PHASE => 0.0,\n" +
                "CLKOUT1_PHASE => 0.0,\n" +
                "CLKOUT2_PHASE => 0.0,\n" +
                "CLKOUT3_PHASE => 0.0,\n" +
                "CLKOUT4_PHASE => 0.0,\n" +
                "CLKOUT5_PHASE => 0.0,\n" +
                "CLKOUT6_PHASE => 0.0,\n" +
                "CLKOUT4_CASCADE => FALSE, -- Cascade CLKOUT4 counter with CLKOUT6 (FALSE, TRUE)\n" +
                "REF_JITTER1 => 0.0,\n" +
                "STARTUP_WAIT => TRUE\n" +
                ")\n" +
                "port map (\n" +
                "CLKOUT0 => PORT_out,\n" +
                "CLKFBOUT => feedback,\n" +
                "CLKIN1 => PORT_in,\n" +
                "PWRDWN => DEV_NULL,\n" +
                "RST => DEV_NULL,\n" +
                "CLKFBIN => feedback\n" +
                ");\n" +
                "end DIG_MMCME2_BASE_arch;" , TestHelper.removeCommentLines(vhdl));
    }


    public void testArtix7_ClockCascading() throws PinException, NodeException, ElementNotFoundException, IOException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/ArtixClockCascading.dig");
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
                "  component DIG_MMCME2_BASE_CC\n" +
                "      generic (\n" +
                "        D_PARAM : integer;\n" +
                "        M_PARAM : real;\n" +
                "        DIV_PARAM : integer;\n" +
                "        DIV4_PARAM : integer;\n" +
                "        PERIOD_PARAM: real);\n" +
                "      port (\n" +
                "        PORT_in: in std_logic;\n" +
                "        PORT_out: out std_logic );\n" +
                "  end component;\n" +
                "  signal S0: std_logic;\n" +
                "  signal S1: std_logic;\n" +
                "begin\n" +
                "  gate0 : NOT_GATE\n" +
                "    port map (\n" +
                "      PORT_out => S0,\n" +
                "      PORT_in => S1 );\n" +
                "  gate1 : DIG_MMCME2_BASE_CC\n" +
                "    generic map (\n" +
                "      D_PARAM => 2,\n" +
                "      M_PARAM => 12.0,\n" +
                "      DIV_PARAM => 127,\n" +
                "      DIV4_PARAM => 128,\n" +
                "      PERIOD_PARAM => 10.0 )\n" +
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
                "Library UNISIM;\n" +
                "use UNISIM.vcomponents.all;\n" +
                "entity DIG_MMCME2_BASE_CC is\n" +
                "    generic (\n" +
                "      D_PARAM : integer;\n" +
                "      M_PARAM : real;\n" +
                "      DIV_PARAM : integer;\n" +
                "      DIV4_PARAM : integer;\n" +
                "      PERIOD_PARAM: real);\n" +
                "    port (\n" +
                "      PORT_in: in std_logic;\n" +
                "      PORT_out: out std_logic );\n" +
                "end DIG_MMCME2_BASE_CC;\n" +
                "architecture DIG_MMCME2_BASE_CC_arch of DIG_MMCME2_BASE_CC is\n" +
                "    signal DEV_NULL: std_logic;\n" +
                "    signal feedback: std_logic;\n" +
                "begin\n" +
                "DEV_NULL <= '0';\n" +
                "MMCME2_BASE_inst : MMCME2_BASE\n" +
                "generic map (\n" +
                "BANDWIDTH => \"OPTIMIZED\", -- Jitter programming (OPTIMIZED, HIGH, LOW)\n" +
                "CLKFBOUT_MULT_F => M_PARAM,\n" +
                "DIVCLK_DIVIDE => D_PARAM,\n" +
                "CLKFBOUT_PHASE => 0.0,\n" +
                "CLKIN1_PERIOD => PERIOD_PARAM,\n" +
                "CLKOUT1_DIVIDE => 1,\n" +
                "CLKOUT2_DIVIDE => 1,\n" +
                "CLKOUT3_DIVIDE => 1,\n" +
                "CLKOUT4_DIVIDE => DIV4_PARAM,\n" +
                "CLKOUT5_DIVIDE => 1,\n" +
                "CLKOUT6_DIVIDE => DIV_PARAM,\n" +
                "CLKOUT0_DIVIDE_F => 1.0,\n" +
                "CLKOUT0_DUTY_CYCLE => 0.5,\n" +
                "CLKOUT1_DUTY_CYCLE => 0.5,\n" +
                "CLKOUT2_DUTY_CYCLE => 0.5,\n" +
                "CLKOUT3_DUTY_CYCLE => 0.5,\n" +
                "CLKOUT4_DUTY_CYCLE => 0.5,\n" +
                "CLKOUT5_DUTY_CYCLE => 0.5,\n" +
                "CLKOUT6_DUTY_CYCLE => 0.5,\n" +
                "CLKOUT0_PHASE => 0.0,\n" +
                "CLKOUT1_PHASE => 0.0,\n" +
                "CLKOUT2_PHASE => 0.0,\n" +
                "CLKOUT3_PHASE => 0.0,\n" +
                "CLKOUT4_PHASE => 0.0,\n" +
                "CLKOUT5_PHASE => 0.0,\n" +
                "CLKOUT6_PHASE => 0.0,\n" +
                "CLKOUT4_CASCADE => true, -- Cascade CLKOUT4 counter with CLKOUT6 (FALSE, TRUE)\n" +
                "REF_JITTER1 => 0.0,\n" +
                "STARTUP_WAIT => TRUE\n" +
                ")\n" +
                "port map (\n" +
                "CLKOUT4 => PORT_out,\n" +
                "CLKFBOUT => feedback,\n" +
                "CLKIN1 => PORT_in,\n" +
                "PWRDWN => DEV_NULL,\n" +
                "RST => DEV_NULL,\n" +
                "CLKFBIN => feedback\n" +
                ");\n" +
                "end DIG_MMCME2_BASE_CC_arch;" , TestHelper.removeCommentLines(vhdl));
    }


}
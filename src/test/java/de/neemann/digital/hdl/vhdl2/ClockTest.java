/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl2;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.hdl.hgs.HGSEvalException;
import de.neemann.digital.hdl.model2.HDLCircuit;
import de.neemann.digital.hdl.model2.HDLException;
import de.neemann.digital.hdl.model2.HDLModel;
import de.neemann.digital.hdl.model2.clock.ClockIntegratorGeneric;
import de.neemann.digital.hdl.model2.clock.HDLClockIntegrator;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.printer.CodePrinterStr;
import de.neemann.digital.hdl.boards.ClockIntegratorARTIX7;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.io.IOException;

public class ClockTest extends TestCase {

    public void testGeneric() throws PinException, NodeException, ElementNotFoundException, IOException, HDLException, HGSEvalException {
        String code = create(new ClockIntegratorGeneric(10));

        assertEquals("\n" +
                "LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "USE ieee.std_logic_unsigned.all;\n" +
                "\n" +
                "entity DIG_simpleClockDivider is\n" +
                "  generic (\n" +
                "    maxCounter : integer );  \n" +
                "  port (\n" +
                "    cout: out std_logic;\n" +
                "    cin: in std_logic );\n" +
                "end DIG_simpleClockDivider;\n" +
                "\n" +
                "architecture Behavioral of DIG_simpleClockDivider is\n" +
                "  -- Don't use a logic signal as clock source in a real world application!\n" +
                "  -- Use the on chip clock resources instead!\n" +
                "  signal counter: integer range 0 to maxCounter := 0;\n" +
                "  signal state: std_logic;\n" +
                "begin\n" +
                "  process (cin)\n" +
                "  begin\n" +
                "    if rising_edge(cin) then\n" +
                "       if counter = maxCounter then\n" +
                "          counter <= 0;\n" +
                "          state <= NOT (state);\n" +
                "       else\n" +
                "          counter <= counter+1;\n" +
                "       end if;\n" +
                "    end if;\n" +
                "  end process;\n" +
                "  cout <= state;\n" +
                "end Behavioral;\n" +
                "\n" +
                "\n" +
                "LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "\n" +
                "entity DIG_D_FF is\n" +
                "  \n" +
                "  port ( D  : in std_logic;\n" +
                "         C  : in std_logic;\n" +
                "         Q  : out std_logic;\n" +
                "         notQ : out std_logic );\n" +
                "end DIG_D_FF;\n" +
                "\n" +
                "architecture Behavioral of DIG_D_FF is\n" +
                "   signal state : std_logic := '0';\n" +
                "begin\n" +
                "   Q    <= state;\n" +
                "   notQ <= NOT( state );\n" +
                "\n" +
                "   process(C)\n" +
                "   begin\n" +
                "      if rising_edge(C) then\n" +
                "        state  <= D;\n" +
                "      end if;\n" +
                "   end process;\n" +
                "end Behavioral;\n" +
                "\n" +
                "\n" +
                "LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "\n" +
                "entity main is\n" +
                "  port (\n" +
                "    A: in std_logic;\n" +
                "    C: in std_logic;\n" +
                "    X: out std_logic);\n" +
                "end main;\n" +
                "\n" +
                "architecture Behavioral of main is\n" +
                "  signal s0: std_logic;\n" +
                "begin\n" +
                "  gate0: entity work.DIG_simpleClockDivider\n" +
                "    generic map (\n" +
                "      maxCounter => 50)\n" +
                "    port map (\n" +
                "      cin => C,\n" +
                "      cout => s0);\n" +
                "  gate1: entity work.DIG_D_FF\n" +
                "    port map (\n" +
                "      D => A,\n" +
                "      C => s0,\n" +
                "      Q => X);\n" +
                "end Behavioral;\n", code);
    }

    public void testARTIX() throws ElementNotFoundException, NodeException, PinException, IOException, HDLException, HGSEvalException {
        String code = create(new ClockIntegratorARTIX7(10));
        assertEquals("\n" +
                "LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "\n" +
                "Library UNISIM;\n" +
                "use UNISIM.vcomponents.all;\n" +
                "\n" +
                "entity DIG_MMCME2_BASE is\n" +
                "  generic (\n" +
                "    D_PARAM : integer;\n" +
                "    M_PARAM : real;\n" +
                "\n" +
                "    DIV_PARAM : integer;\n" +
                "    DIV4_PARAM : integer;\n" +
                "\n" +
                "    PERIOD_PARAM: real);\n" +
                "  port (\n" +
                "    cin: in std_logic;\n" +
                "    cout: out std_logic );\n" +
                "end DIG_MMCME2_BASE;\n" +
                "\n" +
                "architecture DIG_MMCME2_BASE_arch of DIG_MMCME2_BASE is\n" +
                "\n" +
                "    signal DEV_NULL: std_logic;\n" +
                "    signal feedback: std_logic;\n" +
                "\n" +
                "begin\n" +
                "\n" +
                "DEV_NULL <= '0';\n" +
                "\n" +
                "-- code taken from the \"Vivado Design Suite 7 Series FPGA Libraries Guide\" (UG953)\n" +
                "\n" +
                "MMCME2_BASE_inst : MMCME2_BASE\n" +
                "generic map (\n" +
                "BANDWIDTH => \"OPTIMIZED\", -- Jitter programming (OPTIMIZED, HIGH, LOW)\n" +
                "CLKFBOUT_MULT_F => M_PARAM,\n" +
                "-- Multiply value for all CLKOUT (2.000-64.000).\n" +
                "DIVCLK_DIVIDE => D_PARAM,\n" +
                "-- Master division value (1-106)\n" +
                "CLKFBOUT_PHASE => 0.0,\n" +
                "-- Phase offset in degrees of CLKFB (-360.000-360.000).\n" +
                "CLKIN1_PERIOD => PERIOD_PARAM,\n" +
                "-- Input clock period in ns to ps resolution (i.e. 33.333 is 30 MHz).\n" +
                "-- CLKOUT0_DIVIDE - CLKOUT6_DIVIDE: Divide amount for each CLKOUT (1-128)\n" +
                "CLKOUT1_DIVIDE => 1,\n" +
                "CLKOUT2_DIVIDE => 1,\n" +
                "CLKOUT3_DIVIDE => 1,\n" +
                "\n" +
                "CLKOUT4_DIVIDE => DIV4_PARAM,\n" +
                "CLKOUT5_DIVIDE => 1,\n" +
                "CLKOUT6_DIVIDE => DIV_PARAM,\n" +
                "CLKOUT0_DIVIDE_F => 1.0,\n" +
                "\n" +
                "-- Divide amount for CLKOUT0 (1.000-128.000).\n" +
                "-- CLKOUT0_DUTY_CYCLE - CLKOUT6_DUTY_CYCLE: Duty cycle for each CLKOUT (0.01-0.99).\n" +
                "CLKOUT0_DUTY_CYCLE => 0.5,\n" +
                "CLKOUT1_DUTY_CYCLE => 0.5,\n" +
                "CLKOUT2_DUTY_CYCLE => 0.5,\n" +
                "CLKOUT3_DUTY_CYCLE => 0.5,\n" +
                "CLKOUT4_DUTY_CYCLE => 0.5,\n" +
                "CLKOUT5_DUTY_CYCLE => 0.5,\n" +
                "CLKOUT6_DUTY_CYCLE => 0.5,\n" +
                "-- CLKOUT0_PHASE - CLKOUT6_PHASE: Phase offset for each CLKOUT (-360.000-360.000).\n" +
                "CLKOUT0_PHASE => 0.0,\n" +
                "CLKOUT1_PHASE => 0.0,\n" +
                "CLKOUT2_PHASE => 0.0,\n" +
                "CLKOUT3_PHASE => 0.0,\n" +
                "CLKOUT4_PHASE => 0.0,\n" +
                "CLKOUT5_PHASE => 0.0,\n" +
                "CLKOUT6_PHASE => 0.0,\n" +
                "\n" +
                "CLKOUT4_CASCADE => true, -- Cascade CLKOUT4 counter with CLKOUT6 (FALSE, TRUE)\n" +
                "\n" +
                "REF_JITTER1 => 0.0,\n" +
                "-- Reference input jitter in UI (0.000-0.999).\n" +
                "STARTUP_WAIT => TRUE\n" +
                "-- Delays DONE until MMCM is locked (FALSE, TRUE)\n" +
                ")\n" +
                "port map (\n" +
                "-- Clock Outputs: 1-bit (each) output: User configurable clock outputs\n" +
                "\n" +
                "CLKOUT4 => cout,\n" +
                "\n" +
                "-- 1-bit output: CLKOUT6\n" +
                "-- Feedback Clocks: 1-bit (each) output: Clock feedback ports\n" +
                "CLKFBOUT => feedback,\n" +
                "-- 1-bit output: Feedback clock\n" +
                "--CLKFBOUTB => CLKFBOUTB, -- 1-bit output: Inverted CLKFBOUT\n" +
                "-- Status Ports: 1-bit (each) output: MMCM status ports\n" +
                "--LOCKED => LOCKED,\n" +
                "-- 1-bit output: LOCK\n" +
                "-- Clock Inputs: 1-bit (each) input: Clock input\n" +
                "CLKIN1 => cin,\n" +
                "-- 1-bit input: Clock\n" +
                "-- Control Ports: 1-bit (each) input: MMCM control ports\n" +
                "PWRDWN => DEV_NULL,\n" +
                "-- 1-bit input: Power-down\n" +
                "RST => DEV_NULL,\n" +
                "-- 1-bit input: Reset\n" +
                "-- Feedback Clocks: 1-bit (each) input: Clock feedback ports\n" +
                "CLKFBIN => feedback\n" +
                "-- 1-bit input: Feedback clock\n" +
                ");\n" +
                "\n" +
                "end DIG_MMCME2_BASE_arch;\n" +
                "\n" +
                "\n" +
                "LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "\n" +
                "entity DIG_D_FF is\n" +
                "  \n" +
                "  port ( D  : in std_logic;\n" +
                "         C  : in std_logic;\n" +
                "         Q  : out std_logic;\n" +
                "         notQ : out std_logic );\n" +
                "end DIG_D_FF;\n" +
                "\n" +
                "architecture Behavioral of DIG_D_FF is\n" +
                "   signal state : std_logic := '0';\n" +
                "begin\n" +
                "   Q    <= state;\n" +
                "   notQ <= NOT( state );\n" +
                "\n" +
                "   process(C)\n" +
                "   begin\n" +
                "      if rising_edge(C) then\n" +
                "        state  <= D;\n" +
                "      end if;\n" +
                "   end process;\n" +
                "end Behavioral;\n" +
                "\n" +
                "\n" +
                "LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.numeric_std.all;\n" +
                "\n" +
                "entity main is\n" +
                "  port (\n" +
                "    A: in std_logic;\n" +
                "    C: in std_logic;\n" +
                "    X: out std_logic);\n" +
                "end main;\n" +
                "\n" +
                "architecture Behavioral of main is\n" +
                "  signal s0: std_logic;\n" +
                "begin\n" +
                "  gate0: entity work.DIG_MMCME2_BASE\n" +
                "    generic map (\n" +
                "      D_PARAM => 1,\n" +
                "      M_PARAM => 12.0,\n" +
                "      DIV_PARAM => 10,\n" +
                "      DIV4_PARAM => 120,\n" +
                "      PERIOD_PARAM => 10.0)\n" +
                "    port map (\n" +
                "      cin => C,\n" +
                "      cout => s0);\n" +
                "  gate1: entity work.DIG_D_FF\n" +
                "    port map (\n" +
                "      D => A,\n" +
                "      C => s0,\n" +
                "      Q => X);\n" +
                "end Behavioral;\n", code);
    }

    String create(HDLClockIntegrator ci) throws IOException, PinException, NodeException, ElementNotFoundException, HDLException, HGSEvalException {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/model2/clock.dig");
        HDLCircuit c = new HDLCircuit(
                br.getCircuit(), "main",
                new HDLModel(br.getLibrary()),
                ci);

        c.applyDefaultOptimizations();

        CodePrinter out = new CodePrinterStr();
        new VHDLCreator(out).printHDLCircuit(c);

        return out.toString();
    }

}

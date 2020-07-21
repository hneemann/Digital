/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl2;

import de.neemann.digital.hdl.model2.HDLCircuit;
import de.neemann.digital.hdl.model2.HDLModel;
import de.neemann.digital.hdl.model2.clock.ClockIntegratorGeneric;
import de.neemann.digital.hdl.model2.clock.HDLClockIntegrator;
import de.neemann.digital.hdl.printer.CodePrinter;
import de.neemann.digital.hdl.printer.CodePrinterStr;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

public class ClockTest extends TestCase {

    public void testGeneric() throws Exception {
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
                "  generic (\n" +
                "    Default: std_logic ); \n" +
                "  port ( D  : in std_logic;\n" +
                "         C  : in std_logic;\n" +
                "         Q  : out std_logic;\n" +
                "         notQ : out std_logic );\n" +
                "end DIG_D_FF;\n" +
                "\n" +
                "architecture Behavioral of DIG_D_FF is\n" +
                "   signal state : std_logic := Default;\n" +
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
                "    generic map (\n" +
                "      Default => '0')\n" +
                "    port map (\n" +
                "      D => A,\n" +
                "      C => s0,\n" +
                "      Q => X);\n" +
                "end Behavioral;\n", code);
    }

    String create(HDLClockIntegrator ci) throws Exception {
        ToBreakRunner br = new ToBreakRunner("dig/hdl/model2/clock.dig");
        HDLCircuit c = new HDLCircuit(
                br.getCircuit(), "main",
                new HDLModel(br.getLibrary()),
                0, ci);

        c.applyDefaultOptimizations();

        CodePrinter out = new CodePrinterStr();
        new VHDLCreator(out, br.getLibrary()).printHDLCircuit(c);

        return out.toString();
    }

}

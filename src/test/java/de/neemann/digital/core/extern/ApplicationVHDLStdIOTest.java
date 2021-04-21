/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.extern.handler.ProcessInterface;
import de.neemann.digital.hdl.hgs.HGSEvalException;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

public class ApplicationVHDLStdIOTest extends TestCase {

    private static class TestApp extends ApplicationVHDLStdIO {

        @Override
        public ProcessInterface start(String label, String code, PortDefinition inputs, PortDefinition outputs, File root) {
            return null;
        }

        @Override
        public boolean checkSupported() {
            return false;
        }

        @Override
        public String checkCode(String label, String code, PortDefinition inputs, PortDefinition outputs, File root) {
            return null;
        }
    }

    public void testTemplate() throws HGSEvalException {
        String code = new TestApp()
                .createVHDL("add",
                        "code",
                        new PortDefinition("a:4,b:4,c_i"),
                        new PortDefinition("s:4,c_o"), null);

        assertEquals("code\n\nlibrary IEEE;\n" +
                "use IEEE.std_logic_1164.all;\n" +
                "use std.textio.all;\n" +
                "\n" +
                "entity stdIOInterface is end;\n" +
                "\n" +
                "architecture stdIOInterface_a of stdIOInterface is\n" +
                "\n" +
                "  function chr(sl: std_logic) return character is\n" +
                "    variable c: character;\n" +
                "  begin\n" +
                "      case sl is\n" +
                "         when 'U' => c:= 'U';\n" +
                "         when 'X' => c:= 'X';\n" +
                "         when '0' => c:= '0';\n" +
                "         when '1' => c:= '1';\n" +
                "         when 'Z' => c:= 'Z';\n" +
                "         when 'W' => c:= 'W';\n" +
                "         when 'L' => c:= 'L';\n" +
                "         when 'H' => c:= 'H';\n" +
                "         when '-' => c:= '-';\n" +
                "      end case;\n" +
                "    return c;\n" +
                "  end chr;\n" +
                "\n" +
                "  function str(slv: std_logic_vector) return string is\n" +
                "     variable result : string (1 to slv'length);\n" +
                "     variable r : integer;\n" +
                "  begin\n" +
                "     r := slv'length;\n" +
                "     for i in slv'range loop\n" +
                "        result(r) := chr(slv(i));\n" +
                "        r := r - 1;\n" +
                "     end loop;\n" +
                "     return result;\n" +
                "  end str;\n" +
                "\n" +
                "  component add\n" +
                "    port (\n" +
                "      a : in std_logic_vector(3 downto 0);\n" +
                "      b : in std_logic_vector(3 downto 0);\n" +
                "      c_i : in std_logic;\n" +
                "      s : out std_logic_vector(3 downto 0);\n" +
                "      c_o : out std_logic );\n" +
                "  end component;\n" +
                "\n" +
                "  signal in_a : std_logic_vector(3 downto 0);\n" +
                "  signal in_b : std_logic_vector(3 downto 0);\n" +
                "  signal in_c_i : std_logic;\n" +
                "  signal out_s : std_logic_vector(3 downto 0);\n" +
                "  signal out_c_o : std_logic;\n" +
                "\n" +
                "  signal mainIn : std_logic_vector(8 downto 0);\n" +
                "  signal mainOut : std_logic_vector(4 downto 0);\n" +
                "\n" +
                "begin\n" +
                "  UserCode: add port map (\n" +
                "    a => in_a,\n" +
                "    b => in_b,\n" +
                "    c_i => in_c_i,\n" +
                "    s => out_s,\n" +
                "    c_o => out_c_o\n" +
                "  );\n" +
                "\n" +
                "  in_a <= mainIn(3 downto 0);\n" +
                "  in_b <= mainIn(7 downto 4);\n" +
                "  in_c_i <= mainIn(8);\n" +
                "  mainOut(3 downto 0) <= out_s;\n" +
                "  mainOut(4) <= out_c_o;\n" +
                "\n" +
                "\n" +
                "\tprocess\n" +
                "\t\tvariable ll: line;\n" +
                "\n" +
                "\tbegin\n" +
                "\t\twrite(ll, string'(\"\"));\n" +
                "\t\twhile (ll.all /= \"End\") loop\n" +
                "\t\t\tdeallocate(ll);\n" +
                "\t\t\treadline(input, ll);\n" +
                "\t\t\tfor i in ll'range loop\n" +
                "\t\t\t\tcase ll(i) is\n" +
                "\t\t\t\t\twhen '0' => mainIn(i-1) <= '0';\n" +
                "\t\t\t\t\twhen '1' => mainIn(i-1) <= '1';\n" +
                "\t\t\t\t\twhen 'Z' => mainIn(i-1) <= 'Z';\n" +
                "\t\t\t\t\twhen ' ' =>  next;\n" +
                "\t\t\t\t\twhen others => next;\n" +
                "\t\t\t\tend case;\n" +
                "\t\t\tend loop;\n" +
                "\t\t\twait for 1 ns;\n" +
                "\t\t\tdeallocate(ll);\n" +
                "\t\t\twrite(ll, \"Digital:\" & string'(str(mainOut)));\n" +
                "\t\t\twriteline(output, ll);\n" +
                "\t\tend loop;\n" +
                "\t\twait;\n" +
                "\tend process;\n" +
                "end;", code);
    }


    public void testExtraction() {
        ElementAttributes attr = extractParameters("LIBRARY ieee;\n" +
                "USE ieee.std_logic_1164.all;\n" +
                "USE ieee.std_logic_unsigned.all;\n" +
                "\n" +
                "entity add is\n" +
                "  port (\n" +
                "    a: in std_logic_vector(3 downto 0);\n" +
                "    b: in std_logic_vector(3 downto 0);\n" +
                "    c_i: in std_logic;\n" +
                "    s: out std_logic_vector(3 downto 0);\n" +
                "    c_o: out std_logic );\n" +
                "end add;\n" +
                "\n" +
                "architecture add_arch of add is\n" +
                "begin\n" +
                "end add_arch;", true);

        assertEquals("add", attr.getLabel());
        assertEquals("a:4,b:4,c_i", attr.get(Keys.EXTERNAL_INPUTS));
        assertEquals("s:4,c_o", attr.get(Keys.EXTERNAL_OUTPUTS));
    }

    public void testExtractionUpper() {
        ElementAttributes attr = extractParameters("LIBRARY Ieee;\n" +
                "USE Ieee.std_logic_1164.all;\n" +
                "USE Ieee.std_logic_unsigned.all;\n" +
                "\n" +
                "Entity Add Is\n" +
                "  Port (\n" +
                "    A: In Std_logic_vector(3 Downto 0);\n" +
                "    B: In Std_logic_vector(3 Downto 0);\n" +
                "    C_i: In Std_logic;\n" +
                "    S: Out Std_logic_vector(3 Downto 0);\n" +
                "    C_o: Out Std_logic );\n" +
                "End Add;\n" +
                "\n" +
                "architecture Add_arch Of Add Is\n" +
                "begin\n" +
                "end Add_arch;", true);

        assertEquals("Add", attr.getLabel());
        assertEquals("A:4,B:4,C_i", attr.get(Keys.EXTERNAL_INPUTS));
        assertEquals("S:4,C_o", attr.get(Keys.EXTERNAL_OUTPUTS));
    }

    public void testExtractionCompact() {
        ElementAttributes attr = extractParameters("library IEEE;\n" +
                "use IEEE.std_logic_1164.all;\n" +
                "use IEEE.numeric_std.all;\n" +
                "\n" +
                "\n" +
                "entity nBitZaehler is\n" +
                "\tport (LoadIn : in std_logic_vector (7 downto 0); load,reset,clk : in std_logic; CountOut : out std_logic_vector (7 downto 0));\n" +
                "end nBitZaehler;\n" +
                "\n" +
                "architecture nBitZaehlerRTL of nBitZaehler is\n" +
                "\tsignal ALUOut : unsigned(7 downto 0);  -- internal\n" +
                "\tsignal ALUIn : unsigned(7 downto 0);  -- internal\n" +
                "begin\n" +
                "end nBitZaehlerRTL;", true);

        assertEquals("nBitZaehler", attr.getLabel());
        assertEquals("LoadIn:8,load,reset,clk", attr.get(Keys.EXTERNAL_INPUTS));
        assertEquals("CountOut:8", attr.get(Keys.EXTERNAL_OUTPUTS));
    }

    public void testExtractionComment() {
        ElementAttributes attr = extractParameters("-- comment at start\n" +
                "library IEEE;\n" +
                "use IEEE.std_logic_1164.all;\n" +
                "use IEEE.numeric_std.all;\n" +
                "\n" +
                "\n" +
                "entity nBitZaehler is -- commnet\n" +
                "\tport (LoadIn : in std_logic_vector (7 downto 0);--comment \n" +
                "\tload,reset,clk : in std_logic; CountOut : out std_logic_vector (7 downto 0));--comment\n" +
                "end nBitZaehler;\n" +
                "\n" +
                "architecture nBitZaehlerRTL of nBitZaehler is\n" +
                "begin\n" +
                "end nBitZaehlerRTL;", true);

        assertEquals("nBitZaehler", attr.getLabel());
        assertEquals("LoadIn:8,load,reset,clk", attr.get(Keys.EXTERNAL_INPUTS));
        assertEquals("CountOut:8", attr.get(Keys.EXTERNAL_OUTPUTS));
    }


    public void testExtractionFail() {
        extractParameters("library IEEE;\n" +
                "use IEEE.std_logic_1164.all;\n" +
                "use IEEE.numeric_std.all;\n" +
                "\n" +
                "\n" +
                "entity nBitZaehler is\n" +
                "\tgeneric(size : natural := 32);\n" +
                "\tport (LoadIn : in std_logic_vector ((size-1) downto 0); load,reset,clk : in std_logic; CountOut : out std_logic_vector (size-1 downto 0));\n" +
                "end nBitZaehler;\n", false);
    }

    public ElementAttributes extractParameters(String code, boolean workExpected) {
        TestApp ta = new TestApp();
        ElementAttributes attr = new ElementAttributes();
        attr.set(Keys.EXTERNAL_CODE, code);
        assertEquals(workExpected, ta.ensureConsistency(attr, null));
        return attr;
    }
}
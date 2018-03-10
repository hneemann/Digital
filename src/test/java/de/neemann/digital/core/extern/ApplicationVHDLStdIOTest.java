/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.extern.handler.ProcessInterface;
import junit.framework.TestCase;

import java.io.IOException;

public class ApplicationVHDLStdIOTest extends TestCase {

    private class TestApp extends ApplicationVHDLStdIO {

        @Override
        public ProcessInterface start(String label, String code, PortDefinition inputs, PortDefinition outputs) throws IOException {
            return null;
        }

        @Override
        public boolean checkSupported() {
            return false;
        }

        @Override
        public String checkCode(String label, String code, PortDefinition inputs, PortDefinition outputs) throws IOException {
            return null;
        }
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

        assertEquals("add", attr.getCleanLabel());
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

        assertEquals("Add", attr.getCleanLabel());
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

        assertEquals("nBitZaehler", attr.getCleanLabel());
        assertEquals("LoadIn:8,load,reset,clk", attr.get(Keys.EXTERNAL_INPUTS));
        assertEquals("CountOut:8", attr.get(Keys.EXTERNAL_OUTPUTS));
    }

    public void testExtractionComment() {
        ElementAttributes attr = extractParameters("-- comment at start\n"+
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

        assertEquals("nBitZaehler", attr.getCleanLabel());
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
        assertEquals(workExpected, ta.ensureConsistency(attr));
        return attr;
    }
}
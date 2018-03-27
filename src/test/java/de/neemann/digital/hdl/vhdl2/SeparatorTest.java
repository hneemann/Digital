/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl2;

import de.neemann.digital.hdl.printer.CodePrinterStr;
import junit.framework.TestCase;

import java.io.IOException;

public class SeparatorTest extends TestCase {

    public void testSeparator() throws IOException {
        CodePrinterStr out = new CodePrinterStr();

        out.println("open (").inc();
        Separator sep = new Separator(out, ",\n");
        for (int i = 0; i < 4; i++) {
            sep.check();
            out.print("item").print(i);
        }
        out.println(")").dec();
        out.print("close");

        assertEquals("open (\n" +
                "  item0,\n" +
                "  item1,\n" +
                "  item2,\n" +
                "  item3)\n" +
                "close", out.toString());
    }

    public void testSeparator2() throws IOException {
        CodePrinterStr out = new CodePrinterStr();

        out.println("open (").inc();
        Separator sep = new Separator(out, ",\n");
        for (int i = 0; i < 4; i++) {
            sep.check();
            out.print("item").print(i);
            String ic = "item" + i + " comment";
            sep.setLineFinalizer(o -> o.printComment(" \\\\ ", ic));
        }
        sep.close();
        out.println(")").dec();
        out.print("close");

        assertEquals("open (\n" +
                "  item0, \\\\ item0 comment\n" +
                "  item1, \\\\ item1 comment\n" +
                "  item2, \\\\ item2 comment\n" +
                "  item3 \\\\ item3 comment\n" +
                "  )\n" +
                "close", out.toString());
    }

    public void testSeparator3() throws IOException {
        CodePrinterStr out = new CodePrinterStr();

        out.println("open (").inc();
        Separator sep = new Separator(out, ",");
        for (int i = 0; i < 4; i++) {
            sep.check();
            out.print("item").print(i);
            if ((i&1)==0) {
                String ic = "item" + i + " comment";
                sep.setLineFinalizer(o -> o.printComment(" \\\\ ", ic));
            }
        }
        sep.close();
        out.println(")").dec();
        out.print("close");

        assertEquals("open (\n" +
                "  item0, \\\\ item0 comment\n" +
                "  item1,item2, \\\\ item2 comment\n" +
                "  item3)\n" +
                "close", out.toString());
    }

}
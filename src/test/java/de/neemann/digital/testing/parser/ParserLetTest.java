/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser;

import de.neemann.digital.data.Value;
import junit.framework.TestCase;

import java.io.IOException;

public class ParserLetTest extends TestCase {

    public void testLoop() throws IOException, ParserException {
        Parser parser = new Parser("A B\nloop(n,10)\n let a=n*2; let b = a * 2;\nC (b)\nend loop").parse();
        LineCollector td = new LineCollector(parser);

        assertEquals(2, td.getNames().size());
        assertEquals(10, td.getLines().size());

        for (int i = 0; i < 10; i++) {
            assertEquals(Value.Type.CLOCK, td.getLines().get(i).getValue(0).getType());
            assertEquals(i * 4, td.getLines().get(i).getValue(1).getValue());
        }
    }

    public void testLoopOverwrite() throws IOException, ParserException {
        Parser parser = new Parser("A B\nloop(n,10)\n let n=n*2;\nC (n)\nend loop").parse();
        LineCollector td = new LineCollector(parser);

        assertEquals(2, td.getNames().size());
        assertEquals(10, td.getLines().size());

        for (int i = 0; i < 10; i++) {
            assertEquals(Value.Type.CLOCK, td.getLines().get(i).getValue(0).getType());
            assertEquals(i * 2, td.getLines().get(i).getValue(1).getValue());
        }
    }

    public void testLoopNested() throws IOException, ParserException {
        Parser parser = new Parser(
                "A B\n"
                        + "loop(n,3)\n"
                        + "  let a=n*2;\n"
                        + "  loop(m,3)\n"
                        + "    let b=m*3+a;\n"
                        + "    (a) (b)\n"
                        + "  end loop\n"
                        + "end loop").parse();
        LineCollector td = new LineCollector(parser);

        assertEquals(2, td.getNames().size());
        assertEquals(9, td.getLines().size());

        for (int n = 0; n < 3; n++) {
            for (int m = 0; m < 3; m++) {
                int row = n * 3 + m;
                assertEquals(n * 2, td.getLines().get(row).getValue(0).getValue());
                assertEquals(m * 3 + n * 2, td.getLines().get(row).getValue(1).getValue());
            }
        }
    }


}

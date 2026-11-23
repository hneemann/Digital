/*
 * Copyright (c) 2025 Alessandro Pellegrini.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.cli;

import de.neemann.digital.cli.cli.CLIException;
import de.neemann.digital.integration.Resources;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

public class LengthExportTest extends TestCase {

    public void testLength() throws CLIException {
        File source = new File(Resources.getRoot(), "../../main/dig/combinatorial/FullAdder.dig");
        PrintStream old = System.out;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (PrintStream out = new PrintStream(baos)) {
                System.setOut(out);
                new LengthExport().execute(new String[]{source.getPath()});
            }
            String outStr = baos.toString();
            assertEquals("3", outStr.replaceAll("\\R", ""));
        } finally {
            System.setOut(old);
        }
    }

}
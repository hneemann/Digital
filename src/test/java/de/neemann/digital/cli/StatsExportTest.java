/*
 * Copyright (c) 2020 Helmut Neemann.
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

public class StatsExportTest extends TestCase {

    public void testStats() throws CLIException {
        File source = new File(Resources.getRoot(), "../../main/dig/sequential/Counter-T.dig");
        PrintStream old = System.out;
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try (PrintStream out = new PrintStream(baos)) {
                System.setOut(out);
                new StatsExport().execute(new String[]{source.getPath()});
            }
            String outStr = baos.toString();
            assertTrue(outStr.contains("FlipflopT,,,,4"));
        } finally {
            System.setOut(old);
        }
    }

}
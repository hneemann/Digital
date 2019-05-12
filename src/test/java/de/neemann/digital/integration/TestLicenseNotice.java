/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.integration;

import de.neemann.digital.lang.TestLang;
import junit.framework.TestCase;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class TestLicenseNotice extends TestCase {

    private int checkedFiles;

    /**
     * enforces that all java files contain a proper copyright notice
     */
    public void testUsages() throws IOException {
        File sources = TestLang.getSourceFiles().getParentFile().getParentFile();
        parseTree(sources);
        assertTrue(checkedFiles > 700);
    }

    private void parseTree(File file) throws IOException {
        File[] files = file.listFiles();
        if (files != null)
            for (File f : files) {
                if (f.isDirectory() && f.getName().charAt(0) != '.')
                    parseTree(f);
                if (f.isFile() && f.getName().endsWith(".java")) {
                    checkSourceFile(f);
                }
            }
    }

    private void checkSourceFile(File f) throws IOException {
        int state = 0;

        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null && state < 2) {
                switch (state) {
                    case 0:
                        if (line.trim().equals("* Use of this source code is governed by the GPL v3 license"))
                            state = 1;
                        break;
                    case 1:
                        if (line.trim().equals("* that can be found in the LICENSE file."))
                            state = 2;
                        else
                            state = 0;
                        break;
                }
            }
        }

        // Do not add files without permission from the project owner!
        if (f.getName().equals("GifSequenceWriter.java")      // Creative Commons Attribution 3.0 Unported License
                || f.getName().equals("TextLineNumber.java")) // Seems to be public domain
            return;

        if (state != 2)
            fail("found java file without proper license notice: " + f + "\n\n"
                    + "Every java file must contain the lines\n\n"
                    + " * Use of this source code is governed by the GPL v3 license\n"
                    + " * that can be found in the LICENSE file.\n\n"
                    + "Add these lines only if you have the right to do that!");

        checkedFiles++;
    }

}

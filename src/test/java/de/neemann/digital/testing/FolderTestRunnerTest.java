/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing;

import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.integration.Resources;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

public class FolderTestRunnerTest extends TestCase {

    private static final int[] ROWS = new int[]{8, 512, 4, 4, 512};

    public void testFolderTest() throws InterruptedException, IOException {
        File f = new File(Resources.getRoot(), "dig/test/arith");
        FolderTestRunner ft = new FolderTestRunner(f);
        assertEquals(5, ft.getFiles().size());


        ElementLibrary library = new ElementLibrary();
        library.setRootFilePath(f.getParentFile());
        ShapeFactory shapeFactory = new ShapeFactory(library);
        ft.startTests(
                (fileToTest, row) -> {
                    assertEquals("row " + row, ROWS[row], fileToTest.getRowCount());
                    assertEquals(FolderTestRunner.FileToTest.Status.passed, fileToTest.getStatus());
                },
                shapeFactory,
                library);

        ft.waitUntilFinished();
    }
}
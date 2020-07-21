/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.library;

import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.integration.Resources;
import de.neemann.digital.integration.ToBreakRunner;
import de.neemann.digital.testing.TestCaseDescription;
import de.neemann.digital.testing.TestCaseElement;
import de.neemann.digital.testing.TestExecutor;
import junit.framework.TestCase;

import java.io.File;

public class JarComponentManagerTest extends TestCase {

    public void testMissingJar() throws Exception {
        try {
            new ToBreakRunner("dig/jarLib/jarLibTest.dig");
            fail();
        } catch (ElementNotFoundException e) {
            assertTrue(true);
        }
    }

    public void testJarAvailable() throws Exception {
        ToBreakRunner br = new ToBreakRunner("dig/jarLib/jarLibTest.dig") {
            @Override
            public void initLibrary(ElementLibrary library) {
                library.addExternalJarComponents(new File(Resources.getRoot(), "dig/jarLib/pluginExample-1.0-SNAPSHOT.jar"));
                assertNull(library.checkForException());
            }
        };

        for (VisualElement ve : br.getCircuit().getElements()) {
            if (ve.equalsDescription(TestCaseElement.TESTCASEDESCRIPTION)) {
                TestCaseDescription td = ve.getElementAttributes().get(TestCaseElement.TESTDATA);
                TestExecutor tr = new TestExecutor(td).create(br.getModel());
                assertTrue(tr.allPassed());
            }
        }
    }

}

/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.library;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.integration.Resources;
import de.neemann.digital.integration.ToBreakRunner;
import de.neemann.digital.testing.TestCaseDescription;
import de.neemann.digital.testing.TestCaseElement;
import de.neemann.digital.testing.TestExecutor;
import de.neemann.digital.testing.TestingDataException;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

public class JarComponentManagerTest extends TestCase {

    public void testMissingJar() throws PinException, NodeException, IOException {
        try {
            new ToBreakRunner("dig/jarLib/jarLibTest.dig");
            fail();
        } catch (ElementNotFoundException e) {
            assertTrue(true);
        }
    }

    public void testJarAvailable() throws PinException, NodeException, IOException, ElementNotFoundException, TestingDataException {
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

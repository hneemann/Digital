/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.library;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.integration.Resources;
import de.neemann.digital.integration.ToBreakRunner;
import de.neemann.digital.testing.TestExecutor;
import junit.framework.TestCase;

import java.io.File;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Keys;

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
                library.addExternalJarComponents(
                        new File(Resources.getRoot(), "dig/jarLib/pluginExample-1.0-SNAPSHOT.jar"));
                assertNull(library.checkForException());
            }
        };

        for (Circuit.TestCase tc : br.getCircuit().getTestCases())
            assertTrue(new TestExecutor(tc, br.getCircuit(), br.getLibrary()).execute().allPassed());
    }

    public void testJarDebug() throws Exception {
        ElementLibrary library = new ElementLibrary();
        library.registerComponentSource(new CustomTestComponentSource());
        LibraryNode component = library.getElementNodeOrNull("CustomTestComponent");
        assertTrue(component instanceof LibraryNode);
        assertEquals(component.getName(), "CustomTestComponent");
        
        
    }

    private static class CustomTestComponent implements Element {

        /**
         * Custom test component description
         */
        public static final ElementTypeDescription DESCRIPTION = new ElementTypeDescription("CustomTestComponent",
                CustomTestComponent.class)
                .addAttribute(Keys.LABEL);

        /**
         * Creates a new custom test component
         *
         * @param attr the attributes
         */
        @SuppressWarnings("unused")
        public CustomTestComponent(ElementAttributes attr) {
        }

        @Override
        public void setInputs(ObservableValues inputs) throws NodeException {
        }

        @Override
        public ObservableValues getOutputs() {
            return ObservableValues.EMPTY_LIST;
        }

        @Override
        public void registerNodes(Model model) {
        }
    }

    private static class CustomTestComponentSource implements ComponentSource {

        /**
         * Registers custom test component to component library
         */
        @Override
        public void registerComponents(ComponentManager manager) throws InvalidNodeException {

            manager.addComponent("CustomTestComponentsLibrary", CustomTestComponent.DESCRIPTION);

        }
    }
}

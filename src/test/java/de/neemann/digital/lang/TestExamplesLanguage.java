/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.lang;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.integration.FileScanner;
import de.neemann.digital.integration.Resources;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.io.File;

public class TestExamplesLanguage extends TestCase {

    public void testDistExamples() throws Exception {
        File examples = new File(Resources.getRoot().getParentFile().getParentFile(), "/main/dig");
        new FileScanner(TestExamplesLanguage::check).scan(examples);
    }

    private static void check(File file) throws Exception {
        if (file.getPath().contains("74xx")
                || file.getPath().contains("EPROMs")
                || file.getPath().contains("RAMs"))
            return;

        Circuit circuit = new ToBreakRunner(file).getCircuit();
        check(circuit.getAttributes());
        for (VisualElement ve : circuit.getElements())
            check(ve.getElementAttributes());
    }

    private static void check(ElementAttributes attributes) {
        String descr = attributes.get(Keys.DESCRIPTION);
        if (descr.length() > 0 && !descr.startsWith("Board:")) {
            final String lowerCase = descr.toLowerCase();
            assertTrue('"' + descr + "\" is not available in german and english", lowerCase.contains("{{de ") && lowerCase.endsWith("}}"));
        }
    }

    /*
    public void testCopy() throws PinException, NodeException, ElementNotFoundException, IOException {
        File source = new File(Resources.getRoot().getParentFile().getParentFile(), "/main/dig/cmos/d-tg-ff.dig");
        File dest = new File(Resources.getRoot().getParentFile().getParentFile(), "/main/dig/cmos/d-tg-ff2.dig");
        copy(source, dest);
    }

    private static void copy(File source, File dest) throws PinException, NodeException, ElementNotFoundException, IOException {
        Circuit sourceCircuit = new ToBreakRunner(source).getCircuit();
        Circuit destCircuit = new ToBreakRunner(dest).getCircuit();

        destCircuit.getAttributes().set(Keys.DESCRIPTION, sourceCircuit.getAttributes().get(Keys.DESCRIPTION));

        for (VisualElement ve : sourceCircuit.getElements()) {
            String sourceDescr = ve.getElementAttributes().get(Keys.DESCRIPTION);
            String label = ve.getElementAttributes().getCleanLabel();
            if (sourceDescr.length() > 0 && label.length() > 0) {

                VisualElement found = null;
                for (VisualElement vd : destCircuit.getElements())
                    if (ve.getElementName().equals(vd.getElementName()) &&
                            vd.getElementAttributes().getCleanLabel().equals(label))
                        found = vd;

                if (found!=null)
                    found.getElementAttributes().set(Keys.DESCRIPTION, sourceDescr);
            }
        }

        destCircuit.save(destCircuit.getOrigin());
    }*/

}

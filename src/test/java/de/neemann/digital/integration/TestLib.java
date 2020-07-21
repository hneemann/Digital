/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.integration;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.memory.EEPROM;
import de.neemann.digital.core.memory.ROM;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.shapes.CustomCircuitShapeType;
import junit.framework.TestCase;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Tests the files in the lib folder for consistency.
 */
public class TestLib extends TestCase {
    private HashMap<String, File> descrMap;
    private int count74xx;

    public void testLib() throws Exception {
        File examples = new File(Resources.getRoot().getParentFile().getParentFile(), "/main/dig/lib");
        descrMap = new HashMap<>();
        new FileScanner(this::check).scan(examples);
        assertTrue(count74xx >= 60);
    }

    private void check(File dig) throws Exception {
        Circuit circuit = new ToBreakRunner(dig).getCircuit();
        boolean is74xx = !dig.getName().endsWith("-inc.dig") && dig.getPath().contains("DIL Chips");

        if (is74xx) {
            assertEquals("is not DIL", CustomCircuitShapeType.DIL, circuit.getAttributes().get(Keys.SHAPE_TYPE));
            count74xx++;

            final int w = circuit.getAttributes().get(Keys.WIDTH);
            assertTrue("chip to small", w >= 5);
        }

        assertTrue("is not locked", circuit.getAttributes().get(Keys.LOCKED_MODE));

        final String descr = circuit.getAttributes().get(Keys.DESCRIPTION);
        assertTrue("missing description", descr.length() > 0);

        File f = descrMap.get(descr);
        if (f != null)
            fail("duplicate description '" + descr + "' in " + f + " and " + dig);
        descrMap.put(descr, dig);

        PinChecker pc = new PinChecker(is74xx);
        for (VisualElement e : circuit.getElements()) {
            if (e.equalsDescription(In.DESCRIPTION))
                pc.checkPin(e);
            if (e.equalsDescription(Out.DESCRIPTION))
                pc.checkPin(e);
            if (e.equalsDescription(Clock.DESCRIPTION))
                pc.checkPin(e);

            if (e.equalsDescription(ROM.DESCRIPTION) || e.equalsDescription(EEPROM.DESCRIPTION))
                assertEquals("*", e.getElementAttributes().getLabel());
        }

        if (is74xx) {
            assertTrue("GND is missing", pc.isGND);
            assertTrue("VCC is missing", pc.isVCC);
        }
    }


    private class PinChecker {
        private final HashSet<Integer> pinMap;
        private final HashSet<String> nameMap;
        private final boolean is74xx;
        private boolean isVCC = false;
        private boolean isGND = false;

        private PinChecker(boolean is74xx) {
            this.is74xx = is74xx;
            pinMap = new HashSet<>();
            nameMap = new HashSet<>();
        }

        private void checkPin(VisualElement e) {
            int pn = e.getElementAttributes().getIntPinNumber();
            final String label = e.getElementAttributes().getLabel();

            if (label.equalsIgnoreCase("VCC"))
                isVCC = true;
            if (label.equalsIgnoreCase("GND"))
                isGND = true;

            if (is74xx) {
                assertTrue("missing pin number: " + label, pn != 0);

                assertFalse("non unique pin number: " + pn, pinMap.contains(pn));
                pinMap.add(pn);
            }

            assertFalse("non unique pin label: " + label, nameMap.contains(label));
            nameMap.add(label);

            assertTrue("missing pin label", label.length() > 0);
        }

    }
}

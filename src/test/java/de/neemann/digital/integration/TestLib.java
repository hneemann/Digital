package de.neemann.digital.integration;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementNotFoundException;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Tests the files in the lib folder for consistency.
 * Created by hneemann on 13.05.17.
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

    private void check(File dig) throws PinException, NodeException, ElementNotFoundException, IOException {
        Circuit circuit = new ToBreakRunner(dig).getCircuit();
        boolean is74xx = dig.getPath().contains("74xx");

        if (is74xx) {
            assertTrue("is not DIL", circuit.getAttributes().get(Keys.IS_DIL));
            count74xx++;
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

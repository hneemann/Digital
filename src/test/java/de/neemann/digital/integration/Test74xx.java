package de.neemann.digital.integration;

import de.neemann.digital.core.NodeException;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
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
 * Tests the files in the 74xx/lib folder for consistency.
 * Created by hneemann on 13.05.17.
 */
public class Test74xx extends TestCase {
    private HashMap<String, File> descrMap;

    public void test74xx() throws Exception {
        File examples = new File(Resources.getRoot().getParentFile().getParentFile(), "/main/dig/74xx/lib");
        descrMap = new HashMap<>();
        new FileScanner(this::check).scan(examples);
    }

    private void check(File dig) throws PinException, NodeException, ElementNotFoundException, IOException {
        System.out.println(dig);
        Circuit circuit = new ToBreakRunner(dig).getCircuit();
        assertTrue("is not DIL", circuit.getAttributes().get(Keys.IS_DIL));
        assertTrue("is not locked", circuit.getAttributes().get(Keys.LOCKED_MODE));

        final String descr = circuit.getAttributes().get(Keys.DESCRIPTION);
        assertTrue("missing description", descr.length() > 0);

        File f = descrMap.get(descr);
        if (f != null)
            fail("duplicate description '"+descr+"' in " + f + " and " + dig);
        descrMap.put(descr, dig);

        PinChecker pc = new PinChecker();
        for (VisualElement e : circuit.getElements()) {
            if (e.equalsDescription(In.DESCRIPTION))
                pc.checkPin(e);
            if (e.equalsDescription(Out.DESCRIPTION))
                pc.checkPin(e);
        }
    }


    private class PinChecker {
        private final HashSet<Integer> pinMap;
        private final HashSet<String> nameMap;

        private PinChecker() {
            pinMap = new HashSet<>();
            nameMap = new HashSet<>();
        }

        private void checkPin(VisualElement e) {
            int pn = e.getElementAttributes().get(Keys.PINNUMBER);
            final String label = e.getElementAttributes().getLabel();
            assertTrue("missing pin number: " + label, pn != 0);

            assertFalse("non unique pin number: " + pn, pinMap.contains(pn));
            pinMap.add(pn);

            assertFalse("non unique pin name: " + label, nameMap.contains(label));
            nameMap.add(label);

            assertTrue("missing pin label", label.length() > 0);
        }

    }
}

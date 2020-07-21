/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.integration;

import de.neemann.digital.core.ExceptionWithOrigin;
import de.neemann.digital.core.NodeException;
import de.neemann.digital.draw.elements.PinException;
import junit.framework.TestCase;

public class TestErrorOrigin extends TestCase {

    public void testErrorMessage() throws Exception {
        try {
            new ToBreakRunner("/dig/errorOrigin/main.dig");
            fail();
        } catch (PinException e) {
            assertNotNull(e.getVisualElement());
            assertEquals("mid.dig", e.getVisualElement().getElementName());
            checkOrigin(e, "src/test/resources/dig/errorOrigin/inner.dig");
        }
    }

    private void checkOrigin(ExceptionWithOrigin e, String origin) {
        assertEquals(1, e.getOrigin().size());
        String file = e.getOrigin().iterator().next().getPath().replace('\\', '/');
        assertTrue(file, file.endsWith(origin));
    }

    public void testErrorMessage2() throws Exception {
        try {
            new ToBreakRunner("/dig/errorOrigin/main2.dig");
            fail();
        } catch (PinException e) {
            assertNotNull(e.getVisualElement());
            assertEquals("mid2.dig", e.getVisualElement().getElementName());
            checkOrigin(e, "src/test/resources/dig/errorOrigin/mid2.dig");
        }
    }

    public void testErrorMessage3() throws Exception {
        try {
            new ToBreakRunner("/dig/errorOrigin/main3.dig");
            fail();
        } catch (PinException e) {
            assertNotNull(e.getVisualElement());
            assertEquals("midOk.dig", e.getVisualElement().getElementName());
            checkOrigin(e, "src/test/resources/dig/errorOrigin/midOk.dig");
        }
    }

    public void testErrorMessage4() throws Exception {
        try {
            new ToBreakRunner("/dig/errorOrigin/main4.dig");
            fail();
        } catch (PinException e) {
            assertNotNull(e.getVisualElement());
            assertEquals("And", e.getVisualElement().getElementName());
            checkOrigin(e, "src/test/resources/dig/errorOrigin/main4.dig");
        }
    }

    public void testErrorMessage5() throws Exception {
        try {
            new ToBreakRunner("/dig/errorOrigin/main5.dig");
            fail();
        } catch (NodeException e) {
            assertNotNull(e.getVisualElement());
            assertEquals("midBit.dig", e.getVisualElement().getElementName());
            checkOrigin(e, "src/test/resources/dig/errorOrigin/innerBit.dig");
        }
    }

}

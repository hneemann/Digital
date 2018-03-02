/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.gui.language;

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Arrays;

/**
 */
public class ResourcesTest extends TestCase {
    private static final String example
            = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\n" +
            "<resources>\n" +
            "  <string name=\"menu_save\">Speichern</string>\n" +
            "  <string name=\"menu_open\">\u00D6ffnen</string>\n" +
            "</resources>";

    public void testWrite() throws Exception {
        Resources res = new Resources();
        res.put("menu_open", "\u00D6ffnen");
        res.put("menu_save", "Speichern");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        res.save(baos);
        assertTrue(Arrays.equals(example.getBytes("utf-8"), baos.toByteArray()));
    }

    public void testRead() throws Exception {
        Resources res = new Resources(new ByteArrayInputStream(example.getBytes("utf-8")));

        assertEquals("\u00D6ffnen", res.get("menu_open"));
        assertEquals("Speichern", res.get("menu_save"));
    }
}

/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl;

import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.hdl.vhdl2.entities.VHDLTemplate;
import junit.framework.TestCase;

public class TestHDLExportFlag extends TestCase {

    public void testHDLExportFlag() {
        ElementLibrary lib = new ElementLibrary();
        for (ElementLibrary.ElementContainer ec : lib) {
            ElementTypeDescription etd = ec.getDescription();
            boolean hdlExportFlag = etd.isSupportsHDL();
            if (VHDLTemplate.isTemplate(etd))
                assertTrue("HDL template available for " + etd.getName(), hdlExportFlag);
        }
    }
}

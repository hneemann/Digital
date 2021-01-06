/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl;

import de.neemann.digital.core.basic.*;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.io.*;
import de.neemann.digital.core.wiring.Clock;
import de.neemann.digital.core.wiring.Splitter;
import de.neemann.digital.draw.elements.Tunnel;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.GenericCode;
import de.neemann.digital.draw.library.GenericInitCode;
import de.neemann.digital.hdl.vhdl2.entities.VHDLTemplate;
import de.neemann.digital.testing.TestCaseElement;
import junit.framework.TestCase;

import java.util.HashSet;

public class TestHDLExportFlag extends TestCase {

    private static final HashSet<ElementTypeDescription> implicitSupported = new HashSet<>();

    static {
        implicitSupported.add(And.DESCRIPTION);
        implicitSupported.add(NAnd.DESCRIPTION);
        implicitSupported.add(Or.DESCRIPTION);
        implicitSupported.add(NOr.DESCRIPTION);
        implicitSupported.add(XOr.DESCRIPTION);
        implicitSupported.add(XNOr.DESCRIPTION);
        implicitSupported.add(Not.DESCRIPTION);

        implicitSupported.add(Out.DESCRIPTION);
        implicitSupported.add(In.DESCRIPTION);
        implicitSupported.add(Clock.DESCRIPTION);

        implicitSupported.add(Ground.DESCRIPTION);
        implicitSupported.add(VDD.DESCRIPTION);
        implicitSupported.add(Const.DESCRIPTION);

        implicitSupported.add(Tunnel.DESCRIPTION);
        implicitSupported.add(Splitter.DESCRIPTION);

        implicitSupported.add(TestCaseElement.DESCRIPTION);
        implicitSupported.add(GenericInitCode.DESCRIPTION);
        implicitSupported.add(GenericCode.DESCRIPTION);
    }

    public void testHDLExportFlag() {
        ElementLibrary lib = new ElementLibrary();
        for (ElementLibrary.ElementContainer ec : lib) {
            ElementTypeDescription etd = ec.getDescription();
            boolean hdlExportFlag = etd.isSupportsHDL();
            boolean isTemplate = VHDLTemplate.isTemplate(etd);
            if (isTemplate)
                assertTrue("HDL template available for " + etd.getName(), hdlExportFlag);
            else {
                if (hdlExportFlag && !implicitSupported.contains(etd))
                    fail(etd.getName() + " is flagged as supporting HDL");
            }
        }
    }

}

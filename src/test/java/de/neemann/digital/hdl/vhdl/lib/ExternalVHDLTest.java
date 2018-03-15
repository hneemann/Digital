/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl.lib;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import de.neemann.digital.hdl.printer.CodePrinterStr;
import de.neemann.digital.hdl.vhdl.VHDLGenerator;
import de.neemann.digital.integration.Resources;
import de.neemann.digital.lang.Lang;
import junit.framework.TestCase;

import java.io.File;
import java.io.IOException;

public class ExternalVHDLTest extends TestCase {

    public void testNotUniqueCode() {
        try {
            ElementLibrary library = new ElementLibrary();
            ShapeFactory shapeFactory = new ShapeFactory(library);
            File source = new File(Resources.getRoot(), "dig/external/exportError.dig");
            Circuit circuit = Circuit.loadCircuit(source, shapeFactory);
            try (VHDLGenerator vhdl = new VHDLGenerator(library, new CodePrinterStr())) {
                vhdl.export(circuit);
            }
            fail();
        } catch (IOException e) {
            String message = e.getCause().getMessage();
            assertEquals(Lang.get("err_ifExternalComponentIsUsedTwiceCodeMustBeIdentical"), message);
        }
    }
}
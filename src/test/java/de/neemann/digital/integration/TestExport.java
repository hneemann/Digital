/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.integration;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.graphics.Export;
import de.neemann.digital.draw.graphics.ExportFactory;
import de.neemann.digital.draw.graphics.GraphicSVG;
import de.neemann.digital.draw.graphics.GraphicsImage;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;

/**
 * Loads the processor and exports it to the different export instances
 * Only checks that something is written without an error
 */
public class TestExport extends TestCase {

    private static ByteArrayOutputStream export(String file, ExportFactory creator) throws Exception {
        Circuit circuit = new ToBreakRunner(file).getCircuit();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new Export(circuit, creator).export(baos);
        return baos;
    }

    public void testSVGExport() throws Exception {
        ByteArrayOutputStream baos
                = export("../../main/dig/processor/Processor.dig",
                (out) -> new GraphicSVG(out, null, 15));

        assertTrue(baos.size() > 20000);
    }

    public void testPNGExport() throws Exception {
        ByteArrayOutputStream baos
                = export("../../main/dig/processor/Processor.dig",
                (out) -> new GraphicsImage(out, "PNG", 1));

        assertTrue(baos.size() > 45000);
    }
}

/*
 * Copyright (c) 2019 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.integration.ToBreakRunner;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

public class SVGExportTest extends TestCase {

    private static ByteArrayOutputStream export(String file, ExportFactory creator) throws Exception {
        Circuit circuit = new ToBreakRunner(file).getCircuit();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        new Export(circuit, creator).export(baos);
        return baos;
    }

    public void testSVGExportInOut() throws Exception {
        ElementAttributes attr = new ElementAttributes()
                .set(SVGSettings.LATEX, true);

        assertFalse(SVGSettings.PINS_IN_MATH_MODE.getDefault());

        ByteArrayOutputStream baos
                = export("dig/export/labels.dig",
                (out) -> new GraphicSVG(out, attr));

        String actual = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(actual.contains(">A<"));
        assertTrue(actual.contains("$Y_n$"));
    }

    public void testSVGExportInOutMath() throws Exception {
        ElementAttributes attr = new ElementAttributes()
                .set(SVGSettings.LATEX, true)
                .set(SVGSettings.PINS_IN_MATH_MODE, true);

        ByteArrayOutputStream baos
                = export("dig/export/labels.dig",
                (out) -> new GraphicSVG(out, attr));

        String actual = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(actual.contains("$A$"));
        assertTrue(actual.contains("$Y_n$"));
    }

    public void testSVGExportShapePins() throws Exception {
        ElementAttributes attr = new ElementAttributes()
                .set(SVGSettings.LATEX, true)
                .set(SVGSettings.PINS_IN_MATH_MODE, false);
        ByteArrayOutputStream baos
                = export("dig/export/labels2.dig",
                (out) -> new GraphicSVG(out, attr));

        String actual = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(actual.contains("{\\scriptsize A}"));
        assertTrue(actual.contains("$Y_n$"));
    }

    public void testSVGExportShapePinsMath() throws Exception {
        ElementAttributes attr = new ElementAttributes()
                .set(SVGSettings.LATEX, true)
                .set(SVGSettings.PINS_IN_MATH_MODE, true);
        ByteArrayOutputStream baos
                = export("dig/export/labels2.dig",
                (out) -> new GraphicSVG(out, attr));

        String actual = new String(baos.toByteArray(), StandardCharsets.UTF_8);
        assertTrue(actual.contains("{\\scriptsize $A$}"));
        assertTrue(actual.contains("$Y_n$"));
    }


}
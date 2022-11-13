/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import de.neemann.digital.core.basic.And;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.VisualElement;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import junit.framework.TestCase;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 *
 */
public class GraphicSVGLaTeXTest extends TestCase {
    public void testFormatText() throws Exception {
        GraphicSVG.TextStyle gs = new TextFormatLaTeX(false);

        assertEquals("$Z_0$", gs.format("$Z_0$", Style.NORMAL));
        assertEquals("$Z_{in}$", gs.format("$Z_{in}$", Style.NORMAL));
        assertEquals("$Z_0$", gs.format("Z_0", Style.NORMAL));
        assertEquals("\\&amp;", gs.format("&", Style.NORMAL));
        assertEquals("$\\geq\\!\\!{}$1", gs.format("\u22651", Style.NORMAL));
        assertEquals("$\\geq\\!\\!{}1$", gs.format("$\u22651$", Style.NORMAL));
        assertEquals("$\\overline{\\mbox{Q}}$", gs.format("~Q", Style.NORMAL));
        assertEquals("$\\overline{Q}$", gs.format("$~Q$", Style.NORMAL));
        assertEquals("\\textless{}a\\textgreater{}", gs.format("<a>", Style.NORMAL));
        assertEquals("Grün", gs.format("Grün", Style.NORMAL));


        assertEquals("{\\scriptsize Grün}", gs.format("Grün", Style.SHAPE_PIN));
        assertEquals("{\\scriptsize $Z_0$}", gs.format("Z_0", Style.SHAPE_PIN));
        assertEquals("{\\tiny $Z_0$}", gs.format("Z_0", Style.SHAPE_SPLITTER));
        assertEquals("{\\tiny $Z_0$}", gs.format("Z_0", Style.WIRE_BITS));
    }

    public void testCleanLabel() throws IOException {
        check("$Z_0$", "$Z_0$");
        check("$Z_{in}$", "$Z_{in}$");
        check("Z_0", "$Z_0$");
        check("$Z_0^n$", "$Z^n_0$");
    }

    private void check(String orig, String LaTeX) throws IOException {
        GraphicSVG.TextStyle gs = new TextFormatLaTeX(false);
        assertEquals(LaTeX, gs.format(orig, Style.NORMAL));
    }

    private ByteArrayOutputStream createSVG(ElementAttributes a) throws IOException {
        Circuit c = new Circuit();
        ElementLibrary el = new ElementLibrary();
        ShapeFactory sf = new ShapeFactory(el, false);
        c.add(new VisualElement(And.DESCRIPTION.getName()).setShapeFactory(sf));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        GraphicSVG gr = new GraphicSVG(baos, a);
        gr.setBoundingBox(new Vector(-20, -20), new Vector(20, 20));
        c.drawTo(gr);
        gr.close();
        return baos;
    }

    public void testAmpersand() throws IOException {
        ByteArrayOutputStream baos = createSVG(new ElementAttributes().set(SVGSettings.LATEX, false));
        assertTrue(baos.toString().contains(">&amp;<"));

        baos = createSVG(new ElementAttributes().set(SVGSettings.LATEX, true));
        assertTrue(baos.toString().contains(">\\&amp;<"));
    }

}

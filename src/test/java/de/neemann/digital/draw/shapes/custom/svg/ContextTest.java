/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes.custom.svg;

import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Transform;
import de.neemann.digital.draw.graphics.TransformMatrix;
import de.neemann.digital.draw.graphics.TransformTranslate;
import junit.framework.TestCase;

import java.awt.*;

public class ContextTest extends TestCase {

    public void testColor() throws SvgException {
        assertEquals(new Color(0x80, 0x60, 0x40), Context.readStyle(new Context(), "stroke:#806040").getStroke());
        assertEquals(new Color(0x88, 0x66, 0x44), Context.readStyle(new Context(), "stroke:#864").getStroke());
        assertEquals(new Color(80, 60, 40), Context.readStyle(new Context(), "stroke:rgb(80,60,40)").getStroke());
        assertEquals(new Color(63, 127, 191), Context.readStyle(new Context(), "stroke:rgb(25%,50%,75%)").getStroke());
    }

    public void testInkscape1() throws SvgException {
        Context c = Context.readStyle(new Context(), "fill:#000000;fill-opacity:0.5;stroke:none");
        assertNull(c.getStroke());
        assertEquals(new Color(0, 0, 0, 127), c.getFilled());
    }

    public void testCSS() {
        Context c = new Context();
        c.addClasses("  .z{a:1}\n .y{a:2}");
        assertEquals("a:1", c.getCssClass("z"));
        assertEquals("a:2", c.getCssClass("y"));
    }

    public void testFontSize() throws SvgException {
        assertEquals(Style.NORMAL.getFontSize(), new Context().getFontSize(), 1e-4);
        assertEquals(10, Context.readStyle(new Context(), "font-size:10").getFontSize(), 1e-4);
        assertEquals(16, Context.readStyle(new Context(), "font-size:12pt").getFontSize(), 1e-4);
        assertEquals(12*16, Context.readStyle(new Context(), "font-size:12pc").getFontSize(), 1e-4);
        assertEquals(10, Context.readStyle(new Context(), "font-size:10px").getFontSize(), 1e-4);
        assertEquals(20, Context.readStyle(new Context(), "font-size:10;font-size:2em").getFontSize(), 1e-4);
        assertEquals(15, Context.readStyle(new Context(), "font-size:10;font-size:150%").getFontSize(), 1e-4);
        assertEquals(96, Context.readStyle(new Context(), "font-size:25.4mm").getFontSize(), 1e-4);
        assertEquals(96, Context.readStyle(new Context(), "font-size:2.54cm").getFontSize(), 1e-4);
        assertEquals(96, Context.readStyle(new Context(), "font-size:1in").getFontSize(), 1e-4);
    }

}
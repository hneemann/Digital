package de.neemann.digital.draw.shapes.custom.svg;

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


}
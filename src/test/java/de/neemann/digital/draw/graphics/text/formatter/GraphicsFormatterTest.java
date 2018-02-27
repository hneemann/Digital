package de.neemann.digital.draw.graphics.text.formatter;

import de.neemann.digital.draw.graphics.text.text.Simple;
import de.neemann.digital.draw.graphics.text.text.Text;
import junit.framework.TestCase;

import java.awt.*;

public class GraphicsFormatterTest extends TestCase {

    /**
     * Ensures that a simple string leads to the simplest text fragment
     */
    public void testSimple() {
        GraphicsFormatter.Fragment f = GraphicsFormatter.createFragment(
                (fragment, font, str) -> {
                    assertEquals("Q", str);
                    fragment.set(10, 10, 5);
                },
                Font.decode(null), "Q");

        assertTrue(f instanceof GraphicsFormatter.TextFragment);
    }

}
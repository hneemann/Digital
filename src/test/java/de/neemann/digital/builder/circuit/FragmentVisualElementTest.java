package de.neemann.digital.builder.circuit;

import de.neemann.digital.core.flipflops.FlipflopJK;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import junit.framework.TestCase;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 * @author hneemann
 */
public class FragmentVisualElementTest extends TestCase {

    public void testBox() throws Exception {
        ShapeFactory shapeFactory = new ShapeFactory(new ElementLibrary());
        FragmentVisualElement ve = new FragmentVisualElement(FlipflopJK.DESCRIPTION,shapeFactory);
        ve.setPos(new Vector(0,0));
        Box box = ve.doLayout();
        assertEquals(SIZE*3, box.getHeight());
        assertEquals(SIZE*3, box.getWidth());
    }
}
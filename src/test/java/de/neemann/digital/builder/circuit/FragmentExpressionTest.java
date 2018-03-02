/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.circuit;

import de.neemann.digital.core.flipflops.FlipflopJK;
import de.neemann.digital.draw.elements.Tunnel;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.shapes.ShapeFactory;
import junit.framework.TestCase;

import static de.neemann.digital.draw.shapes.GenericShape.SIZE;

/**
 */
public class FragmentExpressionTest extends TestCase {

    public void testBox() throws Exception {
        ShapeFactory shapeFactory = new ShapeFactory(new ElementLibrary());
        FragmentVisualElement ve = new FragmentVisualElement(FlipflopJK.DESCRIPTION,shapeFactory);
        FragmentExpression fe = new FragmentExpression(ve, new FragmentVisualElement(Tunnel.DESCRIPTION, shapeFactory));

        fe.setPos(new Vector(0,0));
        Box box = fe.doLayout();
        assertEquals(SIZE*3, box.getHeight());
        assertEquals(SIZE*4, box.getWidth());
    }

    public void testCalcBackOffset() throws Exception {
        assertEquals(0, FragmentExpression.calcBackOffset(1, 0));

        assertEquals(1, FragmentExpression.calcBackOffset(2, 0));
        assertEquals(1, FragmentExpression.calcBackOffset(2, 1));

        assertEquals(1, FragmentExpression.calcBackOffset(3, 0));
        assertEquals(0, FragmentExpression.calcBackOffset(3, 1));
        assertEquals(1, FragmentExpression.calcBackOffset(3, 2));

        assertEquals(1, FragmentExpression.calcBackOffset(4, 0));
        assertEquals(2, FragmentExpression.calcBackOffset(4, 1));
        assertEquals(2, FragmentExpression.calcBackOffset(4, 2));
        assertEquals(1, FragmentExpression.calcBackOffset(4, 3));

        assertEquals(1, FragmentExpression.calcBackOffset(5, 0));
        assertEquals(2, FragmentExpression.calcBackOffset(5, 1));
        assertEquals(0, FragmentExpression.calcBackOffset(5, 2));
        assertEquals(2, FragmentExpression.calcBackOffset(5, 3));
        assertEquals(1, FragmentExpression.calcBackOffset(5, 4));

        assertEquals(1, FragmentExpression.calcBackOffset(6, 0));
        assertEquals(2, FragmentExpression.calcBackOffset(6, 1));
        assertEquals(3, FragmentExpression.calcBackOffset(6, 2));
        assertEquals(3, FragmentExpression.calcBackOffset(6, 3));
        assertEquals(2, FragmentExpression.calcBackOffset(6, 4));
        assertEquals(1, FragmentExpression.calcBackOffset(6, 5));

        assertEquals(1, FragmentExpression.calcBackOffset(7, 0));
        assertEquals(2, FragmentExpression.calcBackOffset(7, 1));
        assertEquals(3, FragmentExpression.calcBackOffset(7, 2));
        assertEquals(0, FragmentExpression.calcBackOffset(7, 3));
        assertEquals(3, FragmentExpression.calcBackOffset(7, 4));
        assertEquals(2, FragmentExpression.calcBackOffset(7, 5));
        assertEquals(1, FragmentExpression.calcBackOffset(7, 6));

    }

}

/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes.custom.svg;

import de.neemann.digital.draw.graphics.Transform;
import de.neemann.digital.draw.graphics.TransformMatrix;
import de.neemann.digital.draw.graphics.TransformTranslate;
import junit.framework.TestCase;

public class TransformParserTest extends TestCase {

    private static final Transform TRANS = Transform.mul(
            new TransformTranslate(-10, -10),
            Transform.mul(
                    TransformMatrix.rotate(45),
                    new TransformTranslate(10, 10)));

    public void testTransformParser() throws SvgException {
        equals(new TransformTranslate(10, 10), new TransformParser("translate(10,10)").parse());
        equals(new TransformTranslate(10, 0), new TransformParser("translate(10)").parse());
        equals(new TransformTranslate(10, 10), new TransformParser("translate(100e-1,1e1)").parse());

        equals(TransformMatrix.rotate(45), new TransformParser("rotate(45)").parse());
        equals(TRANS, new TransformParser("rotate(45,10,10)").parse());
        equals(TRANS, new TransformParser("translate(10,10) rotate(45) translate(-10,-10)").parse());

        equals(TransformMatrix.scale(2, 2), new TransformParser("scale(2,2)").parse());
    }

    private void equals(Transform a, Transform b) {
        float[] am = a.getMatrix().getValues();
        float[] bm = b.getMatrix().getValues();
        for (int i = 0; i < 6; i++)
            assertEquals("V" + i, am[i], bm[i], 1e-4);
    }

}
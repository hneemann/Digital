/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import junit.framework.TestCase;

public class TransformMatrixTest extends TestCase {

    public void testScale() {
        TransformMatrix tr = TransformMatrix.scale(2, 3);
        VectorInterface p = new VectorFloat(3, 4).transform(tr);
        assertEquals(6, p.getXFloat(), 1e-4);
        assertEquals(12, p.getYFloat(), 1e-4);
    }

    public void testRotate() {
        TransformMatrix tr = TransformMatrix.rotate(45);
        VectorInterface p = new VectorFloat(2, 0).transform(tr);
        assertEquals(Math.sqrt(2), p.getXFloat(), 1e-4);
        assertEquals(-Math.sqrt(2), p.getYFloat(), 1e-4);
    }

    public void testRotateInverse() {
        Transform tr = new TransformRotate(new Vector(2,3),1);
        VectorInterface p = new VectorFloat(7, 8);

        VectorInterface t = p.transform(tr).transform(tr.invert());
        assertEquals(p.getXFloat(), t.getXFloat(), 1e-4);
        assertEquals(p.getYFloat(), t.getYFloat(), 1e-4);
    }

    public void testInverse() {
        TransformMatrix tr = new TransformMatrix(1, 2, 3, 4, 5, 6);
        VectorInterface p = new VectorFloat(7, 8);

        VectorInterface t = p.transform(tr).transform(tr.invert());
        assertEquals(p.getXFloat(), t.getXFloat(), 1e-4);
        assertEquals(p.getYFloat(), t.getYFloat(), 1e-4);
    }

}
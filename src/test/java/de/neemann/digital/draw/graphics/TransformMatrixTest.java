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
        assertEquals(Math.sqrt(2), p.getYFloat(), 1e-4);
    }

    public void testRotateInverse() {
        Transform tr = new TransformRotate(new Vector(2, 3), 1);
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

    public void testUniform() {
        assertTrue(TransformMatrix.rotate(30).isUniform());
        assertTrue(new TransformRotate(new Vector(2, 2), 0).getMatrix().isUniform());
        assertTrue(new TransformRotate(new Vector(2, 2), 1).getMatrix().isUniform());
        assertTrue(new TransformRotate(new Vector(2, 2), 2).getMatrix().isUniform());
        assertTrue(new TransformRotate(new Vector(2, 2), 3).getMatrix().isUniform());
        assertTrue(new TransformTranslate(4, 5).getMatrix().isUniform());
        assertTrue(TransformMatrix.scale(2, 2).isUniform());

        assertFalse(TransformMatrix.scale(2, 3).isUniform());
    }

    public void testNoRotation() {
        assertFalse(TransformMatrix.rotate(30).noRotation());
        assertTrue(new TransformRotate(new Vector(2, 2), 0).getMatrix().noRotation());
        assertFalse(new TransformRotate(new Vector(2, 2), 1).getMatrix().noRotation());
        assertTrue(new TransformRotate(new Vector(2, 2), 2).getMatrix().noRotation());
        assertFalse(new TransformRotate(new Vector(2, 2), 3).getMatrix().noRotation());
        assertTrue(new TransformTranslate(4, 5).getMatrix().noRotation());
        assertTrue(TransformMatrix.scale(2, 2).noRotation());

        assertTrue(TransformMatrix.scale(2, 3).noRotation());
    }

    public void testMul() {
        final TransformMatrix t1 = new TransformTranslate(10, 10).getMatrix();
        final TransformMatrix t2 = new TransformTranslate(10, 10).getMatrix();
        final VectorFloat v = new VectorFloat(2, 3);
        compare(v.transform(t1).transform(t2), v.transform(Transform.mul(t1, t2)));
    }

    public void testMul2() {
        final TransformMatrix t1 = new TransformTranslate(10, 10).getMatrix();
        final TransformMatrix t2 = new TransformRotate(new Vector(10, 10), 1).getMatrix();
        final VectorFloat v = new VectorFloat(2, 3);
        compare(v.transform(t1).transform(t2), v.transform(Transform.mul(t1, t2)));
    }

    public void testMul3() {
        final TransformMatrix t1 = new TransformTranslate(10, 10).getMatrix();
        final TransformMatrix t2 = TransformMatrix.rotate(45);
        final VectorFloat v = new VectorFloat(2, 3);
        compare(v.transform(t1).transform(t2), v.transform(Transform.mul(t1, t2)));
    }

    private void compare(VectorInterface v1, VectorInterface v2) {
        assertEquals(v1.getXFloat(), v2.getXFloat(), 1e-4);
        assertEquals(v1.getYFloat(), v2.getYFloat(), 1e-4);
    }

}
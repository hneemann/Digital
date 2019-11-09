/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a 2D Vector
 */
public class Vector implements VectorInterface {

    /**
     * the x coordinate
     */
    public final int x;
    /**
     * the y coordinate
     */
    public final int y;

    /**
     * Creates a new instance
     *
     * @param x x
     * @param y y
     */
    public Vector(int x, int y) {
        this.x = x;
        this.y = y;
    }

    /**
     * Creates a copy of the given vector
     *
     * @param pos the vector to copy
     */
    public Vector(VectorInterface pos) {
        this(pos.getX(), pos.getY());
    }


    /**
     * Returns a new vector
     * shorthand for new Vector(x,y)
     *
     * @param x x
     * @param y y
     * @return the vector
     */
    public static Vector vec(int x, int y) {
        return new Vector(x, y);
    }


    /**
     * returns the minimum vector from the given vectors.
     *
     * @param p the vectors to evaluate
     * @return the minimum
     */
    public static Vector min(VectorInterface... p) {
        int x = p[0].getX();
        int y = p[0].getY();
        for (int i = 1; i < p.length; i++) {
            if (p[i].getX() < x) x = p[i].getX();
            if (p[i].getY() < y) y = p[i].getY();
        }
        return new Vector(x, y);
    }

    /**
     * returns the maximim vector from the given vectors.
     *
     * @param p the vectors to evaluate
     * @return the maximum
     */
    public static Vector max(VectorInterface... p) {
        int x = p[0].getX();
        int y = p[0].getY();
        for (int i = 1; i < p.length; i++) {
            if (p[i].getX() > x) x = p[i].getX();
            if (p[i].getY() > y) y = p[i].getY();
        }
        return new Vector(x, y);
    }

    /**
     * returns the width of the given vectors.
     *
     * @param p the vectors
     * @return max(p)-min(p)
     */
    public static Vector width(VectorInterface... p) {
        int x1 = p[0].getX();
        int y1 = p[0].getY();
        int x2 = x1;
        int y2 = y1;
        for (int i = 1; i < p.length; i++) {
            if (p[i].getX() < x1) x1 = p[i].getX();
            if (p[i].getY() < y1) y1 = p[i].getY();
            if (p[i].getX() > x2) x2 = p[i].getX();
            if (p[i].getY() > y2) y2 = p[i].getY();
        }
        return new Vector(x2 - x1, y2 - y1);
    }

    @Override
    public Vector add(VectorInterface a) {
        return new Vector(x + a.getX(), y + a.getY());
    }

    /**
     * Creates a new vector which has the value this+(x,y)
     *
     * @param x x
     * @param y y
     * @return this+(x,y)
     */
    public Vector add(int x, int y) {
        return new Vector(this.x + x, this.y + y);
    }

    /**
     * Adds an offset to every vector in the given list
     *
     * @param vectors the original vectors
     * @param offs    the offset
     * @return the new list
     */
    public static List<Vector> add(List<Vector> vectors, Vector offs) {
        ArrayList<Vector> newVec = new ArrayList<>();
        for (Vector v : vectors)
            newVec.add(v.add(offs));
        return newVec;
    }

    @Override
    public Vector sub(VectorInterface a) {
        return new Vector(x - a.getX(), y - a.getY());
    }

    /**
     * Creates a new vector which has the value this*a
     *
     * @param a a
     * @return this*a
     */
    public Vector mul(int a) {
        return new Vector(x * a, y * a);
    }

    /**
     * Creates a new vector which has the value this*a
     *
     * @param a a
     * @return this*a
     */
    public VectorFloat mul(float a) {
        return new VectorFloat(x * a, y * a);
    }

    @Override
    public Vector div(int d) {
        return new Vector(x / d, y / d);
    }

    @Override
    public String toString() {
        return "(x=" + x
                + ", y=" + y
                + ')';
    }

    /**
     * Checks if this vector is inside the given bounding box
     *
     * @param min upper left corner
     * @param max lower right corner
     * @return true is inside
     */
    public boolean inside(Vector min, Vector max) {
        return min.x <= x && x <= max.x && min.y <= y && y <= max.y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Vector vector = (Vector) o;

        return x == vector.x && y == vector.y;

    }

    @Override
    public int hashCode() {
        int result = x;
        result = 31 * result + y;
        return result;
    }

    /**
     * @return true if this vector is (0,0)
     */
    public boolean isZero() {
        return x == 0 && y == 0;
    }

    @Override
    public VectorFloat norm() {
        float l = (float) Math.sqrt(x * x + y * y);
        return new VectorFloat(x / l, y / l);
    }

    @Override
    public int getX() {
        return x;
    }

    @Override
    public int getY() {
        return y;
    }

    @Override
    public float getXFloat() {
        return x;
    }

    @Override
    public float getYFloat() {
        return y;
    }

    @Override
    public VectorInterface transform(Transform tr) {
        return tr.transform(this);
    }

    @Override
    public Vector round() {
        return this;
    }

    @Override
    public float len() {
        return (float) Math.sqrt(x * x + y * y);
    }

    @Override
    public VectorFloat toFloat() {
        return new VectorFloat(x, y);
    }

    @Override
    public Vector getOrthogonal() {
        return new Vector(y, -x);
    }

}

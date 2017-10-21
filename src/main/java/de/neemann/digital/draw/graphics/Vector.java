package de.neemann.digital.draw.graphics;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a 2D Vector
 *
 * @author hneemann
 */
public class Vector {

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
    public Vector(Vector pos) {
        this(pos.x, pos.y);
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
    public static Vector min(Vector... p) {
        int x = p[0].x;
        int y = p[0].y;
        for (int i = 1; i < p.length; i++) {
            if (p[i].x < x) x = p[i].x;
            if (p[i].y < y) y = p[i].y;
        }
        return new Vector(x, y);
    }

    /**
     * returns the maximim vector from the given vectors.
     *
     * @param p the vectors to evaluate
     * @return the maximum
     */
    public static Vector max(Vector... p) {
        int x = p[0].x;
        int y = p[0].y;
        for (int i = 1; i < p.length; i++) {
            if (p[i].x > x) x = p[i].x;
            if (p[i].y > y) y = p[i].y;
        }
        return new Vector(x, y);
    }

    /**
     * returns the width of the given vectors.
     *
     * @param p the vectors
     * @return max(p)-min(p)
     */
    public static Vector width(Vector... p) {
        int x1 = p[0].x;
        int y1 = p[0].y;
        int x2 = x1;
        int y2 = y1;
        for (int i = 1; i < p.length; i++) {
            if (p[i].x < x1) x1 = p[i].x;
            if (p[i].y < y1) y1 = p[i].y;
            if (p[i].x > x2) x2 = p[i].x;
            if (p[i].y > y2) y2 = p[i].y;
        }
        return new Vector(x2 - x1, y2 - y1);
    }

    /**
     * Creates a new vector which has the value this+a
     *
     * @param a a
     * @return this+a
     */
    public Vector add(Vector a) {
        return new Vector(x + a.x, y + a.y);
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

    /**
     * Creates a new vector which has the value this-a
     *
     * @param a a
     * @return this-a
     */
    public Vector sub(Vector a) {
        return new Vector(x - a.x, y - a.y);
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
     * Creates a new vector which has the value this/d
     *
     * @param d a
     * @return this/d
     */
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

        if (x != vector.x) return false;
        return y == vector.y;

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

    /**
     * @return the norm multiplied by 128
     */
    public Vector norm128() {
        float l = (float) Math.sqrt(x * x + y * y);
        return new Vector(Math.round(x * 128 / l), Math.round(y * 128 / l));
    }

}

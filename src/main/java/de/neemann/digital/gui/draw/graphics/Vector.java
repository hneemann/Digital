package de.neemann.digital.gui.draw.graphics;

import de.neemann.digital.gui.draw.parts.Moveable;

/**
 * @author hneemann
 */
public class Vector implements Moveable {

    public int x;
    public int y;

    public Vector(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public Vector(Vector pos) {
        this(pos.x, pos.y);
    }

    public static Vector min(Vector... p) {
        int x = p[0].x;
        int y = p[0].y;
        for (int i = 1; i < p.length; i++) {
            if (p[i].x < x) x = p[i].x;
            if (p[i].y < y) y = p[i].y;
        }
        return new Vector(x, y);
    }

    public static Vector max(Vector... p) {
        int x = p[0].x;
        int y = p[0].y;
        for (int i = 1; i < p.length; i++) {
            if (p[i].x > x) x = p[i].x;
            if (p[i].y > y) y = p[i].y;
        }
        return new Vector(x, y);
    }

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

    public Vector add(Vector a) {
        return new Vector(x + a.x, y + a.y);
    }

    public Vector add(int x, int y) {
        return new Vector(this.x + x, this.y + y);
    }

    public Vector sub(Vector a) {
        return new Vector(x - a.x, y - a.y);
    }

    public Vector mul(int a) {
        return new Vector(x * a, y * a);
    }

    public Vector div(int d) {
        return new Vector(x / d, y / d);
    }

    @Override
    public String toString() {
        return "Vector{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    @Override
    public void move(Vector delta) {
        x += delta.x;
        y += delta.y;
    }

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
}

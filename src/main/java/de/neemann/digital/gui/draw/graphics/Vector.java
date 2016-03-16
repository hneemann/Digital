package de.neemann.digital.gui.draw.graphics;

/**
 * @author hneemann
 */
public class Vector {

    public final int x;
    public final int y;

    public Vector(int x, int y) {
        this.x = x;
        this.y = y;
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

    @Override
    public String toString() {
        return "Vector{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }
}

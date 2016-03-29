package de.neemann.digital.draw.graphics;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class Polygon {

    private ArrayList<Vector> points;
    private boolean closed;

    public Polygon() {
        this(new ArrayList<>(), true);
    }

    public Polygon(boolean closed) {
        this(new ArrayList<>(), closed);
    }

    public Polygon(ArrayList<Vector> points, boolean closed) {
        this.points = points;
        this.closed = closed;
    }

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public Polygon add(int x, int y) {
        return add(new Vector(x, y));
    }

    public Polygon add(Vector p) {
        points.add(p);
        return this;
    }

    public ArrayList<Vector> getPoints() {
        return points;
    }


    public Vector get(int i) {
        return points.get(i);
    }

    public int size() {
        return points.size();
    }
}

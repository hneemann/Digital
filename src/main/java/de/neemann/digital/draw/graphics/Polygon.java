package de.neemann.digital.draw.graphics;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * A polygon representation used by the {@link Graphic} interface.
 *
 * @author hneemann
 */
public class Polygon implements Iterable<Vector> {

    private final ArrayList<Vector> points;
    private final boolean closed;

    /**
     * Creates e new closed polygon
     */
    public Polygon() {
        this(new ArrayList<>(), true);
    }

    /**
     * Creates e new instance
     *
     * @param closed true if polygon is closed
     */
    public Polygon(boolean closed) {
        this(new ArrayList<>(), closed);
    }

    /**
     * Creates e new instance
     *
     * @param points the polygons points
     * @param closed true if polygon is closed
     */
    public Polygon(ArrayList<Vector> points, boolean closed) {
        this.points = points;
        this.closed = closed;
    }

    /**
     * @return true if polygon is closed
     */
    public boolean isClosed() {
        return closed;
    }

    /**
     * Adds a point to the polygon
     *
     * @param x x coordinate
     * @param y y coordinate
     * @return this for chained calls
     */
    public Polygon add(int x, int y) {
        return add(new Vector(x, y));
    }

    /**
     * Adds a new point to the polygon.
     *
     * @param p the point to add
     * @return this for chained calls
     */
    public Polygon add(Vector p) {
        points.add(p);
        return this;
    }

    /**
     * @return the number of points
     */
    public int size() {
        return points.size();
    }

    /**
     * Returns one of the points
     *
     * @param i the index
     * @return the i'th point
     */
    public Vector get(int i) {
        return points.get(i);
    }

    @Override
    public Iterator<Vector> iterator() {
        return points.iterator();
    }
}

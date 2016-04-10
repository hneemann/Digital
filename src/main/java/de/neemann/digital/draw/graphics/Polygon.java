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

    /**
     * Try's to add the given line to the polygon.
     * Its possible to add a line if one of the points matches the first or the last
     * point of the polygon. In that case the other point is added and true is returned.
     * If it is not possible to add the line a false is returned
     *
     * @param p1 first point of the line
     * @param p2 second point of the line
     * @return true if line was added
     */
    public boolean addLine(Vector p1, Vector p2) {
        return check(p1, p2) || check(p2, p1);
    }

    private boolean check(Vector p1, Vector p2) {
        if (p1.equals(getFirst())) {
            points.add(0, p2);
            return true;
        } else if (p1.equals(getLast())) {
            points.add(p2);
            return true;
        } else
            return false;
    }

    /**
     * @return the first point of the polygon
     */
    public Vector getFirst() {
        return points.get(0);
    }

    /**
     * @return the last point of the polygon
     */
    public Vector getLast() {
        return points.get(points.size() - 1);
    }

    /**
     * Append the given polygon to this polygon
     *
     * @param p2 the polygon to append
     * @return this for chained calls
     */
    public Polygon append(Polygon p2) {
        points.addAll(p2.points);
        return this;
    }

    /**
     * Returns a new polygon with reverse point order.
     *
     * @return returns this polygon with reverse order of points
     */
    public Polygon reverse() {
        Polygon p = new Polygon(closed);
        for (int i = points.size() - 1; i >= 0; i--)
            p.add(points.get(i));
        return p;
    }
}

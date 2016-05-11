package de.neemann.digital.draw.graphics;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A polygon representation used by the {@link Graphic} interface.
 *
 * @author hneemann
 */
public class Polygon implements Iterable<Vector> {

    private final ArrayList<Vector> points;
    private final HashSet<Integer> isBezierStart;
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
        isBezierStart = new HashSet<>();
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
     * Adds a new bezier line to the polygon.
     *
     * @param c1 the first control point to add
     * @param c2 the second control point to add
     * @param p  the end point to add
     * @return this for chained calls
     */
    public Polygon add(Vector c1, Vector c2, Vector p) {
        isBezierStart.add(points.size());
        points.add(c1);
        points.add(c2);
        points.add(p);
        return this;
    }

    /**
     * Returns true if the point with the given index is a bezier start point
     *
     * @param n the index
     * @return true if point is bezier start
     */
    public boolean isBezierStart(int n) {
        return isBezierStart.contains(n);
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
        if (!p2.isBezierStart.isEmpty())
            throw new RuntimeException("append of bezier not supported");
        for (int i = 1; i < p2.points.size(); i++)
            points.add(p2.points.get(i));
        return this;
    }

    /**
     * Returns a new polygon with reverse point order.
     *
     * @return returns this polygon with reverse order of points
     */
    public Polygon reverse() {
        if (!isBezierStart.isEmpty())
            throw new RuntimeException("reverse of bezier not supported");
        Polygon p = new Polygon(closed);
        for (int i = points.size() - 1; i >= 0; i--)
            p.add(points.get(i));
        return p;
    }

    /**
     * Transforms this polygon
     *
     * @param transform the transformation
     * @return the transformed polygon
     */
    public Polygon transform(Transform transform) {
        Polygon p = new Polygon(closed);
        for (Vector v : points)
            p.add(transform.transform(v));
        p.isBezierStart.addAll(isBezierStart);
        return p;
    }
}

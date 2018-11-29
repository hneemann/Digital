/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * A polygon representation used by the {@link Graphic} interface.
 */
public class Polygon implements GenericPath {

    private final ArrayList<VectorInterface> points;
    private final HashSet<Integer> isBezierStart;
    private boolean closed;

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
    public Polygon(ArrayList<VectorInterface> points, boolean closed) {
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
    public Polygon add(VectorInterface p) {
        points.add(p);
        return this;
    }

    /**
     * Adds a new cubic bezier curve to the polygon.
     *
     * @param c1 the first control point to add
     * @param c2 the second control point to add
     * @param p  the end point to add
     * @return this for chained calls
     */
    public Polygon add(VectorInterface c1, VectorInterface c2, VectorInterface p) {
        if (points.size() == 0)
            throw new RuntimeException("cubic bezier curve is not allowed to be the first path element");
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
    public VectorInterface get(int i) {
        return points.get(i);
    }

    @Override
    public Iterator<VectorInterface> iterator() {
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
    public boolean addLine(VectorInterface p1, VectorInterface p2) {
        return check(p1, p2) || check(p2, p1);
    }

    private boolean check(VectorInterface p1, VectorInterface p2) {
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
    public VectorInterface getFirst() {
        return points.get(0);
    }

    /**
     * @return the last point of the polygon
     */
    public VectorInterface getLast() {
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
        if (transform == Transform.IDENTITY)
            return this;

        Polygon p = new Polygon(closed);
        for (VectorInterface v : points)
            p.add(v.transform(transform));
        p.isBezierStart.addAll(isBezierStart);
        return p;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("M ");
        VectorInterface v = points.get(0);
        sb.append(str(v.getXFloat())).append(",").append(str(v.getYFloat())).append(" ");
        //modification of loop variable i is intended!
        //CHECKSTYLE.OFF: ModifiedControlVariable
        for (int i = 1; i < points.size(); i++) {
            v = points.get(i);
            if (isBezierStart.contains(i)) {
                sb.append("C ").append(str(v.getXFloat())).append(",").append(str(v.getYFloat())).append(" ");
                v = points.get(i + 1);
                sb.append(str(v.getXFloat())).append(",").append(str(v.getYFloat())).append(" ");
                v = points.get(i + 2);
                sb.append(str(v.getXFloat())).append(",").append(str(v.getYFloat())).append(" ");
                i += 2;
            } else
                sb.append("L ").append(str(v.getXFloat())).append(",").append(str(v.getYFloat())).append(" ");
        }
        //CHECKSTYLE.ON: ModifiedControlVariable
        if (closed)
            sb.append("z");
        return sb.toString();
    }

    private static String str(float f) {
        if (f == Math.round(f))
            return Integer.toString(Math.round(f));
        else
            return Float.toString(f);
    }

    /**
     * Creates a polygon from a SVG path
     *
     * @param path the svg path
     * @return the polygon or null if there was an error
     */
    public static Polygon createFromPath(String path) {
        try {
            return new PolygonParser(path).create();
        } catch (PolygonParser.ParserException e) {
            return null;
        }
    }

    void setClosed(boolean closed) {
        this.closed = closed;
    }

	@Override
	public Path2D toPath2D() {
        Path2D path = new GeneralPath();
        //modification of loop variable i is intended!
        //CHECKSTYLE.OFF: ModifiedControlVariable
        for (int i = 0; i < size(); i++) {
            if (i == 0) {
                path.moveTo(get(i).getXFloat(), get(i).getYFloat());
            } else {
                if (isBezierStart(i)) {
                    path.curveTo(get(i).getXFloat(), get(i).getYFloat(),
                            get(i + 1).getXFloat(), get(i + 1).getYFloat(),
                            get(i + 2).getXFloat(), get(i + 2).getYFloat());
                    i += 2;
                } else
                    path.lineTo(get(i).getXFloat(), get(i).getYFloat());
            }
        }
        //CHECKSTYLE.ON: ModifiedControlVariable

        if (isClosed())
            path.closePath();
		return path;
	}
}

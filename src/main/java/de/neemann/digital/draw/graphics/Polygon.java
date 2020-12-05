/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

import java.awt.geom.Path2D;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * A polygon representation used by the {@link Graphic} interface.
 */
public class Polygon implements Iterable<Polygon.PathElement> {

    private final ArrayList<PathElement> path;
    private boolean closed;
    private boolean hasSpecialElements = false;
    private boolean evenOdd;

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
        this.closed = closed;
        this.path = new ArrayList<>();
        for (VectorInterface p : points)
            add(p);
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
        if (path.isEmpty())
            add(new MoveTo(p));
        else
            add(new LineTo(p));
        return this;
    }

    private void add(PathElement pe) {
        path.add(pe);
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
        if (path.size() == 0)
            throw new RuntimeException("cubic bezier curve is not allowed to be the first path element");
        add(new CurveTo(c1, c2, p));
        hasSpecialElements = true;
        return this;
    }

    /**
     * Adds a new quadratic bezier curve to the polygon.
     *
     * @param c the control point to add
     * @param p the end point to add
     * @return this for chained calls
     */
    public Polygon add(VectorInterface c, VectorInterface p) {
        if (path.size() == 0)
            throw new RuntimeException("quadratic bezier curve is not allowed to be the first path element");
        add(new QuadTo(c, p));
        hasSpecialElements = true;
        return this;
    }

    /**
     * Closes the actual path
     */
    public void addClosePath() {
        add(new ClosePath());
    }

    /**
     * Adds a moveto to the path
     *
     * @param p the point to move to
     */
    public void addMoveTo(VectorFloat p) {
        add(new MoveTo(p));
    }

    /**
     * @return true if filled in even odd mode
     */
    public boolean getEvenOdd() {
        return evenOdd;
    }

    /**
     * Sets the even odd mode used to fill the polygon
     *
     * @param evenOdd true is even odd mode is needed
     * @return this for chained calls
     */
    public Polygon setEvenOdd(boolean evenOdd) {
        this.evenOdd = evenOdd;
        return this;
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
        if (closed)
            return false;

        if (p1.equals(getFirst())) {
            if (p2.equals(getLast()))
                closed = true;
            else {
                removeInitialMoveTo();
                path.add(0, new MoveTo(p2));
            }
            return true;
        } else if (p1.equals(getLast())) {
            if (p2.equals(getFirst()))
                closed = true;
            else
                path.add(new LineTo(p2));
            return true;
        } else
            return false;
    }

    private void removeInitialMoveTo() {
        if (!(path.get(0) instanceof MoveTo))
            throw new RuntimeException("initial path element is not a MoveTo!");
        path.set(0, new LineTo(path.get(0)));
    }

    /**
     * @return the first point of the polygon
     */
    public VectorInterface getFirst() {
        return path.get(0).getPoint();
    }

    /**
     * @return the last point of the polygon
     */
    public VectorInterface getLast() {
        return path.get(path.size() - 1).getPoint();
    }

    /**
     * Append the given polygon to this polygon
     *
     * @param p2 the polygon to append
     * @return this for chained calls
     */
    public Polygon append(Polygon p2) {
        if (hasSpecialElements || p2.hasSpecialElements)
            throw new RuntimeException("append of bezier not supported");

        if (p2.getLast().equals(getFirst())) {
            for (int i = 1; i < p2.path.size() - 1; i++)
                add(p2.path.get(i).getPoint());
            closed = true;
        } else {
            for (int i = 1; i < p2.path.size(); i++)
                add(p2.path.get(i).getPoint());
        }
        return this;
    }

    /**
     * Returns a new polygon with reverse point order.
     *
     * @return returns this polygon with reverse order of points
     */
    public Polygon reverse() {
        if (hasSpecialElements)
            throw new RuntimeException("append of bezier not supported");
        Polygon p = new Polygon(closed);
        for (int i = path.size() - 1; i >= 0; i--)
            p.add(path.get(i).getPoint());
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

        Polygon p = new Polygon(closed).setEvenOdd(evenOdd);
        for (PathElement pe : path)
            p.add(pe.transform(transform));
        return p;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (PathElement pe : path) {
            if (sb.length() > 0)
                sb.append(' ');
            sb.append(pe.toString());
        }

        if (closed)
            sb.append(" Z");
        return sb.toString();
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
    public Iterator<PathElement> iterator() {
        return path.iterator();
    }

    /**
     * Draw this polygon to a {@link Path2D} instance.
     *
     * @param path2d the Path2d instance.
     */
    public void drawTo(Path2D path2d) {
        for (PathElement pe : path)
            pe.drawTo(path2d);
        if (closed)
            path2d.closePath();
        if (evenOdd)
            path2d.setWindingRule(Path2D.WIND_EVEN_ODD);
    }

    /**
     * Traverses all points
     *
     * @param v the visitor to use
     */
    public void traverse(PointVisitor v) {
        VectorInterface start = null;
        for (PathElement pe : path)
            start = pe.traverse(start, v);
    }

    /**
     * Creates a new polygon with rounded edges
     *
     * @param rad the radius of the rounding
     * @return the new polygon
     */
    public Polygon roundEdges(int rad) {
        Polygon newPoly = new Polygon(closed);
        int len = path.size();
        if (!closed) len--;
        for (int i = 0; i < len; i++) {
            VectorInterface p0 = path.get(i).getPoint();
            VectorInterface p1 = path.get(wrapIndex(i + 1)).getPoint();
            VectorInterface p2 = path.get(wrapIndex(i + 2)).getPoint();

            VectorInterface d0 = p1.sub(p0);
            float l0 = d0.len();
            VectorInterface d1 = p2.sub(p1);
            float l1 = d1.len();

            VectorInterface n0 = p0.add(d0.mul(rad / l0));
            VectorInterface n1 = p0.add(d0.mul((l0 - rad) / l0));
            VectorInterface n2 = p1.add(d1.mul(rad / l1));

            if (i == 0) {
                if (closed)
                    newPoly.add(n0);
                else
                    newPoly.add(p0);
            }

            if (!closed && i == len - 1) {
                newPoly.add(p1);
            } else {
                newPoly.add(n1);
                newPoly.add(p1, n2);
            }
        }

        return newPoly;
    }

    private int wrapIndex(int i) {
        if (i >= path.size())
            return i - path.size();
        else
            return i;
    }

    /**
     * Visitor used to traverse all points
     */
    public interface PointVisitor {
        /**
         * Called with every point
         *
         * @param p the point
         */
        void visit(VectorInterface p);
    }

    /**
     * A element of the path
     */
    public interface PathElement {
        /**
         * @return the coordinate of this path element
         */
        VectorInterface getPoint();

        /**
         * Returns the transformed path element
         *
         * @param transform the transformation
         * @return the transormated path element
         */
        PathElement transform(Transform transform);

        /**
         * Draws this path element to a Path2D instance.
         *
         * @param path2d the a Path2D instance
         */
        void drawTo(Path2D path2d);

        /**
         * Traverses all points
         *
         * @param start the start point
         * @param v     the visitor to use
         * @return the end point
         */
        VectorInterface traverse(VectorInterface start, PointVisitor v);
    }

    private static String str(float f) {
        if (f == Math.round(f))
            return Integer.toString(Math.round(f));
        else
            return Float.toString(f);
    }

    private static String str(VectorInterface p) {
        return str(p.getXFloat()) + "," + str(p.getYFloat());
    }

    //LineTo can not be final because its overridden. Maybe checkstyle has a bug?
    //CHECKSTYLE.OFF: FinalClass
    private static class LineTo implements PathElement {
        protected final VectorInterface p;

        private LineTo(VectorInterface p) {
            this.p = p;
        }

        private LineTo(PathElement pathElement) {
            this(pathElement.getPoint());
        }

        @Override
        public VectorInterface getPoint() {
            return p;
        }

        @Override
        public PathElement transform(Transform transform) {
            return new LineTo(p.transform(transform));
        }

        @Override
        public void drawTo(Path2D path2d) {
            path2d.lineTo(p.getXFloat(), p.getYFloat());
        }

        @Override
        public String toString() {
            return "L " + str(p);
        }

        @Override
        public VectorInterface traverse(VectorInterface start, PointVisitor v) {
            v.visit(p);
            return p;
        }
    }
    //CHECKSTYLE.ON: FinalClass

    private static final class MoveTo extends LineTo {
        private MoveTo(VectorInterface p) {
            super(p);
        }

        @Override
        public String toString() {
            return "M " + str(p);
        }

        @Override
        public void drawTo(Path2D path2d) {
            path2d.moveTo(p.getXFloat(), p.getYFloat());
        }

        @Override
        public PathElement transform(Transform transform) {
            return new MoveTo(p.transform(transform));
        }
    }

    private static final class CurveTo implements PathElement {
        private final VectorInterface c1;
        private final VectorInterface c2;
        private final VectorInterface p;

        private CurveTo(VectorInterface c1, VectorInterface c2, VectorInterface p) {
            this.c1 = c1;
            this.c2 = c2;
            this.p = p;
        }

        @Override
        public VectorInterface getPoint() {
            return p;
        }

        @Override
        public PathElement transform(Transform transform) {
            return new CurveTo(
                    c1.transform(transform),
                    c2.transform(transform),
                    p.transform(transform)
            );
        }

        @Override
        public String toString() {
            return "C " + str(c1) + " " + str(c2) + " " + str(p);
        }

        @Override
        public void drawTo(Path2D path2d) {
            path2d.curveTo(c1.getXFloat(), c1.getYFloat(),
                    c2.getXFloat(), c2.getYFloat(),
                    p.getXFloat(), p.getYFloat());
        }

        private VectorInterface getPos(VectorInterface start, float t) {
            float omt = 1 - t;
            return start.mul(omt * omt * omt)
                    .add(c1.mul(3 * t * omt * omt))
                    .add(c2.mul(3 * t * t * omt))
                    .add(p.mul(t * t * t));
        }

        @Override
        public VectorInterface traverse(VectorInterface start, PointVisitor v) {
            v.visit(getPos(start, 0.25f));
            v.visit(getPos(start, 0.5f));
            v.visit(getPos(start, 0.75f));
            v.visit(p);
            return p;
        }
    }

    private static final class QuadTo implements PathElement {
        private final VectorInterface c;
        private final VectorInterface p;

        private QuadTo(VectorInterface c, VectorInterface p) {
            this.c = c;
            this.p = p;
        }

        @Override
        public VectorInterface getPoint() {
            return p;
        }

        @Override
        public PathElement transform(Transform transform) {
            return new QuadTo(
                    c.transform(transform),
                    p.transform(transform)
            );
        }

        @Override
        public String toString() {
            return "Q " + str(c) + " " + str(p);
        }

        @Override
        public void drawTo(Path2D path2d) {
            path2d.quadTo(c.getXFloat(), c.getYFloat(),
                    p.getXFloat(), p.getYFloat());
        }

        private VectorInterface getPos(VectorInterface start, float t) {
            float omt = 1 - t;
            return start.mul(omt * omt).add(c.mul(2 * t * omt)).add(p.mul(t * t));
        }

        @Override
        public VectorInterface traverse(VectorInterface start, PointVisitor v) {
            v.visit(getPos(start, 0.4f));
            v.visit(getPos(start, 0.5f));
            v.visit(getPos(start, 0.6f));
            v.visit(p);
            return p;
        }
    }

    private static final class ClosePath implements PathElement {
        @Override
        public VectorInterface getPoint() {
            return null;
        }

        @Override
        public PathElement transform(Transform transform) {
            return this;
        }

        @Override
        public void drawTo(Path2D path2d) {
            path2d.closePath();
        }

        @Override
        public String toString() {
            return "Z";
        }

        @Override
        public VectorInterface traverse(VectorInterface start, PointVisitor v) {
            return null;
        }
    }
}

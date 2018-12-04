/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

/**
 * Creates a polygon from a path
 */
public class PolygonParser {
    private final SVGTokenizer t;
    private float x;
    private float y;
    private VectorFloat polyStart;
    private VectorInterface lastQuadraticControlPoint;
    private VectorInterface lastCubicControlPoint;
    private String command = "";

    /**
     * Creates a new instance
     *
     * @param path the path to parse
     */
    public PolygonParser(String path) {
        t = new SVGTokenizer(path);
    }

    private float nextValue() throws SVGTokenizer.TokenizerException {
        return t.readFloat();
    }

    private VectorFloat nextVector() throws SVGTokenizer.TokenizerException {
        x = nextValue();
        y = nextValue();
        return new VectorFloat(x, y);
    }

    private VectorFloat nextVectorInc() throws SVGTokenizer.TokenizerException {
        x += nextValue();
        y += nextValue();
        return new VectorFloat(x, y);
    }

    private VectorFloat nextVectorRel() throws SVGTokenizer.TokenizerException {
        return new VectorFloat(x + nextValue(), y + nextValue());
    }

    /**
     * Creates a polygon from the given path.
     *
     * @return the polygon
     * @throws ParserException ParserException
     */
    public Polygon create() throws ParserException {
        try {
            Polygon p = new Polygon(false);
            boolean closedPending = false;
            while (!t.isEOF()) {
                if (t.nextIsNumber()) {
                    if (command.equals("m"))
                        command = "l";
                    else if (command.equals("M"))
                        command = "L";
                } else
                    command = t.readCommand();
                switch (command) {
                    case "M":
                        if (closedPending) {
                            closedPending = false;
                            p.addClosePath();
                        }
                        p.addMoveTo(setPolyStart(nextVector()));
                        clearControl();
                        break;
                    case "m":
                        if (closedPending) {
                            closedPending = false;
                            p.addClosePath();
                        }
                        p.addMoveTo(setPolyStart(nextVectorInc()));
                        clearControl();
                        break;
                    case "V":
                        y = nextValue();
                        p.add(getCurrent());
                        clearControl();
                        break;
                    case "v":
                        y += nextValue();
                        p.add(getCurrent());
                        clearControl();
                        break;
                    case "H":
                        x = nextValue();
                        p.add(getCurrent());
                        clearControl();
                        break;
                    case "h":
                        x += nextValue();
                        p.add(getCurrent());
                        clearControl();
                        break;
                    case "l":
                        p.add(nextVectorInc());
                        clearControl();
                        break;
                    case "L":
                        p.add(nextVector());
                        clearControl();
                        break;
                    case "c":
                        p.add(nextVectorRel(), setLastC3(nextVectorRel()), nextVectorInc());
                        break;
                    case "C":
                        p.add(nextVector(), setLastC3(nextVector()), nextVector());
                        break;
                    case "q":
                        p.add(setLastC2(nextVectorRel()), nextVectorInc());
                        break;
                    case "Q":
                        p.add(setLastC2(nextVector()), nextVector());
                        break;
                    case "s":
                        addCubicWithReflect(p, getCurrent(), nextVectorRel(), nextVectorInc());
                        break;
                    case "S":
                        addCubicWithReflect(p, getCurrent(), nextVector(), nextVector());
                        break;
                    case "t":
                        addQuadraticWithReflect(p, getCurrent(), nextVectorInc());
                        break;
                    case "T":
                        addQuadraticWithReflect(p, getCurrent(), nextVector());
                        break;
                    case "a":
                        addArc(p, getCurrent(), nextValue(), nextValue(), nextValue(), nextValue() != 0, nextValue() != 0, nextVectorInc());
                        clearControl();
                        break;
                    case "A":
                        addArc(p, getCurrent(), nextValue(), nextValue(), nextValue(), nextValue() != 0, nextValue() != 0, nextVector());
                        clearControl();
                        break;
                    case "Z":
                    case "z":
                        closedPending = true;
                        if (polyStart != null) {
                            x = polyStart.getXFloat();
                            y = polyStart.getYFloat();
                        }
                        clearControl();
                        break;
                    default:
                        throw new ParserException("unsupported path command " + command);
                }
            }
            if (closedPending)
                p.setClosed(true);
            return p;
        } catch (SVGTokenizer.TokenizerException e) {
            throw new ParserException("error parsing a path", e);
        }
    }

    private VectorFloat setPolyStart(VectorFloat v) {
        polyStart = v;
        return v;
    }

    private VectorInterface getCurrent() {
        return new VectorFloat(x, y);
    }

    private VectorInterface setLastC2(VectorInterface p) {
        lastQuadraticControlPoint = p;
        lastCubicControlPoint = null;
        return p;
    }

    private VectorInterface setLastC3(VectorInterface p) {
        lastCubicControlPoint = p;
        lastQuadraticControlPoint = null;
        return p;
    }

    private void clearControl() {
        lastQuadraticControlPoint = null;
        lastCubicControlPoint = null;
    }

    private VectorInterface getLastC2() {
        if (lastQuadraticControlPoint == null)
            return getCurrent();
        return lastQuadraticControlPoint;
    }

    private VectorInterface getLastC3() {
        if (lastCubicControlPoint == null)
            return getCurrent();
        return lastCubicControlPoint;
    }

    /*
     * Substitutes the arc by a number of quadratic bezier curves
     */
    //CHECKSTYLE.OFF: ParameterNumberCheck
    private void addArc(Polygon p, VectorInterface current, float rx, float ry, float rot, boolean large, boolean sweep, VectorFloat pos) {

        // if rx=0 or ry=0 add a straight line
        if (rx == 0 || ry == 0) {
            p.add(pos);
            return;
        }

        // take the absolute value of rx, ry
        if (rx < 0)
            rx = -rx;
        if (ry < 0)
            ry = -ry;

        // transform the ellipse to a circle
        Transform tr = Transform.IDENTITY;
        if (rx != ry)
            tr = TransformMatrix.scale(1, rx / ry);

        if (rot != 0)
            tr = Transform.mul(TransformMatrix.rotate(-rot), tr);

        Transform invert = tr.invert();

        VectorInterface p1 = current.transform(tr);
        VectorInterface p2 = pos.transform(tr);

        // ellipse is transformed to a circle with radius r
        float r = rx;

        // correct invalid radii
        final float dist = p1.sub(p2).len();
        if (dist > r * 2)
            r = dist / 2;

        double x1 = p1.getXFloat();
        double y1 = p1.getYFloat();
        double x2 = p2.getXFloat();
        double y2 = p2.getYFloat();

        double x1q = x1 * x1;
        double y1q = y1 * y1;
        double x2q = x2 * x2;
        double y2q = y2 * y2;
        double rq = r * r;

        try {
            double x0A = (r * (y1 - y2) * sqrt(rq * (4 * rq - y1q + y2 * (2 * y1 - y2)) - rq * (x1q - 2 * x1 * x2 + x2q)) * sign(x1 - x2) + r * (x1 + x2) * sqrt(rq * (y1q - 2 * y1 * y2 + y2q) + rq * (x1q - 2 * x1 * x2 + x2q))) / (2 * r * sqrt(rq * (y1q - 2 * y1 * y2 + y2q) + rq * (x1q - 2 * x1 * x2 + x2q)));
            double y0A = (r * (y1 + y2) * sqrt(rq * (y1q - 2 * y1 * y2 + y2q) + rq * (x1q - 2 * x1 * x2 + x2q)) - r * sqrt(rq * (4 * rq - y1q + y2 * (2 * y1 - y2)) - rq * (x1q - 2 * x1 * x2 + x2q)) * abs(x1 - x2)) / (2 * r * sqrt(rq * (y1q - 2 * y1 * y2 + y2q) + rq * (x1q - 2 * x1 * x2 + x2q)));
            double x0B = (r * (x1 + x2) * sqrt(rq * (y1q - 2 * y1 * y2 + y2q) + rq * (x1q - 2 * x1 * x2 + x2q)) - r * (y1 - y2) * sqrt(rq * (4 * rq - y1q + y2 * (2 * y1 - y2)) - rq * (x1q - 2 * x1 * x2 + x2q)) * sign(x1 - x2)) / (2 * r * sqrt(rq * (y1q - 2 * y1 * y2 + y2q) + rq * (x1q - 2 * x1 * x2 + x2q)));
            double y0B = (r * sqrt(rq * (4 * rq - y1q + y2 * (2 * y1 - y2)) - rq * (x1q - 2 * x1 * x2 + x2q)) * abs(x1 - x2) + r * (y1 + y2) * sqrt(rq * (y1q - 2 * y1 * y2 + y2q) + rq * (x1q - 2 * x1 * x2 + x2q))) / (2 * r * sqrt(rq * (y1q - 2 * y1 * y2 + y2q) + rq * (x1q - 2 * x1 * x2 + x2q)));

            double startA = Math.atan2(y1 - y0A, x1 - x0A);
            double endA = Math.atan2(y2 - y0A, x2 - x0A);

            double startB = Math.atan2(y1 - y0B, x1 - x0B);
            double endB = Math.atan2(y2 - y0B, x2 - x0B);

            if (sweep) {
                if (endA < startA) endA += 2 * Math.PI;
                if (endB < startB) endB += 2 * Math.PI;
            } else {
                if (endA > startA) endA -= 2 * Math.PI;
                if (endB > startB) endB -= 2 * Math.PI;
            }

            double sizeA = Math.abs(startA - endA);
            double sizeB = Math.abs(startB - endB);

            double start = startA;
            double end = endA;
            double x0 = x0A;
            double y0 = y0A;
            if (large ^ (sizeA > sizeB)) {
                start = startB;
                end = endB;
                x0 = x0B;
                y0 = y0B;
            }

            // a full circle leads to 12 quadratic bezier curves
            int n = (int) Math.round(Math.abs(start - end) / (2 * Math.PI / 12));
            if (n < 1) n = 1;
            double delta = Math.abs(start - end) / n;
            if (!sweep) delta = -delta;

            double lastStart = start;
            start += delta;
            for (int i = 1; i < n; i++) {
                addArcPoint(p, lastStart, start, x0, y0, r, invert);
                lastStart = start;
                start += delta;
            }
            addArcPoint(p, lastStart, end, x0, y0, r, invert);

        } catch (SqrtException e) {
            p.add(pos);
        }
    }
    //CHECKSTYLE.ON: ParameterNumberCheck

    private void addArcPoint(Polygon p, double alpha0, double alpha1, double x0, double y0, float r, Transform tr) {
        final double mean = (alpha0 + alpha1) / 2;
        double rLong = r / Math.cos(Math.abs(alpha0 - alpha1) / 2);
        final VectorInterface c = new VectorFloat((float) (x0 + rLong * Math.cos(mean)), (float) (y0 + rLong * Math.sin(mean)));
        final VectorInterface p1 = new VectorFloat((float) (x0 + r * Math.cos(alpha1)), (float) (y0 + r * Math.sin(alpha1)));
        p.add(c.transform(tr), p1.transform(tr));
    }

    private static double sqrt(double x) throws SqrtException {
        if (x > 0)
            return Math.sqrt(x);
        if (x > -1e-6)
            return 0;

        throw new SqrtException();
    }

    private static double sign(double x) {
        return Math.signum(x);
    }

    private static double abs(double x) {
        return Math.abs(x);
    }

    private void addQuadraticWithReflect(Polygon poly, VectorInterface start, VectorInterface p) {
        VectorInterface c = start.add(start.sub(getLastC2()));
        poly.add(setLastC2(c), p);
    }

    private void addCubicWithReflect(Polygon poly, VectorInterface start, VectorInterface c2, VectorInterface p) {
        VectorInterface c1 = start.add(start.sub(getLastC3()));
        poly.add(c1, setLastC3(c2), p);
    }

    /**
     * The parser exception
     */
    public static final class ParserException extends Exception {
        private ParserException(String message) {
            super(message);
        }

        private ParserException(String message, Exception cause) {
            super(message, cause);
        }
    }

    /**
     * Parses a polygon.
     *
     * @return the polygon
     * @throws ParserException ParserException
     */
    public Polygon parsePolygon() throws ParserException {
        try {
            return parsePolygonPolyline(true);
        } catch (SVGTokenizer.TokenizerException e) {
            throw new ParserException("error parsing a polygon", e);
        }
    }

    /**
     * Parses a polyline.
     *
     * @return the polygon
     * @throws ParserException ParserException
     */
    public Polygon parsePolyline() throws ParserException {
        try {
            return parsePolygonPolyline(false);
        } catch (SVGTokenizer.TokenizerException e) {
            throw new ParserException("error parsing a polyline", e);
        }
    }

    private Polygon parsePolygonPolyline(boolean closed) throws SVGTokenizer.TokenizerException {
        Polygon p = new Polygon(closed);
        while (!t.isEOF())
            p.add(new VectorFloat(nextValue(), nextValue()));
        return p;
    }

    private static class SqrtException extends Exception {
    }
}

package de.neemann.digital.draw.graphics.svg;

import java.util.ArrayList;
import java.util.HashSet;

import org.w3c.dom.Element;

import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Vector;

/**
 * Representation of a SVG- Path
 * @author felix
 */
public class SVGPath implements SVGFragment, SVGDrawable {

    private ArrayList<Vector> corners = new ArrayList<Vector>();
    private SVGStyle style;
    private boolean closed = false;
    private final HashSet<Integer> isBezierStart = new HashSet<>();
    private Vector lastForCurve = null;

    /**
     * Creates a Path from XML
     * @param element
     *            the corresponding XML Element
     * @throws NoParsableSVGException
     *             if the SVG is not correct at this point
     */
    public SVGPath(Element element) throws NoParsableSVGException {
        corners.add(new Vector(0, 0));
        String[] d;
        try {
            style = new SVGStyle(element.getAttribute("style"));
            String tmp = element.getAttribute("d");
            tmp = tmp.replaceAll("-", " -");
            tmp = tmp.replaceAll("([0-9])([a-zA-Z])", "$1 $2");
            tmp = tmp.replaceAll("([a-zA-Z]) ([0-9])", "$1$2");
            tmp = tmp.replaceAll("([a-zA-Z]) -([0-9])", "$1-$2");
            tmp = tmp.replaceAll(",", " ");
            d = tmp.split(" ");
            ArrayList<String> part = new ArrayList<String>();
            for (String s : d) {
                if (!s.isEmpty()) {
                    if (s.substring(0, 1).matches("[a-zA-Z]") && !part.isEmpty()) {
                        manageTypes(part);
                        part = new ArrayList<String>();
                    }
                    part.add(s);
                }
            }
            if (!part.isEmpty())
                manageTypes(part);
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoParsableSVGException();
        }
    }

    /**
     * Divides the types of statements to individual Methods
     * @param b
     *            statement
     * @throws NoParsableSVGException
     *             if the statement is unknown
     */
    public void manageTypes(ArrayList<String> b) throws NoParsableSVGException {
        String statement = b.get(0).substring(0, 1);
        boolean abs = statement.matches("[A-Z]");
        b.set(0, b.get(0).replaceAll(statement, ""));
        for (int i = 0; i < b.size(); i++) {
            if (b.get(i) == null || b.get(i).isEmpty()) {
                b.remove(i);
            }
        }
        try {
            switch (statement.toLowerCase().charAt(0)) {
            case 'm':
                setMoveTo(getIntFromString(b.get(0)), getIntFromString(b.get(1)));
                return;
            case 'z':
                closePath();
                return;
            case 'l':
                for (int i = 0; i < b.size() - 1; i += 2) {
                    lineTo(getIntFromString(b.get(i)), getIntFromString(b.get(i + 1)), abs);
                }
                return;
            case 'h':
                horizontalLine(getIntFromString(b.get(0)), abs);
                return;
            case 'v':
                verticalLine(getIntFromString(b.get(0)), abs);
                return;
            case 'c':
                bezierCurve(b, abs, false);
                return;
            case 's':
                bezierCurve(b, abs, true);
                return;
            case 'q':
                bezierCurve(getIntFromString(b.get(0)), getIntFromString(b.get(1)), getIntFromString(b.get(2)),
                        getIntFromString(b.get(3)), abs);
                return;
            case 't':
                bezierCurve(lastForCurve.x, lastForCurve.y, getIntFromString(b.get(0)), getIntFromString(b.get(1)),
                        abs);
                return;
            case 'a':
                // TODO
                bezierCurve(b, abs, false);
                return;
            default:
                throw new NoParsableSVGException();
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoParsableSVGException();
        }
    }

    /**
     * Sets the Startvector
     * @param a
     *            x
     * @param b
     *            y
     */
    private void setMoveTo(int a, int b) {
        corners.set(0, new Vector(a, b));
    }

    /**
     * Draws a Line between the first and the Last Vector
     */
    private void closePath() {
        closed = true;
    }

    /**
     * Draws a Line to the given pos
     * @param a
     *            x
     * @param b
     *            y
     * @param abs
     *            Whether its an absolute pos
     */
    private void lineTo(int a, int b, boolean abs) {
        Vector v = new Vector(a, b);
        if (!abs)
            v = v.add(corners.get(corners.size() - 1));
        corners.add(v);
    }

    /**
     * draws a horizontal Line
     * @param length
     *            length of the line
     * @param abs
     *            Whether its an absolute pos
     */
    private void horizontalLine(int length, boolean abs) {
        lineTo(length, abs ? corners.get(corners.size() - 1).y : 0, abs);
    }

    /**
     * draws a vertical Line
     * @param length
     *            length of the line
     * @param abs
     *            Whether its an absolute pos
     */
    private void verticalLine(int length, boolean abs) {
        lineTo(abs ? corners.get(corners.size() - 1).x : 0, length, abs);
    }

    /**
     * Draws a qubic bezier Curve
     * @param x1
     *            x1
     * @param y1
     *            y1
     * @param x2
     *            x2
     * @param y2
     *            y2
     * @param x3
     *            x3
     * @param y3
     *            y3
     * @param abs
     *            whether they're absolute positions
     */
    private void bezierCurve(int x1, int y1, int x2, int y2, int x3, int y3, boolean abs) {
        System.out.println("Zeichne Bezierkurve: " + x1 + "/" + y1 + " " + x2 + "/" + y2 + " " + x3 + "/" + y3);
        Vector v1 = new Vector(x1, y1);
        Vector v2 = new Vector(x2, y2);
        Vector v3 = new Vector(x3, y3);
        if (!abs) {
            v1 = v1.add(corners.get(corners.size() - 1));
            v2 = v2.add(corners.get(corners.size() - 1));
            v3 = v3.add(corners.get(corners.size() - 1));
        }
        corners.add(v1);
        isBezierStart.add(corners.size() - 1);
        corners.add(v2);
        corners.add(v3);
        lastForCurve = v3;
    }

    /**
     * Creates a quadratic bezier curve
     * @param x1
     *            x1
     * @param y1
     *            y1
     * @param x2
     *            x2
     * @param y2
     *            y2
     * @param abs
     *            absolute Positions
     */
    private void bezierCurve(int x1, int y1, int x2, int y2, boolean abs) {
        int ax = corners.get(corners.size() - 1).x;
        int ay = corners.get(corners.size() - 1).y;
        int xn1 = ax + (2 / 3) * (x1 - ax);
        int yn1 = ay + (2 / 3) * (y1 - ay);
        int xn2 = x2 + (2 / 3) * (x1 - x2);
        int yn2 = y2 + (2 / 3) * (y1 - y2);
        bezierCurve(xn1, yn1, xn2, yn2, x2, y2, abs);
    }

    /**
     * Creates a pseudo-bezier Curve
     * @param points
     *            List of points as String
     * @param abs
     *            whether its about absolut positions
     * @param sht
     *            if its a smooth curve(starts with last Vector from last curve
     */
    private void bezierCurve(ArrayList<String> points, boolean abs, boolean sht) throws Exception {
        if (sht && points.size() == 4) {
            bezierCurve(lastForCurve.x, lastForCurve.y, getIntFromString(points.get(0)),
                    getIntFromString(points.get(1)), getIntFromString(points.get(2)), getIntFromString(points.get(3)),
                    abs);
        } else if (!sht && points.size() == 6) {
            bezierCurve(getIntFromString(points.get(0)), getIntFromString(points.get(1)),
                    getIntFromString(points.get(2)), getIntFromString(points.get(3)), getIntFromString(points.get(4)),
                    getIntFromString(points.get(5)), abs);
        } else {
            if (sht) {
                lineTo(lastForCurve.x, lastForCurve.y, abs);
            }
            for (int i = 0; i < points.size() - 1; i += 2) {
                lineTo(getIntFromString(points.get(i)), getIntFromString(points.get(i + 1)), abs);
            }
            lastForCurve = new Vector(getIntFromString(points.get(points.size() - 2)),
                    getIntFromString(points.get(points.size() - 1)));
        }

    }

    /**
     * Turns a String into an integer
     * @param inp
     *            input String
     * @return corresponding int
     */
    private int getIntFromString(String inp) {
        return (int) Double.parseDouble(inp);
    }

    @Override
    public SVGDrawable[] getDrawables() {
        return new SVGDrawable[] {
                this
        };
    }

    @Override
    public void draw(Graphic graphic) {
        Polygon p = new Polygon(closed);
        /**
         * for checkstyle...
         */
        int adder = 1;
        for (int i = 0; i < corners.size(); i += adder) {
            if (isBezierStart.contains(i)) {
                p.add(corners.get(i), corners.get(i + 1), corners.get(i + 2));
                adder = 3;
            } else {
                p.add(corners.get(i));
                adder = 1;
            }
        }
        if (style.getShallFilled())
            graphic.drawPolygon(p, style.getInnerStyle());
        if (style.getShallRanded())
            graphic.drawPolygon(p, style.getStyle());
    }
}

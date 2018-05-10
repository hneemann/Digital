/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.svg;

import java.util.ArrayList;
import java.util.HashSet;

import org.w3c.dom.Element;

import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.VectorFloat;
import de.neemann.digital.draw.graphics.VectorInterface;

/**
 * Representation of a SVG- Path
 * @author felix
 */
public class SVGPath implements SVGFragment, SVGDrawable {

    private ArrayList<VectorFloat> corners = new ArrayList<VectorFloat>();
    private SVGStyle style;
    private boolean closed = false;
    private final HashSet<Integer> isBezierStart = new HashSet<>();

    /**
     * Creates a Path from XML
     * @param element
     *            the corresponding XML Element
     * @throws NoParsableSVGException
     *             if the SVG is not correct at this point
     */
    public SVGPath(Element element) throws NoParsableSVGException {
        corners.add(new VectorFloat(0, 0));
        String[] d;
        try {
            style = new SVGStyle(element.getAttribute("style"));
            String tmp = element.getAttribute("d");
            tmp = tmp.replaceAll("-", " -");
            tmp = tmp.replaceAll("([0-9])([a-zA-Z])", "$1 $2");
            tmp = tmp.replaceAll("([a-zA-Z]) ([0-9])", "$1$2");
            tmp = tmp.replaceAll("([a-zA-Z]) -([0-9])", "$1-$2");
            tmp = tmp.replaceAll(",", " ");
            d = tmp.split("[ ]+");
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
                setMoveTo(getFloatFromString(b.get(0)), getFloatFromString(b.get(1)));
                if (b.size() < 4)
                    return;
                else {
                    b.remove(0);
                    b.remove(0);
                }
            case 'l':
                for (int i = 0; i < b.size() - 1; i += 2) {
                    lineTo(getFloatFromString(b.get(i)), getFloatFromString(b.get(i + 1)), abs);
                }
                return;
            case 'z':
                closePath();
                return;
            case 'h':
                horizontalLine(getFloatFromString(b.get(0)), abs);
                return;
            case 'v':
                verticalLine(getFloatFromString(b.get(0)), abs);
                return;
            case 'c':
                for (int i = 0; i < b.size() - 5; i += 6) {
                    bezierCurve(getFloatFromString(b.get(i)), getFloatFromString(b.get(i + 1)),
                            getFloatFromString(b.get(i + 2)), getFloatFromString(b.get(i + 3)),
                            getFloatFromString(b.get(i + 4)), getFloatFromString(b.get(i + 5)),
                            abs);
                }
                return;
            case 's':
                for (int i = 0; i < b.size() - 3; i += 4) {
                    bezierCurve(
                            2 * corners.get(corners.size() - 1).getXFloat()
                                    - corners.get(corners.size() - 2).getXFloat(),
                            2 * corners.get(corners.size() - 1).getYFloat()
                                    - corners.get(corners.size() - 2).getYFloat(),
                            getFloatFromString(b.get(i + 0)), getFloatFromString(b.get(i + 1)),
                            getFloatFromString(b.get(i + 2)), getFloatFromString(b.get(i + 3)),
                            abs);
                }
                return;
            case 'q':
                for (int i = 0; i < b.size() - 3; i += 4) {
                    bezierCurve(getFloatFromString(b.get(i + 0)), getFloatFromString(b.get(i + 1)),
                            getFloatFromString(b.get(i + 2)), getFloatFromString(b.get(i + 3)),
                            abs);
                }
                return;
            case 't':
                for (int i = 0; i < b.size() - 1; i += 2) {
                    bezierCurve(
                            2 * corners.get(corners.size() - 1).getXFloat()
                                    - corners.get(corners.size() - 2).getXFloat(),
                            2 * corners.get(corners.size() - 1).getYFloat()
                                    - corners.get(corners.size() - 2).getYFloat(),
                            getFloatFromString(b.get(i + 0)), getFloatFromString(b.get(i + 1)),
                            abs);
                }
                return;
            case 'a':
                ellipse(b, abs);
                return;
            default:
                System.err
                        .println("Unknown Control Sequence: " + statement.toLowerCase().charAt(0));
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
    private void setMoveTo(float a, float b) {
        corners.set(0, new VectorFloat(a, b));
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
    private void lineTo(float a, float b, boolean abs) {
        VectorFloat v = new VectorFloat(a, b);
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
    private void horizontalLine(float length, boolean abs) {
        lineTo(length, abs ? corners.get(corners.size() - 1).getYFloat() : 0, abs);
    }

    /**
     * draws a vertical Line
     * @param length
     *            length of the line
     * @param abs
     *            Whether its an absolute pos
     */
    private void verticalLine(float length, boolean abs) {
        lineTo(abs ? corners.get(corners.size() - 1).getXFloat() : 0, length, abs);
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
    private void bezierCurve(float x1, float y1, float x2, float y2, float x3, float y3,
            boolean abs) {
        VectorFloat v1 = new VectorFloat(x1, y1);
        VectorFloat v2 = new VectorFloat(x2, y2);
        VectorFloat v3 = new VectorFloat(x3, y3);
        if (!abs) {
            v1 = v1.add(corners.get(corners.size() - 1));
            v2 = v2.add(corners.get(corners.size() - 1));
            v3 = v3.add(corners.get(corners.size() - 1));
        }
        corners.add(v1);
        isBezierStart.add(corners.size() - 1);
        corners.add(v2);
        corners.add(v3);
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
    private void bezierCurve(float x1, float y1, float x2, float y2, boolean abs) {
        VectorFloat start = corners.get(corners.size() - 1);
        VectorFloat controlQ = new VectorFloat(x1, y1);
        VectorFloat end = new VectorFloat(x2, y2);
        if (!abs) {
            controlQ = controlQ.add(start);
            end = end.add(start);
        }
        VectorFloat controlC1 = start.add(controlQ.sub(start).mul(2).div(3));
        VectorFloat controlC2 = end.add(controlQ.sub(end).mul(2).div(3));
        bezierCurve(controlC1.getXFloat(), controlC1.getYFloat(), controlC2.getXFloat(),
                controlC2.getYFloat(), end.getXFloat(), end.getYFloat(), true);
    }

    /**
     * Creates a ellipse TODO
     * @param param
     *            List of parameters
     * @param abs
     *            absolute values
     */
    private void ellipse(ArrayList<String> param, boolean abs) {
        ArrayList<Float> p = new ArrayList<Float>();
        for (String s : param) {
            p.add(getFloatFromString(s));
        }
        for (int i = 0; i < p.size() - 6; i += 7) {
            float rx = Math.abs(p.get(i));
            float ry = Math.abs(p.get(i + 1));
            float rot = p.get(i + 2) % 360;
            boolean large = p.get(i + 3) == 1;
            boolean sweep = p.get(i + 4) == 1;
            VectorFloat start = corners.get(corners.size() - 1);
            VectorFloat end = new VectorFloat(p.get(i + 5), p.get(i + 6));
            ellipse(rx, ry, rot, large, sweep, end, start, abs);
        }
    }

    /**
     * Approximation of an ellipse. Skips the ellipse
     * @param rx
     *            radius x
     * @param ry
     *            radius y
     * @param rot
     *            rotation
     * @param large
     *            Flag for a large arc
     * @param sweep
     *            Flag for Sweep
     * @param end
     *            Endpoint
     * @param start
     *            Startpoint
     * @param abs
     *            Absolute Position
     */
    private void ellipse(float rx, float ry, float rot, boolean large, boolean sweep,
            VectorFloat end, VectorFloat start, boolean abs) {
        if (end.getXFloat() == start.getXFloat() && end.getYFloat() == start.getYFloat())
            return;
        if(!abs)
            end= end.add(start);
        corners.add(end);
    }

    /**
     * Turns a String into an integer
     * @param inp
     *            input String
     * @return corresponding int
     */
    private float getFloatFromString(String inp) {
        return Float.parseFloat(inp);
    }

    @Override
    public SVGDrawable[] getDrawables() {
        return new SVGDrawable[] {this};
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

    @Override
    public VectorFloat getPos() {
        return corners.get(0);
    }

    @Override
    public void move(VectorFloat diff) {
        ArrayList<VectorFloat> tmp = new ArrayList<VectorFloat>();
        for (VectorInterface v : corners) {
            tmp.add(new VectorFloat(v.getXFloat() - diff.getXFloat(),
                    v.getYFloat() - diff.getYFloat()));
        }
        corners = tmp;
    }

    @Override
    public void scale(double faktor) {
        ArrayList<VectorFloat> tmp = new ArrayList<VectorFloat>();
        for (VectorInterface v : corners) {
            tmp.add(new VectorFloat((float) (v.getXFloat() * faktor),
                    (float) (v.getYFloat() * faktor)));
        }
        corners = tmp;
    }
}

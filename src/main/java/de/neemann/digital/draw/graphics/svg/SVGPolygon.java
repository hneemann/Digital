/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.svg;

import java.util.ArrayList;

import org.w3c.dom.Element;

import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Transform;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.graphics.VectorFloat;
import de.neemann.digital.draw.graphics.VectorInterface;

/**
 * Representation of a SVG Polygon
 * @author felix
 */
public class SVGPolygon implements SVGFragment, SVGDrawable {

    private ArrayList<VectorInterface> corners;
    private SVGStyle style;
    private final boolean closed;

    /**
     * Creates a SVGPolygon
     * @param element
     *            XML Element
     * @param closed
     *            whether its closed
     */
    public SVGPolygon(Element element, boolean closed) {
        this.closed = closed;
        corners = new ArrayList<VectorInterface>();
        style = new SVGStyle(element.getAttribute("style"));
        String[] points = element.getAttribute("points").split(" ");
        for (String s : points) {
            String[] tmp = s.split(",");
            corners.add(new VectorFloat(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1])));
        }
    }

    /**
     * Creates a SVGPolygon from a XML Element
     * @param element
     *            XML Element
     */
    public SVGPolygon(Element element) {
        this(element, true);
    }

    /**
     * Creates a SVGPolygon from given values
     * @param corners
     *            List of the corners
     * @param style
     *            style of the polygon
     */
    public SVGPolygon(ArrayList<VectorInterface> corners, SVGStyle style) {
        this.corners = corners;
        this.style = style;
        closed = true;
    }

    @Override
    public SVGDrawable[] getDrawables() {
        return new SVGPolygon[] {this};
    }

    @Override
    public void draw(Graphic graphic) {
        Polygon p = new Polygon(corners, closed);
        if (style.getShallFilled()) {
            graphic.drawPolygon(p, style.getInnerStyle());
        }
        if (style.getShallRanded())
            graphic.drawPolygon(p, style.getStyle());
    }

    @Override
    public VectorInterface getPos() {
        return corners.get(0);
    }

    @Override
    public void move(Vector diff) {
        ArrayList<VectorInterface> tmp = new ArrayList<VectorInterface>();
        for (VectorInterface v : corners) {
            tmp.add(new VectorFloat(v.getXFloat() - diff.getX(), v.getYFloat() - diff.getY()));
        }
        corners = tmp;
    }
}

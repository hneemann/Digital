/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.svg;

import java.util.ArrayList;

import org.w3c.dom.Element;

import de.neemann.digital.draw.graphics.VectorFloat;
import de.neemann.digital.draw.graphics.VectorInterface;

/**
 * Representation and parsing logic of a rectangle
 * @author felix
 */
public class SVGRectangle implements SVGFragment {
    private float x = 0;
    private float y = 0;
    private float rx = 0;
    private float ry = 0;
    private float width = 0;
    private float height = 0;
    private SVGStyle style;

    /**
     * Creates a SVGRectangle from the corresponding XML-Tag
     * @param element
     *            XML-Tag from SVG
     * @throws NoParsableSVGException
     *             if the SVG is not valid at this point
     */
    public SVGRectangle(Element element) throws NoParsableSVGException {
        try {
            if (!element.getAttribute("width").isEmpty())
                width = Float.parseFloat(element.getAttribute("width"));
            if (!element.getAttribute("height").isEmpty())
                height = Float.parseFloat(element.getAttribute("height"));
            if (!element.getAttribute("x").isEmpty())
                x = Float.parseFloat(element.getAttribute("x"));
            if (!element.getAttribute("y").isEmpty())
                y = Float.parseFloat(element.getAttribute("y"));
            if (!element.getAttribute("rx").isEmpty())
                rx = Float.parseFloat(element.getAttribute("rx"));
            if (!element.getAttribute("ry").isEmpty())
                ry = Float.parseFloat(element.getAttribute("ry"));
            style = new SVGStyle(element.getAttribute("style"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoParsableSVGException();
        }

    }

    /**
     * calulates a Polygon width the information from the SVG File. Rounded corners will be flatten
     * down
     * @return Polygon
     */
    private SVGPolygon calculatePolygon() {
        ArrayList<VectorInterface> corners = new ArrayList<VectorInterface>();
        if (rx * ry != 0) {
            float w = width - 2 * rx;
            float h = height - 2 * ry;
            corners.add(new VectorFloat(x + rx, y));
            corners.add(new VectorFloat(x + rx + w, y));
            corners.add(new VectorFloat(x + width, y + ry));
            corners.add(new VectorFloat(x + width, y + ry + h));
            corners.add(new VectorFloat(x + rx + w, y + height));
            corners.add(new VectorFloat(x + rx, y + height));
            corners.add(new VectorFloat(x, y + ry + h));
            corners.add(new VectorFloat(x, y + ry));
        } else {
            corners.add(new VectorFloat(x, y));
            corners.add(new VectorFloat(x + width, y));
            corners.add(new VectorFloat(x + width, y + height));
            corners.add(new VectorFloat(x, y + height));
        }
        return new SVGPolygon(corners, style);
    }

    @Override
    public SVGPolygon[] getDrawables() {
        return new SVGPolygon[] {calculatePolygon()};
    }

    @Override
    public VectorInterface getPos() {
        return new VectorFloat(x, y);
    }

    @Override
    public synchronized void move(VectorFloat diff) {
        x -= diff.getXFloat();
        y -= diff.getYFloat();
//        rx -= diff.getXFloat();
//        ry -= diff.getYFloat();
    }

    @Override
    public void scale(double faktor) {
        x *= faktor;
        y *= faktor;
        rx *= faktor;
        ry *= faktor;
        width*=faktor;
        height*=faktor;
    }
}

/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.svg;

import java.util.ArrayList;

import org.w3c.dom.Element;

import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.graphics.VectorFloat;
import de.neemann.digital.draw.graphics.VectorInterface;

/**
 * Representation and parsing logic of a rectangle
 * @author felix
 */
public class SVGRectangle implements SVGFragment {
    private int x = 0;
    private int y = 0;
    private int rx = 0;
    private int ry = 0;
    private int width = 0;
    private int height = 0;
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
            width = (int) Double.parseDouble(element.getAttribute("width"));
            height = (int) Double.parseDouble(element.getAttribute("height"));
            x = (int) Double.parseDouble(element.getAttribute("x"));
            y = (int) Double.parseDouble(element.getAttribute("y"));
            if (!element.getAttribute("rx").isEmpty())
                rx = (int) Double.parseDouble(element.getAttribute("rx"));
            if (!element.getAttribute("ry").isEmpty())
                ry = (int) Double.parseDouble(element.getAttribute("ry"));
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
            int w = width - 2 * rx;
            int h = height - 2 * ry;
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
    public void move(Vector diff) {
        x -= diff.getX();
        y -= diff.getY();
    }
}

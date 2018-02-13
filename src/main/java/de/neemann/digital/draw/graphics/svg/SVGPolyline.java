package de.neemann.digital.draw.graphics.svg;

import java.util.ArrayList;

import org.w3c.dom.Element;

import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;

/**
 * Representation of a SVG Polyline
 * @author felix
 */
public class SVGPolyline implements SVGFragment, Drawable {
    private ArrayList<Vector> corners;

    /**
     * Creates a SVGPolyline from a XML Element
     * @param element
     *            the XML Element
     */
    public SVGPolyline(Element element) {
        String[] points = element.getAttribute("points").split(" ");
        for (String s : points) {
            String[] tmp = s.split(",");
            corners.add(new Vector(Integer.parseInt(tmp[0]), Integer.parseInt(tmp[1])));
        }
    }

    @Override
    public Drawable[] getDrawables() {
        return new Drawable[] {
                this
        };
    }

    @Override
    public void draw(Graphic graphic, Vector pos) {
        for (int i = 0; i < corners.size(); i++) {
            corners.set(i, corners.get(i).add(pos));
        }
        Polygon p = new Polygon(corners, false);
        graphic.drawPolygon(p, Style.NORMAL);
    }
}

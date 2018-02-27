package de.neemann.digital.draw.shapes.custom;

import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.graphics.svg.SVGPseudoPin;

/**
 * Draws a Graphic into a custom shape
 * @author felix
 */
public class CustomShapeDrawer implements Graphic {

    private CustomShapeDescription svg = new CustomShapeDescription();

    @Override
    public void drawLine(Vector p1, Vector p2, Style style) {
        svg = svg.addLine(p1, p2, style);
    }

    @Override
    public void drawPolygon(Polygon p, Style style) {
        svg = svg.addPolygon(p, style);
    }

    @Override
    public void drawCircle(Vector p1, Vector p2, Style style) {
        svg = svg.addCircle(p1, p2, style);
    }

    @Override
    public void drawText(Vector p1, Vector p2, String text, Orientation orientation, Style style) {
        svg = svg.addText(p1, p2, text, orientation, style);
    }

    /**
     * Adds a Pin to the Description
     * @param p
     *            Pseudopin
     */
    public void addPin(SVGPseudoPin p) {
        svg = svg.addPin(p);
    }

    /**
     * Gets the Description
     * @return Description
     */
    public CustomShapeDescription getSvg() {
        return svg;
    }

    /**
     * Sets the Description
     * @param svg
     *            Description
     */
    public void setSvg(CustomShapeDescription svg) {
        this.svg = svg;
    }
}

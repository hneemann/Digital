/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes.custom;

import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Polygon;
import de.neemann.digital.draw.graphics.Style;
import de.neemann.digital.draw.graphics.VectorInterface;
import de.neemann.digital.draw.graphics.svg.ImportSVG;
import de.neemann.digital.draw.graphics.svg.SVGPseudoPin;

/**
 * Draws a Graphic into a custom shape
 * @author felix
 */
public class CustomShapeDrawer implements Graphic {

    private CustomShapeDescription svg = new CustomShapeDescription();

    @Override
    public void drawLine(VectorInterface p1, VectorInterface p2, Style style) {
        svg = svg.addLine(ImportSVG.toOldschoolVector(p1), ImportSVG.toOldschoolVector(p1),
                (int) style.getThickness(), style.getColor());
    }

    @Override
    public void drawPolygon(Polygon p, Style style) {
        svg = svg.addPolygon(p, style.getThickness(), style.getColor(), style.isFilled());
    }

    @Override
    public void drawCircle(VectorInterface p1, VectorInterface p2, Style style) {
        svg = svg.addCircle(ImportSVG.toOldschoolVector(p1), ImportSVG.toOldschoolVector(p1),
                style.getThickness(), style.getColor(), style.isFilled());
    }

    @Override
    public void drawText(VectorInterface p1, VectorInterface p2, String text,
            Orientation orientation, Style style) {
        svg = svg.addText(ImportSVG.toOldschoolVector(p1), ImportSVG.toOldschoolVector(p1), text,
                orientation, style.getFontSize(), style.getColor());
    }

    /**
     * Adds a Pin to the Description
     * @param p
     *            Pseudopin
     */
    public void addPin(SVGPseudoPin p) {
        svg = svg.addPin(p.getLabel(), p.getPos(), p.isShowLabel());
        svg.getInOut().put(p.getLabel(), p.isInput());
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

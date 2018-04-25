/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.svg;

import org.w3c.dom.Element;

import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.VectorFloat;

/**
 * Representation of a SVG-Line
 * @author felix
 */
public class SVGLine implements SVGFragment, SVGDrawable {
    private VectorFloat a;
    private VectorFloat b;
    private SVGStyle style;

    /**
     * Creates a Line from a XML-Element
     * @param element
     *            XML Element
     * @throws NoParsableSVGException
     *             if the Element is not valid
     */
    public SVGLine(Element element) throws NoParsableSVGException {
        try {
            a = new VectorFloat(Float.parseFloat(element.getAttribute("x1")),
                    Float.parseFloat(element.getAttribute("y1")));
            b = new VectorFloat(Float.parseFloat(element.getAttribute("x2")),
                    Float.parseFloat(element.getAttribute("y2")));
            style = new SVGStyle(element.getAttribute("style"));
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoParsableSVGException();
        }
    }

    /**
     * Creates a SVGLine with given parameters
     * @param a
     *            Vector to start
     * @param b
     *            Vector to end
     * @param style
     *            Style of the Line
     */
    public SVGLine(VectorFloat a, VectorFloat b, SVGStyle style) {
        this.a = a;
        this.b = b;
        this.style = style;
    }

    @Override
    public SVGDrawable[] getDrawables() {
        return new SVGDrawable[] {this};
    }

    @Override
    public void draw(Graphic graphic) {
        graphic.drawLine(ImportSVG.toOldschoolVector(a), ImportSVG.toOldschoolVector(b),
                style.getStyle());
    }

    @Override
    public VectorFloat getPos() {
        return a;
    }

    @Override
    public void move(VectorFloat diff) {
        a = a.sub(diff);
        b = b.sub(diff);
    }

    @Override
    public void scale(double faktor) {
        a = a.mul((float) faktor);
        b = b.mul((float) faktor);
    }
}

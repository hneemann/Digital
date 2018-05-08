/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.svg;

import java.util.ArrayList;

import org.w3c.dom.Element;

import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.graphics.VectorFloat;

/**
 * Representation of the SVG-Ellipse
 * @author felix
 */
public class SVGEllipse implements SVGFragment, SVGDrawable, SVGPinnable {

    private VectorFloat oben;
    private VectorFloat unten;
    private SVGStyle style;
    private Pins pins;
    private boolean pin;
    private ArrayList<SVGPseudoPin> pseudoPins;
    private SVGPseudoPin me = null;

    /**
     * Creates an ellipse from xml
     * @param element
     *            XML Element
     * @param pins
     *            Necessary Pins
     * @param pseudoPins
     *            PseudoPins
     * @throws NoParsableSVGException
     *             if the SVG's not valid
     */
    public SVGEllipse(Element element, Pins pins, ArrayList<SVGPseudoPin> pseudoPins)
            throws NoParsableSVGException {
        this.pins = pins;
        this.pseudoPins = pseudoPins;
        try {
            int r = 0;
            int rx;
            int ry;
            if (element.hasAttribute("r")) {
                r = (int) Double.parseDouble(element.getAttribute("r"));
            }
            if (element.hasAttribute("rx")) {
                rx = (int) Double.parseDouble(element.getAttribute("rx"));
                ry = (int) Double.parseDouble(element.getAttribute("ry"));
            } else {
                rx = r;
                ry = r;
            }
            int cx = 0;
            int cy = 0;
            if (!element.getAttribute("cx").isEmpty())
                cx = (int) Double.parseDouble(element.getAttribute("cx"));
            if (!element.getAttribute("cx").isEmpty())
                cy = (int) Double.parseDouble(element.getAttribute("cy"));
            style = new SVGStyle(element.getAttribute("style"));
            pin = checkAndInsertPins(element.getAttribute("id"), cx, cy);
            oben = new VectorFloat(cx - rx, cy - ry);
            unten = new VectorFloat(cx + rx, cy + ry);
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoParsableSVGException();
        }
    }

    /**
     * Creates a Ellipse from predefined values
     * @param oben
     *            Upper left Vector
     * @param unten
     *            Lower right Vector
     * @param style
     *            Style of the Ellipse
     */
    public SVGEllipse(VectorFloat oben, VectorFloat unten, SVGStyle style) {
        this.oben = oben;
        this.unten = unten;
        this.style = style;
    }

    /**
     * Ellipse with Legacy Vectors
     * @param oben
     *            Upper Left
     * @param unten
     *            Lower Right
     * @param svgStyle
     *            Style
     */
    public SVGEllipse(Vector oben, Vector unten, SVGStyle svgStyle) {
        this(new VectorFloat(oben.getX(), oben.getY()), new VectorFloat(unten.getX(), unten.getY()),
                svgStyle);
    }

    /**
     * Checks, if a circle appears to be a Pin and appends a new Pin to the circuit, if thats the
     * case
     * @param s
     *            String "id" from the SVG
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @return Pin or not Pin
     * @throws NoParsableSVGException
     *             if the ID is not valid
     */
    private boolean checkAndInsertPins(String s, int x, int y) throws NoParsableSVGException {
        try {
            String[] tmp = s.split(":", 3);
            s = tmp[0].toLowerCase();
            boolean showLabel = true;
            String showString = tmp[1].toLowerCase();
            if (tmp.length > 2 && (showString.startsWith("f") || showString.startsWith("n"))) {
                showLabel = false;
            }
            String label = tmp[tmp.length - 1];
            if (s.startsWith("input") || s.startsWith("i")) {
                pseudoPins.add(new SVGPseudoPin(new Vector(x, y), label, true, pins, showLabel));
                me = pseudoPins.get(pseudoPins.size() - 1);
                return true;
            }
            if (s.startsWith("output") || s.startsWith("o")) {
                pseudoPins.add(new SVGPseudoPin(new Vector(x, y), label, false, pins, showLabel));
                me = pseudoPins.get(pseudoPins.size() - 1);
                return true;
            }
            return false;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public SVGDrawable[] getDrawables() {
        if (pin) {
            return new SVGDrawable[] {};
        }
        return new SVGDrawable[] {this};
    }

    @Override
    public void draw(Graphic graphic) {
        if (!isPin()) {
            if (style.getShallFilled()) {
                graphic.drawCircle(ImportSVG.toOldschoolVector(oben),
                        ImportSVG.toOldschoolVector(unten), style.getInnerStyle());
            }
            if (style.getShallRanded())
                graphic.drawCircle(ImportSVG.toOldschoolVector(oben),
                        ImportSVG.toOldschoolVector(unten), style.getStyle());
        }
    }

    @Override
    public VectorFloat getPos() {
        return oben;
    }

    @Override
    public boolean isPin() {
        return pin;
    }

    @Override
    public SVGPseudoPin[] getPin() {
        return new SVGPseudoPin[] {me};
    }

    @Override
    public void move(VectorFloat diff) {
        oben = oben.sub(diff);
        unten = unten.sub(diff);
    }

    @Override
    public void scale(double faktor) {
        oben = oben.mul((float) faktor);
        unten = unten.mul((float) faktor);
    }
}

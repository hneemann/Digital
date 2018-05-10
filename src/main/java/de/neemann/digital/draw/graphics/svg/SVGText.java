/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.svg;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Vector;
import de.neemann.digital.draw.graphics.VectorFloat;

/**
 * Representation of a SVG Text
 * @author felix
 */
public class SVGText implements SVGFragment, SVGDrawable {
    private VectorFloat p1;
    private VectorFloat p2;
    private String text;
    private Orientation orientation;
    private SVGStyle style;
    private SVGText[] texts;

    /**
     * Creates a Text from an element
     * @param element
     *            The corresponding XML element
     * @throws NoParsableSVGException
     *             if the element's not valid
     */
    public SVGText(Element element) throws NoParsableSVGException {
        this(element, null);
    }

    /**
     * Creates a Text from an element
     * @param element
     *            Corresponding XML element
     * @param style
     *            Parent Style
     * @throws NoParsableSVGException
     *             if the element is not valid
     */
    public SVGText(Element element, SVGStyle style) throws NoParsableSVGException {
        try {
            p1 = new VectorFloat(Float.parseFloat(element.getAttribute("x")),
                    Float.parseFloat(element.getAttribute("y")));
            p2 = p1.sub(new Vector(1, 0));
            if (style == null) {
                this.style = new SVGStyle();
                this.style.setFill("000000");
                this.style.setStyleString(element.getAttribute("style"));
            } else {
                this.style = style;
            }
            NodeList gList = element.getElementsByTagName("*");
            if (gList.getLength() > 0) {
                ArrayList<SVGText> txt = new ArrayList<SVGText>();
                for (int i = 0; i < gList.getLength(); i++) {
                    SVGText tmp = new SVGText((Element) gList.item(i), this.style);
                    tmp.setPos(p1);
                    txt.add(tmp);
                }
                texts = txt.toArray(new SVGText[txt.size()]);
            } else {
                text = element.getTextContent();
            }
            orientation = getOrientation(element.getAttribute("transform"));

        } catch (Exception e) {
            e.printStackTrace();
            throw new NoParsableSVGException();
        }
    }

    /**
     * Adds a Vector
     * @param pos
     *            Vector to add
     */
    public void setPos(VectorFloat pos) {
        // pos = new VectorFloat(pos.getXFloat(), 0);
        // p1 = p1.add(pos);
        // p2 = p2.add(pos);
        p1 = new VectorFloat(pos);
        p2 = p1.sub(new Vector(1, 0));
    }

    /**
     * Parse the Transform Attribute to calculate the rotation
     * @param translate
     *            String from XML
     * @return Orientation of the Text
     * @throws NoParsableSVGException
     *             if the String is not valid
     */
    private Orientation getOrientation(String translate) throws NoParsableSVGException {
        if (translate.isEmpty()) {
            return Orientation.RIGHTTOP;
        }
        if (translate.matches("rotate\\([0-9]*( [0-9]+,[0-9]+)?\\)")) {
            translate = translate.substring(7, translate.length() - 1);
            String[] tmp = translate.split(" ");
            return Orientation.RIGHTTOP.rot(Integer.parseInt(tmp[0]));
        }
        throw new NoParsableSVGException();
    }

    @Override
    public SVGDrawable[] getDrawables() {
        if (texts == null)
            return new SVGDrawable[] {this};
        return texts;
    }

    @Override
    public void draw(Graphic graphic) {
        if (style.getShallFilled()) {
            graphic.drawText(ImportSVG.toOldschoolVector(p1), ImportSVG.toOldschoolVector(p2), text,
                    orientation, style.getInnerStyle());
        }
        if (style.getShallRanded())
            graphic.drawText(ImportSVG.toOldschoolVector(p1), ImportSVG.toOldschoolVector(p2), text,
                    orientation, style.getStyle());
    }

    @Override
    public VectorFloat getPos() {
        return p1;
    }

    @Override
    public void move(VectorFloat diff) {
        p1 = p1.sub(diff);
        p2 = p2.sub(diff);
        if (texts != null)
            for (SVGText t : texts) {
                if (t != null)
                    t.move(diff);
            }
    }

    @Override
    public void scale(double faktor) {
        p1 = p1.mul((float) faktor);
        p2 = p2.mul((float) faktor);
        style.setFontSize(style.getFontSize() * Math.sqrt(faktor));
        if (texts != null)
            for (SVGText t : texts) {
                if (t != null)
                    t.scale(faktor);
            }
    }
}

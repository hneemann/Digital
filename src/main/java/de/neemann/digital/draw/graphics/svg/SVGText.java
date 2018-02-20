package de.neemann.digital.draw.graphics.svg;

import java.util.ArrayList;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Orientation;
import de.neemann.digital.draw.graphics.Vector;

/**
 * Representation of a SVG Text
 * @author felix
 */
public class SVGText implements SVGFragment, SVGDrawable {
    private Vector p1;
    private Vector p2;
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
            p1 = new Vector((int) Double.parseDouble(element.getAttribute("x")),
                    (int) Double.parseDouble(element.getAttribute("y")));
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
    public void setPos(Vector pos) {
        pos = new Vector(pos.x, 0);
        p1 = p1.add(pos);
        p2 = p2.add(pos);
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
            return new SVGDrawable[] {
                    this
            };
        return texts;
    }

    @Override
    public void draw(Graphic graphic) {
        if (style.getShallFilled()) {
            graphic.drawText(p1, p2, text, orientation, style.getInnerStyle());
        }
        if (style.getShallRanded())
            graphic.drawText(p1, p2, text, orientation, style.getStyle());
    }

    @Override
    public Vector getPos() {
        return p1;
    }
}

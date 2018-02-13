package de.neemann.digital.draw.graphics.svg;

import org.w3c.dom.Element;

import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Vector;

/**
 * Representation of the SVG-Ellipse
 * @author felix
 */
public class SVGEllipse implements SVGFragment, Drawable {

    private Vector oben;
    private Vector unten;
    private SVGStyle style;

    /**
     * Creates an ellipse from XML
     * @param element
     *            XML Element
     */
    public SVGEllipse(Element element) {
        System.out.println("Ellipse");
    }

    /**
     * Creates an Ellipse with predefined Values
     * @param x
     *            Upper Left Corner - X
     * @param y
     *            Upper Left Corner - Y
     * @param x2
     *            Lower Right Corner - X
     * @param y2
     *            Lower Right Corner - Y
     * @param style
     *            The Style of the Ellipse
     */
    public SVGEllipse(int x, int y, int x2, int y2, SVGStyle style) {
        oben = new Vector(x, y);
        unten = new Vector(x2, y2);
        this.style = style;
    }

    @Override
    public Drawable[] getDrawables() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void draw(Graphic graphic, Vector pos) {
        oben = oben.add(pos);
        unten = unten.add(pos);
        if (style.getShallFilled()) {
            graphic.drawCircle(oben, unten, style.getInnerStyle());
        }
        if (style.getShallRanded())
            graphic.drawCircle(oben, unten, style.getStyle());
    }
}

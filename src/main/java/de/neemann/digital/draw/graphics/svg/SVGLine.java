package de.neemann.digital.draw.graphics.svg;

import org.w3c.dom.Element;

import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Vector;

/**
 * Representation of a SVG-Line
 * @author felix
 */
public class SVGLine implements SVGFragment, SVGDrawable {
    private Vector a;
    private Vector b;
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
            a = new Vector((int) Double.parseDouble(element.getAttribute("x1")),
                    (int) Double.parseDouble(element.getAttribute("y1")));
            b = new Vector((int) Double.parseDouble(element.getAttribute("x2")),
                    (int) Double.parseDouble(element.getAttribute("y2")));
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
    public SVGLine(Vector a, Vector b, SVGStyle style) {
        this.a = a;
        this.b = b;
        this.style = style;
    }

    @Override
    public SVGDrawable[] getDrawables() {
        return new SVGDrawable[] {
                this
        };
    }

    @Override
    public void draw(Graphic graphic) {
        graphic.drawLine(a, b, style.getStyle());
    }

    @Override
    public Vector getPos() {
        return a;
    }
}

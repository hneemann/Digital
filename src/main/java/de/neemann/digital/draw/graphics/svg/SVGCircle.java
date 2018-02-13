package de.neemann.digital.draw.graphics.svg;

import org.w3c.dom.Element;

/**
 * Representation of a Circle
 */
public class SVGCircle implements SVGFragment {

    private int x;
    private int y;
    private int x2;
    private int y2;
    private SVGStyle style;

    /**
     * Creates a SVG Circle from the corresponding XML Element
     * @param element The XML Element
     * @throws NoParsableSVGException If the XML is not valid at this point
     */
    public SVGCircle(Element element) throws NoParsableSVGException {
        try {
            int r = (int) Double.parseDouble(element.getAttribute("r"));
            int cx = (int) Double.parseDouble(element.getAttribute("cx"));
            int cy = (int) Double.parseDouble(element.getAttribute("cy"));
            style = new SVGStyle(element.getAttribute("style"));
            x = cx - r;
            y = cy - r;
            x2 = cx + r;
            y2 = cy + r;
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoParsableSVGException();
        }
    }

    @Override
    public Drawable[] getDrawables() {
        return new Drawable[] {
                new SVGEllipse(x, y, x2, y2, style)
        };
    }

}

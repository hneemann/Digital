package de.neemann.digital.draw.graphics.svg;

import org.w3c.dom.Element;

import de.neemann.digital.draw.graphics.Graphic;

/**
 * Representation of a SVG-Line
 * @author felix
 */
public class SVGLine implements SVGFragment, SVGDrawable {

    /**
     * Creates a Line from a XML-Element
     * @param element
     *            XML Element
     */
    public SVGLine(Element element) {
        System.out.println("Linie");
    }

    @Override
    public SVGDrawable[] getDrawables() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void draw(Graphic graphic) {
        // TODO Auto-generated method stub

    }
}

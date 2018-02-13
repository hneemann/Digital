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
     */
    public SVGLine(Element element) {
        System.out.println("Linie");
    }
    
    public SVGLine(Vector a,Vector b, SVGStyle style) {
        this.a=a;
        this.b=b;
        this.style=style;
    }

    @Override
    public SVGDrawable[] getDrawables() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void draw(Graphic graphic) {
        graphic.drawLine(a, b, style.getStyle());
    }
}

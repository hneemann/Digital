package de.neemann.digital.draw.graphics.svg;

import org.w3c.dom.Element;

/**
 * Representation of a SVG- Path
 * @author felix
 */
public class SVGPath implements SVGFragment {

    /**
     * Creates a Path from XML
     * @param element
     *            the corresponding XML Element
     * @throws NoParsableSVGException
     *             if the SVG is not correct at this point
     */
    public SVGPath(Element element) throws NoParsableSVGException {
        String[] d;
        try {
            d = element.getAttribute("d").split(" ");
            for (String s : d) {
                System.out.println(s);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoParsableSVGException();
        }
    }

    @Override
    public SVGDrawable[] getDrawables() {
        // TODO Auto-generated method stub
        return null;
    }
}

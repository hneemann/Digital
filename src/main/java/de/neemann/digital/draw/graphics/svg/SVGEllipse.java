package de.neemann.digital.draw.graphics.svg;

import java.util.ArrayList;

import org.w3c.dom.Element;

import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Graphic;
import de.neemann.digital.draw.graphics.Vector;

/**
 * Representation of the SVG-Ellipse
 * @author felix
 */
public class SVGEllipse implements SVGFragment, SVGDrawable {

    private Vector oben;
    private Vector unten;
    private SVGStyle style;
    private Pins pins;
    private boolean pin;
    private PinDescriptions inputs;
    private PinDescriptions outputs;
    private ArrayList<SVGPseudoPin> pseudoPins;

    /**
     * Creates an ellipse from XML
     * @param element
     *            XML Element
     */
    public SVGEllipse(Element element, Pins pins, PinDescriptions inputs, PinDescriptions outputs, ArrayList<SVGPseudoPin> pseudoPins)
            throws NoParsableSVGException {
        this.pins = pins;
        this.inputs = inputs;
        this.outputs = outputs;
        this.pseudoPins=pseudoPins;
        try {
            int r = 0, rx, ry;
            if (element.hasAttribute("r")) {
                r = (int) Double.parseDouble(element.getAttribute("r"));
            }
            if (element.hasAttribute("rx")) {
                rx = (int) Double.parseDouble(element.getAttribute("rx"));
                ry = (int) Double.parseDouble(element.getAttribute("ry"));
            } else {
                rx = ry = r;
            }
            int cx = (int) Double.parseDouble(element.getAttribute("cx"));
            int cy = (int) Double.parseDouble(element.getAttribute("cy"));
            style = new SVGStyle(element.getAttribute("style"));
            pin = checkAndInsertPins(element.getAttribute("id"), cx, cy);
            if (!pin) {
                oben = new Vector(cx - rx, cy - ry);
                unten = new Vector(cx + rx, cy + ry);
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoParsableSVGException();
        }
    }

    public SVGEllipse(Vector oben, Vector unten, SVGStyle style) {
        this.oben = oben;
        this.unten = unten;
        this.style = style;
    }

    /**
     * Checks, if a circle appears to be a Pin and appends a new Pin to the circuit,
     * if thats the case
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
            s = s.toLowerCase();
            if (s.startsWith("input") || s.startsWith("i")) {
                int index = Integer.parseInt(s.replaceAll("[^0-9]*", ""));
                pseudoPins.add(new SVGPseudoPin(new Vector(x, y), inputs, index, true, pins, style));
                return true;
            }
            if (s.startsWith("output") || s.startsWith("o")) {
                int index = Integer.parseInt(s.replaceAll("[^0-9]*", ""));
                pseudoPins.add(new SVGPseudoPin(new Vector(x, y), outputs, index, false, pins, style));
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoParsableSVGException();
        }
    }

    @Override
    public SVGDrawable[] getDrawables() {
        if (pin) {
            ArrayList<SVGDrawable> list = new ArrayList<SVGDrawable>();
            for (SVGPseudoPin p : pseudoPins) {
                for (SVGDrawable d : p.getDrawables())
                    list.add(d);
            }
            return list.toArray(new SVGDrawable[list.size()]);
        }
        return new SVGDrawable[] {
                this
        };
    }

    @Override
    public void draw(Graphic graphic) {
        if (style.getShallFilled()) {
            graphic.drawCircle(oben, unten, style.getInnerStyle());
        }
        if (style.getShallRanded())
            graphic.drawCircle(oben, unten, style.getStyle());
    }
}

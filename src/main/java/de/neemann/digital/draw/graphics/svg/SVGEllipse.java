package de.neemann.digital.draw.graphics.svg;

import org.w3c.dom.Element;

import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.Pin;
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
    private SVGLine pinLine;

    /**
     * Creates an ellipse from XML
     * @param element
     *            XML Element
     */
    public SVGEllipse(Element element, Pins pins, PinDescriptions inputs, PinDescriptions outputs)
            throws NoParsableSVGException {
        this.pins = pins;
        this.inputs = inputs;
        this.outputs = outputs;
        try {
            int r=0, rx, ry;
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
                pins.add(new Pin(applyVectorToGrid(x, y), inputs.get(index)));
                return true;
            }
            if (s.startsWith("output") || s.startsWith("o")) {
                int index = Integer.parseInt(s.replaceAll("[^0-9]*", ""));
                pins.add(new Pin(applyVectorToGrid(x, y), outputs.get(index)));
                return true;
            }
            return false;
        } catch (Exception e) {
            e.printStackTrace();
            throw new NoParsableSVGException();
        }
    }

    /**
     * gives a Vector on the grid
     * @param x
     *            old x value
     * @param y
     *            old y value
     * @return ongrid-Vector
     */
    private Vector applyVectorToGrid(int x, int y) {
        int nx = (int) (Math.round(x / 20.0) * 20);
        int ny = (int) (Math.round(y / 20.0) * 20);
        Vector a = new Vector(x, y);
        Vector b = new Vector(nx, ny);
        if (!a.equals(b)) {
            style.setThickness("2");
            pinLine = new SVGLine(a, b, style);
        }
        return b;
    }

    @Override
    public SVGDrawable[] getDrawables() {
        if (pin) {
            return new SVGDrawable[] {
                    pinLine
            };
        }
        return new SVGDrawable[] {
                this
        };
    }

    @Override
    public void draw(Graphic graphic) {
        // oben = oben.add(pos);
        // unten = unten.add(pos);
        if (style.getShallFilled()) {
            graphic.drawCircle(oben, unten, style.getInnerStyle());
        }
        if (style.getShallRanded())
            graphic.drawCircle(oben, unten, style.getStyle());
    }
}

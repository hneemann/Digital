package de.neemann.digital.draw.graphics.svg;

import org.w3c.dom.Element;

import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Vector;

/**
 * Representation of a Circle
 */
public class SVGCircle implements SVGFragment {

    private int x;
    private int y;
    private int x2;
    private int y2;
    private SVGStyle style;
    private Pins pins;
    private boolean pin;
    private PinDescriptions inputs;
    private PinDescriptions outputs;

    /**
     * Creates a SVG Circle from the corresponding XML Element
     * @param element
     *            The XML Element
     * @param pins
     *            Pins of the circuit
     * @param inputs
     *            InputPins
     * @param outputs
     *            OutputPi
     * @throws NoParsableSVGException
     *             If the XML is not valid at this point
     */
    public SVGCircle(Element element, Pins pins, PinDescriptions inputs, PinDescriptions outputs)
            throws NoParsableSVGException {
        this.pins = pins;
        this.inputs = inputs;
        this.outputs = outputs;
        try {
            int r = (int) Double.parseDouble(element.getAttribute("r"));
            int cx = (int) Double.parseDouble(element.getAttribute("cx"));
            int cy = (int) Double.parseDouble(element.getAttribute("cy"));
            pin = checkAndInsertPins(element.getAttribute("id"), cx, cy);
            if (!pin) {
                style = new SVGStyle(element.getAttribute("style"));
                x = cx - r;
                y = cy - r;
                x2 = cx + r;
                y2 = cy + r;
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
                int index = Integer.parseInt(s.replaceAll("[a-zA-Z]*", ""));
                pins.add(new Pin(applyVectorToGrid(x, y), inputs.get(index)));
                return true;
            }
            if (s.startsWith("output") || s.startsWith("o")) {
                int index = Integer.parseInt(s.replaceAll("[a-zA-Z]*", ""));
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
        return new Vector((int) (Math.round(x / 20.0) * 20), (int) (Math.round(y / 20.0) * 20));
    }

    @Override
    public SVGDrawable[] getDrawables() {
        if (pin) {
            return new SVGDrawable[] {};
        }
        return new SVGDrawable[] {
                new SVGEllipse(x, y, x2, y2, style)
        };
    }

}

package de.neemann.digital.draw.graphics.svg;

import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Vector;

/**
 * Is used to define a Pin or, if the PinsDescriptions are not available, draw a
 * circle which looks like a Pin
 * @author felix
 */
public class SVGPseudoPin implements SVGFragment, SVGPinnable {

    private Vector pos;
    private int index;
    private boolean input;
    private Pins pins;
    private boolean descSet = false;
    private Vector originalPos;

    /**
     * Creates a PseudoPin
     * @param pos
     *            Vector where the Pin is located (Center)
     * @param index
     *            Number of the Pin
     * @param input
     *            if its a input Pin, false if its not
     * @param pins
     *            Pins of the circuit
     * @param style
     *            style of the Pin
     */
    public SVGPseudoPin(Vector pos, int index, boolean input, Pins pins, SVGStyle style) {
        this.index = index;
        this.input = input;
        this.pins = pins;
        this.pos = applyVectorToGrid(pos.x, pos.y);
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
        originalPos = new Vector(x, y);
        int nx = (int) (Math.round(x / 20.0) * 20);
        int ny = (int) (Math.round(y / 20.0) * 20);
        Vector b = new Vector(nx, ny);
        return b;
    }

    /**
     * Checks, whether the Pin is a Input-Pin
     * @return input
     */
    public boolean isInput() {
        return input;
    }

    /**
     * Sets the Pin Description
     * @param pinDesc
     *            Pin Description
     */
    public void setPinDesc(PinDescriptions pinDesc) {
        pins.add(new Pin(pos, pinDesc.get(index)));
        descSet = true;
    }

    @Override
    public SVGDrawable[] getDrawables() {
        String styleString;
        if (input)
            styleString = "fill:#0000ff";
        else
            styleString = "fill:#dd0000";
        SVGEllipse ellipse = new SVGEllipse(getPos().sub(new Vector(2, 2)), getPos().add(new Vector(2, 2)),
                new SVGStyle(styleString));
        return new SVGDrawable[] {
                ellipse
        };
    }

    /**
     * Returns true, if the given coordinates are within the Pin
     * @param x
     *            x coordinate
     * @param y
     *            y coordinate
     * @return contains
     */
    public boolean contains(int x, int y) {
        if (new Vector(x, y).inside(pos.sub(new Vector(2, 2)), pos.add(new Vector(2, 2))))
            return true;
        return false;
    }

    /**
     * Sets the pos of the Pin
     * @param pos
     *            pos
     */
    public void setPos(Vector pos) {
        this.pos = applyVectorToGrid(pos.x, pos.y);
    }

    /**
     * Says if the Pin is a real PIn
     * @return descSet
     */
    public boolean descSet() {
        return descSet;
    }

    @Override
    public Vector getPos() {
        return pos;
    }

    /**
     * Gets the original pos, as its defined in the SVG
     * @return original Pos
     */
    public Vector getOriginalPos() {
        return originalPos;
    }

    /**
     * Gets the Number of the Pin
     * @return index
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the Index
     * @param index
     *            Index
     */
    public void setIndex(int index) {
        this.index = index;
    }

    @Override
    public boolean isPin() {
        return true;
    }

    @Override
    public SVGPseudoPin[] getPin() {
        return new SVGPseudoPin[] {
                this
        };
    }
}

package de.neemann.digital.draw.graphics.svg;

import de.neemann.digital.core.element.PinDescriptions;
import de.neemann.digital.draw.elements.Pin;
import de.neemann.digital.draw.elements.Pins;
import de.neemann.digital.draw.graphics.Vector;

public class SVGPseudoPin implements SVGFragment {

    private Vector pos;
    private PinDescriptions pinDesc;
    private int index;
    private boolean input;
    private Pins pins;
    private SVGLine pinLine;
    private SVGStyle style;

    public SVGPseudoPin(Vector pos, PinDescriptions pinDesc, int index, boolean input, Pins pins, SVGStyle style) {
        this.pinDesc = pinDesc;
        this.index = index;
        this.input = input;
        this.pins = pins;
        this.style = style;
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

    public boolean isInput() {
        return input;
    }

    public void setPinDesc(PinDescriptions pinDesc) {
        this.pinDesc = pinDesc;
        pins.add(new Pin(pos, pinDesc.get(index)));
    }

    @Override
    public SVGDrawable[] getDrawables() {
        if (pinDesc != null) {
            return new SVGDrawable[] {
                    pinLine
            };
        }
        String styleString;
        if (input)
            styleString = "fill:#0000ff";
        else
            styleString = "fill:#dd0000";
        return new SVGDrawable[] {
                pinLine, new SVGEllipse(pos.sub(new Vector(2, 2)), pos.add(new Vector(2, 2)), new SVGStyle(styleString))
        };
    }
}

package de.neemann.digital.gui.draw.parts;

import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.shapes.Drawable;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class Circuit implements Drawable {

    private final ArrayList<Part> parts;
    private final ArrayList<Wire> wires;

    public Circuit() {
        parts = new ArrayList<>();
        wires = new ArrayList<>();
    }

    @Override
    public void drawTo(Graphic graphic) {
        for (Wire w : wires)
            w.drawTo(graphic);
        for (Part p : parts)
            p.drawTo(graphic);
    }

    public void add(Part part) {
        parts.add(part);
    }
}

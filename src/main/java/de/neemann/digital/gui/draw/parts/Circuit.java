package de.neemann.digital.gui.draw.parts;

import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.shapes.Drawable;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class Circuit implements Drawable {

    private final ArrayList<VisualPart> visualParts;
    private final ArrayList<Wire> wires;

    public Circuit() {
        visualParts = new ArrayList<>();
        wires = new ArrayList<>();
    }

    @Override
    public void drawTo(Graphic graphic) {
        for (Wire w : wires)
            w.drawTo(graphic);
        for (VisualPart p : visualParts)
            p.drawTo(graphic);
    }

    public void add(VisualPart visualPart) {
        visualParts.add(visualPart);
    }

    public ArrayList<VisualPart> getParts() {
        return visualParts;
    }
}

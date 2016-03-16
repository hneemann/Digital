package de.neemann.digital.gui.draw.parts;

import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.shapes.Drawable;

import java.util.ArrayList;
import java.util.Iterator;

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

    public void add(Wire wire) {
        wires.add(wire);
    }

    public ArrayList<VisualPart> getParts() {
        return visualParts;
    }

    public ArrayList<Moveable> getElementsMatching(Vector min, Vector max) {
        ArrayList<Moveable> m = new ArrayList<>();
        for (VisualPart vp : visualParts)
            if (vp.matches(min, max))
                m.add(vp);

        for (Wire w : wires) {
            if (w.p1.inside(min, max))
                m.add(w.p1);
            if (w.p2.inside(min, max))
                m.add(w.p2);
        }

        return m;
    }

    public void delete(Vector min, Vector max) {
        {
            Iterator<VisualPart> it = visualParts.iterator();
            while (it.hasNext())
                if (it.next().matches(min, max))
                    it.remove();
        }
        {
            Iterator<Wire> it = wires.iterator();
            while (it.hasNext()) {
                Wire w = it.next();
                if (w.p1.inside(min, max) || w.p2.inside(min, max))
                    it.remove();
            }
        }
    }
}

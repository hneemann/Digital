package de.neemann.digital.gui.draw.parts;

import de.neemann.digital.gui.draw.graphics.Graphic;
import de.neemann.digital.gui.draw.graphics.Style;
import de.neemann.digital.gui.draw.graphics.Vector;
import de.neemann.digital.gui.draw.shapes.Drawable;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * @author hneemann
 */
public class Circuit implements Drawable {
    private static final Vector RAD = new Vector(3, 3);
    private final ArrayList<VisualPart> visualParts;
    private transient ArrayList<Vector> dots;
    private ArrayList<Wire> wires;

    public Circuit() {
        visualParts = new ArrayList<>();
        wires = new ArrayList<>();
        dots = new ArrayList<>();
    }

    @Override
    public void drawTo(Graphic graphic, State state) {
        for (Vector d : getDots())
            graphic.drawCircle(d.sub(RAD), d.add(RAD), Style.WIRE);
        for (Wire w : wires)
            w.drawTo(graphic, state);
        for (VisualPart p : visualParts)
            p.drawTo(graphic, state);
    }

    public void add(VisualPart visualPart) {
        visualParts.add(visualPart);
    }

    public void add(Wire newWire) {
        if (newWire.p1.equals(newWire.p2))
            return;

        int len = wires.size();
        for (int i = 0; i < len; i++) {
            Wire present = wires.get(i);
            if (present.contains(newWire.p1)) {
                wires.set(i, new Wire(present.p1, newWire.p1));
                wires.add(new Wire(present.p2, newWire.p1));
            } else if (present.contains(newWire.p2)) {
                wires.set(i, new Wire(present.p1, newWire.p2));
                wires.add(new Wire(present.p2, newWire.p2));
            }
        }

        wires.add(newWire);
        WireConsistencyChecker checker = new WireConsistencyChecker(wires);
        wires = checker.check();
        dots = null;
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

    public ArrayList<Wire> getWires() {
        return wires;
    }

    public ArrayList<Vector> getDots() {
        if (dots == null)
            dots = WireConsistencyChecker.createDots(wires);
        return dots;
    }

    public void clearState() {
        for (VisualPart vp : visualParts)
            vp.setState(null, null, null);
        for (Wire w : wires)
            w.setValue(null);
    }
}

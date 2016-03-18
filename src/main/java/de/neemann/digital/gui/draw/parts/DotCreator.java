package de.neemann.digital.gui.draw.parts;

import de.neemann.digital.gui.draw.graphics.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hneemann
 */
public class DotCreator {

    private final ArrayList<Dot> dots;
    private final ArrayList<Wire> wires;
    private HashMap<Vector, Dot> map;

    public DotCreator(ArrayList<Wire> wires) {
        this.wires = wires;
        map = new HashMap<>();
        for (Wire w : wires) {
            inc(w.p1, w);
            inc(w.p2, w);
        }

        dots = new ArrayList<>();
        for (Map.Entry<Vector, Dot> e : map.entrySet())
            if (e.getValue().counter > 2)
                dots.add(e.getValue());
        map = null;
    }

    public ArrayList<Dot> getDots() {
        return dots;
    }

    private void inc(Vector vector, Wire w) {
        Dot c = map.get(vector);
        if (c == null) {
            c = new Dot(vector, w);
            map.put(vector, c);
        }
        c.inc();
    }

    public void applyDots() {
        for (Wire w : wires)
            w.noDot();
        for (Dot d : dots)
            d.setDot();
    }

    public static class Dot {
        private final Vector vector;
        private final Wire w;
        private int counter;

        public Dot(Vector vector, Wire w) {
            this.vector = vector;
            this.w = w;
        }

        private void inc() {
            counter++;
        }

        public Vector getVector() {
            return vector;
        }

        public void setDot() {
            w.setDot(vector);
        }
    }

}

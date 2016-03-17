package de.neemann.digital.gui.draw.parts;

import de.neemann.digital.gui.draw.graphics.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author hneemann
 */
public class WireConsistencyChecker {
    private ArrayList<Wire> wires;

    public WireConsistencyChecker(ArrayList<Wire> wires) {
        this.wires = wires;
    }

    public static ArrayList<Vector> createDots(ArrayList<Wire> wires) {
        HashMap<Vector, Counter> map = new HashMap<>();
        for (Wire w : wires) {
            inc(map, w.p1);
            inc(map, w.p2);
        }

        ArrayList<Vector> dots = new ArrayList<>();
        for (Map.Entry<Vector, Counter> e : map.entrySet())
            if (e.getValue().counter > 2)
                dots.add(e.getKey());
        return dots;
    }

    private static void inc(HashMap<Vector, Counter> map, Vector vector) {
        Counter c = map.get(vector);
        if (c == null) {
            c = new Counter();
            map.put(vector, c);
        }
        c.inc();
    }

    public ArrayList<Wire> check() {
        wires = merge(wires);
        return wires;
    }

    private ArrayList<Wire> merge(ArrayList<Wire> wires) {

        ArrayList<Vector> dots = createDots(wires);

        ArrayList<Wire> newWires = new ArrayList<>();
        WireMerger hori = new WireMerger(Wire.Orientation.horzontal);
        WireMerger vert = new WireMerger(Wire.Orientation.vertical);

        for (Wire w : wires) {
            if (!w.p1.equals(w.p2))
                switch (w.getOrientation()) {
                    case horzontal:
                        hori.add(w);
                        break;
                    case vertical:
                        vert.add(w);
                        break;
                    default:
                        newWires.add(w);
                }
        }

        hori.protectDots(dots);
        vert.protectDots(dots);

        hori.addTo(newWires);
        vert.addTo(newWires);

        return newWires;
    }

    private static class Counter {
        private int counter;

        public void inc() {
            counter++;
        }
    }
}

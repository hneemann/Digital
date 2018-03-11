/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.elements;

import de.neemann.digital.draw.graphics.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Is used to create the dots to visualize the connections between wires.
 * Each {@link Wire} has two flags to decide which end of the wire needs to be marked with a dot.
 */
public class DotCreator {

    private final ArrayList<Dot> dots;
    private final ArrayList<Wire> wires;
    private HashMap<Vector, Dot> map;

    /**
     * Creates a new instance
     *
     * @param wires the wires
     */
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

    private void inc(Vector vector, Wire w) {
        Dot c = map.get(vector);
        if (c == null) {
            c = new Dot(vector, w);
            map.put(vector, c);
        }
        c.inc();
    }

    /**
     * Applies the dots to the wires
     */
    public void applyDots() {
        for (Wire w : wires)
            w.noDot();
        for (Dot d : dots)
            d.setDot();
    }

    /**
     * A single dot
     */
    private static class Dot {
        private final Vector vector;
        private final Wire w;
        private int counter;

        Dot(Vector vector, Wire w) {
            this.vector = vector;
            this.w = w;
        }

        private void inc() {
            counter++;
        }

        Vector getVector() {
            return vector;
        }

        void setDot() {
            w.setDot(vector);
        }
    }

}

/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.elements;

import de.neemann.digital.draw.graphics.Vector;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;

/**
 * Merges colinear wires
 */
public class WireMerger {

    private HashMap<Integer, ArrayList<WireContainer>> wireContainers;
    private OrientationHandler handler;

    /**
     * Creates a new instance
     *
     * @param orientation the orientation of wires to merge
     */
    public WireMerger(Wire.Orientation orientation) {
        wireContainers = new HashMap<>();
        switch (orientation) {
            case horizontal:
                handler = new OrientationHandlerHorizontal();
                break;
            case vertical:
                handler = new OrientationHandlerVertical();
                break;
            default:
                throw new RuntimeException("wrong line orientation");
        }
    }

    /**
     * Adds a wire to the merger.
     *
     * @param w the wire to add
     */
    public void add(Wire w) {
        SimpleWire sw = new SimpleWire(handler.getWireClass(w.p1), handler.getS(w.p1), handler.getS(w.p2));
        ArrayList<WireContainer> containers = wireContainers.get(sw.wireClass);
        if (containers == null) {
            containers = new ArrayList<>();
            wireContainers.put(sw.wireClass, containers);
        }
        WireContainer wc = null;
        for (WireContainer container : containers)
            if (Objects.equals(container.color, w.getColor())) {
                wc = container;
                break;
            }
        if (wc == null) {
            wc = new WireContainer(sw.wireClass, w.getColor());
            containers.add(wc);
        }
        wc.add(sw);
    }

    /**
     * Adds a list of wires
     *
     * @param wires the wires
     */
    public void addTo(ArrayList<Wire> wires) {
        for (ArrayList<WireContainer> containers : wireContainers.values()) {
            for (WireContainer wc : containers)
                wc.addTo(wires);
        }
    }

    /**
     * Protects the given coordinates
     * Ensures that wires are not merged if the connection is necessary for a wire to wire connection
     *
     * @param points the coordinated to protect
     */
    public void protectPoints(Collection<Vector> points) {
        for (Vector v : points) {
            ArrayList<WireContainer> containers = wireContainers.get(handler.getWireClass(v));
            if (containers != null) {    // is possible because diagonals are not included
                for (WireContainer wc : containers)
                    wc.protect(handler.getS(v));
            }
        }
    }


    interface OrientationHandler {
        Wire toWire(SimpleWire wire);

        int getS(Vector v);

        int getWireClass(Vector v);
    }

    static class SimpleWire {
        private int wireClass;
        private int s1;
        private int s2;

        /**
         * Creates a new instance
         *
         * @param wireClass the wire class (x or y coordinate depending on orientation)
         * @param s1        first coordinate (y or x coordinate depending on orientation)
         * @param s2        second coordinate
         */
        SimpleWire(int wireClass, int s1, int s2) {
            this.wireClass = wireClass;
            if (s2 < s1) {
                this.s1 = s2;
                this.s2 = s1;
            } else {
                this.s1 = s1;
                this.s2 = s2;
            }
        }

        private boolean tryMerge(SimpleWire other) {
            if (s2 < other.s1 || other.s2 < s1)
                return false;
            else {
                s1 = Math.min(s1, other.s1);
                s2 = Math.max(s2, other.s2);
                return true;
            }
        }

        private boolean containsAsInner(int s) {
            return s1 < s && s2 > s;
        }
    }

    static class OrientationHandlerHorizontal implements OrientationHandler {

        @Override
        public Wire toWire(SimpleWire wire) {
            return new Wire(new Vector(wire.s1, wire.wireClass), new Vector(wire.s2, wire.wireClass));
        }

        @Override
        public int getWireClass(Vector v) {
            return v.y;
        }

        @Override
        public int getS(Vector v) {
            return v.x;
        }
    }

    static class OrientationHandlerVertical implements OrientationHandler {

        @Override
        public Wire toWire(SimpleWire wire) {
            return new Wire(new Vector(wire.wireClass, wire.s1), new Vector(wire.wireClass, wire.s2));
        }

        @Override
        public int getS(Vector v) {
            return v.y;
        }

        @Override
        public int getWireClass(Vector v) {
            return v.x;
        }

    }

    private class WireContainer {
        private int wireClass;
        private Color color;
        private ArrayList<SimpleWire> wires;

        WireContainer(int wireClass, Color color) {
            this.wireClass = wireClass;
            this.color = color;
            wires = new ArrayList<>();
        }

        public void add(SimpleWire newSimpleWire) {
            wires.add(newSimpleWire);
            simplify(newSimpleWire);
        }

        private void simplify(SimpleWire changedWire) {
            for (SimpleWire wire : wires) {
                if (!wire.equals(changedWire)) {
                    if (wire.tryMerge(changedWire)) {
                        wires.remove(changedWire);
                        simplify(wire);
                        return;
                    }
                }
            }
        }

        public void addTo(ArrayList<Wire> list) {
            for (SimpleWire sw : wires)
                list.add(handler.toWire(sw).setColor(color));
        }

        public void protect(int s) {
            int len = wires.size();
            for (int i = 0; i < len; i++) {
                SimpleWire sw = wires.get(i);
                if (sw.containsAsInner(s)) {
                    int s2 = sw.s2;
                    sw.s2 = s;
                    wires.add(new SimpleWire(wireClass, s, s2));
                }
            }
        }
    }
}

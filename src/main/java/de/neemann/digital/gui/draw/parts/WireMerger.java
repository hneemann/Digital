package de.neemann.digital.gui.draw.parts;

import de.neemann.digital.gui.draw.graphics.Vector;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * @author hneemann
 */
public class WireMerger {

    private HashMap<Integer, WireContainer> wireContainers;
    private OrientationHandler handler;

    public WireMerger(Wire.Orientation orientation) {
        wireContainers = new HashMap<>();
        switch (orientation) {
            case horzontal:
                handler = new OrientationHandlerHorizontal();
                break;
            case vertical:
                handler = new OrientationHandlerVertical();
                break;
            default:
                throw new RuntimeException("wrong line orientation");
        }
    }

    public void add(Wire w) {
        SimpleWire sw = new SimpleWire(handler.getWireClass(w.p1), handler.getS(w.p1), handler.getS(w.p2));
        WireContainer wc = wireContainers.get(sw.wireClass);
        if (wc == null) {
            wc = new WireContainer(sw.wireClass);
            wireContainers.put(sw.wireClass, wc);
        }
        wc.add(sw);
    }

    public void addTo(ArrayList<Wire> wires) {
        for (WireContainer wc : wireContainers.values()) {
            wc.addTo(wires);
        }
    }

    public void protectDots(ArrayList<Vector> dots) {
        for (Vector v : dots) {
            WireContainer wc = wireContainers.get(handler.getWireClass(v));
            if (wc != null)    // is possible because diagonals are not included
                wc.protect(handler.getS(v));
        }
    }

    interface OrientationHandler {
        Wire toWire(SimpleWire wire);

        int getS(Vector v);

        int getWireClass(Vector v);
    }

    public static class SimpleWire {
        protected int wireClass;
        protected int s1;
        protected int s2;

        public SimpleWire(int wireClass, int s1, int s2) {
            this.wireClass = wireClass;
            if (s2 < s1) {
                this.s1 = s2;
                this.s2 = s1;
            } else {
                this.s1 = s1;
                this.s2 = s2;
            }
        }

        public boolean tryMerge(SimpleWire other) {
            if (s2 < other.s1 || other.s2 < s1)
                return false;
            else {
                s1 = Math.min(s1, other.s1);
                s2 = Math.max(s2, other.s2);
                return true;
            }
        }

        public boolean containsAsInner(int s) {
            return s1 < s && s2 > s;
        }
    }

    public static class OrientationHandlerHorizontal implements OrientationHandler {

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

    public static class OrientationHandlerVertical implements OrientationHandler {

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
        public int wireClass;
        public ArrayList<SimpleWire> wires;

        public WireContainer(int wireClass) {
            this.wireClass = wireClass;
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
                list.add(handler.toWire(sw));
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

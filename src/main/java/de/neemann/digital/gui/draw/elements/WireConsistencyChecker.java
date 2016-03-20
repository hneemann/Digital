package de.neemann.digital.gui.draw.elements;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class WireConsistencyChecker {
    private ArrayList<Wire> wires;

    public WireConsistencyChecker(ArrayList<Wire> wires) {
        this.wires = wires;
    }

    public ArrayList<Wire> check() {
        wires = merge(wires);
        return wires;
    }

    private ArrayList<Wire> merge(ArrayList<Wire> wires) {

        ArrayList<DotCreator.Dot> dots = new DotCreator(wires).getDots();

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

}

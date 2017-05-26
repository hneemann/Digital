package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;

import java.util.ArrayList;

/**
 * Created by hneemann on 25.05.17.
 */
public final class Modifications implements Modification {
    private final ArrayList<Modification> modifications;

    private Modifications(ArrayList<Modification> modifications) {
        this.modifications = modifications;
    }

    @Override
    public void modify(Circuit circuit) {
        for (Modification m : modifications)
            m.modify(circuit);
    }

    public static final class Builder {
        private final ArrayList<Modification> list;

        public Builder() {
            list = new ArrayList<>();
        }

        public Builder add(Modification m) {
            list.add(m);
            return this;
        }

        public Modification build() {
            return new Modifications(list);
        }
    }
}

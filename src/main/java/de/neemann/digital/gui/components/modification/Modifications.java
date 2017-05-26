package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;

import java.util.ArrayList;

/**
 * A single modification which is build from a set of other modifications.
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

    /**
     * The builder to construct an instance
     */
    public static final class Builder {
        private final ArrayList<Modification> list;

        /**
         * Creates a new instance
         */
        public Builder() {
            list = new ArrayList<>();
        }

        /**
         * Adds a modification to this set
         *
         * @param m the modification to add
         * @return this for chained calls
         */
        public Builder add(Modification m) {
            list.add(m);
            return this;
        }

        /**
         * Builds the unified of modification
         *
         * @return the unified modification
         */
        public Modification build() {
            return new Modifications(list);
        }
    }
}

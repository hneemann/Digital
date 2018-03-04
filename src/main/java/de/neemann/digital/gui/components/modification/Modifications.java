/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.modification;

import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.library.ElementLibrary;

import java.util.ArrayList;

/**
 * A single modification which is build from a set of other modifications.
 */
public final class Modifications implements Modification {
    private final ArrayList<Modification> modifications;
    private final String name;

    private Modifications(ArrayList<Modification> modifications, String name) {
        this.modifications = modifications;
        this.name = name;
    }

    @Override
    public void modify(Circuit circuit, ElementLibrary library) {
        for (Modification m : modifications)
            m.modify(circuit, library);
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * The builder to construct an instance
     */
    public static final class Builder {
        private final ArrayList<Modification> list;
        private final String name;

        /**
         * Creates a new instance
         *
         * @param name the name of this modification
         */
        public Builder(String name) {
            this.name = name;
            list = new ArrayList<>();
        }

        /**
         * Adds a modification to this set
         *
         * @param m the modification to add
         * @return this for chained calls
         */
        public Builder add(Modification m) {
            if (m != null)
                list.add(m);
            return this;
        }

        /**
         * Builds the unified of modification
         *
         * @return the unified modification
         */
        public Modification build() {
            if (list.isEmpty())
                return null;

            if (list.size() == 1)
                return list.get(0);
            else
                return new Modifications(list, name);
        }
    }
}

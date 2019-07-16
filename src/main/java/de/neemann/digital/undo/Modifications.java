/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.undo;

import java.util.ArrayList;

/**
 * A single modification which is build from a set of other modifications.
 *
 * @param <A> The component to track
 */
public final class Modifications<A extends Copyable<A>> implements Modification<A> {
    private final ArrayList<Modification<A>> modifications;
    private final String name;

    private Modifications(ArrayList<Modification<A>> modifications, String name) {
        this.modifications = modifications;
        this.name = name;
    }

    @Override
    public void modify(A a) throws ModifyException {
        for (Modification<A> m : modifications)
            m.modify(a);
    }

    /**
     * @return The contained modifications
     */
    public ArrayList<Modification<A>> getModifications() {
        return modifications;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * The builder to construct an instance
     *
     * @param <A> The component to track
     */
    public static final class Builder<A extends Copyable<A>> {
        private final ArrayList<Modification<A>> list;
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
        public Builder<A> add(Modification<A> m) {
            if (m != null)
                list.add(m);
            return this;
        }

        /**
         * Builds the unified of modification
         *
         * @return the unified modification
         */
        public Modification<A> build() {
            if (list.isEmpty())
                return null;

            if (list.size() == 1)
                return list.get(0);
            else
                return new Modifications<>(list, name);
        }
    }
}

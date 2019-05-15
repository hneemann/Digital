/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.elements;

import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.components.ElementOrderer;
import de.neemann.digital.undo.Modification;
import de.neemann.digital.undo.Modifications;

import java.util.ArrayList;

/**
 * Takes a circuit and generates of all elements of the given name.
 * This list you can use order the entries. See {@link ElementOrderer}.
 */
public class ElementOrder implements ElementOrderer.OrderInterface<String> {

    private final ArrayList<Entry> entries;
    private final Modifications.Builder<Circuit> modifications;
    private final String modificationName;

    /**
     * Creates a new instance
     *
     * @param circuitComponent the circuitComponent witch components are to order
     * @param filter           the filter to select the entries to order
     * @param modificationName name of modification
     */
    public ElementOrder(CircuitComponent circuitComponent, ElementFilter filter, String modificationName) {
        this.modificationName = modificationName;
        ArrayList<VisualElement> elements = circuitComponent.getCircuit().getElements();
        entries = new ArrayList<>();
        for (int i = 0; i < elements.size(); i++)
            if (filter.accept(elements.get(i))) {
                String n = elements.get(i).getElementAttributes().getLabel();
                if (n != null && n.length() > 0)
                    entries.add(new Entry(i, n));
            }
        modifications = new Modifications.Builder<>(modificationName);
    }

    @Override
    public int size() {
        return entries.size();
    }

    @Override
    public String get(int index) {
        return entries.get(index).name;
    }

    @Override
    public void swap(int i, int j) {
        int index1 = entries.get(i).i;
        int index2 = entries.get(j).i;

        int z = entries.get(i).i;
        entries.get(i).i = entries.get(j).i;
        entries.get(j).i = z;

        Entry x = entries.get(i);
        entries.set(i, entries.get(j));
        entries.set(j, x);

        modifications.add(new SwapModification(index1, index2, modificationName));
    }

    /**
     * @return the modification
     */
    public Modification<Circuit> getModifications() {
        return modifications.build();
    }

    private final static class Entry {
        private int i;
        private final String name;

        private Entry(int i, String name) {
            this.i = i;
            this.name = name;
        }
    }

    /**
     * Interface to determine the elements which should appear in the order list
     */
    public interface ElementFilter {
        /**
         * @param element the element to check
         * @return returns true if element is to order
         */
        boolean accept(VisualElement element);
    }

    private static final class SwapModification implements Modification<Circuit> {
        private final int index1;
        private final int index2;
        private final String name;

        private SwapModification(int index1, int index2, String name) {
            this.index1 = index1;
            this.index2 = index2;
            this.name = name;
        }

        @Override
        public void modify(Circuit circuit) {
            ArrayList<VisualElement> elements = circuit.getElements();
            VisualElement y = elements.get(index1);
            elements.set(index1, elements.get(index2));
            elements.set(index2, y);
        }

        @Override
        public String toString() {
            return name;
        }
    }
}

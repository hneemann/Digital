/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.elements;

import de.neemann.digital.gui.components.CircuitComponent;
import de.neemann.digital.gui.components.ElementOrderer;
import de.neemann.digital.gui.components.modification.Modification;
import de.neemann.digital.gui.components.modification.Modifications;

import java.util.ArrayList;

/**
 * Takes a circuit and generates of all elements of the given name.
 * This list you can use order the entries. See {@link ElementOrderer}.
 */
public class ElementOrder implements ElementOrderer.OrderInterface<String> {

    private final ArrayList<Entry> entries;
    private final Modifications.Builder modifications;

    /**
     * Creates a new instance
     *
     * @param circuitComponent the circuitComponent witch components are to order
     * @param filter           the filter to select the entries to order
     * @param name             name of modification
     */
    public ElementOrder(CircuitComponent circuitComponent, ElementFilter filter, String name) {
        ArrayList<VisualElement> elements = circuitComponent.getCircuit().getElements();
        entries = new ArrayList<>();
        for (int i = 0; i < elements.size(); i++)
            if (filter.accept(elements.get(i))) {
                String n = elements.get(i).getElementAttributes().getCleanLabel();
                if (n != null && n.length() > 0)
                    entries.add(new Entry(i, n));
            }
        modifications = new Modifications.Builder(name);
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

        modifications.add((circuit, library) -> {
            ArrayList<VisualElement> elements = circuit.getElements();
            VisualElement y = elements.get(index1);
            elements.set(index1, elements.get(index2));
            elements.set(index2, y);
        });
    }

    /**
     * @return the modification
     */
    public Modification getModifications() {
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
}

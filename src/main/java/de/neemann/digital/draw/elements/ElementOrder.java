package de.neemann.digital.draw.elements;

import de.neemann.digital.gui.components.ElementOrderer;

import java.util.ArrayList;

/**
 * Takes a circuit and generates of all elements of the given name.
 * This list you can use order the entries. See {@link ElementOrderer}.
 *
 * @author hneemann
 */
public class ElementOrder implements ElementOrderer.OrderInterface<String> {

    private final ArrayList<Entry> entries;
    private final ArrayList<VisualElement> elements;
    private final Circuit circuit;

    /**
     * Creates a new instance
     *
     * @param circuit the circuit witch components are to order
     * @param name    the name of the elements to order
     */
    public ElementOrder(Circuit circuit, String name) {
        this(circuit, element -> {
            return element.getElementName().equals(name);
        });
    }

    /**
     * Creates a new instance
     *
     * @param circuit the circuit witch components are to order
     * @param filter  the filter to select the entries to order
     */
    public ElementOrder(Circuit circuit, ElementFilter filter) {
        this.circuit = circuit;
        this.elements = circuit.getElements();
        entries = new ArrayList<>();
        for (int i = 0; i < elements.size(); i++)
            if (filter.accept(elements.get(i))) {
                String n = elements.get(i).getElementAttributes().getCleanLabel();
                if (n != null && n.length() > 0)
                    entries.add(new Entry(i, n));
            }
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
        VisualElement y = elements.get(entries.get(i).i);
        elements.set(entries.get(i).i, elements.get(entries.get(j).i));
        elements.set(entries.get(j).i, y);

        int z = entries.get(i).i;
        entries.get(i).i = entries.get(j).i;
        entries.get(j).i = z;

        Entry x = entries.get(i);
        entries.set(i, entries.get(j));
        entries.set(j, x);

        circuit.modified();
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

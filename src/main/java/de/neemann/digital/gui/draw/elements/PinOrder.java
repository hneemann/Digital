package de.neemann.digital.gui.draw.elements;

import de.neemann.digital.core.element.AttributeKey;
import de.neemann.digital.gui.components.ElementOrderer;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class PinOrder implements ElementOrderer.OrderInterface<String> {

    private final ArrayList<Entry> entries;
    private final ArrayList<VisualElement> elements;

    public PinOrder(Circuit circuit, String name) {
        this.elements = circuit.getElements();
        entries = new ArrayList<>();
        for (int i = 0; i < elements.size(); i++)
            if (elements.get(i).getElementName().equals(name)) {
                String n = elements.get(i).getElementAttributes().get(AttributeKey.Label);
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

    }

    public static class Entry {
        private int i;
        private final String name;

        public Entry(int i, String name) {
            this.i = i;
            this.name = name;
        }
    }
}

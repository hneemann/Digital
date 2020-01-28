/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components;

import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.elements.Circuit;
import de.neemann.digital.draw.elements.Movable;
import de.neemann.digital.draw.elements.VisualElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

/**
 * Used to renumber labels if copied elements are inserted into a circuit.
 * Modifies the elements only if a element is present in the circuit which has
 * the same label as the element to copy.
 */
public class CopiedElementLabelRenamer {

    private final ArrayList<Movable> elements;

    /**
     * Creates a new instance
     *
     * @param circuit  the circuit to copy to
     * @param elements the element to insert
     */
    CopiedElementLabelRenamer(Circuit circuit, ArrayList<Movable> elements) {
        this.elements = elements;


        HashMap<LabelClass, PresentIndex> circuitMap = new HashMap<>();
        for (VisualElement ve : circuit.getElements()) {
            LabelInstance li = LabelInstance.create(ve);
            if (li != null) {
                PresentIndex pi = circuitMap.computeIfAbsent(li.getLabelClass(), labelClass -> new PresentIndex());
                pi.add(li.getNumber());
            }
        }

        HashMap<LabelClass, MinIndex> insertMap = new HashMap<>();
        for (Movable m : elements)
            if (m instanceof VisualElement) {
                LabelInstance li = LabelInstance.create((VisualElement) m);
                if (li != null) {
                    MinIndex mi = insertMap.get(li.getLabelClass());
                    if (mi == null) {
                        mi = new MinIndex(li.getNumber());
                        insertMap.put(li.getLabelClass(), mi);
                    } else {
                        mi.checkMin(li.getNumber());
                    }
                }
            }

        for (Movable m : elements)
            if (m instanceof VisualElement) {
                final VisualElement ve = (VisualElement) m;

                if (!ve.getElementAttributes().get(Keys.PINNUMBER).isEmpty())
                    continue;

                LabelInstance li = LabelInstance.create(ve);
                if (li == null)
                    continue;

                PresentIndex pi = circuitMap.get(li.getLabelClass());
                if (pi == null)
                    continue;

                if (!pi.contains(li.getNumber()))
                    continue;

                int maxAvail = pi.getMax();
                int minToInsert = insertMap.get(li.getLabelClass()).getMin();

                int delta = maxAvail - minToInsert + 1;

                ve.setAttribute(Keys.LABEL, li.getLabel(delta));
            }
    }

    /**
     * @return the elements with renamed labels.
     */
    public ArrayList<Movable> rename() {
        return elements;
    }

    static final class LabelInstance {

        static LabelInstance create(VisualElement ve) {
            return create(ve.getElementName(), ve.getElementAttributes().getLabel());
        }

        static LabelInstance create(String elementName, String fullLabel) {
            if (fullLabel == null)
                return null;

            int pos = fullLabel.length();
            if (pos == 0)
                return null;

            if (!Character.isDigit(fullLabel.charAt(pos - 1)))
                return null;

            int number = 0;
            int base = 1;
            while (pos > 0 && Character.isDigit(fullLabel.charAt(pos - 1))) {
                pos--;
                number += (fullLabel.charAt(pos) - '0') * base;
                base *= 10;
            }

            String label = fullLabel.substring(0, pos);
            LabelClass lc = new LabelClass(elementName, label);

            return new LabelInstance(lc, number);
        }

        private final LabelClass lc;
        private final int number;

        private LabelInstance(LabelClass lc, int number) {
            this.lc = lc;
            this.number = number;
        }

        LabelClass getLabelClass() {
            return lc;
        }

        public int getNumber() {
            return number;
        }

        public String getLabel(int delta) {
            return lc.label + Integer.toString(number + delta);
        }
    }


    static final class LabelClass {
        private final String elementName;
        private final String label;

        private LabelClass(String elementName, String label) {
            this.elementName = elementName;
            this.label = label;
        }

        public String getElementName() {
            return elementName;
        }

        public String getLabel() {
            return label;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            LabelClass that = (LabelClass) o;
            return Objects.equals(elementName, that.elementName)
                    && Objects.equals(label, that.label);
        }

        @Override
        public int hashCode() {
            return Objects.hash(elementName, label);
        }
    }

    private static final class MinIndex {
        private int min;

        private MinIndex(int number) {
            this.min = number;
        }

        void checkMin(int number) {
            if (this.min > number)
                this.min = number;
        }

        public int getMin() {
            return min;
        }
    }

    private static final class PresentIndex {
        private HashSet<Integer> numbers;
        private int max = Integer.MIN_VALUE;

        private PresentIndex() {
            numbers = new HashSet<>();
        }

        public void add(int number) {
            numbers.add(number);
            if (number > max)
                max = number;
        }

        public boolean contains(int number) {
            return numbers.contains(number);
        }

        public int getMax() {
            return max;
        }
    }
}

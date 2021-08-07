/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.stats;

import de.neemann.digital.core.Model;
import de.neemann.digital.core.Node;
import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;
import de.neemann.digital.core.element.Element;
import de.neemann.digital.core.element.PinDescription;
import de.neemann.digital.core.wiring.bus.CommonBusValue;
import de.neemann.digital.draw.elements.PinException;
import de.neemann.digital.lang.Lang;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.TreeMap;

/**
 * Creates the circuit statistics.
 */
public class Statistics {
    private final TreeMap<Index, Counter> map;

    /**
     * Creates a new statistics.
     *
     * @param model the model to count.
     * @throws PinException PinException
     */
    public Statistics(Model model) throws PinException {
        HashSet<CommonBusValue> cbvs = new HashSet<>();
        map = new TreeMap<>();
        for (Node n : model) {
            if (n instanceof Countable) {
                count(new Index((Countable) n));
            } else
                count(new Index(n.getClass().getSimpleName()));

            if (n instanceof Element) {
                for (ObservableValue ov : ((Element) n).getOutputs()) {
                    for (Observer o : ov.getObservers()) {
                        if (o instanceof CommonBusValue)
                            cbvs.add((CommonBusValue) o);
                    }
                }
            }
        }
        for (CommonBusValue cbv : cbvs) {
            PinDescription.PullResistor r = cbv.getResistor();
            if (r == PinDescription.PullResistor.pullDown || r == PinDescription.PullResistor.pullUp)
                count(new Index(r.name(), cbv.getBits()));
        }
    }

    private void count(Index i) {
        Counter counter = map.computeIfAbsent(i, index -> new Counter(i));
        counter.inc();
    }

    /**
     * Creates the table model needed to represent the results
     *
     * @return the table model
     */
    public TableModel getTableModel() {
        return new MyTableModel(new ArrayList<>(map.values()));
    }

    private static final class Index implements Comparable<Index> {
        private final String name;
        private final int bits;
        private final int inputs;
        private final int addrBits;

        private Index(String name, int bits) {
            this.name = name;
            this.bits = bits;
            this.inputs = 0;
            this.addrBits = 0;
        }

        private Index(String name) {
            this.name = name;
            this.bits = 0;
            this.inputs = 0;
            this.addrBits = 0;
        }

        private Index(Countable countable) {
            this.name = countable.getClass().getSimpleName();
            this.bits = countable.getDataBits();
            this.inputs = countable.getInputsCount();
            this.addrBits = countable.getAddrBits();
        }

        @Override
        public int compareTo(Index index) {
            int c = name.compareTo(index.name);
            if (c != 0) return c;
            c = inputs - index.inputs;
            if (c != 0) return c;
            c = bits - index.bits;
            if (c != 0) return c;
            return addrBits - index.addrBits;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Index index = (Index) o;
            return bits == index.bits
                    && inputs == index.inputs
                    && addrBits == index.addrBits
                    && name.equals(index.name);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, bits, inputs, addrBits);
        }
    }

    private static final class Counter {
        private final Index index;
        private int counter;

        private Counter(Index index) {
            this.index = index;
            this.counter = 0;
        }

        private Index getIndex() {
            return index;
        }

        private int getCounter() {
            return counter;
        }

        private void inc() {
            counter++;
        }
    }

    private static final class MyTableModel implements TableModel {

        private final ArrayList<Counter> counters;

        private MyTableModel(ArrayList<Counter> counters) {
            this.counters = counters;
        }

        @Override
        public int getRowCount() {
            return counters.size();
        }

        @Override
        public int getColumnCount() {
            return 5;
        }

        @Override
        public String getColumnName(int i) {
            switch (i) {
                case 0:
                    return Lang.get("stat_part");
                case 1:
                    return Lang.get("stat_inputs");
                case 2:
                    return Lang.get("stat_bits");
                case 3:
                    return Lang.get("stat_addrBits");
                default:
                    return Lang.get("stat_number");
            }
        }

        @Override
        public Class<?> getColumnClass(int i) {
            if (i == 0) return String.class;
            else return Integer.class;
        }

        @Override
        public boolean isCellEditable(int i, int i1) {
            return false;
        }

        @Override
        public Object getValueAt(int row, int col) {
            Counter c = counters.get(row);
            switch (col) {
                case 0:
                    return c.getIndex().name;
                case 1:
                    return checkNull(c.getIndex().inputs);
                case 2:
                    return checkNull(c.getIndex().bits);
                case 3:
                    return checkNull(c.getIndex().addrBits);
                default:
                    return checkNull(c.getCounter());
            }
        }

        private Object checkNull(int i) {
            if (i == 0)
                return null;
            return i;
        }

        @Override
        public void setValueAt(Object o, int i, int i1) {
        }

        @Override
        public void addTableModelListener(TableModelListener tableModelListener) {
        }

        @Override
        public void removeTableModelListener(TableModelListener tableModelListener) {
        }
    }

}

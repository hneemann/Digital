/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.elements;

import de.neemann.digital.core.arithmetic.Add;
import de.neemann.digital.core.arithmetic.Comparator;
import de.neemann.digital.core.arithmetic.Sub;
import de.neemann.digital.core.basic.*;
import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.ElementTypeDescription;
import de.neemann.digital.core.element.Key;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.flipflops.FlipflopD;
import de.neemann.digital.core.io.Const;
import de.neemann.digital.core.io.In;
import de.neemann.digital.core.io.Out;
import de.neemann.digital.core.memory.RAMDualPort;
import de.neemann.digital.core.memory.RAMSinglePort;
import de.neemann.digital.core.memory.ROM;
import de.neemann.digital.core.memory.Register;
import de.neemann.digital.core.switching.NFET;
import de.neemann.digital.core.switching.PFET;
import de.neemann.digital.core.wiring.*;
import de.neemann.digital.draw.library.ElementLibrary;
import de.neemann.digital.draw.library.ElementNotFoundException;
import de.neemann.digital.lang.Lang;

import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;
import java.util.*;

public class Stats {
    private static ArrayList<Key> RELEVANT_KEYS = new ArrayList<>();

    static {
        RELEVANT_KEYS.add(Keys.INPUT_COUNT);
        RELEVANT_KEYS.add(Keys.BITS);
        RELEVANT_KEYS.add(Keys.ADDR_BITS);
        RELEVANT_KEYS.add(Keys.SELECTOR_BITS);
    }

    private static HashMap<String, TransistorCalculator> TRANSISTORS = new HashMap<>();

    static {
        add(NFET.DESCRIPTION, ElementAttributes::getBits);
        add(PFET.DESCRIPTION, ElementAttributes::getBits);

        add(NAnd.DESCRIPTION, attr -> attr.get(Keys.INPUT_COUNT) * attr.getBits() * 2);
        add(And.DESCRIPTION, attr -> (attr.get(Keys.INPUT_COUNT) * 2 + 2) * attr.getBits());
        add(NOr.DESCRIPTION, attr -> attr.get(Keys.INPUT_COUNT) * attr.getBits() * 2);
        add(Or.DESCRIPTION, attr -> (attr.get(Keys.INPUT_COUNT) * 2 + 2) * attr.getBits());
        add(XOr.DESCRIPTION, attr -> (attr.get(Keys.INPUT_COUNT) * 8) * attr.getBits());
        add(XNOr.DESCRIPTION, attr -> (attr.get(Keys.INPUT_COUNT) * 8 + 1) * attr.getBits());
        add(Not.DESCRIPTION, attr -> 2 * attr.getBits());
        add(Driver.DESCRIPTION, attr -> 6 * attr.getBits());
        add(DriverInvSel.DESCRIPTION, attr -> 6 * attr.getBits());
        add(DriverInvSel.DESCRIPTION, attr -> 6 * attr.getBits());
        add(Multiplexer.DESCRIPTION, attr -> {
            int sel = attr.get(Keys.SELECTOR_BITS);
            final int andInputs = sel + 1;
            final int orInputs = 1 << sel;
            return sel * 2 + ((1 << sel) * (andInputs * 2 + 2) + (orInputs * 2 + 2)) * attr.getBits();
        });
        add(Demultiplexer.DESCRIPTION, attr -> {
            int sel = attr.get(Keys.SELECTOR_BITS);
            final int andInputs = sel + 1;
            return sel * 2 + (1 << sel) * (andInputs * 2 + 2) * attr.getBits();
        });
        add(Decoder.DESCRIPTION, attr -> {
            int sel = attr.get(Keys.SELECTOR_BITS);
            return sel * 2 + (1 << sel) * (sel * 2 + 2) * attr.getBits();
        });
        add(FlipflopD.DESCRIPTION, attr -> 26 * attr.getBits());
        add(Register.DESCRIPTION, attr -> 30 * attr.getBits());


        add(Add.DESCRIPTION, attr -> 28 * attr.getBits());
        add(Sub.DESCRIPTION, attr -> 28 * attr.getBits());
        add(Comparator.DESCRIPTION, attr -> 38 * attr.getBits());
        final TransistorCalculator ram = attr -> 40 * attr.get(Keys.ADDR_BITS) + (1 << attr.get(Keys.ADDR_BITS)) * attr.getBits() * 6;
        add(RAMDualPort.DESCRIPTION, ram);
        add(RAMSinglePort.DESCRIPTION, ram);
        add(ROM.DESCRIPTION, attr -> 40 * attr.get(Keys.ADDR_BITS) + (1 << attr.get(Keys.ADDR_BITS)) * attr.getBits());
    }

    private static void add(ElementTypeDescription description, TransistorCalculator tc) {
        TRANSISTORS.put(description.getName(), tc);
    }

    private final ElementLibrary library;
    private TreeMap<EntryKey, Entry> map;

    public Stats(ElementLibrary library) {
        this.library = library;
        map = new TreeMap<>();
    }

    public Stats add(Circuit circuit) throws ElementNotFoundException {
        for (VisualElement ve : circuit.getElements()) {
            if (ve.equalsDescription(In.DESCRIPTION) ||
                    ve.equalsDescription(Out.DESCRIPTION) ||
                    ve.equalsDescription(Const.DESCRIPTION) ||
                    ve.equalsDescription(Tunnel.DESCRIPTION) ||
                    ve.equalsDescription(Splitter.DESCRIPTION) ||
                    ve.equalsDescription(Clock.DESCRIPTION))
                continue;

            ElementAttributes attr = ve.getElementAttributes();
            ElementTypeDescription description = library.getElementType(ve.getElementName());
            add(description, attr);
        }
        return this;
    }

    private void add(ElementTypeDescription description, ElementAttributes attr) throws ElementNotFoundException {
        int transistors = 0;
        Circuit childCircuit = null;
        if (description instanceof ElementLibrary.ElementTypeDescriptionCustom) {
            ElementLibrary.ElementTypeDescriptionCustom c = (ElementLibrary.ElementTypeDescriptionCustom) description;
            childCircuit = c.getCircuit();
            transistors = childCircuit.getAttributes().get(Keys.TRANSISTORS);
            if (transistors > 0)
                childCircuit = null;
        } else {
            TransistorCalculator tr = TRANSISTORS.get(description.getName());
            if (tr != null)
                transistors = tr.transistors(attr);
        }

        EntryKey key = new EntryKey(description, attr, transistors);
        Entry e = map.get(key);
        if (e == null) {
            e = new Entry(key);
            map.put(key, e);
        }
        e.addOne();

        if (childCircuit != null)
            add(childCircuit);
    }

    public TableModel getTableModel() {
        final ArrayList<Row> entries = new ArrayList<>(map.size());
        int tr = 0;
        for (Entry e : map.values()) {
            entries.add(e);
            tr += e.getTransistors();
        }
        final int transistors = tr;
        entries.add(new Row() {
            @Override
            public int getCount() {
                return 0;
            }

            @Override
            public String getDescription() {
                return Lang.get("stat_sum");
            }

            @Override
            public int getTransistorsEach() {
                return 0;
            }

            @Override
            public int getTransistors() {
                return transistors;
            }
        });

        return new MyTableModel(entries);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int transistors = 0;
        for (Entry e : map.values()) {
            sb.append(e.toString());
            sb.append("\n");
            transistors += e.getTransistors();
        }
        if (transistors > 0)
            sb.append(transistors).append(" transistors total");
        return sb.toString();
    }

    private static class EntryKey implements Comparable<EntryKey> {
        private final String name;
        private final int transistors;
        private final HashMap<Key, Object> map;

        private EntryKey(ElementTypeDescription description, ElementAttributes attr, int transistors) {
            this.name = description.getTranslatedName();
            this.transistors = transistors;
            this.map = new HashMap<>();
            for (Key k : description.getAttributeList()) {
                if (RELEVANT_KEYS.contains(k))
                    map.put(k, attr.get(k));
            }
        }

        @Override
        public String toString() {
            StringBuilder sb = getDescriptionInt();
            if (transistors > 0)
                sb.append("; ").append(transistors).append(" transistors each");
            return sb.toString();
        }

        StringBuilder getDescriptionInt() {
            StringBuilder sb = new StringBuilder(name);
            if (!map.isEmpty()) {
                sb.append(" (");
                boolean first = true;
                for (Key k : RELEVANT_KEYS) {
                    Object v = map.get(k);
                    if (v != null) {
                        if (first)
                            first = false;
                        else
                            sb.append(", ");
                        sb.append(k.toString());
                        sb.append(":");
                        sb.append(v);
                    }
                }
                sb.append(")");
            }
            return sb;
        }

        public String getDescription() {
            return getDescriptionInt().toString();
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EntryKey entryKey = (EntryKey) o;
            return transistors == entryKey.transistors &&
                    name.equals(entryKey.name) &&
                    map.equals(entryKey.map);
        }

        @Override
        public int hashCode() {
            return Objects.hash(name, transistors, map);
        }

        @Override
        public int compareTo(EntryKey entryKey) {
            int c = name.compareTo(entryKey.name);
            if (c != 0) return c;
            for (Key k : RELEVANT_KEYS) {
                c = check(k, entryKey);
                if (c != 0) return c;
            }
            return 0;
        }

        private int check(Key key, EntryKey entryKey) {
            Object a = map.get(key);
            Object b = entryKey.map.get(key);
            if (a == null & b == null) return 0;
            if (a instanceof Long)
                return Long.compare((Long) a, (Long) b);
            if (a instanceof Integer)
                return Integer.compare((Integer) a, (Integer) b);
            if (a instanceof String)
                return ((String) a).compareTo((String) b);
            return 0;
        }

    }

    private static final class Entry implements Row {

        private final EntryKey key;
        private int count;

        private Entry(EntryKey key) {
            this.key = key;
        }

        private void addOne() {
            count++;
        }

        @Override
        public String toString() {
            return count + " x " + key;
        }

        @Override
        public int getCount() {
            return count;
        }

        @Override
        public String getDescription() {
            return key.getDescription();
        }

        @Override
        public int getTransistorsEach() {
            return key.transistors;
        }

        @Override
        public int getTransistors() {
            return count * key.transistors;
        }
    }

    private interface TransistorCalculator {
        int transistors(ElementAttributes attr);
    }

    private interface Row {

        int getCount();

        String getDescription();

        int getTransistorsEach();

        int getTransistors();
    }

    private static class MyTableModel implements TableModel {
        private final List<Row> entries;

        private MyTableModel(List<Row> entries) {
            this.entries = entries;
        }

        @Override
        public int getRowCount() {
            return entries.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        public String getColumnName(int i) {
            switch (i) {
                case 0:
                    return Lang.get("stat_number");
                case 1:
                    return Lang.get("stat_part");
                case 2:
                    return Lang.get("stat_transistors");
                default:
                    return Lang.get("stat_transistorsTotal");
            }
        }

        @Override
        public Class<?> getColumnClass(int i) {
            if (i == 1)
                return String.class;
            return Integer.class;
        }

        @Override
        public boolean isCellEditable(int i, int i1) {
            return false;
        }

        @Override
        public Object getValueAt(int row, int col) {
            Row r = entries.get(row);
            switch (col) {
                case 0:
                    return check(r.getCount());
                case 1:
                    return r.getDescription();
                case 2:
                    return check(r.getTransistorsEach());
                default:
                    return check(r.getTransistors());
            }
        }

        private Object check(int i) {
            if (i > 0) return i;
            return null;
        }

        @Override
        public void setValueAt(Object o, int row, int col) {
        }

        @Override
        public void addTableModelListener(TableModelListener tableModelListener) {
        }

        @Override
        public void removeTableModelListener(TableModelListener tableModelListener) {
        }
    }

}

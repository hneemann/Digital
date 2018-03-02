/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;

import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.quinemc.BoolTable;
import de.neemann.digital.analyse.quinemc.BoolTableByteArray;
import de.neemann.digital.analyse.quinemc.TableReducer;
import de.neemann.digital.analyse.quinemc.ThreeStateValue;
import de.neemann.digital.core.Signal;
import junit.framework.TestCase;

import java.util.ArrayList;
import java.util.List;

public class BoolTableExpandedTest extends TestCase {

    public void testRegression() {
        checkTable(new Signals("a", "b").list());
        checkTable(new Signals("a", "c").list());
        checkTable(new Signals("a", "d").list());
        checkTable(new Signals("b", "c").list());
        checkTable(new Signals("b", "d").list());
        checkTable(new Signals("c", "d").list());
    }

    private void checkTable(ArrayList<Signal> in1) {
        ArrayList<Signal> in2 = new Signals("a", "b", "c", "d").list();
        List<Variable> vars = new Vars("a", "b", "c", "d").list();

        check(new BoolTableByteArray(new byte[]{1, 1, 0, 1}), in1, in2, vars);
        check(new BoolTableByteArray(new byte[]{0, 1, 1, 0}), in1, in2, vars);
        check(new BoolTableByteArray(new byte[]{1, 0, 0, 1}), in1, in2, vars);
        check(new BoolTableByteArray(new byte[]{0, 0, 0, 1}), in1, in2, vars);
        check(new BoolTableByteArray(new byte[]{0, 1, 1, 1}), in1, in2, vars);
    }

    private void check(BoolTableByteArray e, ArrayList<Signal> in1, ArrayList<Signal> in2, List<Variable> vars) {
        BoolTableExpanded bt = new BoolTableExpanded(e, in1, in2);

        TableReducer tr = new TableReducer(vars, bt);

        assertTrue(tr.canReduceOnlyCheckTable());

        List<Variable> v = tr.getVars();
        assertEquals(in1.size(), v.size());
        for (int i = 0; i < v.size(); i++)
            assertEquals(in1.get(i).getName(), v.get(i).getIdentifier());

        BoolTable t1 = tr.getTable();
        assertEquals(e.size(), t1.size());
        for (int r = 0; r < e.size(); r++)
            assertEquals(e.get(r), t1.get(r));
    }

    public void testCombined() {
        ArrayList<Signal> in1 = new Signals("b", "c").list();
        ArrayList<Signal> in2 = new Signals("a", "b", "c", "d").list();
        List<Variable> vars = new Vars("a", "b", "c", "d").list();

        BoolTableExpanded bt = new BoolTableExpanded(new BoolTableByteArray(new byte[]{1, 1, 0, 0}), in1, in2);
        TableReducer tr = new TableReducer(vars, bt);
        assertTrue(tr.canReduce());
        List<Variable> v = tr.getVars();
        assertEquals(1, v.size());
        assertEquals("b", v.get(0).getIdentifier());
        BoolTable t1 = tr.getTable();
        assertEquals(ThreeStateValue.one, t1.get(0));
        assertEquals(ThreeStateValue.zero, t1.get(1));
    }

    private static class ListBuilder<A, B> {
        private final ArrayList<A> list;
        private Factory<A, B> factory;

        ListBuilder(Factory<A, B> factory) {
            this.factory = factory;
            list = new ArrayList<>();
        }

        ListBuilder(Factory<A, B> factory, B[] bs) {
            this(factory);
            for (B b : bs)
                add(b);
        }

        public ListBuilder<A, B> add(B b) {
            list.add(factory.make(b));
            return this;
        }

        public ArrayList<A> list() {
            return list;
        }
    }

    public class Signals extends ListBuilder<Signal, String> {
        Signals(String... s) {
            super((a) -> new Signal(a, null), s);
        }
    }

    public class Vars extends ListBuilder<Variable, String> {
        Vars(String... s) {
            super(Variable::new, s);
        }
    }

    private interface Factory<A, B> {
        A make(B b);
    }
}

/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.data;

import junit.framework.TestCase;

import java.io.BufferedWriter;
import java.io.StringWriter;


/**
 */
public class ValueTableTest extends TestCase {
    private ValueTable t = new ValueTable("A", "B", "C")
            .add(new Value[]{new Value(0), new Value(0), new Value(0)})
            .add(new Value[]{new Value(0), new Value(1), Value.getHighZ()});


    public void testGeneral() throws Exception {
        assertEquals(3, t.getColumns());
        assertEquals(2, t.getRows());
        assertEquals("A", t.getColumnName(0));
        assertEquals("B", t.getColumnName(1));
        assertEquals("C", t.getColumnName(2));
        assertTrue(new Value(0).isEqualTo(t.getValue(0, 0)));
        assertTrue(new Value(1).isEqualTo(t.getValue(1, 1)));
        assertTrue(new Value("Z").isEqualTo(t.getValue(1, 2)));
    }

    public void testCSV() throws Exception {
        StringWriter sw = new StringWriter();
        t.saveCSV(new BufferedWriter(sw));
        assertEquals("\"step\",\"A\",\"B\",\"C\"\n" +
                "\"0\",\"0\",\"0\",\"0\"\n" +
                "\"1\",\"0\",\"1\",\"Z\"\n", sw.toString());
    }

    public void testMax() {
        ValueTable t = new ValueTable("A", "B", "C")
                .add(new Value[]{new Value(0), new Value(4), new Value(1)})
                .add(new Value[]{new Value(1), new Value(0), new Value(3)})
                .add(new Value[]{new Value(2), new Value(0), new Value(1)})
                .add(new Value[]{new Value(1), new Value(0), new Value(1)});
        assertEquals(2,t.getMax(0));
        assertEquals(4,t.getMax(1));
        assertEquals(3,t.getMax(2));
    }

    public void testOmit() {
        ValueTable t = new ValueTable("A");
        t.add(new Value[]{new Value(1)});
        t.add(new Value[]{new Value(2)}).omitInTable();
        t.add(new Value[]{new Value(3)});
        t.add(new Value[]{new Value(4)}).omitInTable();
        t.add(new Value[]{new Value(5)});
        assertEquals(3,t.getTableRows());
        assertTrue(new Value(1).isEqualTo(t.getTableValue(0,0)));
        assertTrue(new Value(3).isEqualTo(t.getTableValue(1,0)));
        assertTrue(new Value(5).isEqualTo(t.getTableValue(2,0)));
    }
}

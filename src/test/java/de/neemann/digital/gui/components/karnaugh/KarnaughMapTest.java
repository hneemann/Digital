/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.karnaugh;

import de.neemann.digital.analyse.expression.*;
import de.neemann.digital.analyse.parser.ParseException;
import de.neemann.digital.analyse.parser.Parser;
import de.neemann.digital.analyse.quinemc.BoolTableBoolArray;
import de.neemann.digital.analyse.quinemc.QuineMcCluskey;
import de.neemann.digital.analyse.quinemc.primeselector.PrimeSelectorDefault;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.Iterator;


public class KarnaughMapTest extends TestCase {

    public void testSimple2() throws IOException, ParseException, KarnaughException {
        Expression exp = new Parser("(A ¬B) ∨ (¬A B)").parse().get(0);
        KarnaughMap c = new KarnaughMap(Variable.vars(2), exp);

        assertEquals(2, c.size());

        for (KarnaughMap.Cover co : c)
            assertEquals(1, co.getSize());
    }

    public void testSimple2_singleVar() throws IOException, ParseException, KarnaughException {
        Expression exp = new Parser("A").parse().get(0);
        KarnaughMap c = new KarnaughMap(Variable.vars(2), exp);

        assertEquals(1, c.size());

        for (KarnaughMap.Cover co : c)
            assertEquals(2, co.getSize());
    }

    public void testSimple2_singleAnd() throws IOException, ParseException, KarnaughException {
        Expression exp = new Parser("A B").parse().get(0);
        KarnaughMap c = new KarnaughMap(Variable.vars(2), exp);

        assertEquals(1, c.size());

        for (KarnaughMap.Cover co : c)
            assertEquals(1, co.getSize());
    }

    public void testSimple3() throws IOException, ParseException, KarnaughException {
        Expression exp = new Parser("(A ¬C) ∨ (¬A ¬B) ∨ (B C)").parse().get(0);
        KarnaughMap c = new KarnaughMap(Variable.vars(3), exp);

        assertEquals(3, c.size());

        for (KarnaughMap.Cover co : c)
            assertEquals(2, co.getSize());
    }

    public void testSimple_BUG() throws IOException, ParseException, KarnaughException {
        Expression exp = new Parser("(¬A B ¬C) ∨ (A C) ∨ ¬B").parse().get(0);
        KarnaughMap c = new KarnaughMap(Variable.vars(3), exp);

        assertEquals(3, c.size());

        Iterator<KarnaughMap.Cover> it = c.iterator();
        assertEquals(1, it.next().getSize());
        assertEquals(2, it.next().getSize());
        assertEquals(4, it.next().getSize());
    }

    public void testSimple4() throws IOException, ParseException, KarnaughException {
        Expression exp = new Parser("(¬A ¬C ¬D) ∨ (A B C) ∨ (A ¬B D) ∨ (¬A ¬B C) ∨ (¬B ¬C ¬D)").parse().get(0);
        KarnaughMap c = new KarnaughMap(Variable.vars(4), exp);

        assertEquals(5, c.size());

        for (KarnaughMap.Cover co : c)
            assertEquals(2, co.getSize());
    }

    // in 4x4 map a 8 cell block is drawn in wrong orientation
    public void testBUG_1() throws IOException, ParseException, KarnaughException {
        Expression exp = Not.not(new Variable("D"));
        KarnaughMap c = new KarnaughMap(Variable.vars(4), exp);

        assertEquals(1, c.size());

        KarnaughMap.Cover co = c.iterator().next();
        assertTrue(co.isDisconnected());
        assertFalse(co.onlyEdges());
        assertTrue(co.isVerticalDivided());

        exp = Not.not(new Variable("B"));
        c = new KarnaughMap(Variable.vars(4), exp);

        assertEquals(1, c.size());

        co = c.iterator().next();
        assertTrue(co.isDisconnected());
        assertFalse(co.onlyEdges());
        assertFalse(co.isVerticalDivided());
    }

    /**
     * Creates bool tables with one "one".
     * Calculates the KV map.
     * Tests if the covered cell belongs to the single "one" in the table!
     */
    public void testIndex() throws IOException, ParseException, KarnaughException, ExpressionException {
        for (int vars = 2; vars <= 4; vars++) {
            int rows = 1 << vars;
            for (int row = 0; row < rows; row++) {
                BoolTableBoolArray t = new BoolTableBoolArray(rows); // create bool table
                t.set(row, true);                                    // put one one to the tabel
                Expression exp =
                        new QuineMcCluskey(Variable.vars(vars))
                                .fillTableWith(t)
                                .simplify(new PrimeSelectorDefault())
                                .getExpression();                    // create the expression
                KarnaughMap c = new KarnaughMap(Variable.vars(vars), exp);     // create the KV covers
                assertEquals(1, c.size());                 // there is only on cover
                KarnaughMap.Cover cover = c.iterator().next();
                assertEquals(1, cover.getSize());          // the size of the cover is one cell
                KarnaughMap.Pos pos = cover.getPos();
                // the row in the truth table is the row containing the one.
                assertEquals(row, c.getCell(pos.getRow(), pos.getCol()).getBoolTableRow());
            }
        }
    }


    /**
     * Tests if header description in 4x4 kv map is correct
     */
    public void testHeader4() throws IOException, ParseException, KarnaughException {
        KarnaughMap cov = new KarnaughMap(Variable.vars(4), Constant.ONE);
        KarnaughMap.Header head = cov.getHeaderLeft();
        assertEquals(4, head.size());
        for (int r = 0; r < 4; r++)
            for (int c = 0; c < 4; c++)
                assertTrue(cov.getCell(r, c).isVarInMinTerm(head.getVar(), head.getInvert(r)));

        head = cov.getHeaderRight();
        assertEquals(4, head.size());
        for (int r = 0; r < 4; r++)
            for (int c = 0; c < 4; c++)
                assertTrue(cov.getCell(r, c).isVarInMinTerm(head.getVar(), head.getInvert(r)));

        head = cov.getHeaderTop();
        assertEquals(4, head.size());
        for (int c = 0; c < 4; c++)
            for (int r = 0; r < 4; r++)
                assertTrue(cov.getCell(r, c).isVarInMinTerm(head.getVar(), head.getInvert(c)));

        head = cov.getHeaderBottom();
        assertEquals(4, head.size());
        for (int c = 0; c < 4; c++)
            for (int r = 0; r < 4; r++)
                assertTrue(cov.getCell(r, c).isVarInMinTerm(head.getVar(), head.getInvert(c)));

    }

    /**
     * Tests if header description in 2x4 kv map is correct
     */
    public void testHeader3() throws IOException, ParseException, KarnaughException {
        KarnaughMap cov = new KarnaughMap(Variable.vars(3), Constant.ONE);
        KarnaughMap.Header head = cov.getHeaderLeft();
        assertEquals(2, head.size());
        for (int r = 0; r < 2; r++)
            for (int c = 0; c < 4; c++)
                assertTrue(cov.getCell(r, c).isVarInMinTerm(head.getVar(), head.getInvert(r)));

        assertNull(cov.getHeaderRight());

        head = cov.getHeaderTop();
        assertEquals(4, head.size());
        for (int c = 0; c < 4; c++)
            for (int r = 0; r < 2; r++)
                assertTrue(cov.getCell(r, c).isVarInMinTerm(head.getVar(), head.getInvert(c)));

        head = cov.getHeaderBottom();
        assertEquals(4, head.size());
        for (int c = 0; c < 4; c++)
            for (int r = 0; r < 2; r++)
                assertTrue(cov.getCell(r, c).isVarInMinTerm(head.getVar(), head.getInvert(c)));
    }

    /**
     * Tests if header description in 2x2 kv map is correct
     */
    public void testHeader2() throws IOException, ParseException, KarnaughException {
        KarnaughMap cov = new KarnaughMap(Variable.vars(2), Constant.ONE);
        KarnaughMap.Header head = cov.getHeaderLeft();
        assertEquals(2, head.size());
        for (int r = 0; r < 2; r++)
            for (int c = 0; c < 2; c++)
                assertTrue(cov.getCell(r, c).isVarInMinTerm(head.getVar(), head.getInvert(r)));

        assertNull(cov.getHeaderRight());

        head = cov.getHeaderTop();
        assertEquals(2, head.size());
        for (int c = 0; c < 2; c++)
            for (int r = 0; r < 2; r++)
                assertTrue(cov.getCell(r, c).isVarInMinTerm(head.getVar(), head.getInvert(c)));

        assertNull(cov.getHeaderBottom());
    }

    /**
     * Test if kv map is really a kv map
     * Only the state of one var differs between neighbour cells.
     */
    public void testKVLayout() throws KarnaughException {
        int checks = 0;
        for (int vars = 2; vars <= 4; vars++) {
            for (int mode = 0; mode < 16; mode++) {
                KarnaughMap map = new KarnaughMap(Variable.vars(vars), Constant.ONE, new MapLayout(vars));
                for (int r = 0; r < map.getRows(); r++)
                    for (int c = 0; c < map.getColumns(); c++) {
                        KarnaughMap.Cell cell = map.getCell(r, c);
                        compareCells(cell, map.getCell(r, inc(c, map.getColumns())), vars);
                        compareCells(cell, map.getCell(inc(r, map.getRows()), c), vars);
                        checks += 2;
                    }
            }
        }
        assertEquals((16 + 8 + 4) * 2 * 16, checks);
    }

    private void compareCells(KarnaughMap.Cell a, KarnaughMap.Cell b, int vars) {
        int count = 0;
        for (int v = 0; v < vars; v++) {
            if (a.isVarInMinTerm(v, false) != b.isVarInMinTerm(v, false))
                count++;
        }
        assertEquals(1, count);
    }

    private int inc(int v, int size) {
        v++;
        if (v == size) v = 0;
        return v;
    }

}

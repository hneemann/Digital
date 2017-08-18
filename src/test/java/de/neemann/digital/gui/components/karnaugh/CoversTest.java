package de.neemann.digital.gui.components.karnaugh;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.parser.ParseException;
import de.neemann.digital.analyse.parser.Parser;
import de.neemann.digital.analyse.quinemc.BoolTable;
import de.neemann.digital.analyse.quinemc.BoolTableBoolArray;
import de.neemann.digital.analyse.quinemc.QuineMcCluskey;
import de.neemann.digital.analyse.quinemc.ThreeStateValue;
import de.neemann.digital.analyse.quinemc.primeselector.PrimeSelectorDefault;
import junit.framework.TestCase;

import java.io.IOException;


public class CoversTest extends TestCase {

    public void testSimple2() throws IOException, ParseException, KarnaughException {
        Expression exp = new Parser("(A ¬B) ∨ (¬A B)").parse().get(0);
        Covers c = new Covers(Variable.vars(2), exp);

        assertEquals(2, c.size());

        for (Covers.Cover co : c)
            assertEquals(1, co.getSize());
    }

    public void testSimple2_singleVar() throws IOException, ParseException, KarnaughException {
        Expression exp = new Parser("A").parse().get(0);
        Covers c = new Covers(Variable.vars(2), exp);

        assertEquals(1, c.size());

        for (Covers.Cover co : c)
            assertEquals(2, co.getSize());
    }

    public void testSimple2_singleAnd() throws IOException, ParseException, KarnaughException {
        Expression exp = new Parser("A B").parse().get(0);
        Covers c = new Covers(Variable.vars(2), exp);

        assertEquals(1, c.size());

        for (Covers.Cover co : c)
            assertEquals(1, co.getSize());
    }

    public void testSimple3() throws IOException, ParseException, KarnaughException {
        Expression exp = new Parser("(A ¬C) ∨ (¬A ¬B) ∨ (B C)").parse().get(0);
        Covers c = new Covers(Variable.vars(3), exp);

        assertEquals(3, c.size());

        for (Covers.Cover co : c)
            assertEquals(2, co.getSize());
    }

    public void testSimple4() throws IOException, ParseException, KarnaughException {
        Expression exp = new Parser("(¬A ¬C ¬D) ∨ (A B C) ∨ (A ¬B D) ∨ (¬A ¬B C) ∨ (¬B ¬C ¬D)").parse().get(0);
        Covers c = new Covers(Variable.vars(4), exp);

        assertEquals(5, c.size());

        for (Covers.Cover co : c)
            assertEquals(2, co.getSize());
    }

    /**
     * Creates bool tables with one 1.
     * Calculates the KV map.
     * Tests if the covered cell belongs to the one in the table!
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
                Covers c = new Covers(Variable.vars(vars), exp);     // create the KV covers
                assertEquals(1, c.size());                 // there is only on cover
                Covers.Cover cover = c.iterator().next();
                assertEquals(1, cover.getSize());          // the size of the cover is one cell
                Covers.Pos pos = cover.getPos();
                                                                     // the row in the truth table is the row containing the one.
                assertEquals(row, c.getCell(pos.getRow(), pos.getCol()).getIndex());
            }
        }
    }


}
package de.neemann.digital.gui.components.karnaugh;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.parser.ParseException;
import de.neemann.digital.analyse.parser.Parser;
import junit.framework.TestCase;

import java.io.IOException;


public class CoversTest extends TestCase {


    public void testSimple4() throws IOException, ParseException, KarnaughException {
        Expression exp = new Parser("(¬A ¬C ¬D) ∨ (A B C) ∨ (A ¬B D) ∨ (¬A ¬B C) ∨ (¬B ¬C ¬D)").parse().get(0);
        Covers c = new Covers(Variable.vars(4), exp);

        assertEquals(5, c.size());

        for (Covers.Cover co : c) {
            Covers.Pos pos = co.getPos();
            assertEquals(2, pos.getHeight() * pos.getWidth());
        }
    }


}
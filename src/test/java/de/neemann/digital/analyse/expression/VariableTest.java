package de.neemann.digital.analyse.expression;

import junit.framework.TestCase;

import java.util.ArrayList;

import static de.neemann.digital.analyse.expression.Variable.vars;

/**
 * @author hneemann
 */
public class VariableTest extends TestCase {

    public void testVars() throws Exception {
        ArrayList<Variable> v = vars(5);
        assertEquals(5, v.size());
        assertEquals("A", v.get(0).getIdentifier());
        assertEquals("B", v.get(1).getIdentifier());
        assertEquals("C", v.get(2).getIdentifier());
        assertEquals("D", v.get(3).getIdentifier());
        assertEquals("E", v.get(4).getIdentifier());
    }
}
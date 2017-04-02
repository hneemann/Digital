package de.neemann.digital.analyse.expression;

import junit.framework.TestCase;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;

/**
 * Created by hneemann on 02.04.17.
 */
public class CopyTest extends TestCase {

    public void testIntegral() throws ExpressionException {
        Variable a = new Variable("A");
        Variable b = new Variable("B");
        Variable c = new Variable("C");
        Expression e1 = and(not(or(not(a), not(b), c)), not(and(not(a), not(b))));
        Expression e2 = e1.copy();

        ContextFiller fc = new ContextFiller(e1);

        for (int i = 0; i < fc.getRowCount(); i++) {
            fc.setContextTo(i);
            assertEquals(e1.calculate(fc), e2.calculate(fc));
        }
    }
}

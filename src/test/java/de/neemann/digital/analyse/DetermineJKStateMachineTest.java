/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse;


import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.expression.format.FormatToExpression;
import junit.framework.TestCase;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;
import static de.neemann.digital.analyse.expression.Variable.v;


/**
 */
public class DetermineJKStateMachineTest extends TestCase {

    private Expression a;
    private Expression nota;
    private Expression b;
    private Expression notb;
    private Expression c;
    private Expression notc;

    public void setUp() {
        a = v("a");
        nota = not(a);
        b = v("b");
        notb = not(b);
        c = v("c");
        notc = not(c);
    }

    public void testSimple() throws Exception {

        Expression e = or(and(a, c), and(nota, notb));

        DetermineJKStateMachine jk = new DetermineJKStateMachine("a", e);
        assertEquals(toStr(notb), toStr(jk.getJ()));
        assertEquals(toStr(notc), toStr(jk.getK()));
        assertFalse(jk.isDFF());
    }

    public void testIndex() throws Exception {
        Variable _a = v("Q_0^n");
        Expression _nota = not(_a);
        Expression _b = v("Q_1^n");
        Expression _notb = not(_b);
        Expression _c = v("Q_2^n");
        Expression _notc = not(_c);

        Expression e = or(and(_a, _c), and(_nota, _notb));

        DetermineJKStateMachine jk = new DetermineJKStateMachine(_a.getIdentifier(), e);
        assertEquals(toStr(_notb), toStr(jk.getJ()));
        assertEquals(toStr(_notc), toStr(jk.getK()));
        assertFalse(jk.isDFF());
    }

    private String toStr(Expression expression) {
        return FormatToExpression.UNICODE.format(expression);
    }

    public void testSimple2() throws Exception {

        Expression e = or(and(a, c), and(nota, notb), and(b, c));

        DetermineJKStateMachine jk = new DetermineJKStateMachine("a", e);
        assertEquals("(b ∧ c) ∨ ¬b", toStr(jk.getJ()));
        assertEquals("¬((b ∧ c) ∨ c)", toStr(jk.getK()));
        assertFalse(jk.isDFF());
    }

    public void testSimple3() throws Exception {
        Expression e = or(nota);

        DetermineJKStateMachine jk = new DetermineJKStateMachine("a", e);
        assertEquals("1", toStr(jk.getJ()));
        assertEquals("1", toStr(jk.getK()));
        assertFalse(jk.isDFF());
    }

    public void testSimple4() throws Exception {
        Expression e = or(a);

        DetermineJKStateMachine jk = new DetermineJKStateMachine("a", e);
        assertEquals("0", toStr(jk.getJ()));
        assertEquals("0", toStr(jk.getK()));
        assertFalse(jk.isDFF());
    }

    public void testSimpleD() throws Exception {
        Expression e = or(and(a, b), and(nota, notb));

        DetermineJKStateMachine jk = new DetermineJKStateMachine("c", e);
        assertEquals("(¬a ∧ ¬b) ∨ (a ∧ b)", toStr(jk.getJ()));
        assertEquals("(¬a ∧ ¬b) ∨ (a ∧ b)", toStr(jk.getNK()));
        assertTrue(jk.isDFF());
    }

    public void testSimpleBUG() throws Exception {
        Expression e = or(b, nota);

        DetermineJKStateMachine jk = new DetermineJKStateMachine("a", e);
        assertEquals("1", toStr(jk.getSimplifiedJ()));
        assertEquals("¬b", toStr(jk.getSimplifiedK()));
        assertFalse(jk.isDFF());
    }

}

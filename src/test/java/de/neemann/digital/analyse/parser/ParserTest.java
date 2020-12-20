/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.parser;

import de.neemann.digital.analyse.expression.*;
import de.neemann.digital.analyse.quinemc.QuineMcCluskey;
import de.neemann.digital.core.io.Const;
import junit.framework.TestCase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class ParserTest extends TestCase {

    private static Expression createSingle(String str) throws IOException, ParseException {
        List<Expression> expr = new Parser(str).parse();
        assertEquals(1, expr.size());
        return expr.get(0);
    }

    public void testIdent() throws Exception {
        assertEquals(new Variable("C"), createSingle("C"));
        assertEquals(new Variable("A_1"), createSingle("A_1"));
        assertEquals(new Variable("A\\_1"), createSingle("A\\_1"));
    }

    public void testConst() throws Exception {
        assertEquals(Constant.ZERO, createSingle("0"));
        assertEquals(Constant.ONE, createSingle("1"));
    }

    public void testParseOr() throws Exception {
        assertTrue(createSingle("a+b") instanceof Operation.Or);
        assertTrue(createSingle("a#b") instanceof Operation.Or);
        assertTrue(createSingle("a∨b") instanceof Operation.Or);
        assertTrue(createSingle("a|b") instanceof Operation.Or);
        assertTrue(createSingle("a||b") instanceof Operation.Or);
    }

    public void testParseXOr() throws Exception {
        assertTrue(createSingle("a^b") instanceof Operation.XOr);
        assertTrue(createSingle("a⊻b") instanceof Operation.XOr);

        assertEquals("not(a)", createSingle("a^1").toString());
        assertEquals("a", createSingle("a^0").toString());
        assertEquals("not(a)", createSingle("1^a").toString());
        assertEquals("a", createSingle("0^a").toString());
        assertEquals("false", createSingle("0^0").toString());
        assertEquals("false", createSingle("1^1").toString());
        assertEquals("true", createSingle("1^0").toString());
        assertEquals("true", createSingle("0^1").toString());
    }

    public void testParseAnd() throws Exception {
        assertTrue(createSingle("a*b") instanceof Operation.And);
        assertTrue(createSingle("a∧b") instanceof Operation.And);
        assertTrue(createSingle("a&b") instanceof Operation.And);
        assertTrue(createSingle("a&&b") instanceof Operation.And);
        assertTrue(createSingle("a b") instanceof Operation.And);
    }

    public void testParseParenthesis() throws Exception {
        Expression exp = createSingle("a*(b+c)");
        assertTrue(exp instanceof Operation.And);
        ArrayList<Expression> expList = ((Operation) exp).getExpressions();
        assertEquals(2, expList.size());
        assertTrue(expList.get(0) instanceof Variable);
        assertTrue(expList.get(1) instanceof Operation.Or);
        expList = ((Operation) expList.get(1)).getExpressions();
        assertEquals(2, expList.size());
        assertEquals(new Variable("b"), expList.get(0));
        assertEquals(new Variable("c"), expList.get(1));
    }

    public void testParseNot() throws Exception {
        Expression exp = createSingle("!a");
        assertTrue(exp instanceof Not);
        assertTrue(((Not) exp).getExpression() instanceof Variable);

        exp = createSingle("a'");
        assertTrue(exp instanceof Not);
        assertTrue(((Not) exp).getExpression() instanceof Variable);

        assertTrue(createSingle("~a") instanceof Not);
        assertTrue(createSingle("¬a") instanceof Not);

        assertEquals(Constant.ZERO, createSingle("1'"));
        assertEquals(Constant.ONE, createSingle("0'"));
        assertEquals(Constant.ZERO, createSingle("0''"));
        assertEquals(Constant.ONE, createSingle("1''"));
    }

    public void testParseEqual() throws Exception {
        assertEquals("not(xor(a,b))", createSingle("a=b").toString());
        assertEquals("xor(a,b)", createSingle("a!=b").toString());
        assertEquals("xor(a,b)", createSingle("!(a=b)").toString());
        assertEquals("and(not(xor(a,b)),not(xor(a,c)))", createSingle("(a=b)&(a=c)").toString());
        assertEquals("and(not(xor(a,b)),not(xor(a,c)))", createSingle("a=b & a=c").toString());
        assertEquals("a", createSingle("a=1").toString());
        assertEquals("not(a)", createSingle("a=0").toString());
        assertEquals("a", createSingle("a!=0").toString());
        assertEquals("not(a)", createSingle("a!=1").toString());
        assertEquals("and(a,not(b))", createSingle("a=1 & b=0").toString());
        assertEquals("and(a,not(b))", createSingle("a=1  b=0").toString());
    }

    public void testParseLet() throws Exception {
        Expression exp = createSingle("let u=a+b");
        assertTrue(exp instanceof NamedExpression);
        assertEquals("u", ((NamedExpression) exp).getName());
        assertTrue(((NamedExpression) exp).getExpression() instanceof Operation.Or);
    }

    public void testParseLetError() throws Exception {
        try {
            createSingle("let u+a+b");
            assertTrue(false);
        } catch (ParseException e) {
            assertTrue(true);
        }
    }

    public void testParseList() throws Exception {
        ArrayList<Expression> expList = new Parser("a,b,c").parse();
        assertEquals(3, expList.size());
        assertEquals(new Variable("a"), expList.get(0));
        assertEquals(new Variable("b"), expList.get(1));
        assertEquals(new Variable("c"), expList.get(2));
    }

    public void testParseList2() throws Exception {
        ArrayList<Expression> expList = new Parser("let u=a,let v=b,let w=c").parse();
        assertEquals(3, expList.size());
        assertTrue(expList.get(0) instanceof NamedExpression);
        assertEquals(((NamedExpression) expList.get(0)).getName(), "u");
        assertEquals(((NamedExpression) expList.get(0)).getExpression(), new Variable("a"));
        assertTrue(expList.get(1) instanceof NamedExpression);
        assertEquals(((NamedExpression) expList.get(1)).getName(), "v");
        assertEquals(((NamedExpression) expList.get(1)).getExpression(), new Variable("b"));
        assertTrue(expList.get(2) instanceof NamedExpression);
        assertEquals(((NamedExpression) expList.get(2)).getName(), "w");
        assertEquals(((NamedExpression) expList.get(2)).getExpression(), new Variable("c"));
    }

    public void testParseRegression() throws Exception {
        Expression e = createSingle("B*(B+A)*(B+C)*(A+B+C)");
        Expression simplified = QuineMcCluskey.simplify(e);
        assertEquals(new Variable("B"), simplified);
    }

    public void testParseRegressionOmitAnd() throws Exception {
        Expression e = createSingle("B(B+A)(B+C)(A+B+C)");
        Expression simplified = QuineMcCluskey.simplify(e);
        assertEquals(new Variable("B"), simplified);

        e = createSingle("B (B+A) (B+C) (A+B+C)");
        simplified = QuineMcCluskey.simplify(e);
        assertEquals(new Variable("B"), simplified);
    }

    public void testParseRegression2() throws Exception {
        Expression e = createSingle("(C ∨ B) ∧ (A ∨ C) ∧ (B ∨ ¬C) ∧ (C ∨ ¬A)");
        Expression simplified = QuineMcCluskey.simplify(e);
        assertTrue(simplified instanceof Operation.And);
        ArrayList<Expression> expList = ((Operation) simplified).getExpressions();
        assertEquals(2, expList.size());
        assertEquals(new Variable("B"), expList.get(0));
        assertEquals(new Variable("C"), expList.get(1));
    }

    public void testParseRegression2OmitAnd() throws Exception {
        Expression e = createSingle("(C ∨ B)  (A ∨ C)  (B ∨ ¬C)  (C ∨ ¬A)");
        Expression simplified = QuineMcCluskey.simplify(e);
        assertTrue(simplified instanceof Operation.And);
        ArrayList<Expression> expList = ((Operation) simplified).getExpressions();
        assertEquals(2, expList.size());
        assertEquals(new Variable("B"), expList.get(0));
        assertEquals(new Variable("C"), expList.get(1));
    }

    public void testParseException() throws Exception {
        Parser p = new Parser("C+");
        try {
            p.parse();
            assertTrue(false);
        } catch (ParseException e) {
            assertTrue(true);
        }
    }

    public void testParseException2() throws Exception {
        Parser p = new Parser("(C");
        try {
            p.parse();
            assertTrue(false);
        } catch (ParseException e) {
            assertTrue(true);
        }
    }

    public void testParseException3() throws Exception {
        Parser p = new Parser("*C");
        try {
            p.parse();
            assertTrue(false);
        } catch (ParseException e) {
            assertTrue(true);
        }
    }

    public void testParseException4() throws Exception {
        Parser p = new Parser("A )");
        try {
            p.parse();
            assertTrue(false);
        } catch (ParseException e) {
            assertTrue(true);
        }
    }

    public void testParseException5() throws Exception {
        Parser p = new Parser("ö");
        try {
            p.parse();
            assertTrue(false);
        } catch (ParseException e) {
            assertTrue(true);
        }
    }

}

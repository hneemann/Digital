package de.neemann.digital.analyse.parser;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Not;
import de.neemann.digital.analyse.expression.Operation;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.quinemc.QuineMcCluskey;
import junit.framework.TestCase;

import java.util.ArrayList;

/**
 * @author hneemann
 */
public class ParserTest extends TestCase {

    public void testIdent() throws Exception {
        assertEquals(new Variable("C"), new Parser("C").parse());
        assertEquals(new Variable("A_1"), new Parser("A_1").parse());
        assertEquals(new Variable("A^1"), new Parser("A^1").parse());
    }

    public void testParseOr() throws Exception {
        assertTrue(new Parser("a+b").parse() instanceof Operation.Or);
        assertTrue(new Parser("a ∨ b").parse() instanceof Operation.Or);
        assertTrue(new Parser("a|b").parse() instanceof Operation.Or);
    }

    public void testParseAnd() throws Exception {
        assertTrue(new Parser("a*b").parse() instanceof Operation.And);
        assertTrue(new Parser("a ∧ b").parse() instanceof Operation.And);
        assertTrue(new Parser("a&b").parse() instanceof Operation.And);
    }

    public void testParseParenthesis() throws Exception {
        Parser p = new Parser("a*(b+c)");
        Expression exp = p.parse();
        assertTrue(exp instanceof Operation.And);
        ArrayList<Expression> expList = ((Operation) exp).getExpressions();
        assertEquals(2,expList.size());
        assertTrue(expList.get(0) instanceof Variable);
        assertTrue(expList.get(1) instanceof Operation.Or);
        expList = ((Operation) expList.get(1)).getExpressions();
        assertEquals(2,expList.size());
        assertEquals(new Variable("b"),expList.get(0));
        assertEquals(new Variable("c"),expList.get(1));
    }

    public void testParseNot() throws Exception {
        Parser p = new Parser("!a");
        Expression exp = p.parse();
        assertTrue(exp instanceof Not);
        assertTrue(((Not)exp).getExpression() instanceof Variable);
    }

    public void testParseRegression() throws Exception {
        Parser p = new Parser("B*(B+A)*(B+C)*(A+B+C)");
        Expression e = p.parse();
        Expression simplified = QuineMcCluskey.simplify(e);
        assertEquals(new Variable("B"),simplified);
    }

    public void testParseRegression2() throws Exception {
        Parser p = new Parser("(C ∨ B) ∧ (A ∨ C) ∧ (B ∨ !C) ∧ (C ∨ !A)");
        Expression e = p.parse();
        Expression simplified = QuineMcCluskey.simplify(e);
        assertTrue(simplified instanceof Operation.And);
        ArrayList<Expression> expList = ((Operation) simplified).getExpressions();
        assertEquals(2,expList.size());
        assertEquals(new Variable("B"),expList.get(0));
        assertEquals(new Variable("C"),expList.get(1));
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


}
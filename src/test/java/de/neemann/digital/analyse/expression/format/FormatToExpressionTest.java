/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression.format;


import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.NamedExpression;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.analyse.parser.Parser;
import de.neemann.digital.draw.graphics.text.formatter.LaTeXFormatter;
import junit.framework.TestCase;

import java.util.ArrayList;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;
import static de.neemann.digital.analyse.expression.Variable.v;

/**
 */
public class FormatToExpressionTest extends TestCase {

    public void testFormatExp() throws Exception {
        Variable a = v("A");
        Variable b = v("B");
        Expression e = and(not(or(not(a), not(b))), not(and(not(a), not(b))));

        assertEquals("!(!A || !B) && !(!A && !B)", FormatToExpression.JAVA.format(e));
        assertEquals("NOT (NOT A OR NOT B) AND NOT (NOT A AND NOT B)", FormatToExpression.DERIVE.format(e));
        assertEquals("~(~A + ~B) ~(~A ~B)", FormatToExpression.LOGISIM.format(e));
        assertEquals("¬(¬A ∨ ¬B) ∧ ¬(¬A ∧ ¬B)", FormatToExpression.UNICODE.format(e));
        assertEquals("!(!A + !B) * !(!A * !B)", FormatToExpression.SHORT.format(e));
        assertEquals("!(!A + !B) !(!A !B)", FormatToExpression.SHORTER.format(e));

        assertEquals("\\overline{\\overline{A} \\oder \\overline{B}} \\und \\overline{\\overline{A} \\und \\overline{B}}", LaTeXFormatter.format(e));
    }

    public void testFormatExp2() throws Exception {
        Variable a = v("A");
        Variable b = v("B");
        Variable c = v("C");
        Expression e = or(and(a, not(b), c), and(a, not(b), not(c)));

        assertEquals("(A !B C) + (A !B !C)", FormatToExpression.SHORTER.format(e));
    }

    public void testFormatNamesExp() throws Exception {
        Variable a = v("A");
        Variable b = v("B");
        Expression e = and(a, b);
        NamedExpression n = new NamedExpression("U", e);
        assertEquals("U = A ∧ B", FormatToExpression.UNICODE.format(n));
        n = new NamedExpression("V", n);
        assertEquals("V = U = A ∧ B", FormatToExpression.UNICODE.format(n));
    }

    public void testFormatExpNot() throws Exception, FormatterException {
        Variable a = new Variable("A");
        Expression e = not(a);

        assertEquals("¬A", FormatToExpression.UNICODE.format(e));
    }

    public void testFormatExpNot2() throws Exception {
        Variable a = v("A");
        Variable b = v("B");
        Variable c = v("C");
        Expression e = or(and(a, b), not(c));

        assertEquals("(A ∧ B) ∨ ¬C", FormatToExpression.UNICODE.format(e));
    }

    public void testFormatExpLaTeX() throws Exception {
        Variable a = new Variable("A_n");
        Variable b = new Variable("B_n");
        Expression e = new NamedExpression("Y^{n+1}", and(a, not(b)));
        assertEquals("Y^{n+1} &=& A_n \\und \\overline{B_n}", LaTeXFormatter.format(e));
    }

    public void testFormatTable() throws Exception {
        Variable a = new Variable("A");
        Variable b = new Variable("B");
        Expression e = and(not(or(not(a), not(b))), not(and(not(a), not(b))));

        assertEquals("AB|Y\n" +
                "00|0\n" +
                "01|0\n" +
                "10|0\n" +
                "11|1\n", new FormatToTable().format(e));
    }

    public void testFormatLatex() throws Exception {
        Variable a = new Variable("A");
        Variable b = new Variable("B");
        Expression e = and(not(or(not(a), not(b))), not(and(not(a), not(b))));

        assertEquals("\\begin{tabular}{cc|c}\n" +
                "$A$&$B$&$Y$\\\\\n" +
                "\\hline\n" +
                "0&0&0\\\\\n" +
                "0&1&0\\\\\n" +
                "1&0&0\\\\\n" +
                "1&1&1\\\\\n" +
                "\\end{tabular}\n", new FormatToTableLatex().format(e));
    }


    public void testFormatXOr() throws Exception {
        ArrayList<Expression> e = new Parser("let sum=(A^B)^C, let c = (A B) + ((A^B) C)").parse();
        assertEquals("sum = (A ⊻ B) ⊻ C", FormatToExpression.UNICODE.format(e.get(0)));
        assertEquals("c = (A ∧ B) ∨ ((A ⊻ B) ∧ C)", FormatToExpression.UNICODE.format(e.get(1)));
        assertEquals("sum &=& (A \\xoder B) \\xoder C", LaTeXFormatter.format(e.get(0)));
        assertEquals("sum = (A $ B) $ C", FormatToExpression.CUPL.format(e.get(0)));
        assertEquals("sum = (A ^ B) ^ C", FormatToExpression.JAVA.format(e.get(0)));
        assertEquals("sum = (A ^ B) ^ C", FormatToExpression.SHORT.format(e.get(0)));
        assertEquals("sum = (A ^ B) ^ C", FormatToExpression.SHORTER.format(e.get(0)));
    }
}

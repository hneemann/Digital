package de.neemann.digital.analyse.expression.format;


import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Variable;
import junit.framework.TestCase;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;
import static de.neemann.digital.analyse.expression.Variable.v;

/**
 * @author hneemann
 */
public class FormatterTest extends TestCase {

    public void testFormatExp() throws Exception, FormatterException {
        Variable a = v("A");
        Variable b = v("B");
        Expression e = and(not(or(not(a), not(b))), not(and(not(a), not(b))));

        assertEquals("!(!A || !B) && !(!A && !B)", FormatToExpression.FORMATTER_JAVA.format(e));
        assertEquals("\\nicht{\\nicht{A} \\oder \\nicht{B}} \\und \\nicht{\\nicht{A} \\und \\nicht{B}}", FormatToExpression.FORMATTER_LATEX.format(e));
        assertEquals("NOT (NOT A OR NOT B) AND NOT (NOT A AND NOT B)", FormatToExpression.FORMATTER_DERIVE.format(e));
        assertEquals("~(~A + ~B)  ~(~A  ~B)", FormatToExpression.FORMATTER_LOGISIM.format(e));
        assertEquals("¬(¬A ∨ ¬B) ∧ ¬(¬A ∧ ¬B)", FormatToExpression.FORMATTER_UNICODE.format(e));
    }

    public void testFormatExpNot() throws Exception, FormatterException {
        Variable a = new Variable("A");
        Expression e = not(a);

        assertEquals("¬A", FormatToExpression.FORMATTER_UNICODE.format(e));
    }

    public void testFormatExpNot2() throws Exception, FormatterException {
        Variable a = v("A");
        Variable b = v("B");
        Variable c = v("C");
        Expression e = or(and(a, b), not(c));

        assertEquals("(A ∧ B) ∨ ¬C", FormatToExpression.FORMATTER_UNICODE.format(e));
    }


    public void testFormatExpLaTeX() throws Exception, FormatterException {
        Variable a = new Variable("A_n");
        Variable b = new Variable("B_n");
        Expression e = and(a, not(b));
        assertEquals("Y_{n+1}=A_{n} \\und \\nicht{B_{n}}", FormatToExpression.FORMATTER_LATEX.format("Y_n+1", e));
    }


    public void testFormatTable() throws Exception, FormatterException {
        Variable a = new Variable("A");
        Variable b = new Variable("B");
        Expression e = and(not(or(not(a), not(b))), not(and(not(a), not(b))));

        assertEquals("AB|Y\n" +
                "00|0\n" +
                "01|0\n" +
                "10|0\n" +
                "11|1\n", new FormatToTable().format("", e));

    }

    public void testFormatLatex() throws Exception, FormatterException {
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
                "\\end{tabular}\n", new FormatToTableLatex().format("", e));

    }

}
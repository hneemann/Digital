/*
 * Copyright (c) 2019 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.text.formatter;

import de.neemann.digital.analyse.expression.format.FormatToExpression;
import de.neemann.digital.draw.graphics.text.ParseException;
import de.neemann.digital.draw.graphics.text.Parser;
import junit.framework.TestCase;

public class PlainTextFormatterTest extends TestCase {

    public void testSimple() throws ParseException {
        assertEquals("Hello World", PlainTextFormatter.format(new Parser("Hello World").parse(), FormatToExpression.FORMATTER_JAVA));
        assertEquals("Q", PlainTextFormatter.format(new Parser("Q").parse(), FormatToExpression.FORMATTER_JAVA));
        assertEquals("Q", PlainTextFormatter.format(new Parser("$Q$").parse(), FormatToExpression.FORMATTER_JAVA));
        assertEquals("Q0n+1", PlainTextFormatter.format(new Parser("Q_0^{n+1}").parse(), FormatToExpression.FORMATTER_JAVA));
        assertEquals("Q0n", PlainTextFormatter.format(new Parser("Q_0^n").parse(), FormatToExpression.FORMATTER_JAVA));
        assertEquals("Q0n", PlainTextFormatter.format(new Parser("Q^n_0").parse(), FormatToExpression.FORMATTER_JAVA));
        assertEquals("!Q", PlainTextFormatter.format(new Parser("~{Q}").parse(), FormatToExpression.FORMATTER_JAVA));
        assertEquals("!(A+B)", PlainTextFormatter.format(new Parser("~{A+B}").parse(), FormatToExpression.FORMATTER_JAVA));
        assertEquals("!(!Q)", PlainTextFormatter.format(new Parser("~{~Q}").parse(), FormatToExpression.FORMATTER_JAVA));

        assertEquals("!a0n", PlainTextFormatter.format(new Parser("~{a_0^n}").parse(), FormatToExpression.FORMATTER_JAVA));
    }

}
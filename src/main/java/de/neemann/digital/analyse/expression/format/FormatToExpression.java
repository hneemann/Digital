/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression.format;

import de.neemann.digital.analyse.expression.*;
import de.neemann.digital.draw.graphics.text.formatter.PlainTextFormatter;
import de.neemann.digital.draw.graphics.text.text.ExpressionToText;

import java.util.Objects;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;
import static de.neemann.digital.analyse.expression.Variable.v;

/**
 * Used to format an expression to a simple string.
 * The hard work is meanwhile done by two separate classes called {@link PlainTextFormatter} and {@link ExpressionToText}.
 * At first the expression is converted to a {@link de.neemann.digital.draw.graphics.text.text.Text} instance and then
 * formatted to a string by the {@link PlainTextFormatter}.
 */
public final class FormatToExpression implements Formatter {

    /**
     * Creates a string compatible to Java
     */
    public static final FormatToExpression FORMATTER_JAVA = new FormatToExpression("||", "&&", "^", "!", "false", "true", "=");
    /**
     * Creates a string compatible to Derive
     */
    public static final FormatToExpression FORMATTER_DERIVE = new FormatToExpression("OR", "AND", "XOR", "NOT ", "false", "true", "=");
    /**
     * Creates a string compatible to WinCUPL
     */
    public static final FormatToExpression FORMATTER_CUPL = new FormatToExpression("#", "&", "$", "!", "'b'0", "'b'1", "=").setKeepVars();
    /**
     * Creates a string compatible to Logisim
     */
    public static final FormatToExpression FORMATTER_LOGISIM = new FormatToExpression("+", "", "^", "~", "false", "true", "=");
    /**
     * Creates a unicode string
     */
    public static final FormatToExpression FORMATTER_UNICODE = new FormatToExpression("\u2228", "\u2227", "\u22BB", "\u00AC", "0", "1", "=");
    /**
     * Creates a unicode string with no AND character
     */
    public static final FormatToExpression FORMATTER_UNICODE_NOAND = new FormatToExpression("\u2228", "", "\u22BB", "\u00AC", "0", "1", "=");
    /**
     * Creates a short string representation
     */
    public static final FormatToExpression FORMATTER_SHORT = new FormatToExpression("+", "*", "^", "!", "0", "1", "=");
    /**
     * Creates a short string representation
     */
    public static final FormatToExpression FORMATTER_SHORTER = new FormatToExpression("+", "", "^", "!", "0", "1", "=");
    /**
     * Creates a LaTeX representation
     */
    public static final FormatToExpression FORMATTER_LATEX = new FormatToExpression("\\oder", "\\und", "\\xoder", "", "0", "1", "&=&");


    private static final Expression TOSTRING_EXPR;
    private static FormatToExpression defaultFormat = FORMATTER_UNICODE;

    static {
        Variable a = v("A");
        Variable b = v("B");
        TOSTRING_EXPR = or(and(a, not(b)), and(not(a), b), Constant.ZERO);
    }


    private static FormatToExpression[] availFormats = new FormatToExpression[]{
            FORMATTER_UNICODE,
            FORMATTER_UNICODE_NOAND,
            FORMATTER_DERIVE,
            FORMATTER_JAVA,
            FORMATTER_CUPL,
            FORMATTER_LOGISIM,
            FORMATTER_SHORT,
            FORMATTER_SHORTER
    };

    /**
     * Formats a expression to a string.
     * Uses the default format for presentation on the screen.
     *
     * @param exp the expression to format
     * @return the string representation
     */
    public static String defaultFormat(Expression exp) {
        return defaultFormat.format(exp);
    }

    /**
     * Sets the default format
     *
     * @param defaultFormat the default format
     */
    public static void setDefaultFormat(FormatToExpression defaultFormat) {
        FormatToExpression.defaultFormat = defaultFormat;
    }

    private final String orString;
    private final String andString;
    private final String falseString;
    private final String trueString;
    private final String equal;
    private final String xorString;
    private final String notString;
    private boolean keepVars;

    private FormatToExpression(String orString, String andString, String xorString, String notString, String falseString, String trueString, String equal) {
        this.orString = orString;
        this.andString = andString;
        this.xorString = xorString;
        this.notString = notString;
        this.falseString = falseString;
        this.trueString = trueString;
        this.equal = equal;
    }

    private FormatToExpression setKeepVars() {
        keepVars = true;
        return this;
    }

    /**
     * returns the available formats useful for screen representation
     *
     * @return list of available formats
     */
    public static FormatToExpression[] getAvailFormats() {
        return availFormats;
    }

    /**
     * @return the default format
     */
    public static FormatToExpression getDefaultFormat() {
        return defaultFormat;
    }

    /**
     * @return the OR string
     */
    public String getOrString() {
        return orString;
    }

    /**
     * @return the AND string
     */
    public String getAndString() {
        return andString;
    }

    /**
     * @return the XOR string
     */
    public String getXorString() {
        return xorString;
    }

    /**
     * @return the NOT string
     */
    public String getNot() {
        return notString;
    }

    /**
     * @return the EQUAL string
     */
    public String getEqual() {
        if (equal == null)  // compatibility with old config files!!!
            return "=";
        return equal;
    }

    /**
     * Formats the given constant
     *
     * @param value th constant
     * @return the string representation
     */
    public String constant(boolean value) {
        if (value) return trueString;
        else return falseString;
    }

    @Override
    public String format(Expression expression) {
        return PlainTextFormatter.format(new ExpressionToText().setFormatIdentifiers(!keepVars).createText(expression, this), this);
    }

    @Override
    public String toString() {
        return format(TOSTRING_EXPR);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FormatToExpression that = (FormatToExpression) o;
        return Objects.equals(orString, that.orString)
                && Objects.equals(andString, that.andString)
                && Objects.equals(falseString, that.falseString)
                && Objects.equals(trueString, that.trueString)
                && Objects.equals(xorString, that.xorString)
                && Objects.equals(notString, that.notString)
                && Objects.equals(getEqual(), that.getEqual());
    }

    @Override
    public int hashCode() {
        return Objects.hash(orString, andString, falseString, trueString, xorString, notString, getEqual());
    }

}

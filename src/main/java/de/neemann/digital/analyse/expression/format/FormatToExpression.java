/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression.format;

import de.neemann.digital.analyse.expression.*;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.draw.graphics.text.formatter.PlainTextFormatter;
import de.neemann.digital.draw.graphics.text.text.ExpressionToText;
import de.neemann.digital.gui.Settings;

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
public enum FormatToExpression implements Formatter {

    /**
     * Creates a string compatible to Java
     */
    JAVA("||", "&&", "^", "!", "false", "true", "="),
    /**
     * Creates a string compatible to Derive
     */
    DERIVE("OR", "AND", "XOR", "NOT ", "false", "true", "="),
    /**
     * Creates a string compatible to WinCUPL
     */
    CUPL("#", "&", "$", "!", "'b'0", "'b'1", "=", true),
    /**
     * Creates a string compatible to Logisim
     */
    LOGISIM("+", "", "^", "~", "false", "true", "="),
    /**
     * Creates a unicode string
     */
    UNICODE("\u2228", "\u2227", "\u22BB", "\u00AC", "0", "1", "="),
    /**
     * Creates a unicode string with no AND character
     */
    UNICODE_NOAND("\u2228", "", "\u22BB", "\u00AC", "0", "1", "="),
    /**
     * Creates a short string representation
     */
    SHORT("+", "*", "^", "!", "0", "1", "="),
    /**
     * Creates a short string representation
     */
    SHORTER("+", "", "^", "!", "0", "1", "="),
    /**
     * Creates a LaTeX representation
     */
    LATEX("\\oder", "\\und", "\\xoder", "", "0", "1", "&=&");

    private static final Expression TOSTRING_EXPR;

    static {
        Variable a = v("A");
        Variable b = v("B");
        TOSTRING_EXPR = or(and(a, not(b)), and(not(a), b), Constant.ZERO);
    }


    /**
     * @return the default format
     */
    public static FormatToExpression getDefaultFormat() {
        return Settings.getInstance().get(Keys.SETTINGS_EXPRESSION_FORMAT);
    }

    /**
     * Formats a expression to a string.
     * Uses the default format for presentation on the screen.
     *
     * @param exp the expression to format
     * @return the string representation
     */
    public static String defaultFormat(Expression exp) {
        return getDefaultFormat().format(exp);
    }

    private final String orString;
    private final String andString;
    private final String falseString;
    private final String trueString;
    private final String equal;
    private final String xorString;
    private final String notString;
    private final boolean keepVars;
    private String name;

    FormatToExpression(String orString, String andString, String xorString, String notString, String falseString, String trueString, String equal) {
        this(orString, andString, xorString, notString, falseString, trueString, equal, false);
    }

    //CHECKSTYLE.OFF: ParameterNumber
    FormatToExpression(String orString, String andString, String xorString, String notString, String falseString, String trueString, String equal, boolean keepVars) {
        this.orString = orString;
        this.andString = andString;
        this.xorString = xorString;
        this.notString = notString;
        this.falseString = falseString;
        this.trueString = trueString;
        this.equal = equal;
        this.keepVars = keepVars;
    }
    //CHECKSTYLE.ON: ParameterNumber

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
        if (name == null)
            name = format(TOSTRING_EXPR);
        return name;
    }

}

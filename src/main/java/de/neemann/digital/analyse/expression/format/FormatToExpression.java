/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.expression.format;

import de.neemann.digital.analyse.expression.*;

import java.util.Objects;

import static de.neemann.digital.analyse.expression.Not.not;
import static de.neemann.digital.analyse.expression.Operation.and;
import static de.neemann.digital.analyse.expression.Operation.or;
import static de.neemann.digital.analyse.expression.Variable.v;

/**
 * Used to format an expression to a simple string
 */
public class FormatToExpression implements Formatter {

    /**
     * Creates a string compatible to Java
     */
    public static final FormatToExpression FORMATTER_JAVA = new FormatToExpression("||", "&&", "^", "!", "false", "true");
    /**
     * Creates a string compatible to Derive
     */
    public static final FormatToExpression FORMATTER_DERIVE = new FormatToExpression("OR", "AND", "XOR", "NOT ", "false", "true");
    /**
     * Creates a string compatible to WinCUPL
     */
    public static final FormatToExpression FORMATTER_CUPL = new FormatToExpression("#", "&", "$", "!", "'b'0", "'b'1");
    /**
     * Creates a string compatible to Logisim
     */
    public static final FormatToExpression FORMATTER_LOGISIM = new FormatToExpression("+", "", "^", "~", "false", "true");
    /**
     * Creates a unicode string
     */
    public static final FormatToExpression FORMATTER_UNICODE = new FormatToExpression("\u2228", "\u2227", "\u22BB", "\u00AC", "0", "1");
    /**
     * Creates a unicode string with no AND character
     */
    public static final FormatToExpression FORMATTER_UNICODE_NOAND = new FormatToExpression("\u2228", "", "\u22BB", "\u00AC", "0", "1");
    /**
     * Creates a short string representation
     */
    public static final FormatToExpression FORMATTER_SHORT = new FormatToExpression("+", "*", "^", "!", "0", "1");
    /**
     * Creates a short string representation
     */
    public static final FormatToExpression FORMATTER_SHORTER = new FormatToExpression("+", "", "^", "!", "0", "1");
    /**
     * Creates a LaTeX representation
     */
    public static final FormatToExpression FORMATTER_LATEX = new FormatterLatex();


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
     * @throws FormatterException FormatterException
     */
    public static String defaultFormat(Expression exp) throws FormatterException {
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
    private final String xorString;
    private final String notString;

    /**
     * Derives a new formatter from the parent
     *
     * @param parent the parent
     */
    public FormatToExpression(FormatToExpression parent) {
        this(parent.orString, parent.andString, parent.xorString, parent.notString, parent.falseString, parent.trueString);
    }

    private FormatToExpression(String orString, String andString, String xorString, String notString, String falseString, String trueString) {
        this.orString = orString;
        this.andString = andString;
        this.xorString = xorString;
        this.notString = notString;
        this.falseString = falseString;
        this.trueString = trueString;
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
     * Formats the given expression
     *
     * @param expression the expression
     * @return the formated string
     * @throws FormatterException FormatterException
     */
    @Override
    public String format(Expression expression) throws FormatterException {
        if (expression instanceof Variable) {
            return identifier(((Variable) expression).getIdentifier());
        } else if (expression instanceof Constant) {
            return constant(((Constant) expression).getValue());
        } else if (expression instanceof Not) {
            return formatNot((Not) expression);
        } else if (expression instanceof Operation.And) {
            return formatAnd((Operation.And) expression);
        } else if (expression instanceof Operation.Or) {
            return formatOr((Operation.Or) expression);
        } else if (expression instanceof Operation.XOr) {
            return formatXOr((Operation.XOr) expression);
        } else if (expression instanceof NamedExpression) {
            NamedExpression ne = (NamedExpression) expression;
            return identifier(ne.getName()) + " = " + format(ne.getExpression());
        } else throw new FormatterException("unknown type " + expression.getClass().getSimpleName());

    }

    /**
     * Formats a not expression
     *
     * @param expression the nor expression
     * @return the formatted string
     * @throws FormatterException FormatterException
     */
    protected String formatNot(Not expression) throws FormatterException {
        if (expression.getExpression() instanceof Operation)
            return notString + "(" + format(expression.getExpression()) + ")";
        else
            return notString + format(expression.getExpression());
    }

    private String formatAnd(Operation.And expression) throws FormatterException {
        return formatOp(expression.getExpressions(), andString);
    }

    private String formatOr(Operation.Or expression) throws FormatterException {
        return formatOp(expression.getExpressions(), orString);
    }

    private String formatXOr(Operation.XOr expression) throws FormatterException {
        return formatOp(expression.getExpressions(), xorString == null ? "^" : xorString);  // fixes stored old formats
    }

    /**
     * Formats an operation
     *
     * @param expressions the expressions
     * @param opString    the string representation of the operation
     * @return the formatted string
     * @throws FormatterException FormatterException
     */
    private String formatOp(Iterable<Expression> expressions, String opString) throws FormatterException {
        StringBuilder sb = new StringBuilder();
        for (Expression e : expressions) {
            if (sb.length() > 0) {
                sb.append(" ");
                if (opString.length() > 0)
                    sb.append(opString).append(" ");
            }
            if (e instanceof Operation)
                sb.append("(").append(format(e)).append(")");
            else
                sb.append(format(e));
        }
        return sb.toString();
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

    /**
     * Formats the given identifier
     *
     * @param identifier the identifier
     * @return the string representation of the identifier
     */
    public String identifier(String identifier) {
        return identifier;
    }

    @Override
    public String toString() {
        try {
            return format(TOSTRING_EXPR);
        } catch (FormatterException e1) {
            return "format error";
        }
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
                && Objects.equals(notString, that.notString);
    }

    @Override
    public int hashCode() {
        return Objects.hash(orString, andString, falseString, trueString, xorString, notString);
    }

    private static class FormatterLatex extends FormatToExpression {
        FormatterLatex() {
            super("\\oder", "\\und", "\\xoder", null, "0", "1");
        }

        @Override
        public String formatNot(Not expression) throws FormatterException {
            return "\\nicht{" + format(expression.getExpression()) + "}";
        }

        @Override
        public String identifier(String identifier) {
            return FormatToTableLatex.formatIdentifier(identifier);
        }
    }
}

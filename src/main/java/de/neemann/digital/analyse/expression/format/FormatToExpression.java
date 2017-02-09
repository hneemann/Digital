package de.neemann.digital.analyse.expression.format;

import de.neemann.digital.analyse.expression.*;

/**
 * Used to format an expression to a simple string
 *
 * @author hneemann
 */
public class FormatToExpression implements Formatter {

    /**
     * Creates a string compatible to Java
     */
    public static final FormatToExpression FORMATTER_JAVA = new FormatToExpression("||", "&&", "!", "false", "true");
    /**
     * Creates a string compatible to Derive
     */
    public static final FormatToExpression FORMATTER_DERIVE = new FormatToExpression("OR", "AND", "NOT ", "false", "true");
    /**
     * Creates a string compatible to WinCUPL
     */
    public static final FormatToExpression FORMATTER_CUPL = new FormatToExpression("#", "&", "!", "0", "1");
    /**
     * Creates a string compatible to Logisim
     */
    public static final FormatToExpression FORMATTER_LOGISIM = new FormatToExpression("+", "", "~", "false", "true");
    /**
     * Creates a unicode string
     */
    public static final FormatToExpression FORMATTER_UNICODE = new FormatToExpression("\u2228", "\u2227", "\u00AC", "0", "1");
    /**
     * Creates a LaTeX representation
     */
    public static final FormatToExpression FORMATTER_LATEX = new FormatterLatex();

    private final String orString;
    private final String andString;
    private final String falseString;
    private final String trueString;
    private final String notString;

    private FormatToExpression(String orString, String andString, String notString, String falseString, String trueString) {
        this.orString = orString;
        this.andString = andString;
        this.notString = notString;
        this.falseString = falseString;
        this.trueString = trueString;
    }

    @Override
    public String format(String name, Expression expression) throws FormatterException {
        return identifier(name) + "=" + format(expression);
    }

    /**
     * Formats the given expression
     *
     * @param expression the expression
     * @return the formated string
     * @throws FormatterException FormatterException
     */
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
        } else if (expression instanceof NamedExpression) {
            NamedExpression ne = (NamedExpression) expression;
            return ne.getName() + " = " + format(ne.getExpression());
        } else throw new FormatterException("unknown type " + expression.getClass().getSimpleName());

    }

    /**
     * Formats a not expression
     *
     * @param expression the nor expression
     * @return the formatted string
     * @throws FormatterException FormatterException
     */
    public String formatNot(Not expression) throws FormatterException {
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

    /**
     * Formats an operation
     *
     * @param expressions the expressions
     * @param opString    the string representation of the operation
     * @return the formated string
     * @throws FormatterException FormatterException
     */
    public String formatOp(Iterable<Expression> expressions, String opString) throws FormatterException {
        StringBuilder sb = new StringBuilder();
        for (Expression e : expressions) {
            if (sb.length() > 0) {
                sb.append(" ").append(opString).append(" ");
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

    private static class FormatterLatex extends FormatToExpression {
        FormatterLatex() {
            super("\\oder", "\\und", null, "0", "1");
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

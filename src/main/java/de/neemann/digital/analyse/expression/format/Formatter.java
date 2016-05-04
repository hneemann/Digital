package de.neemann.digital.analyse.expression.format;


import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;

/**
 * Used to format an expression
 *
 * @author hneemann
 */
public interface Formatter {

    /**
     * Formats an expression
     *
     * @param name       name of the expression
     * @param expression the expression
     * @return the formated expression
     * @throws FormatterException  FormatterException
     * @throws ExpressionException ExpressionException
     */
    String format(String name, Expression expression) throws FormatterException, ExpressionException;
}

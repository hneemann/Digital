package de.neemann.digital.analyse.expression;

/**
 * The context used to evaluate an expression
 *
 * @author hneemann
 */
public interface Context {
    /**
     * Returns the value of the given variable
     *
     * @param variable the variable
     * @return the variables value
     * @throws ExpressionException ExpressionException
     */
    boolean get(Variable variable) throws ExpressionException;
}

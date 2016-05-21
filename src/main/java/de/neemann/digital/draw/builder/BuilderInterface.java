package de.neemann.digital.draw.builder;

import de.neemann.digital.analyse.expression.Expression;

/**
 * Interface used to create a circuit
 * There are two implementations: {@link CircuitBuilder} creates a circuit and {@link CuplCreator} creates a CUPL file
 * which contains the circuit.
 *
 * @param <T> concrete Builder Type
 * @author hneemann
 */
public interface BuilderInterface<T extends BuilderInterface> {
    /**
     * Adds an expression to the circuit
     *
     * @param name       the output name
     * @param expression the expression
     * @return this for chained calls
     * @throws BuilderException BuilderException
     */
    T addExpression(String name, Expression expression) throws BuilderException;

    /**
     * Add a state of a state machine
     *
     * @param name       name of the state
     * @param expression the expression describing next state
     * @return this for chained calls
     * @throws BuilderException BuilderException
     */
    T addState(String name, Expression expression) throws BuilderException;
}

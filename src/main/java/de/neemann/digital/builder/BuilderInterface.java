package de.neemann.digital.builder;

import de.neemann.digital.analyse.expression.Expression;

/**
 * Interface used to create a circuit
 * There are two implementations: the {@link de.neemann.digital.builder.circuit.CircuitBuilder} creates a circuit and the
 * {@link de.neemann.digital.builder.Gal16v8.CuplExporter} creates a CUPL file
 * which contains the circuit.
 *
 * @param <T> concrete Builder Type
 * @author hneemann
 */
public interface BuilderInterface<T extends BuilderInterface> {
    /**
     * Adds an combinatorial expression to the builder
     *
     * @param name       the output name
     * @param expression the expression
     * @return this for chained calls
     * @throws BuilderException BuilderException
     */
    T addCombinatorial(String name, Expression expression) throws BuilderException;

    /**
     * Add a sequential registered to the builder
     *
     * @param name       name of the state
     * @param expression the expression describing next state
     * @return this for chained calls
     * @throws BuilderException BuilderException
     */
    T addSequential(String name, Expression expression) throws BuilderException;
}

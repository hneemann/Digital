/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.gui.components.table;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.ExpressionException;
import de.neemann.digital.analyse.expression.format.FormatterException;
import de.neemann.digital.analyse.expression.modify.ExpressionModifier;
import de.neemann.digital.builder.BuilderException;
import de.neemann.digital.builder.BuilderInterface;
import de.neemann.digital.lang.Lang;

import java.util.HashSet;

/**
 * Helper to fill the a {@link BuilderInterface} with pre calculated expressions
 * stored in {@link ExpressionListenerStore} instance.
 * <p>
 */
public class BuilderExpressionCreator {
    private final HashSet<String> contained;
    private final BuilderInterface builder;
    private final ExpressionModifier[] modifier;
    private boolean useJKOptimizer = false;

    /**
     * Create a new instance
     *
     * @param builder  the builder
     * @param modifier the modifier tp modify the expression
     */
    public BuilderExpressionCreator(BuilderInterface builder, ExpressionModifier... modifier) {
        contained = new HashSet<>();
        this.builder = builder;
        this.modifier = modifier;
    }

    /**
     * Fills the builder
     *
     * @param expressions the expressions to use
     * @throws ExpressionException ExpressionException
     * @throws FormatterException  FormatterException
     */
    public void create(ExpressionListenerStore expressions) throws ExpressionException, FormatterException {
        if (expressions == null)
            throw new ExpressionException(Lang.get("err_noExpressionsAvailable"));

        ExpressionListener el = new ExpressionListener() {
            @Override
            public void resultFound(String name, Expression expression) throws FormatterException, ExpressionException {
                if (!contained.contains(name)) {
                    contained.add(name);
                    try {
                        String n = ExpressionListenerJK.isSequentialVar(name);
                        if (n!=null) {
                            builder.addSequential(n, ExpressionModifier.modifyExpression(expression, modifier));
                        } else
                            builder.addCombinatorial(name, ExpressionModifier.modifyExpression(expression, modifier));
                    } catch (BuilderException e) {
                        throw new RuntimeException(e);
                    }
                }
            }

            @Override
            public void close() {
            }
        };
        if (useJKOptimizer)
            el = new ExpressionListenerOptimizeJK(el);

        expressions.replayTo(el);
        el.close();
    }

    /**
     * Enables the usage of JK-Flipflops instead of D-Flipflops
     *
     * @param useJKOptimizer true if use JK flipflops
     * @return this for chained calls
     */
    public BuilderExpressionCreator setUseJKOptimizer(boolean useJKOptimizer) {
        this.useJKOptimizer = useJKOptimizer;
        return this;
    }
}

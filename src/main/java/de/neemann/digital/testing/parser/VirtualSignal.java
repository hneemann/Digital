/*
 * Copyright (c) 2020 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.Observer;

/**
 * A virtual test signal
 */
public class VirtualSignal {
    private final String sigName;
    private final Expression sigExpression;

    /**
     * Creates a new instance
     *
     * @param sigName       the signals name
     * @param sigExpression the expression
     */
    public VirtualSignal(String sigName, Expression sigExpression) {
        this.sigName = sigName;
        this.sigExpression = sigExpression;
    }

    /**
     * @return the name of the expression
     */
    public String getName() {
        return sigName;
    }

    /**
     * The value of the expression.
     * This method does not return a real observable value.
     * It is just a placeholder for a value.
     * Only the method {@link ObservableValue#getValue()} is working!
     *
     * @param context the context to use
     * @return the created observable value instance
     */
    public ObservableValue getValue(Context context) {
        return new ObservableValue(sigName, 64) {
            @Override
            public long getValue() {
                try {
                    return sigExpression.value(context);
                } catch (ParserException e) {
                    throw new RuntimeException(e);
                }
            }

            @Override
            public Observer addObserver(Observer observer) {
                throw new RuntimeException("not supported");
            }
        };
    }
}

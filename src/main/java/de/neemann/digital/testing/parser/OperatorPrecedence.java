/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser;

/**
 * Defines the operator precedence
 */
public enum OperatorPrecedence {
    /**
     * the operator precedence
     */
    MUL, ADD, SHIFT, COMPARE, EQUAL, AND, XOR, OR;

    /**
     * @return the lowest operator precedence to start evaluation with
     */
    public static OperatorPrecedence lowest() {
        return OR;
    }

    /**
     * @return the predecessor or null if there is none
     */
    public OperatorPrecedence getNextHigherPrecedence() {
        if (ordinal() == 0)
            return null;
        else
            return values()[ordinal() - 1];
    }

}

/*
 * Copyright (c) 2021 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser;

/**
 * Defines the operator precedences
 */
public enum OperatorPrecedence {
    /**
     * the operator precedences
     */
    MUL, ADD, SHIFT, COMPARE, EQUAL, AND, XOR, OR;

    /**
     * @return the OperatorPrecedence to start with
     */
    public static OperatorPrecedence first() {
        return OR;
    }

    /**
     * @return the predecessor or null if there is none
     */
    public OperatorPrecedence getPredecessor() {
        if (ordinal() == 0)
            return null;
        else
            return values()[ordinal() - 1];
    }

}

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
    MUL, ADD, SHIFT, COMPARE, EQUAL, AND, XOR, OR
}

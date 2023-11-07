/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes;

/**
 * Defines the kind of shape used for a embedded circuit
 */
public enum CustomCircuitShapeType {
    /**
     * Uses the shape specified in the circuit itself
     */
    DEFAULT,

    /**
     * The default shape. inputs at the left, outputs at the right
     */
    SIMPLE,

    /**
     * A DIL shape
     */
    DIL,

    /**
     * Pin positions are dependent on the pin positions in the circuit
     */
    LAYOUT,

    /**
     * The shape is a minified version of the circuit
     */
    MINIMIZED,

    /**
     * Shape is defined in the circuit itself.
     */
    CUSTOM
}

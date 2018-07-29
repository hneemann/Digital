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
     * The default shape. inputs at the left, outputs at the right
     */
    DEFAULT,

    /**
     * A DIL shape
     */
    DIL,

    /**
     * Pin positions are dependent on the pin positions in the circuit
     */
    LAYOUT,

    /**
     * Shape is defined in the circuit itself.
     */
    CUSTOM
}

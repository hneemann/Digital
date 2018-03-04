/*
 * Copyright (c) 2016 Helmut Neemann, Rüdiger Heintz
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.arithmetic;

/**
 * the barrel shifter mode
 */
public enum BarrelShifterMode {
    /**
     * logical shift
     */
    logical,
    /**
     * rotate
     */
    rotate,
    /**
     * arithmetic shift
     */
    arithmetic;
}

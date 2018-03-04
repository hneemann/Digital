/*
 * Copyright (c) 2016 Helmut Neemann, Rüdiger Heintz
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.arithmetic;


import de.neemann.digital.core.ObservableValue;

/**
 * the barrel shifter direction
 */
public enum LeftRightFormat {
    /**
     * the default format as defined in {@link ObservableValue#getValueString()}
     */
    left,
    /**
     * right
     */
    right;
}

/*
 * Copyright (c) 2021 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.io;

/**
 * The probe display mode
 */
public enum ProbeMode {
    /**
     * shows the value
     */
    VALUE,
    /**
     * counts an rising edges
     */
    UP,
    /**
     * counts an falling edges
     */
    DOWN,
    /**
     * counts an both edges
     */
    BOTH
}

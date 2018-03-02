/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model;

/**
 * A hdl element
 */
public interface HDLInterface {

    /**
     * @return the ports of this hdl element
     */
    Ports getPorts();

}

/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import de.neemann.digital.draw.elements.PinException;

/**
 * The simplest possible node
 */
public interface NodeInterface extends Observer {

    /**
     * returns the outputs effected by this node
     *
     * @return the outputs
     * @throws PinException PinException
     */
    ObservableValues getOutputs() throws PinException;

}

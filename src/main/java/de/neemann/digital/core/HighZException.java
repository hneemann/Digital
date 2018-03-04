/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core;

import de.neemann.digital.lang.Lang;

/**
 * Is thrown if a high z value is read.
 */
public class HighZException extends RuntimeException {

    /**
     * Creates a new instance
     *
     * @param causedObservable the affected value
     */
    public HighZException(ObservableValue... causedObservable) {
        super(Lang.get("err_readOfHighZ"));
    }
}

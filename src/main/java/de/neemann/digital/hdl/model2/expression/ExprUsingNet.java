/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.model2.expression;

import de.neemann.digital.hdl.model2.HDLNet;

/**
 * Implemented by all expressions using a net
 */
public interface ExprUsingNet {
    /**
     * @return the used net
     */
    HDLNet getNet();
}

/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

import de.neemann.digital.lang.Lang;

/**
 *
 * @author ideras
 */
public class HGSException extends Exception {
    private final int line;

    /**
     * Creates a new instance.
     *
     * @param line the source line
     * @param message the message.
     */
    public HGSException(int line, String message) {
        super(Lang.get("errMsgWithLine", line, message));
        this.line = line;
    }

    /**
     * Return the source line.
     *
     * @return the source line.
     */
    public int getLine() {
        return line;
    }
}

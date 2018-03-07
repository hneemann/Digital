/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern;

import de.neemann.digital.core.extern.handler.Generic;
import de.neemann.digital.lang.Lang;

import java.io.IOException;

/**
 * The factory used to create external processes
 */
public final class ProcessFactory {


    private ProcessFactory() {
    }

    /**
     * The process types
     */
    public enum Type {
        /**
         * generic executable
         */
        Generic,
        /**
         * ghdl vhdl interpreter
         */
        GHDL
    }

    /**
     * Creates a new process
     *
     * @param type   the type of the prosess
     * @param code   the code to use
     * @param params the parameters to use
     * @return the created process handler
     * @throws IOException IOException
     */
    public static ProcessHandler create(Type type, String code, String params) throws IOException {
        switch (type) {
            case Generic:
                return new Generic(params);
            default:
                throw new IOException(Lang.get("err_processType_N_notFound", type.name()));
        }
    }

}

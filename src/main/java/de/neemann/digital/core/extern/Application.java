/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.extern.handler.ProcessInterface;

import java.io.IOException;

/**
 * Represents an application
 */
public interface Application {

    /**
     * The available types of applications
     */
    enum Type {
        /**
         * generic executable
         */
        Generic,
        /**
         * ghdl vhdl interpreter
         */
        GHDL,
        /**
         * Icarus verilog interpreter
         */
        IVERILOG
    }

    /**
     * Creates a new application instance
     *
     * @param type the type of the process
     * @param attr the elements attributes
     * @return the created process handler
     */
    static Application create(Type type, ElementAttributes attr) {
        switch (type) {
            case Generic:
                return new ApplicationGeneric();
            case GHDL:
                return new ApplicationGHDL(attr);
            case IVERILOG:
                return new ApplicationIVerilog();
            default:
                return null;
        }
    }


    /**
     * Creates an interface to a running process.
     * This interface is used to pass values back and forth.
     *
     * @param label   the codes label
     * @param code    the code itself
     * @param inputs  the inputs expected by Digital
     * @param outputs the outputs expected by Digital
     * @return the ProcessInterface
     * @throws IOException IOException
     */
    ProcessInterface start(String label, String code, PortDefinition inputs, PortDefinition outputs) throws IOException;

    /**
     * Used to make the component consistent.
     * Could extract the label and the input and output configuration from the code or vice versa.
     * If this is not supported, nothing is done.
     *
     * @param attributes the attributed of this component
     * @return true if attributes are modified
     */
    default boolean ensureConsistency(ElementAttributes attributes) {
        return false;
    }

    /**
     * @return true if the code check function is supported
     */
    default boolean checkSupported() {
        return false;
    }

    /**
     * Checks the given code.
     * If there was no error a null is returned.
     *
     * @param label   the codes label
     * @param code    the code itself
     * @param inputs  the inputs expected by Digital
     * @param outputs the outputs expected by Digital
     * @return the applications message, maybe null
     * @throws IOException IOException
     */
    default String checkCode(String label, String code, PortDefinition inputs, PortDefinition outputs) throws IOException {
        return null;
    }
}

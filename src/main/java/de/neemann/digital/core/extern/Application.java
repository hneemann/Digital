/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern;

import de.neemann.digital.core.element.ElementAttributes;
import de.neemann.digital.core.element.Keys;
import de.neemann.digital.core.extern.handler.ProcessInterface;
import de.neemann.digital.gui.components.AttributeDialog;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

/**
 * Represents an application
 */
public interface Application {
    /**
     * Extract the code from the attributes.
     * The code is either stored directly or there is a file given.
     * The AttributeDialog can be used to resolve relative path
     *
     * @param attr   the attributes
     * @param dialog the attribute dialog
     * @return the code
     * @throws IOException IOException
     */
    static String getCode(ElementAttributes attr, AttributeDialog dialog) throws IOException {
        if (attr.contains(Keys.EXTERNAL_CODE))
            return attr.get(Keys.EXTERNAL_CODE);

        if (attr.contains(Keys.EXTERNAL_CODE_FILE)) {
            if (dialog == null) {
                return readCode(attr.getFile(Keys.EXTERNAL_CODE_FILE));
            } else {
                return readCode(attr.getFile(Keys.EXTERNAL_CODE_FILE), dialog.getMain().getBaseFileName());
            }
        }

        return "";
    }

    /**
     * Extract the code from the attributes.
     * The code is either stored directly or there is a file given.
     *
     * @param attr the attributes
     * @return the code
     * @throws IOException IOException
     */
    static String getCode(ElementAttributes attr) throws IOException {
        return getCode(attr, null);
    }

    /**
     * Reads the code from a file
     *
     * @param file the file
     * @return the code
     * @throws IOException IOException
     */
    static String readCode(File file) throws IOException {
        return readCode(file, null);
    }

    /**
     * Reads the code from a file.
     * Origin path can be used to resolve relative paths
     *
     * @param file          the file
     * @param circuitOrigin the origin file
     * @return the code
     * @throws IOException IOException
     */
    static String readCode(File file, File circuitOrigin) throws IOException {
        if (!file.exists() && circuitOrigin != null) {
            // Attempt to resolve file from circuit folder
            file = new File(circuitOrigin.getParentFile(), file.getName());
        }

        byte[] data = Files.readAllBytes(file.toPath());
        return new String(data, StandardCharsets.UTF_8);
    }

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
                return new ApplicationIVerilog(attr);
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
     * @param dialog     the attribute dialog to resolve relative path
     * @return true if attributes are modified
     */
    default boolean ensureConsistency(ElementAttributes attributes, AttributeDialog dialog) {
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

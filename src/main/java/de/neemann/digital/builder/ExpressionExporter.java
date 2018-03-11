/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder;

import de.neemann.digital.builder.jedec.FuseMapFillerException;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Interface used to create Jedec files.
 * Every supported device implements this interface.
 *
 * @param <T> concrete type of {@link ExpressionExporter}
 */
public interface ExpressionExporter<T extends ExpressionExporter> {

    /**
     * @return builder to add expressions
     */
    BuilderInterface getBuilder();

    /**
     * Gets the pin mapping which is to use/was used.
     * You can modify the mapping before getBuilder is called.
     * After the export you will find the used pin mapping.
     *
     * @return the actual pin mapping
     */
    PinMap getPinMapping();

    /**
     * Writes the JEDEC file to the given output stream
     *
     * @param out the output stream
     * @throws FuseMapFillerException FuseMapFillerException
     * @throws IOException            IOException
     * @throws PinMapException        PinMapException
     */
    void writeTo(OutputStream out) throws FuseMapFillerException, IOException, PinMapException;
}

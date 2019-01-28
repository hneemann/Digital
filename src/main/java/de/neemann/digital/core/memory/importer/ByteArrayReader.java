/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory.importer;

import java.io.IOException;

/**
 * Reader which reads bytes from a source.
 */
public interface ByteArrayReader {
    /**
     * Read the data
     *
     * @param byteArray the byte array to write the data to
     * @throws IOException IOException
     */
    void read(ByteArray byteArray) throws IOException;
}

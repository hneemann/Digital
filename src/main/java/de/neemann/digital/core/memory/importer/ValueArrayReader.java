/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory.importer;

import java.io.IOException;

/**
 * Reader which reads valued data, which is maybe more the one byte per value.
 */
public interface ValueArrayReader {
    /**
     * Read the data
     *
     * @param valueArray the value array to write the data to
     * @throws IOException IOException
     */
    void read(ValueArray valueArray) throws IOException;
}

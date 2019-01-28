/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory.importer;

import de.neemann.digital.core.memory.DataFieldImporter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Reads a file to a value array.
 * In this class the file format is determined by the file suffix.
 */
public final class Importer {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataFieldImporter.class);

    private Importer() {
    }

    /**
     * Reads a file
     *
     * @param file   the file to read
     * @param values the data destination
     * @throws IOException IOException
     */
    public void read(File file, ValueArray values) throws IOException {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".hex")) {
            try {
                new LogisimReader(file).read(values);
            } catch (IOException e) {
                LOGGER.info(file + ": could not read native hex, try intel hex");
                new IntelHexReader(file).read(new ByteArrayFromValueArray(values));
            }
        } else {
            LOGGER.info(file + ": read as binary");
            new BinReader(file).read(new ByteArrayFromValueArray(values));
        }
    }
}

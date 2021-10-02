/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory.importer;

import de.neemann.digital.core.memory.DataField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * Reads a file to a value array.
 * In this class the file format is determined by the file suffix.
 */
public final class Importer {
    private static final Logger LOGGER = LoggerFactory.getLogger(Importer.class);

    private Importer() {
    }

    /**
     * Reads the given file to a single data field.
     *
     * @param hexFile   the file to read
     * @param dataBits  the bits used in the data field
     * @param bigEndian uses big endian at import
     * @return the data field
     * @throws IOException IOException
     */
    public static DataField read(File hexFile, int dataBits, boolean bigEndian) throws IOException {
        DataField df = new DataField(1024);
        read(hexFile, new DataFieldValueArray(df, dataBits), bigEndian);
        return df;
    }

    /**
     * Reads a file to the given ValueArray
     *
     * @param file      the file to read
     * @param values    the data destination
     * @param bigEndian uses big endian at import
     * @throws IOException IOException
     */
    public static void read(File file, ValueArray values, boolean bigEndian) throws IOException {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".hex")) {
            try {
                new LogisimReader(file).read(values);
            } catch (IOException e) {
                LOGGER.info(file + ": could not read native hex, try intel hex");
                new IntelHexReader(file).read(new ByteArrayFromValueArray(values, bigEndian));
            }
        } else {
            LOGGER.info(file + ": read as binary");
            new BinReader(file).read(new ByteArrayFromValueArray(values, bigEndian));
        }
    }
}

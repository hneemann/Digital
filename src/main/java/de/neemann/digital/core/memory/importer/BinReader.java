/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory.importer;

import java.io.*;

/**
 * Used to import raw binary data.
 */
public class BinReader implements ByteArrayReader {

    private InputStream inputStream;

    /**
     * Reads a file
     *
     * @param file the file to read
     * @throws FileNotFoundException FileNotFoundException
     */
    public BinReader(File file) throws FileNotFoundException {
        this.inputStream = new FileInputStream(file);
    }

    /**
     * Reads a input stream
     *
     * @param inputStream the inputStream to read
     */
    public BinReader(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void read(ByteArray byteArray) throws IOException {
        try {
            int d;
            int addr = 0;
            while ((d = inputStream.read()) >= 0) {
                byteArray.set(addr, d);
                addr++;
            }
        } finally {
            inputStream.close();
        }

    }
}

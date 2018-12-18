/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Helper to import data memory
 */
public final class DataFieldImporter {
    private static final Logger LOGGER = LoggerFactory.getLogger(DataFieldImporter.class);

    private DataFieldImporter() {
    }

    /**
     * Imports a file and converts it into a DataField instance
     *
     * @param file     the file to read
     * @param dataBits the data bits of the target memory
     * @return the DataField instance
     * @throws IOException IOException
     */
    public static DataField read(File file, int dataBits) throws IOException {
        String name = file.getName().toLowerCase();
        if (name.endsWith(".hex")) {
            try {
                return new DataField(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            } catch (IOException e) {
                LOGGER.info(file + ": could not read native hex, try intel hex");
                return readToByteArray(file, dataBits, new IntelHexReader());
            }
        } else {
            LOGGER.info(file + ": read as binary");
            return readToByteArray(file, dataBits, DataFieldImporter::readBinary);
        }
    }

    private static DataField readToByteArray(File file, int dataBits, Reader reader) throws IOException {
        DataField dataField = new DataField(1024);
        reader.read(file, createByteArray(dataField, dataBits));
        dataField.trim();
        return dataField;
    }

    interface ByteArray {
        void set(int addr, int aByte);
    }

    static ByteArray createByteArray(DataField dataField, int dataBits) {
        if (dataBits <= 8)
            return dataField::setData;
        return new ByteArrayMod(dataField, (dataBits - 1) / 8 + 1);
    }

    private static final class ByteArrayMod implements ByteArray {
        private final DataField dataField;
        private final int div;

        private ByteArrayMod(DataField dataField, int div) {
            this.dataField = dataField;
            this.div = div;
        }

        @Override
        public void set(int addr, int aByte) {
            int a = addr / div;
            int b = addr % div;

            long val = dataField.getDataWord(a);
            val = val | ((((long) aByte) & 0xff) << (b * 8));
            dataField.setData(a, val);
        }
    }

    interface Reader {
        void read(File file, ByteArray byteArray) throws IOException;
    }

    private static void readBinary(File file, ByteArray byteArray) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            int d;
            int addr = 0;
            while ((d = in.read()) >= 0) {
                byteArray.set(addr, d);
                addr++;
            }
        }
    }

}

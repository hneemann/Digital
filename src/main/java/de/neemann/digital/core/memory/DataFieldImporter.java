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
                return readByteArray(file, dataBits, IntelHexReader::new);
            }
        } else {
            LOGGER.info(file + ": read as binary");
            return readByteArray(file, dataBits, DataFieldImporter::readBinary);
        }
    }

    private static DataField readByteArray(File file, int dataBits, Reader reader) throws IOException {
        DataField dataField = new DataField(1024);
        reader.read(file, create(dataField, dataBits));
        dataField.trim();
        return dataField;
    }

    interface DataArray {
        void put(int addr, int aByte);
    }

    static DataArray create(DataField dataField, int dataBits) {
        if (dataBits <= 8)
            return dataField::setData;
        return new DataArrayMod(dataField, (dataBits - 1) / 8 + 1);
    }

    private static final class DataArrayMod implements DataArray {
        private final DataField dataField;
        private final int div;

        private DataArrayMod(DataField dataField, int div) {
            this.dataField = dataField;
            this.div = div;
        }

        @Override
        public void put(int addr, int aByte) {
            int a = addr / div;
            int b = addr % div;

            long val = dataField.getDataWord(a);
            val = val | ((((long) aByte) & 0xff) << (b * 8));
            dataField.setData(a, val);
        }
    }

    interface Reader {
        void read(File file, DataArray dataArray) throws IOException;
    }

    private static void readBinary(File file, DataArray dataArray) throws IOException {
        try (InputStream in = new FileInputStream(file)) {
            int d;
            int addr = 0;
            while ((d = in.read()) > 0) {
                dataArray.put(addr, d);
                addr++;
            }
        }
    }

}

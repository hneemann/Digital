/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Reader for intel hex files
 */
class IntelHexReader {
    private final BufferedReader bufferedReader;
    private final DataFieldImporter.DataArray dataArray;
    private final int[] data;
    private int segment = 0;

    /**
     * Creates a new reader instance
     *
     * @param file      the file to read
     * @param dataArray the array to write the data to
     * @throws IOException IOException
     */
    IntelHexReader(File file, DataFieldImporter.DataArray dataArray) throws IOException {
        this(new FileInputStream(file), dataArray);
    }

    /**
     * Creates a new reader instance
     *
     * @param inputStream the stream to read
     * @param dataArray   the array to write the data to
     * @throws IOException IOException
     */
    IntelHexReader(InputStream inputStream, DataFieldImporter.DataArray dataArray) throws IOException {
        this(new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8)), dataArray);
    }

    private IntelHexReader(BufferedReader bufferedReader, DataFieldImporter.DataArray dataArray) throws IOException {
        this.bufferedReader = bufferedReader;
        this.dataArray = dataArray;
        data = new int[300];
        read();
    }

    private void read() throws IOException {
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            int payload = parseLine(line);
            switch (data[3]) {
                case 0:
                    readData(payload);
                    break;
                case 2:
                    readDataSegment(payload);
                    break;
            }
        }
    }

    private void readDataSegment(int len) throws IOException {
        if (len != 2)
            throw new IOException("invalid segment address");
        segment = ((data[4] << 8) + data[5])<<4;
    }

    private void readData(int len) {
        int addr = (data[1] << 8) + data[2];
        for (int i = 0; i < len; i++)
            dataArray.put(segment + addr + i, data[i + 4]);
    }

    private int parseLine(String line) throws IOException {
        if (line.charAt(0) != ':')
            throw new IOException("not a intel hex file");

        int addr = 0;
        int p = 1;
        while (p < line.length()) {
            data[addr] = Integer.parseInt(line.substring(p, p + 2), 16);
            addr++;
            p += 2;
        }

        int payload = addr - 5;

        if (payload < 0)
            throw new IOException("not a intel hex file");

        if (data[0] != payload)
            throw new IOException("invalid record size");

        int sum = 0;
        for (int i = 0; i < addr; i++)
            sum += data[i];

        sum = sum & 0xff;
        if (sum != 0)
            throw new IOException("wrong checksum in intel hex file: 0x" + Integer.toHexString(sum));

        return payload;
    }
}

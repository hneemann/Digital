/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory.importer;

import java.io.*;
import java.nio.charset.StandardCharsets;

/**
 * Reader for intel hex files
 */
public class IntelHexReader implements ByteArrayReader {
    private final int[] data;
    private final File file;
    private int segment = 0;

    /**
     * Creates a new reader instance
     */
    IntelHexReader(File file) {
        this.file = file;
        data = new int[300];
    }

    @Override
    public void read(ByteArray byteArray) throws IOException {
        try (Reader r = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            read(r, byteArray);
        }
    }

    void read(Reader reader, ByteArray byteArray) throws IOException {
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                int payload = parseLine(line);
                switch (data[3]) {
                    case 0:
                        readData(payload, byteArray);
                        break;
                    case 2:
                        readDataSegment(payload);
                        break;
                }
            }
        }
    }

    private void readDataSegment(int len) throws IOException {
        if (len != 2)
            throw new IOException("invalid segment address");
        segment = ((data[4] << 8) + data[5]) << 4;
    }

    private void readData(int len, ByteArray byteArray) {
        int addr = (data[1] << 8) + data[2];
        for (int i = 0; i < len; i++)
            byteArray.set(segment + addr + i, data[i + 4]);
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

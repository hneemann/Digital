/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.jedec;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 */
public class JedecWriter extends FilterOutputStream {

    private static final int LINELEN = 32;
    private static final String LINEEND = "\r\n";
    private static final int STX = 2;
    private static final int ETX = 3;

    private int checksum;

    /**
     * Creates a new jedec writer
     *
     * @param out the stream to write the data to
     * @throws IOException IOException
     */
    public JedecWriter(OutputStream out) throws IOException {
        super(out);
        write(STX);
    }

    /**
     * Writes the string to the file
     *
     * @param s the string to write
     * @return this for chained calls
     * @throws IOException IOException
     */
    public JedecWriter print(String s) throws IOException {
        write(s.getBytes(StandardCharsets.ISO_8859_1));
        return this;
    }

    /**
     * Writes the string to the file
     * Adds a carriage return and a line feed
     *
     * @param s the string to write
     * @return this for chained calls
     * @throws IOException IOException
     */
    public JedecWriter println(String s) throws IOException {
        print(s);
        print(LINEEND);
        return this;
    }

    /**
     * Writes a fuse map to the JEDEC file
     *
     * @param fuseMap the fuse map
     * @return this for chained calls
     * @throws IOException IOException
     */
    public JedecWriter write(FuseMap fuseMap) throws IOException {
        println("QF" + fuseMap.getFuses() + "*");
        println("G0*");
        println("F0*");
        int lines = (fuseMap.getFuses() - 1) / LINELEN + 1;
        for (int li = 0; li < lines; li++) {
            int pos = li * LINELEN;
            int len = LINELEN;
            if (pos + len > fuseMap.getFuses())
                len = fuseMap.getFuses() - pos;

            boolean containsOne = false;
            StringBuilder sb = new StringBuilder();
            sb.append("L").append(pos).append(" ");
            for (int i = 0; i < len; i++)
                if (fuseMap.getFuse(pos + i)) {
                    sb.append("1");
                    containsOne = true;
                } else
                    sb.append("0");

            if (containsOne) {
                print(sb.toString());
                print("*");
                print(LINEEND);
            }
        }
        println("C" + toHex(fuseMap.getJedecChecksum(), 4) + "*");
        return this;
    }

    @Override
    public void write(int b) throws IOException {
        super.write(b);
        checksum += (b & 0x7f);
    }

    @Override
    public void close() throws IOException {
        write(ETX);
        out.write(toHex(checksum & 0xffff, 4).getBytes(StandardCharsets.ISO_8859_1));
        super.close();
    }

    private String toHex(int checksum, int s) {
        String h = Integer.toHexString(checksum).toUpperCase();
        while (h.length() < 4)
            h = "0" + h;
        return h;
    }

    int getChecksum() {
        return checksum;
    }
}

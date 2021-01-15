/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory.importer;

import de.neemann.digital.core.Bits;
import de.neemann.digital.lang.Lang;

import java.io.*;
import java.util.StringTokenizer;

/**
 * Reader to read the original Logisim hex file format
 */
public class LogisimReader implements ValueArrayReader {
    private final Reader reader;

    /**
     * Creates a new instance
     *
     * @param file the file to read
     * @throws FileNotFoundException FileNotFoundException
     */
    public LogisimReader(File file) throws FileNotFoundException {
        this(new FileReader(file));
    }

    /**
     * Creates a new instance
     *
     * @param reader the reader to used
     */
    public LogisimReader(Reader reader) {
        this.reader = reader;
    }

    @Override
    public void read(ValueArray valueArray) throws IOException {
        try (BufferedReader br = new BufferedReader(reader)) {
            String header = br.readLine();
            if (header == null || !header.equals("v2.0 raw"))
                throw new IOException(Lang.get("err_invalidFileFormat"));
            String line;
            int pos = 0;
            while ((line = br.readLine()) != null) {
                try {
                    int p = line.indexOf('#');
                    if (p >= 0)
                        line = line.substring(0, p).trim();
                    else
                        line = line.trim();

                    StringTokenizer tc = new StringTokenizer(line, " \t");
                    while (tc.hasMoreTokens()) {
                        String num = tc.nextToken();
                        int rle = 1;
                        p = num.indexOf('*');
                        if (p > 0) {
                            rle = Integer.parseInt(num.substring(0, p));
                            num = num.substring(p + 1).trim();
                        }

                        if (num.length() > 2 && num.charAt(0) == '0' && (num.charAt(1) == 'x' || num.charAt(1) == 'X'))
                            num = num.substring(2);

                        if (num.length() > 0) {
                            long v = Bits.decode(num, 0, 16);
                            for (int i = 0; i < rle; i++) {
                                valueArray.set(pos, v);
                                pos++;
                            }
                        }
                    }

                } catch (Bits.NumberFormatException e) {
                    throw new IOException(e);
                }
            }
        }
    }

}

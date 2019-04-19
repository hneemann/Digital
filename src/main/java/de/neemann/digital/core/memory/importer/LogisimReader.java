/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory.importer;

import de.neemann.digital.core.Bits;
import de.neemann.digital.lang.Lang;

import java.io.*;

/**
 * Reader to read the original Logisim hex file format
 */
public class LogisimReader implements ValueArrayReader {
    private Reader reader;

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

                    int rle = 1;
                    p = line.indexOf('*');
                    if (p > 0) {
                        rle = Integer.parseInt(line.substring(0, p));
                        line = line.substring(p + 1).trim();
                    }

                    if (line.length() > 2 && line.charAt(0) == '0' && (line.charAt(1) == 'x' || line.charAt(1) == 'X'))
                        line = line.substring(2);

                    if (line.length() > 0) {
                        long v = Bits.decode(line, 0, 16);
                        for (int i = 0; i < rle; i++) {
                            valueArray.set(pos, v);
                            pos++;
                        }
                    }
                } catch (Bits.NumberFormatException e) {
                    throw new IOException(e);
                }
            }
        }
    }


}

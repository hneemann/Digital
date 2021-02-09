/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.memory.importer;

import de.neemann.digital.core.Bits;
import de.neemann.digital.lang.Lang;

import java.io.*;

import static java.io.StreamTokenizer.*;

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
        try (BufferedReader bufferedReader = new BufferedReader(reader)) {
            String header = bufferedReader.readLine();
            if (header == null || !header.equals("v2.0 raw"))
                throw new IOException(Lang.get("err_invalidFileFormat"));

            StreamTokenizer t = new StreamTokenizer(bufferedReader);
            t.resetSyntax();
            t.commentChar('#');
            t.wordChars('a', 'f');
            t.wordChars('A', 'F');
            t.wordChars('x', 'x');
            t.wordChars('X', 'X');
            t.wordChars('0', '9');
            t.whitespaceChars(0, ' ');

            int pos = 0;
            while (t.nextToken() != TT_EOF) {
                try {
                    String vStr = t.sval;
                    if (vStr == null)
                        throw new IOException("invalid token in line " + t.lineno());
                    if (t.nextToken() == '*') {
                        t.nextToken();
                        if (t.sval == null)
                            throw new IOException("invalid token in line " + t.lineno());
                        long v = getHexLong(t.sval);
                        int reps = (int) Bits.decode(vStr, 0, 10);
                        for (int i = 0; i < reps; i++) {
                            valueArray.set(pos, v);
                            pos++;
                        }
                    } else {
                        t.pushBack();
                        valueArray.set(pos, getHexLong(vStr));
                        pos++;
                    }
                } catch (Bits.NumberFormatException e) {
                    throw new IOException(e);
                }
            }
        }
    }

    private long getHexLong(String vStr) throws Bits.NumberFormatException {
        int p = 0;
        if (vStr.length() > 2 && vStr.charAt(0) == '0' && (vStr.charAt(1) == 'x' || vStr.charAt(1) == 'X'))
            p = 2;

        return Bits.decode(vStr, p, 16);
    }

}

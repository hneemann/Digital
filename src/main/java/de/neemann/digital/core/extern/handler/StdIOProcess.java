/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern.handler;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.extern.ProcessHandler;

import java.io.*;
import java.util.StringTokenizer;

/**
 * The generic process description
 */
public abstract class StdIOProcess implements ProcessHandler {
    private BufferedWriter writer;
    private BufferedReader reader;

    /**
     * Sets the reader and writer
     *
     * @param reader the reader
     * @param writer the writer
     */
    public void setReaderWriter(BufferedReader reader, BufferedWriter writer) {
        this.reader = reader;
        this.writer = writer;
    }

    /**
     * Sets the reader and writer
     *
     * @param in  the input stream
     * @param out the output stream
     */
    public void setInputOutputStream(InputStream in, OutputStream out) {
        setReaderWriter(
                new BufferedReader(new InputStreamReader(in)),
                new BufferedWriter(new OutputStreamWriter(out)));
    }

    @Override
    public void writeValues(ObservableValues values) throws IOException {
        boolean first = true;
        for (ObservableValue v : values) {
            if (first)
                first = false;
            else
                writer.write(",");
            writer.write(v.getName());
            writer.write("=");
            writer.write(Long.toHexString(v.getValue()));
        }
        writer.write("\n");
        writer.flush();
    }

    @Override
    public void readValuesTo(ObservableValues values) throws IOException {
        String line;
        while ((line = reader.readLine()) != null) {
            if (line.startsWith("digital:")) {
                StringTokenizer st = new StringTokenizer(line.substring(8), ",=", true);
                for (ObservableValue v : values) {
                    if (!st.hasMoreTokens())
                        throw new IOException("not enough values received!");

                    String name = st.nextToken().trim();
                    if (name.equals(","))
                        name = st.nextToken().trim();

                    if (!name.equals(v.getName()))
                        throw new IOException("values in wrong order: expected " + v.getName() + " but found " + name);

                    if (!st.nextToken().equals("="))
                        throw new IOException("= expected");

                    final String valStr = st.nextToken();
                    if (valStr.equals("Z"))
                        v.setToHighZ();
                    else
                        v.setValue(Long.parseLong(valStr, 16));

                }
                return;
            }
        }
    }

}

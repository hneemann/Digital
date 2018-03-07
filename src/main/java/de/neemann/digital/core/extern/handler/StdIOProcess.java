/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern.handler;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.core.extern.ProcessHandler;
import de.neemann.digital.lang.Lang;

import java.io.*;

/**
 * The generic process description
 */
public class StdIOProcess implements ProcessHandler {
    private static final String PREFIX = "digital:";
    private Process process;
    private BufferedWriter writer;
    private Thread thread;

    private final Object lock = new Object();
    private String dataFound;


    /**
     * Set the already started process
     *
     * @param process the process to use
     */
    public void setProcess(Process process) {
        this.process = process;
        setInputOutputStream(process.getInputStream(), process.getOutputStream());

    }

    /**
     * Sets the reader and writer
     *
     * @param in  the input stream
     * @param out the output stream
     */
    private void setInputOutputStream(InputStream in, OutputStream out) {
        setReaderWriter(
                new BufferedReader(new InputStreamReader(in)),
                new BufferedWriter(new OutputStreamWriter(out)));
    }

    /**
     * Sets the reader and writer
     *
     * @param reader the reader
     * @param writer the writer
     */
    private void setReaderWriter(BufferedReader reader, BufferedWriter writer) {
        this.writer = writer;

        thread = new Thread(() -> {
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(PREFIX)) {
                        synchronized (lock) {
                            while (dataFound != null)
                                lock.wait();
                            dataFound = line;
                            lock.notify();
                        }
                    }
                }
            } catch (IOException | InterruptedException e) {
                e.printStackTrace();
            }
        });
        thread.setDaemon(true);
        thread.start();
    }

    private String readLine() throws IOException {
        synchronized (lock) {
            try {
                long startTime = System.currentTimeMillis();
                while (dataFound == null && (System.currentTimeMillis() - startTime) < 5000)
                    lock.wait(1000);

                if (dataFound == null)
                    throw new IOException(Lang.get("err_timeoutReadingData"));

                String line = dataFound;
                dataFound = null;
                lock.notify();

                return line;
            } catch (InterruptedException e) {
                return null;
            }
        }
    }

    @Override
    public void writeValues(ObservableValues values) throws IOException {
        for (ObservableValue v : values) {
            final int bits = v.getBits();
            final long value = v.getValue();
            final long highZ = v.getHighZ();
            long mask = 1;
            for (int i = 0; i < bits; i++) {
                if ((highZ & mask) != 0)
                    writer.write('Z');
                else {
                    if ((value & mask) != 0)
                        writer.write('1');
                    else
                        writer.write('0');
                }
                mask <<= 1;
            }
        }
        writer.write("\n");
        writer.flush();
    }

    @Override
    public void readValues(ObservableValues values) throws IOException {
        String line = readLine();
        if (line != null) {
            int pos = PREFIX.length();
            int len = line.length();
            for (ObservableValue v : values) {
                final int bits = v.getBits();

                if (pos + bits > len)
                    throw new IOException(Lang.get("err_notEnoughDataReceived"));

                long value = 0;
                long highZ = 0;
                long mask = 1;
                for (int i = 0; i < bits; i++) {
                    char c = line.charAt(pos);
                    switch (c) {
                        case 'z':
                        case 'Z':
                            highZ |= mask;
                            break;
                        case 'h':
                        case 'H':
                        case '1':
                            value |= mask;
                            break;
                        case 'l':
                        case 'L':
                        case '0':
                            break;
                        default:
                            throw new IOException(Lang.get("err_invalidCharacterReceived_N", "" + c));
                    }
                    mask <<= 1;
                    pos++;
                }
                v.set(value, highZ);
            }
        } else
            throw new IOException(Lang.get("err_processTerminatedUnexpected"));
    }

    @Override
    public void close() throws IOException {
        process.destroy();

        thread.interrupt();

        try {
            thread.join(1000);
        } catch (InterruptedException e) {
            // its ok, I just want to terminate the process!
        }

        if (thread.isAlive())
            throw new IOException(Lang.get("err_couldNotTerminateProcess"));
    }
}

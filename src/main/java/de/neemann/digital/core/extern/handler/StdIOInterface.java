/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern.handler;

import de.neemann.digital.core.ObservableValue;
import de.neemann.digital.core.ObservableValues;
import de.neemann.digital.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.LinkedList;

/**
 * Communicates with an external process by sending values and receiving results via the stdio.
 * All bits of all values are send as a stream of '0', '1' or 'Z'. The lsb is send first.
 * The last bit is followed by an end of line character.
 * <p>
 * If the application wants to send values back, a new line needs to start with the string "Digital:".
 * After that all bit of all values needs to be send. The lsb needs to be send first.
 * The last bit needs to be followed by an end of line character.
 */
public class StdIOInterface implements ProcessInterface {
    private static final Logger LOGGER = LoggerFactory.getLogger(StdIOInterface.class);
    private static final String PREFIX = "Digital:";
    private static final int MAX_CONSOLE_LINES = 30;
    private static final long TIMEOUT = 5000;
    private final Process process;
    private BufferedWriter writer;
    private Thread thread;
    private LinkedList<String> consoleOut;

    private final Object lock = new Object();
    private String dataFound;
    private boolean terminated = false;


    /**
     * Set the already started process
     *
     * @param process the process to use
     */
    public StdIOInterface(Process process) {
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
        consoleOut = new LinkedList<>();
        terminated = false;
        thread = new Thread(() -> {
            LOGGER.debug("reader-thread started");
            try {
                String line;
                while ((line = reader.readLine()) != null) {
                    consoleOut.add(line);
                    while (consoleOut.size() > MAX_CONSOLE_LINES)
                        consoleOut.removeFirst();
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
            synchronized (lock) {
                terminated = true;
                lock.notify();
            }
            LOGGER.debug("reader-thread terminated");
        });
        thread.setDaemon(true);
        thread.start();
    }

    private String readLine() throws IOException {
        synchronized (lock) {
            try {
                long startTime = System.currentTimeMillis();
                long time = 0;
                while (dataFound == null && !terminated && (time - startTime) < TIMEOUT) {
                    lock.wait(1000);
                    time = System.currentTimeMillis();
                }

                if (!((time - startTime) < TIMEOUT))
                    throw new IOException(Lang.get("err_timeoutReadingData_O", getConsoleOut()));

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
        try {
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
        } catch (IOException e) {
            throw new IOException(Lang.get("err_writingToStdOut_O", getConsoleOut()), e);
        }
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
                    throw new IOException(Lang.get("err_notEnoughDataReceived_O", getConsoleOut()));

                long value = 0;
                long highZ = 0;
                long mask = 1;
                for (int i = 0; i < bits; i++) {
                    char c = line.charAt(pos);
                    switch (c) {
                        case 'Z':
                        case 'z':
                            highZ |= mask;
                            break;
                        case 'H':
                        case '1':
                            value |= mask;
                            break;
                        case 'W':
                        case 'X':
                        case 'x':
                        case 'U':
                        case 'L':
                        case '0':
                            break;
                        default:
                            throw new IOException(Lang.get("err_invalidCharacterReceived_N_O", "" + c, getConsoleOut()));
                    }
                    mask <<= 1;
                    pos++;
                }
                v.set(value, highZ);
            }
        } else
            throw new IOException(Lang.get("err_processTerminatedUnexpected_O", getConsoleOutNoWarn(consoleOut)));
    }

    /**
     * Returns the console out without warnings.
     * Used to remove not needed content which obfuscates the real error cause.
     *
     * @param consoleOut the console out
     * @return the clean error message
     */
    public String getConsoleOutNoWarn(LinkedList<String> consoleOut) {
        return getConsoleOut();
    }

    private String getConsoleOut() {
        StringBuilder sb = new StringBuilder();
        for (String s : consoleOut)
            sb.append(s).append("\n");
        return sb.toString();
    }

    @Override
    public void close() throws IOException {
        if (process != null)
            process.destroy();

        if (thread != null && thread.isAlive()) {
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
}

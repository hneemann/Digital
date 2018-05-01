/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.tt2;

import de.neemann.digital.lang.Lang;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * OSExecute is used to start external programs
 * It is used to start external fitters like fit1502.exe
 */
public class OSExecute {

    private final ProcessBuilder processBuilder;
    private int timeOutSec = 30;
    private StreamReader consoleReader;
    private Process process;
    private boolean ignoreReturnCode = false;

    /**
     * Creates a new instance
     *
     * @param args the program to start
     */
    public OSExecute(String... args) {
        processBuilder = new ProcessBuilder(args);
    }

    /**
     * Creates a new instance
     *
     * @param args the program to start
     */
    public OSExecute(List<String> args) {
        processBuilder = new ProcessBuilder(args);
    }

    /**
     * Sets the working directory
     *
     * @param workingDir the working directory
     * @return this for chained calls
     */
    public OSExecute setWorkingDir(File workingDir) {
        processBuilder.directory(workingDir);
        return this;
    }

    /**
     * Sets an environment variable
     *
     * @param key   the key
     * @param value the value
     * @return this for chained calls
     */
    public OSExecute setEnvVar(String key, String value) {
        processBuilder.environment().put(key, value);
        return this;
    }

    /**
     * Sets the time out period.
     *
     * @param timeOutSec time out in seconds
     * @return this for chained calls
     */
    public OSExecute setTimeOutSec(int timeOutSec) {
        this.timeOutSec = timeOutSec;
        return this;
    }

    /**
     * Starts the execution and waits for its completion.
     *
     * @return the console output of the started process
     * @throws IOException IOException
     */
    public String startAndWait() throws IOException {
        startProcess();
        return waitForProcess();
    }

    /**
     * Starts the process.
     *
     * @throws IOException IOException
     */
    public void startProcess() throws IOException {
        processBuilder.redirectErrorStream(true);

        process = processBuilder.start();

        InputStream console = process.getInputStream();
        consoleReader = new StreamReader(console);
        consoleReader.start();
    }

    /**
     * Sends a terminate signal to the running process.
     */
    public void terminate() {
        if (process.isAlive()) {
            process.destroy();
            ignoreReturnCode = true;
        }
    }

    /**
     * waits for the process to terminate
     *
     * @return the console output of the started process
     * @throws IOException IOException
     */
    public String waitForProcess() throws IOException {
        try {
            process.waitFor(timeOutSec, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (process.isAlive()) {
            process.destroy();
            consoleReader.interrupt();
            throw new IOException(Lang.get("err_processDoesNotTerminate_N", processBuilder.command()));
        }

        if (process.exitValue() != 0 && !ignoreReturnCode)
            throw new IOException(Lang.get("err_processExitedWithError_N1_N2", process.exitValue(), "\n" + consoleReader.toString()));

        try {
            consoleReader.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (consoleReader.getException() != null)
            throw consoleReader.getException();

        return consoleReader.toString();

    }

    /**
     * @return true if process is alive.
     */
    public boolean isAlive() {
        return process.isAlive();
    }

    private static final class StreamReader extends Thread {
        private final InputStream console;
        private final ByteArrayOutputStream baos;
        private IOException exception;

        private StreamReader(InputStream console) {
            this.console = console;
            baos = new ByteArrayOutputStream();
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                byte[] data = new byte[4096];
                int l;
                while ((l = console.read(data)) >= 0) {
                    baos.write(data, 0, l);
                }
            } catch (IOException e) {
                exception = e;
            }
        }

        private IOException getException() {
            return exception;
        }

        @Override
        public String toString() {
            return baos.toString();
        }
    }
}

/*
 * Copyright (c) 2017 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.builder.tt2;

import de.neemann.digital.lang.Lang;

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
        if (timeOutSec == 0)
            timeOutSec = Integer.MAX_VALUE;
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
        processBuilder.redirectErrorStream(true);

        process = processBuilder.start();

        InputStream console = process.getInputStream();
        StreamReader consoleReader = new StreamReader(console);
        consoleReader.start();

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

        try {
            consoleReader.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (process.exitValue() != 0 && !ignoreReturnCode)
            throw new IOException(Lang.get("err_processExitedWithError_N1_N2", process.exitValue(), "\n" + consoleReader.toString()));

        if (consoleReader.getException() != null)
            throw consoleReader.getException();

        return consoleReader.toString();
    }

    /**
     * Sends a terminate signal to the running process.
     */
    public void terminate() {
        if (process.isAlive()) {
            ignoreReturnCode = true;
            process.destroy();
        }
    }

    /**
     * @return true if process is alive.
     */
    public boolean isAlive() {
        return process.isAlive();
    }

    /**
     * Start process in its own thread.
     *
     * @param callback the callback functions
     * @return this for chained calls
     */
    public OSExecute startInThread(ProcessCallback callback) {
        new WaitThread(this, callback).start();
        return this;
    }

    private static final class StreamReader extends Thread {
        private final InputStream console;
        private final RotationByteArrayOutputStream baos;
        private IOException exception;

        private StreamReader(InputStream console) {
            this.console = console;
            baos = new RotationByteArrayOutputStream(20 * 1024);
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                byte[] data = new byte[4096];
                int l;
                while ((l = console.read(data)) >= 0) {
                    synchronized (baos) {
                        baos.write(data, 0, l);
                    }
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
            synchronized (baos) {
                return baos.toString();
            }
        }
    }

    private static final class WaitThread extends Thread {
        private final OSExecute os;
        private final ProcessCallback callback;

        private WaitThread(OSExecute os, ProcessCallback callback) {
            this.os = os;
            this.callback = callback;
            setDaemon(true);
        }

        @Override
        public void run() {
            try {
                String result = os.startAndWait();
                if (callback != null)
                    callback.processTerminated(result);
            } catch (Exception e) {
                if (callback != null)
                    callback.exception(e);
            }
        }
    }

    /**
     * Process callback functions
     */
    public interface ProcessCallback {
        /**
         * The console out after process is terminated
         *
         * @param consoleOut the console output
         */
        void processTerminated(String consoleOut);

        /**
         * Called if an exception is thrown
         *
         * @param e the exception
         */
        void exception(Exception e);
    }
}

/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern.handler;

import de.neemann.digital.lang.Lang;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Helper to start and wait for a process.
 */
public final class ProcessStarter {

    private ProcessStarter() {
    }

    /**
     * Helper to start a process.
     * If result value is not null an exception is thrown.
     *
     * @param dir  the folder in which the process is started
     * @param args the argument
     * @return the console output
     * @throws IOException IOException
     */
    public static String start(File dir, String... args) throws IOException {
        ProcessBuilder pb = new ProcessBuilder(args).redirectErrorStream(true).directory(dir);
        Process p;
        try {
            p = pb.start();
        } catch (IOException e) {
            throw new IOException(Lang.get("err_couldNotStartProcess_N", Arrays.toString(args)));
        }
        ReaderThread rt = new ReaderThread(p.getInputStream());
        rt.start();
        try {
            int exitValue = p.waitFor();
            rt.join();

            String output = rt.toString();

            if (exitValue != 0)
                throw new IOException(Lang.get("err_exitValueNotNull_N_O", exitValue, output));

            return output;
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    private static final class ReaderThread extends Thread {
        private final ByteArrayOutputStream baos;
        private final InputStream in;

        private ReaderThread(InputStream in) {
            this.in = in;
            baos = new ByteArrayOutputStream();
        }

        @Override
        public void run() {
            try {
                try {
                    byte[] buffer = new byte[4096];
                    int len;
                    while ((len = in.read(buffer)) > 0)
                        baos.write(buffer, 0, len);
                } finally {
                    in.close();
                }
            } catch (IOException e) {
                // do nothing, simply end the thread
            }
        }

        @Override
        public String toString() {
            return baos.toString();
        }
    }
}

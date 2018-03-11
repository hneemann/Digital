/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern;

import de.neemann.digital.lang.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

/**
 * Helper to start and wait for a process.
 */
public final class ProcessStarter {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessStarter.class);

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
        ProcessBuilder pb = new ProcessBuilder(args).redirectErrorStream(true);
        if (dir != null)
            pb.directory(dir);
        Process p;
        try {
            p = pb.start();
        } catch (IOException e) {
            throw new CouldNotStartProcessException(Lang.get("err_couldNotStartProcess_N", Arrays.toString(args)), e);
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

    /**
     * Removes a folder inclusive the contents
     *
     * @param dir the folder to remove
     */
    public static void removeFolder(File dir) {
        File[] list = dir.listFiles();
        if (list != null) {
            for (File f : list) {
                if (f.isDirectory())
                    removeFolder(f);
                else if (!f.delete()) LOGGER.warn("file " + f + " could not be deleted!");
            }
        }
        if (!dir.delete()) LOGGER.warn("dir " + dir + " could not be deleted!");
    }

    /**
     * Merges the given string.
     * If all strings are null or empty, null is returned
     *
     * @param strings the strings to join
     * @return the joined string or null
     */
    public static String joinStrings(String... strings) {
        StringBuilder sb = new StringBuilder();
        for (String s : strings) {
            if (s != null) {
                String str = s.trim();
                if (str.length() > 0) {
                    if (sb.length() > 0)
                        sb.append("\n");
                    sb.append(str);
                }
            }
        }
        if (sb.length() > 0)
            return sb.toString();
        else
            return null;
    }

    /**
     * Thrown if process could not be started
     */
    public static final class CouldNotStartProcessException extends IOException {
        private CouldNotStartProcessException(String message, IOException cause) {
            super(message, cause);
        }
    }
}

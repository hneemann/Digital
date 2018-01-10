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
 * <p>
 * Created by hneemann on 10.03.17.
 */
public class OSExecute {

    private final ProcessBuilder procesBuilder;

    /**
     * Creates a new instance
     *
     * @param args the program to start
     */
    public OSExecute(String... args) {
        procesBuilder = new ProcessBuilder(args);
    }

    /**
     * Creates a new instance
     *
     * @param args the program to start
     */
    public OSExecute(List<String> args) {
        procesBuilder = new ProcessBuilder(args);
    }

    /**
     * Sets the working directory
     *
     * @param workingDir the working directory
     */
    public void setWorkingDir(File workingDir) {
        procesBuilder.directory(workingDir);
    }

    /**
     * Sets an environment variable
     *
     * @param key   the key
     * @param value the value
     */
    public void setEnvVar(String key, String value) {
        procesBuilder.environment().put(key, value);
    }

    /**
     * Starts the execution and waits for its completion.
     *
     * @return the console output of the started process
     * @throws IOException IOException
     */
    public String start() throws IOException {
        procesBuilder.redirectErrorStream(true);

        Process p = procesBuilder.start();

        InputStream console = p.getInputStream();
        StreamReader sr = new StreamReader(console);
        sr.start();

        try {
            p.waitFor(30, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (p.isAlive()) {
            p.destroy();
            sr.interrupt();
            throw new IOException(Lang.get("err_processDoesNotTerminate_N", procesBuilder.command()));
        }

        if (p.exitValue() != 0)
            throw new IOException(Lang.get("err_processExitedWithError_N1_N2", p.exitValue(), "\n" + sr.toString()));

        try {
            sr.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (sr.getException() != null)
            throw sr.getException();

        return sr.toString();
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

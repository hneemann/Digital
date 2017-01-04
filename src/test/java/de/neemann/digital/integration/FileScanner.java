package de.neemann.digital.integration;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

/**
 * @author hneemann
 */
public class FileScanner {

    private Interface test;
    private ArrayList<Error> errors;

    public FileScanner(Interface test) {
        this.test = test;
    }

    public int scan(File path) throws Exception {
        errors = new ArrayList<>();
        int count = scanIntern(path);
        if (errors.isEmpty())
            return count;

        System.out.println("errors:");
        for (Error e : errors) {
            System.err.println("----> error in: " + e.f);
            e.e.printStackTrace();
        }
        throw new Exception("errors testing files");
    }

    private int scanIntern(File path) throws IOException {
        int count = 0;
        File[] files = path.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    if (f.getName().charAt(0) != '.') {
                        count += scanIntern(f);
                    }
                } else {
                    if (f.getName().endsWith(".dig")) {
                        try {
                            test.check(f);
                        } catch (Exception e) {
                            errors.add(new Error(f, e));
                        }
                        count++;
                    }
                }
            }
        }
        return count;
    }

    public interface Interface {
        void check(File f) throws Exception;
    }

    private static class Error {

        private final File f;
        private final Exception e;

        private Error(File f, Exception e) {
            this.f = f;
            this.e = e;
        }
    }
}

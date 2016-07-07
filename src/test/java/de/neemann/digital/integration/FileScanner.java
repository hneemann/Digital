package de.neemann.digital.integration;

import java.io.File;

/**
 * @author hneemann
 */
public class FileScanner {

    private Interface test;

    public FileScanner(Interface test) {
        this.test = test;
    }

    public int scan(File path) throws Exception {
        int count = 0;
        File[] files = path.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    if (f.getName().charAt(0) != '.') {
                        count += scan(f);
                    }
                } else {
                    if (f.getName().endsWith(".dig")) {
                        test.check(f);
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
}

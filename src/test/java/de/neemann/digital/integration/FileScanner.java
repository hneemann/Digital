/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.integration;

import java.io.File;
import java.util.ArrayList;

/**
 */
public class FileScanner {

    private Interface test;
    private ArrayList<Error> errors;
    private int pos;
    private boolean output = true;
    private String suffix=".dig";

    public FileScanner(Interface test) {
        this.test = test;
    }

    public FileScanner noOutput() {
        output = false;
        return this;
    }

    public FileScanner setSuffix(String suffix) {
        this.suffix = suffix;
        return this;
    }

    public int scan(File path) throws Exception {
        errors = new ArrayList<>();
        int count;
        try {
            count = scanIntern(path);
        } catch (SkipAllException e) {
            System.err.println("all tests are skipped: " + e.getMessage());
            throw e;
        }

        if (output && pos > 0)
            System.out.println();

        System.out.println("-- tested " + count + " examples");
        if (errors.isEmpty())
            return count;

        System.err.println("errors: " + errors.size());
        for (Error e : errors) {
            System.err.println("----> error in: " + e.f);
            e.e.printStackTrace();
        }
        throw new Exception("errors testing files: " + errors.size());
    }

    private int scanIntern(File path) throws Exception {
        int count = 0;
        File[] files = path.listFiles();
        if (files != null) {
            for (File f : files) {
                if (f.isDirectory()) {
                    if (f.getName().charAt(0) != '.') {
                        count += scanIntern(f);
                    }
                } else {
                    String name = f.getName();
                    if (name.endsWith(suffix)) {
                        if (output) {
                            if (pos + name.length() >= 78) {
                                System.out.println();
                                pos = 0;
                            }
                            System.out.print(name);
                            pos += name.length();
                        }
                        try {
                            test.check(f);
                        } catch (SkipAllException e) {
                            throw e;
                        } catch (Throwable e) {
                            errors.add(new Error(f, e));
                        }
                        if (output) {
                            System.out.print(", ");
                            pos += 2;
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
        private final Throwable e;

        private Error(File f, Throwable e) {
            this.f = f;
            this.e = e;
        }
    }

    public static class SkipAllException extends Exception {
        public SkipAllException(String s) {
            super(s);
        }
    }
}

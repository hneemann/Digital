/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.lang;

import de.neemann.digital.integration.Resources;
import de.neemann.gui.language.Bundle;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 */
public class TestLang extends TestCase {
    private static final String SOURCEPATH = "/home/hneemann/Dokumente/Java/digital/src/main/java";

    private HashMap<String, LangSet> map = new HashMap<>();

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Bundle b = new Bundle("lang/lang");
        addFrom(b, "en");
        addFrom(b, "de");
    }

    /**
     * enforces that at least german and english translations have identical keys
     */
    public void testLang() {
        for (Map.Entry<String, LangSet> l : map.entrySet()) {
            if (l.getValue().languages.size() != 2) {
                throw new RuntimeException("key " + l.getKey() + " is only available in " + l.getValue());
            }
        }
    }

    /**
     * Finds usages of keys which are not present in the language xml files
     *
     * @throws IOException IOException
     */
    public void testUsages() throws IOException {
        File sources = getSourceFiles();
        HashSet<String> keys = new HashSet<>();
        parseTree(sources, keys);
        // check also test code. Is needed because documentation generation uses language key also.
        parseTree(new File(Resources.getRoot(), "../java"), keys);

        // check templates for error messages
        parseTree(new File(sources, "../resources"), keys);

        StringBuilder sb = new StringBuilder();
        for (String key : map.keySet()) {
            if (!keys.contains(key)) {
                if (!(key.startsWith("key_") || key.startsWith("elem_"))) {
                    if (sb.length() > 0)
                        sb.append(", ");
                    sb.append('"').append(key).append('"');
                }
            }
        }
        if (sb.length() > 0)
            fail("there are unused language keys: " + sb.toString());
    }

    public static File getSourceFiles() {
        String sources = System.getProperty("sources");
        if (sources == null) {
            System.out.println("environment variable sources not set!!!");
            System.out.println("Try to use hardcoded " + SOURCEPATH);
            sources = SOURCEPATH;
        }
        return new File(sources);
    }

    private static final String PATTERN = "Lang.get(\"";
    private static final String TEM_PATTERN = "panic(\"";

    private void parseTree(File file, HashSet<String> keys) throws IOException {
        File[] files = file.listFiles();
        if (files != null)
            for (File f : files) {
                if (f.isDirectory() && f.getName().charAt(0) != '.')
                    parseTree(f, keys);
                if (f.isFile()) {
                    try {
                        if (f.getName().endsWith(".java"))
                            checkSourceFile(f, keys, PATTERN);
                        if (f.getName().endsWith(".tem") || f.getName().endsWith(".v"))
                            checkSourceFile(f, keys, TEM_PATTERN);
                    } catch (AssertionFailedError e) {
                        throw new AssertionFailedError(e.getMessage() + " in file " + f);
                    }
                }
            }
    }

    private void checkSourceFile(File f, HashSet<String> keys, String pattern) throws IOException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"))) {
            int linecount = 0;
            String line;
            while ((line = r.readLine()) != null) {
                linecount++;
                try {
                    checkSourceLine(line, keys, pattern);
                } catch (AssertionFailedError e) {
                    throw new AssertionFailedError(e.getMessage() + " in line " + linecount);
                }
            }
        }
    }

    private void checkSourceLine(String line, HashSet<String> keys, String PATTERN) {
        if (line.contains(PATTERN)) {
            int pos = line.indexOf(PATTERN, 0);
            while (pos >= 0) {

                StringBuilder sb = new StringBuilder();
                pos += PATTERN.length();
                while (line.charAt(pos) != '\"') {
                    sb.append(line.charAt(pos));
                    pos++;
                }
                pos++;
                while (line.charAt(pos) == ' ') pos++;
                char nextChar = line.charAt(pos);

                if (nextChar != '+') {
                    final String key = sb.toString();
                    keys.add(key);
                    checkSourceKey(key);
                }
                pos = line.indexOf(PATTERN, pos);
            }
        }
    }

    private void checkSourceKey(String key) {
        assertTrue("key " + key + " not present!", map.containsKey(key));
    }

    private void addFrom(Bundle b, String lang) {
        for (String k : b.getResources(lang).getKeys()) {
            LangSet l = map.get(k);
            if (l == null) {
                l = new LangSet();
                map.put(k, l);
            }
            l.add(lang);
        }

    }

    private class LangSet {
        private HashSet<String> languages;

        private LangSet() {
            languages = new HashSet<>();
        }

        public void add(String lang) {
            languages.add(lang);
        }

        @Override
        public String toString() {
            return languages.toString();
        }
    }
}

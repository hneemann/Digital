package de.neemann.digital.lang;

import de.neemann.gui.language.Bundle;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

/**
 * Created by hneemann on 20.10.16.
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
        String sources = System.getProperty("sources");
        if (sources == null) {
            System.out.println("environment variable sources not set!!!");
            System.out.println("Try to use hardcoded " + SOURCEPATH);
            sources = SOURCEPATH;
        }

        parseTree(new File(sources));
    }

    private void parseTree(File file) throws IOException {
        File[] files = file.listFiles();
        if (files != null)
            for (File f : files) {
                if (f.isDirectory() && f.getName().charAt(0) != '.')
                    parseTree(f);
                if (f.isFile() && f.getName().endsWith(".java")) {
                    try {
                        checkSourceFile(f);
                    } catch (AssertionFailedError e) {
                        throw new AssertionFailedError(e.getMessage() + " in file " + f);
                    }
                }
            }
    }

    private void checkSourceFile(File f) throws IOException {
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), "utf-8"))) {
            int linecount = 0;
            String line;
            while ((line = r.readLine()) != null) {
                linecount++;
                try {
                    checkSourceLine(line);
                } catch (AssertionFailedError e) {
                    throw new AssertionFailedError(e.getMessage() + " in line " + linecount);
                }
            }
        }
    }

    private static final String PATTERN = "Lang.get(\"";

    private void checkSourceLine(String line) {
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

                if (nextChar != '+')
                    checkSourceKey(sb.toString());
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

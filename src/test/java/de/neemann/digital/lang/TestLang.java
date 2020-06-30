/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.lang;

import de.neemann.digital.integration.Resources;
import de.neemann.gui.language.Bundle;
import de.neemann.gui.language.Language;
import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.jdom2.CDATA;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.jdom2.output.Format;
import org.jdom2.output.XMLOutputter;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Checks the language files to make sure they are consistent.
 */
public class TestLang extends TestCase {
    private static final String SOURCEPATH = "/home/hneemann/Dokumente/Java/digital/src/main/java";

    private HashMap<String, LangSet> map = new HashMap<>();
    private Bundle bundle;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        bundle = new Bundle("lang/lang");
        addKeysFrom("en");
        addKeysFrom("de");
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
     * Finds usages of keys in the code which are not present in the language xml files
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
                if (!(key.startsWith("key_") || key.startsWith("elem_")
                        || key.startsWith("attr_panel_") || key.startsWith("tutorial")
                        || key.startsWith("cli_help_") || key.startsWith("colorName_"))) {
                    if (sb.length() > 0)
                        sb.append(", ");
                    sb.append('"').append(key).append('"');
                }
            }
        }
        if (sb.length() > 0)
            fail("there are unused language keys: " + sb.toString());
    }

    /**
     * Outputs all missing or modified language keys in languages which are not
     * forced to be complete. (all languages except de and en)
     */
    public void testAdditionalLanguages() throws IOException, JDOMException {
        de.neemann.gui.language.Resources master = bundle.getResources("en");
        for (Language l : bundle.getSupportedLanguages()) {
            if (!(l.getName().equals("de") || l.getName().equals("en")))
                checkLanguage(master, l);
        }
    }

    private void checkLanguage(de.neemann.gui.language.Resources master, Language l) throws IOException, JDOMException {
        ArrayList<String> missing = new ArrayList<>();
        final File rootFolder = new File(Resources.getRoot(), "../../main/resources/lang");
        File lang = new File(rootFolder, "lang_" + l.getName() + ".xml");

        if (!lang.exists()) {
            new XMLOutputter(Format.getPrettyFormat()).output(new Document().setRootElement(new Element("resources")), new FileOutputStream(lang));
            File f2 = new File(rootFolder, "lang_" + l.getName() + "_ref.xml");
            new XMLOutputter(Format.getPrettyFormat()).output(new Document().setRootElement(new Element("resources")), new FileOutputStream(f2));
        }

        de.neemann.gui.language.Resources langResources = new de.neemann.gui.language.Resources(lang);

        Set<String> langKeys = langResources.getKeys();
        for (String k : master.getKeys()) {
            if (!langKeys.contains(k))
                missing.add(k);
        }

        ArrayList<Element> dif = new ArrayList<>();

        if (!missing.isEmpty()) {
            missing.sort(String::compareTo);
            for (String k : missing)
                dif.add(new Element("key").setAttribute("name", k).setAttribute("type", "new")
                        .addContent(addTextTo(new Element("en"), master.get(k)))
                        .addContent(new Element(l.getName()).setText("-")));
        }

        ArrayList<String> obsolete = new ArrayList<>();
        for (String k : langKeys) {
            if (!master.getKeys().contains(k))
                obsolete.add(k);

            String val = langResources.get(k);
            assertFalse(l + "; " + k + "; not trimmed: >" + val + "<", !val.contains("\n") && !val.equals(val.trim()));
        }
        if (!obsolete.isEmpty()) {
            fail("Obsolete language keys for: " + l + "; " + obsolete);
        }

        ArrayList<String> modified = new ArrayList<>();
        de.neemann.gui.language.Resources refResource =
                new de.neemann.gui.language.Resources(new File(rootFolder, "lang_" + l.getName() + "_ref.xml"));
        for (String k : master.getKeys()) {
            String m = master.get(k);
            String o = refResource.get(k);
            if (m != null && o != null && !removeSpacesFrom(m).equals(removeSpacesFrom(o)))
                modified.add(k);
        }

        if (!modified.isEmpty()) {
            for (String k : modified)
                dif.add(new Element("key").setAttribute("name", k).setAttribute("type", "modified")
                        .addContent(addTextTo(new Element("en"), master.get(k)))
                        .addContent(addTextTo(new Element(l.getName()), langResources.get(k))));
        }

        ArrayList<String> missingInRef = new ArrayList<>();
        for (String k : langKeys)
            if (refResource.get(k) == null)
                missingInRef.add(k);

        if (!missingInRef.isEmpty())
            fail("Missing keys in the reference file for: " + l + ";  " + missingInRef);

        File filename = new File(Resources.getRoot(), "../../../target/lang_diff_" + l.getName() + ".xml");
        if (dif.isEmpty()) {
            filename.delete();
        } else {
            restoreOriginalKeyOrder(dif);
            try {
                Element root = new Element("language").setAttribute("name", l.getName());
                for (Element e : dif)
                    root.addContent(e);
                Document doc = new Document(root);
                new XMLOutputter(Format.getPrettyFormat()).output(doc, new FileOutputStream(filename));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private String removeSpacesFrom(String str) {
        StringBuilder sb = new StringBuilder();
        boolean wasSpace = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == ' ' || c == '\n' || c == '\r' || c == '\t') {
                if (sb.length() > 0)
                    wasSpace = true;
            } else {
                if (wasSpace)
                    sb.append(' ');
                sb.append(c);
                wasSpace = false;
            }
        }
        return sb.toString();
    }

    private Element addTextTo(Element el, String text) {
        if (text.contains("<html>"))
            el.addContent(new CDATA(text));
        else
            el.setText(text);
        return el;
    }

    private void restoreOriginalKeyOrder(List<Element> dif) throws IOException, JDOMException {
        File langPath = new File(Resources.getRoot(), "../../main/resources/lang");
        if (!langPath.exists())
            throw new IOException("lang folder not found!");

        Element orig = new SAXBuilder().build(new File(langPath, "lang_de.xml")).getRootElement();
        int i = 0;
        HashMap<String, Integer> orderMap = new HashMap<>();
        for (Element e : (List<Element>) orig.getChildren())
            orderMap.put(e.getAttributeValue("name"), i++);

        dif.sort((e1, e2) -> {
            String k1 = e1.getAttributeValue("name");
            String k2 = e2.getAttributeValue("name");
            return orderMap.get(k1) - orderMap.get(k2);
        });
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
        try (BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(f), StandardCharsets.UTF_8))) {
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

    private void checkSourceLine(String line, HashSet<String> keys, String pattern) {
        if (line.contains(pattern)) {
            int pos = line.indexOf(pattern);
            while (pos >= 0) {

                StringBuilder sb = new StringBuilder();
                pos += pattern.length();
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
                pos = line.indexOf(pattern, pos);
            }
        }
    }

    private void checkSourceKey(String key) {
        assertTrue("key " + key + " not present!", map.containsKey(key));
    }

    private void addKeysFrom(String lang) {
        final de.neemann.gui.language.Resources resources = bundle.getResources(lang);
        for (String k : resources.getKeys()) {
            LangSet l = map.get(k);
            if (l == null) {
                l = new LangSet();
                map.put(k, l);
            }
            l.add(lang);

            String val = resources.get(k);
            assertFalse("not trimmed: >" + val + "<", !val.contains("\n") && !val.equals(val.trim()));
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

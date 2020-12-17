/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.gui.language;

import com.thoughtworks.xstream.XStream;
import de.neemann.digital.XStreamValid;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Bundle {

    private final String name;
    private final ArrayList<Language> list;

    /**
     * Creates a new instance
     *
     * @param name the bundles name
     */
    public Bundle(String name) {
        this.name = name;
        InputStream in = getClass().getClassLoader().getResourceAsStream(name + ".xml");
        XStream xStream = new XStreamValid();
        xStream.alias("languages", MyLang.class);
        xStream.addImplicitCollection(MyLang.class, "lang");
        xStream.alias("lang", MyLangEntry.class);
        xStream.aliasAttribute(MyLangEntry.class, "name", "name");
        xStream.aliasAttribute(MyLangEntry.class, "filename", "file");
        xStream.aliasAttribute(MyLangEntry.class, "displayName", "display");
        MyLang l = (MyLang) xStream.fromXML(in);
        list = new ArrayList<>();
        for (MyLangEntry e : l.lang)
            list.add(new Language(e.name, e.displayName, e.filename));
    }

    /**
     * returns the resources for the given language
     *
     * @param lang the language
     * @return the resources or null if not available
     */
    public Resources getResources(String lang) {
        String found = null;
        int bestMatchLen = 0;
        for (Language l : list) {
            String n = l.getName();
            int m = 0;
            while (m < n.length() && m < lang.length() && n.charAt(m) == lang.charAt(m)) {
                m++;
            }
            if (m > 1 && m > bestMatchLen) {
                bestMatchLen = m;
                found = n;
            }
        }
        if (found == null)
            return null;

        InputStream in = getClass().getClassLoader().getResourceAsStream(name + "_" + found + ".xml");
        if (in == null)
            return null;

        return new Resources(in, found);
    }

    /**
     * Returns all supported languages
     *
     * @return the languages
     */
    public List<Language> getSupportedLanguages() {
        return list;
    }

    private static class MyLang {
        private ArrayList<MyLangEntry> lang;
    }

    private static class MyLangEntry {
        private String name;
        private String displayName;
        private String filename;
    }
}

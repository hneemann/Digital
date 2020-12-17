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
 * Provides the list of languages which are supported.
 * Also contains the code to match certain language code to a resource name.
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

    private enum Match {NONE, LANG, LANG_COUNTRY}

    /**
     * Resolves the best matching resource name.
     *
     * @param lang name in the form en-us, or de-at
     * @return the best matching resource name
     */
    public String findResource(String lang) {
        for (Language l : list)
            if (l.getName().equals(lang))
                return lang;

        String found = null;
        Match bestMatch = Match.NONE;
        for (Language l : list) {
            String n = l.getName();
            int m = 0;
            while (m < n.length() && m < lang.length() && n.charAt(m) == lang.charAt(m)) {
                m++;
            }
            Match match = Match.NONE;
            if (m == 5)
                match = Match.LANG_COUNTRY;
            else if (m == 2 || m == 3)
                match = Match.LANG;

            if (match.compareTo(bestMatch) > 0) {
                bestMatch = match;
                found = n;
            }
        }

        return found;
    }

    /**
     * returns the resources for the given language
     *
     * @param lang the language
     * @return the resources or null if not available
     */
    public Resources getResources(String lang) {
        Language found = null;
        for (Language l : list)
            if (l.getName().equals(lang))
                found = l;

        if (found == null)
            return null;

        InputStream in = getClass().getClassLoader().getResourceAsStream(name + "_" + lang + ".xml");
        if (in == null)
            return null;

        return new Resources(in);
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

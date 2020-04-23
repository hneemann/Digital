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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Bundle {

    private final Map<String, String> languages;
    private final String name;
    private final ArrayList<Language> list;

    private static XStream getxStream() {
        XStream xStream = new XStreamValid();
        xStream.alias("languages", Map.class);
        xStream.registerConverter(new Resources.MapEntryConverter("string"));
        return xStream;
    }

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
        languages = new HashMap<>();
        list = new ArrayList<>();
        for (MyLangEntry e : l.lang) {
            languages.put(e.name, e.displayName);
            list.add(new Language(e.name, e.displayName, e.filename));
        }
    }

    /**
     * returns the resources for the given language
     *
     * @param lang the language
     * @return the resources or null if not available
     */
    public Resources getResources(String lang) {
        if (!languages.containsKey(lang))
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

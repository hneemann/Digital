/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.gui.language;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.StaxDriver;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 */
public class Bundle {

    private final HashMap<String, String> languages;
    private final String name;
    private final ArrayList<Language> list;

    private static XStream getxStream() {
        XStream xStream = new XStream(new StaxDriver());
        xStream.alias("languages", Map.class);
        xStream.registerConverter(new Resources.MapEntryConverter());
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
        XStream xStream = getxStream();
        languages = (HashMap<String, String>) xStream.fromXML(in);
        list = new ArrayList<>();
        for (Map.Entry<String, String> e : languages.entrySet())
            list.add(new Language(e.getKey(), e.getValue()));
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
}

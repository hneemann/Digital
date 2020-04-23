/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.gui.language;

import java.util.Locale;

/**
 * Abstraction of a language
 */
public final class Language implements Comparable<Language> {

    private final String name;
    private final String displayName;
    private final String filename;

    /**
     * Creates a new instance
     *
     * @param name the languages name
     */
    public Language(String name) {
        this(name, "", "");
    }

    /**
     * Creates a new instance with the current language
     */
    public Language() {
        this(Locale.getDefault().getLanguage());
    }

    /**
     * Creates new instance
     *
     * @param name        name, eq. "en" or "de"
     * @param displayName the name shown to the user
     * @param filename    a name that contains only ASCII characters
     */
    public Language(String name, String displayName, String filename) {
        this.name = name;
        this.displayName = displayName;
        this.filename = filename;
    }


    @Override
    public String toString() {
        return displayName;
    }

    /**
     * returns the name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    @Override
    public int compareTo(Language o) {
        return displayName.compareTo(o.displayName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Language language = (Language) o;

        return name != null ? name.equals(language.name) : language.name == null;

    }

    @Override
    public int hashCode() {
        return name != null ? name.hashCode() : 0;
    }

    /**
     * @return a name that contains only ASCII characters
     */
    public String getFileName() {
        return filename;
    }
}

package de.neemann.digital.lang;

import java.util.Locale;

/**
 * @author hneemann
 */
public final class Language {

    private final String name;
    private transient Locale locale;

    /**
     * Creates a new instance
     *
     * @param name the languages name
     */
    public Language(String name) {
        this.name = name;
    }

    /**
     * Creates a new instance
     *
     * @param locale the locale
     */
    public Language(Locale locale) {
        this(locale.getLanguage());
        this.locale = locale;
    }

    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns the locale
     *
     * @return the locale
     */
    Locale getLocale() {
        return locale;
    }

    /**
     * returns the name
     *
     * @return the name
     */
    public String getName() {
        return name;
    }
}

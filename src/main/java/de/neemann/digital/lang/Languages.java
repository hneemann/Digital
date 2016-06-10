package de.neemann.digital.lang;

import java.util.ArrayList;
import java.util.Locale;

/**
 * @author hneemann
 */
public final class Languages {

    private static final class InstanceHolder {
        private static final Languages INSTANCE = new Languages();
    }

    /**
     * @return the languages instance
     */
    public static Languages getInstance() {
        return InstanceHolder.INSTANCE;
    }

    private final ArrayList<Language> list = new ArrayList<>();
    private String[] strArray;

    private Languages() {
        add(Locale.US);
        add(Locale.GERMANY);
    }

    private void add(Locale locale) {
        list.add(new Language(locale));
    }

    /**
     * Returns a list of all supported languages
     *
     * @return the languages list
     */
    public String[] getSupportedLanguages() {
        if (strArray == null) {
            strArray = new String[list.size()];
            for (int i = 0; i < strArray.length; i++)
                strArray[i] = list.get(i).getLocale().getDisplayName();
        }
        return strArray;
    }

    /**
     * Returns the index of the given language
     *
     * @param language the language
     * @return the index
     */
    public int getIndexOf(Language language) {
        for (int i = 0; i < list.size(); i++)
            if (list.get(i).getName().equals(language.getName()))
                return i;
        return 0;
    }

    /**
     * Returns the language by the languages index
     *
     * @param index the index
     * @return the language
     */
    public Language getByIndex(int index) {
        return list.get(index);
    }

    /**
     * Returns the default language
     *
     * @return the systems language, if there is a translation, US otherwise
     */
    public Language getDefault() {
        String lang = Locale.getDefault().getLanguage();
        for (Language l : list) {
            if (l.getLocale().getLanguage().equals(lang))
                return l;
        }
        return list.get(0);
    }

    /**
     * Returns a locale by the given name
     *
     * @param lang the language name
     * @return the locale matching the language
     */
    public Locale getLocaleByName(String lang) {
        return getLanguageByName(lang).getLocale();
    }

    private Language getLanguageByName(String lang) {
        if (lang == null)
            return getDefault();

        for (Language l : list)
            if (l.getName().equals(lang))
                return l;

        return getDefault();
    }

}

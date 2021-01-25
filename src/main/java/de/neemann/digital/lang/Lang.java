/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.lang;

import de.neemann.gui.language.Bundle;
import de.neemann.gui.language.Language;
import de.neemann.gui.language.Resources;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Locale;
import java.util.prefs.Preferences;

/**
 *
 */
public final class Lang {
    private static final Preferences PREFS = Preferences.userRoot().node("dig");
    private static final String LANGUAGE = "lang";

    private static class InstanceHolder {
        static final Lang INSTANCE = new Lang();
    }

    /**
     * gets an internationalized string
     *
     * @param key    the key
     * @param params optional parameters
     * @return the internationalized string of key if no translation present
     */
    public static String get(String key, Object... params) {
        return InstanceHolder.INSTANCE.getKey(key, params);
    }


    /**
     * Sets the GUI language
     *
     * @param language the language
     */
    public static void setLanguage(Language language) {
        PREFS.put(LANGUAGE, language.getName());
    }

    /**
     * gets an internationalized string
     *
     * @param key    the key
     * @param params optional parameters
     * @return the internationalized string or null if no translation present
     */
    public static String getNull(String key, Object... params) {
        return InstanceHolder.INSTANCE.getKeyNull(key, params);
    }

    /**
     * @return the resource bundle
     */
    public static Bundle getBundle() {
        return InstanceHolder.INSTANCE.bundle;
    }

    /**
     * @return the current language
     */
    public static Language currentLanguage() {
        return InstanceHolder.INSTANCE.currentLanguage;
    }

    /**
     * Sets the GUI language
     *
     * @param language the language
     */
    public static void setActualRuntimeLanguage(Language language) {
        InstanceHolder.INSTANCE.setRuntimeLanguage(language);
    }

    private final Bundle bundle;
    private final Resources defaultBundle;
    private Resources localeBundle;
    private Language currentLanguage;

    private Lang() {
        bundle = new Bundle("lang/lang");
        defaultBundle = bundle.getResources("en");
        String lang = PREFS.get(LANGUAGE, Locale.getDefault().getLanguage());
        try {
            localeBundle = bundle.getResources(lang);
        } catch (Exception e) {
            localeBundle = null;
            System.err.println("error reading translation for " + lang);
            e.printStackTrace();
        }

        if (localeBundle != null)
            currentLanguage = new Language(lang);
        else
            currentLanguage = new Language("en");
    }

    /**
     * Resolves a multilingual user defined string.
     * Allows the user to define multilingual content by using a simple syntax:
     * You can enter strings like "Word! {{de Welt!}} {{fr Monde!}}". This method extracts
     * the appropriate string for the current language.
     *
     * @param text the complete text description
     * @return the evaluated text.
     */
    public static String evalMultilingualContent(String text) {
        return evalMultilingualContent(text, currentLanguage());
    }

    static String evalMultilingualContent(String text, Language currentLanguage) {
        int pos = text.indexOf("{{");
        if (pos < 0) return text;

        String def = text.substring(0, pos).trim();

        do {
            int p2 = pos + 1;
            int count = 1;
            while (count > 0) {
                char c = text.charAt(p2);
                if (c == '{') count++;
                else if (c == '}') count--;
                p2++;
                if (p2 == text.length() && count > 0) {
                    p2 = 1;
                    break;
                }
            }
            p2 -= 2;

            if (p2 > pos + 3) {
                String l = text.substring(pos + 2, pos + 4);
                if (l.equalsIgnoreCase(currentLanguage.getName()))
                    return text.substring(pos + 4, p2).trim();
                if (l.equalsIgnoreCase("en") && def.isEmpty())
                    def = text.substring(pos + 4, p2).trim();

                pos = text.indexOf("{{", p2);
            } else
                return def;
        } while (pos > 0);

        return def;
    }

    /**
     * Only used for generation of documentation
     *
     * @param lang the language to use
     */
    private void setRuntimeLanguage(Language lang) {
        currentLanguage = lang;
        localeBundle = bundle.getResources(lang.getName());
    }

    private String getKey(String key, Object... params) {
        String str = getKeyNull(key, params);
        if (str != null)
            return str;
        else {
            System.out.println("<string name=\"" + key + "\">" + key.substring(key.indexOf("_") + 1) + "</string>");

            // If there is a missing language key and we are in testing mode which is the case
            // if the 'testdata' system property is set, let the test fail!
            // If we are in production usage, don't let the program  crash, simply return the key itself instead!
            if (System.getProperty("testdata") != null)
                throw new Error("missing language key '" + key + "'");

            return key + " " + Arrays.asList(params).toString();
        }
    }

    private String getKeyNull(String key, Object... params) {
        String str = null;
        if (localeBundle != null)
            str = decodeString(localeBundle, key, params);
        if (str == null)
            str = decodeString(defaultBundle, key, params);
        return str;
    }

    private String decodeString(Resources resources, String key, Object[] params) {
        String str = resources.get(key);
        if (str != null && params != null && params.length > 0)
            str = MessageFormat.format(str, params);
        return str;
    }

}

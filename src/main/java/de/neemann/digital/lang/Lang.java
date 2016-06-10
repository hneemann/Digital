package de.neemann.digital.lang;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;

/**
 * @author hneemann
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

    private final ResourceBundle defaultBundle;
    private ResourceBundle localeBundle;

    private Lang() {
        defaultBundle = ResourceBundle.getBundle("lang/lang", Locale.ENGLISH);
        try {
            String lang = PREFS.get(LANGUAGE, null);
            Locale currentLocale = Languages.getInstance().getLocaleByName(lang);
            localeBundle = ResourceBundle.getBundle("lang/lang", currentLocale);
        } catch (MissingResourceException e) {
        }
    }

    private String getKey(String key, Object... params) {
        try {
            return decodeString(localeBundle, key, params);
        } catch (MissingResourceException e) {
            System.out.println(key + "=" + key.substring(key.indexOf("_") + 1));
            try {
                return decodeString(defaultBundle, key, params);
            } catch (MissingResourceException e1) {
                return key;
            }
        }
    }

    private String getKeyNull(String key, Object... params) {
        try {
            return decodeString(localeBundle, key, params);
        } catch (MissingResourceException e) {
//            System.out.println(key + "=" + key.substring(key.indexOf("_") + 1));
            return null;
        }
    }

    private String decodeString(ResourceBundle bundle, String key, Object[] params) {
        String str = bundle.getString(key);
        if (params != null && params.length > 0)
            str = MessageFormat.format(str, params);
        return str;
    }

}

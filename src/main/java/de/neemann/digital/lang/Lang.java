package de.neemann.digital.lang;

import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

/**
 * @author hneemann
 */
public class Lang {

    private static class InstanceHolder {
        static final Lang INSTANCE = new Lang();
    }

    public static String get(String key, Object... params) {
        return InstanceHolder.INSTANCE.getKey(key, params);
    }

    private final ResourceBundle defaultBundle;
    private ResourceBundle localeBundle;

    private Lang() {
        Locale currentLocale = Locale.getDefault();
        defaultBundle = ResourceBundle.getBundle("lang/lang", Locale.ENGLISH);
        try {
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

    private String decodeString(ResourceBundle bundle, String key, Object[] params) {
        String str = bundle.getString(key);
        if (params != null && params.length > 0)
            str = MessageFormat.format(str, params);
        return str;
    }

}

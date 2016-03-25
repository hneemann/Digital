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

    private ResourceBundle bundle;

    private Lang() {
        Locale currentLocale = Locale.getDefault();
        try {
            bundle = ResourceBundle.getBundle("lang/lang", currentLocale);
        } catch (MissingResourceException e) {
            bundle = ResourceBundle.getBundle("lang/lang", Locale.ENGLISH);
        }
    }

    private String getKey(String key, Object... params) {
        try {
            String str = bundle.getString(key);
            if (params != null && params.length > 0)
                str = MessageFormat.format(str, params);
            return str;
        } catch (MissingResourceException e) {
            System.out.println(key + "=" + key.substring(key.indexOf("_") + 1));
            return key + " is missing";
        }
    }

}

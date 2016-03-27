package de.neemann.gui;

/**
 */
public class StringUtils {

    public static void addList(StringBuilder sb, Iterable<?> i, String separator) {
        boolean first = true;
        for (Object o : i) {
            if (first)
                first = false;
            else
                sb.append(separator);
            sb.append(o.toString());
        }
    }

    public static String getExceptionMessage(Throwable e) {
        StringBuilder sb = new StringBuilder();

        while (e != null) {
            sb.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
            e = e.getCause();
            if (e != null)
                sb.append("\ncaused by: ");
        }

        return sb.toString();
    }

}

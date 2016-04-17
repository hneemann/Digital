package de.neemann.gui;

/**
 * Some helper functions concerning strings
 */
public final class StringUtils {

    private StringUtils() {
    }

    /**
     * Adds a list to a {@link StringBuilder}
     *
     * @param sb        the StringBuilder
     * @param i         the list
     * @param separator the separator to use
     */
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

    /**
     * Creates a exception message
     *
     * @param e the {@link Throwable} instance
     * @return the message
     */
    public static String getExceptionMessage(Throwable e) {
        StringBuilder sb = new StringBuilder();

        while (e != null) {
//            sb.append(e.getClass().getSimpleName()).append(": ").append(e.getMessage());
            sb.append(e.getMessage());
            e = e.getCause();
            if (e != null)
                sb.append("\ncaused by: ");
        }

        return sb.toString();
    }

}

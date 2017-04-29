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
            final String m = e.getMessage();
            if (m != null && m.length() > 0)
                sb.append(m);
            else
                sb.append(e.getClass().getSimpleName());
            e = e.getCause();
            if (e != null)
                sb.append("\ncaused by: ");
        }

        return sb.toString();
    }

    /**
     * Formats text to html if it contains line breaks.
     * Short texts are unchanged. Ignores the containing line breaks.
     *
     * @param text the text
     * @return the unchanged text or a HTML segment
     */
    public static String textToHTML(String text) {
        return new LineBreaker().toHTML().breakLines(text);
    }

}

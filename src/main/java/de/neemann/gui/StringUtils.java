package de.neemann.gui;

/**
 * Some helper functions concerning strings
 */
public final class StringUtils {

    private static final int DEF_COLS = 60;

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

    /**
     * Formats text to html if it contains line breaks.
     * Short texts are unchanged.
     *
     * @param text the text
     * @return the unchanged text or a HTML segment
     */
    public static String textToHTML(String text) {
        String toolTipText = StringUtils.breakLines(text);
        if (toolTipText == null)
            return null;

        if (toolTipText.indexOf('\n') >= 0)
            toolTipText = "<html>" + toolTipText.replace("\n", "<br>") + "</html>";
        return toolTipText;
    }


    /**
     * Breaks a string separate lines, all multiple spaces and line breaks are removed.
     * calls {@code breakLines(text, 60)}.
     *
     * @param text the text to format
     * @return the formatted text
     */
    public static String breakLines(String text) {
        return breakLines(text, DEF_COLS);
    }

    /**
     * Breaks a string into separate lines, all multiple blanks and line breaks are removed.
     *
     * @param text the text to format
     * @param cols the number of columns
     * @return the formatted text
     */
    public static String breakLines(String text, int cols) {
        return breakLinesLabel("", 0, text, cols);
    }

    /**
     * Format a text width indentation
     *
     * @param label  label to print in front of the text
     * @param indent cols to indent the label
     * @param text   the text
     * @return the formatted text
     */
    public static String breakLinesLabel(String label, int indent, String text) {
        return breakLinesLabel(label, indent, text, DEF_COLS);
    }

    /**
     * Format a text width indentation
     *
     * @param label  label to print in front of the text
     * @param indent cols to indent the label
     * @param text   the text
     * @param cols   the number of columns
     * @return the formatted text
     */
    public static String breakLinesLabel(String label, int indent, String text, int cols) {
        if (text == null)
            return null;

        StringBuilder sb = new StringBuilder(label);
        for (int i = 0; i < indent - label.length(); i++)
            sb.append(" ");

        int pos = indent;
        boolean wasBlank = false;
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            switch (c) {
                case '\n':
                case '\r':
                case ' ':
                    if (!wasBlank) {
                        if (pos > cols) {
                            sb.append('\n');
                            for (int j = 0; j < indent; j++)
                                sb.append(" ");

                            pos = indent;
                        } else {
                            sb.append(' ');
                        }
                    }
                    wasBlank = true;
                    break;
                default:
                    sb.append(c);
                    wasBlank = false;
                    pos++;
            }
        }
        return sb.toString();
    }

}

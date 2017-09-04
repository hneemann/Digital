package de.neemann.digital.hdl.printer;

import java.io.*;

/**
 * Pretty printer for code witch handles indentation
 */
public class CodePrinter implements Closeable {
    // VHDL is defined to be ISO 8859-1
    private static final String CHARSET = "ISO8859-1";
    protected final OutputStream out;
    private final int indentWidth;
    private File file;
    private int ident = 0;
    private boolean newLine = true;

    /**
     * Creates a new instance
     *
     * @param file the output file
     * @throws FileNotFoundException FileNotFoundException
     */
    public CodePrinter(File file) throws FileNotFoundException {
        this(new FileOutputStream(file), 2);
        this.file = file;
    }

    /**
     * Creates a new instance
     *
     * @param out the output stream
     */
    public CodePrinter(OutputStream out) {
        this(out, 2);
    }


    /**
     * Creates a new instance
     *
     * @param out         the output stream
     * @param indentWidth the indent width
     */
    public CodePrinter(OutputStream out, int indentWidth) {
        this.indentWidth = indentWidth;
        if (out instanceof ByteArrayOutputStream || out instanceof BufferedOutputStream)
            this.out = out;
        else
            this.out = new BufferedOutputStream(out);
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    /**
     * Prints the given string and creates a line feed
     *
     * @param str the string to print
     * @return this for chained calls
     * @throws IOException IOException
     */
    public CodePrinter println(String str) throws IOException {
        print(str);
        println();
        return this;
    }

    /**
     * Prints a line feed
     *
     * @return this for chained calls
     * @throws IOException IOException
     */
    public CodePrinter println() throws IOException {
        print('\n');
        return this;
    }

    /**
     * prints a end of line
     *
     * @throws IOException IOException
     */
    public void eol() throws IOException {
        print('\n');
    }


    /**
     * Prints the given string
     *
     * @param str the string to print
     * @return this for chained calls
     * @throws IOException IOException
     */
    public CodePrinter print(String str) throws IOException {
        for (int i = 0; i < str.length(); i++)
            print(str.charAt(i));
        return this;
    }

    /**
     * Prints an int
     *
     * @param i the int to print
     * @return this for chained calls
     * @throws IOException IOException
     */
    public CodePrinter print(int i) throws IOException {
        print(Integer.toString(i));
        return this;
    }

    /**
     * Prints a long
     *
     * @param l the int to print
     * @return this for chained calls
     * @throws IOException IOException
     */
    public CodePrinter print(long l) throws IOException {
        print(Long.toString(l));
        return this;
    }

    /**
     * Prints the given character
     *
     * @param c the character to print
     * @return this for chained calls
     * @throws IOException IOException
     */
    public CodePrinter print(char c) throws IOException {
        if (newLine && c != '\n') {
            int pos = ident * indentWidth;
            for (int i = 0; i < pos; i++)
                out.write(' ');
            newLine = false;
        }
        if (c < 128)
            out.write(c);
        else
            out.write(("" + c).getBytes(CHARSET));
        if (c == '\n') {
            newLine = true;
            eolIsWritten();
        }
        return this;
    }

    /**
     * called after a eol is written.
     * Does nothing in this implementation.
     *
     * @throws IOException IOException
     */
    protected void eolIsWritten() throws IOException {
    }

    /**
     * increases the indentation
     *
     * @return this for chained calls
     */
    public CodePrinter inc() {
        ident++;
        return this;
    }

    /**
     * decreases the indentation
     *
     * @return this for chained calls
     */
    public CodePrinter dec() {
        if (ident > 0)
            ident--;
        return this;
    }

    /**
     * @return the file or null if file is not known
     */
    public File getFile() {
        return file;
    }
}

package de.neemann.digital.hdl.printer;

import java.io.*;

/**
 * Pretty printer for code witch handles indentation
 */
public class CodePrinter implements Closeable {
    protected final OutputStream out;
    private final int indentWidth;
    private int ident = 0;
    private boolean newLine = true;

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
        out.write(c);
        if (c == '\n')
            newLine = true;
        return this;
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

}

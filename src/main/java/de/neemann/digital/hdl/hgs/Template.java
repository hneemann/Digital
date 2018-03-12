/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

import java.io.*;

/**
 * Represents a template
 */
public class Template {
    private final Statement s;

    /**
     * Creates a new template.
     *
     * @param text the text to parse
     * @throws IOException     IOException
     * @throws ParserException ParserException
     */
    public Template(String text) throws IOException, ParserException {
        this(new StringReader(text));
    }

    /**
     * Creates a new template.
     *
     * @param in the text to parse
     * @throws IOException     IOException
     * @throws ParserException ParserException
     */
    public Template(InputStream in) throws IOException, ParserException {
        this(new InputStreamReader(in));
    }

    /**
     * Creates a new template.
     *
     * @param reader the text to parse
     * @throws IOException     IOException
     * @throws ParserException ParserException
     */
    public Template(Reader reader) throws IOException, ParserException {
        try {
            this.s = new Parser(reader).parse();
        } finally {
            reader.close();
        }
    }


    /**
     * Evaluates the template with an empty context
     *
     * @return the context
     * @throws EvalException EvalException
     */
    public Context execute() throws EvalException {
        return execute(new Context());
    }

    /**
     * Evaluates the template with the given context.
     * Create a new context every time this method is called!
     *
     * @param c the context
     * @return the context
     * @throws EvalException EvalException
     */
    public Context execute(Context c) throws EvalException {
        s.execute(c);
        return c;
    }

}

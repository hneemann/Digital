package de.neemann.digital.testing.parser;

/**
 * Expression thrown by the parser
 * <p>
 * Created by Helmut.Neemann on 02.12.2016.
 */
public class ParserException extends Exception {
    private final Tokenizer.Token tok;
    private final int line;

    /**
     * Creates a new instance
     *
     * @param message the error message
     * @param tok     the token which causes the error
     * @param line    the line number
     */
    public ParserException(String message, Tokenizer.Token tok, int line) {
        super(message);
        this.tok = tok;
        this.line = line;
    }

    /**
     * Creates a new instance
     *
     * @param message the error message
     * @param line    the line number
     */
    public ParserException(String message, int line) {
        this(message, null, line);
    }

    @Override
    public String getMessage() {
        if (tok != null)
            return super.getMessage() + ": " + tok.name() + " in line " + line;
        else
            return super.getMessage() + " in line " + line;
    }
}

package de.neemann.digital.analyse.parser;

import de.neemann.digital.analyse.expression.*;
import de.neemann.digital.lang.Lang;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import static de.neemann.digital.analyse.parser.Tokenizer.Token.*;

/**
 * Class to parse a string to an expression
 *
 * @author hneemann
 */
public class Parser {

    private final Tokenizer tokenizer;

    /**
     * Creates a new instance
     *
     * @param expression the string to parse
     */
    public Parser(String expression) {
        this(new StringReader(expression));
    }

    /**
     * Creates a new instance
     *
     * @param reader the reader to read the expression
     */
    public Parser(Reader reader) {
        tokenizer = new Tokenizer(reader);
    }

    /**
     * Parses the the string expression and returns a expression instance
     *
     * @return the expresion instance
     * @throws IOException    IOException
     * @throws ParseException ParseException
     */
    public Expression parse() throws IOException, ParseException {
        Expression expr = parseOr();
        if (!(tokenizer.next() == EOF))
            throw new ParseException(Lang.get("err_parserUnexpectedEndOfExpression"));
        return expr;
    }

    private Expression parseOr() throws IOException, ParseException {
        Expression ex = parseAnd();
        while (tokenizer.peek() == OR) {
            tokenizer.consume();
            ex = Operation.or(ex, parseAnd());
        }
        return ex;
    }

    private Expression parseAnd() throws IOException, ParseException {
        Expression ex = parseSimpleExp();
        while (tokenizer.peek() == AND) {
            tokenizer.consume();
            ex = Operation.and(ex, parseSimpleExp());
        }
        return ex;
    }

    private Expression parseSimpleExp() throws IOException, ParseException {
        switch (tokenizer.next()) {
            case NOT:
                return Not.not(parseSimpleExp());
            case OPEN:
                Expression exp = parseOr();
                if (!(tokenizer.next() == CLOSE))
                    throw new ParseException(Lang.get("err_parserMissingClosedParenthesis"));
                return exp;
            case IDENT:
                return new Variable(tokenizer.getIdent());
            case ONE:
                return Constant.ONE;
            case ZERO:
                return Constant.ZERO;
            default:
                throw new ParseException(Lang.get("err_parserUnexpectedToken_N", tokenizer.toString()));
        }
    }
}

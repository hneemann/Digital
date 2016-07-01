package de.neemann.digital.analyse.parser;

import de.neemann.digital.analyse.expression.Expression;
import de.neemann.digital.analyse.expression.Not;
import de.neemann.digital.analyse.expression.Operation;
import de.neemann.digital.analyse.expression.Variable;
import de.neemann.digital.lang.Lang;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;

import static java.io.StreamTokenizer.TT_EOF;
import static java.io.StreamTokenizer.TT_WORD;

/**
 * Class to parse a string to an expression
 *
 * @author hneemann
 */
public class Parser {

    private final StreamTokenizer tokenizer;

    /**
     * Creates a new instance
     * @param expression the string to parse
     */
    public Parser(String expression) {
        this(new StringReader(expression));
    }

    /**
     * Creates a new instance
     * @param reader the reader to read the expression
     */
    public Parser(Reader reader) {
        tokenizer = new StreamTokenizer(reader);
        tokenizer.wordChars('_', '_');
        tokenizer.wordChars('^', '^');
        tokenizer.wordChars('0', '9');
//        tokenizer.ordinaryChar('∧'); StreamTokenizer can not handle ordinary chars > 255
//        tokenizer.ordinaryChar('∨');
    }

    private boolean isNext(String str) throws IOException {
        int t = tokenizer.nextToken();
        if (t == TT_WORD && tokenizer.sval.equalsIgnoreCase(str))
            return true;

        tokenizer.pushBack();
        return false;
    }

    private boolean isNext(int c) throws IOException {
        int t = tokenizer.nextToken();
        if (t == c)
            return true;

        tokenizer.pushBack();
        return false;
    }


    /**
     * Parses the the string expression and returns a expression instance
     * @return the expresion instance
     * @throws IOException IOException
     * @throws ParseException ParseException
     */
    public Expression parse() throws IOException, ParseException {
        Expression expr = parseOr();
        if (!isNext(TT_EOF))
            throw new ParseException(Lang.get("err_parserUnexpectedEndOfExpression"));
        return expr;
    }

    private Expression parseOr() throws IOException, ParseException {
        Expression ex = parseAnd();
        while (isNext('+') || isNext("∨") || isNext('|')) {
            ex = Operation.or(ex, parseAnd());
        }
        return ex;
    }

    private Expression parseAnd() throws IOException, ParseException {
        Expression ex = parseSimpleExp();
        while (isNext('*') || isNext("∧") || isNext('&')) {
            ex = Operation.and(ex, parseSimpleExp());
        }
        return ex;
    }

    private Expression parseSimpleExp() throws IOException, ParseException {
        if (isNext('!')) {
            return Not.not(parseSimpleExp());
        } else if (isNext('(')) {
            Expression exp = parseOr();
            if (!isNext(')'))
                throw new ParseException(Lang.get("err_parserMissingClosedParenthesis"));
            return exp;
        } else if (isNext(TT_WORD)) {
            return new Variable(tokenizer.sval);
        } else
            throw new ParseException(Lang.get("err_parserUnexpectedToken_N", tokenizer.sval));
    }

}

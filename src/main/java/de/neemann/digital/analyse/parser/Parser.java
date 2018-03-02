/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.analyse.parser;

import de.neemann.digital.analyse.expression.*;
import de.neemann.digital.lang.Lang;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import static de.neemann.digital.analyse.parser.Tokenizer.Token.*;

/**
 * Class to parse a string to an expression
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
     * @return the expression instance
     * @throws IOException    IOException
     * @throws ParseException ParseException
     */
    public ArrayList<Expression> parse() throws IOException, ParseException {
        ArrayList<Expression> list = new ArrayList<>();
        while (true) {
            list.add(parseLet());
            switch (tokenizer.next()) {
                case EOF:
                    return list;
                case COMMA:
                    break;
                default:
                    throw new ParseException(Lang.get("err_parserUnexpectedToken_N", tokenizer.toString()));
            }
        }
    }

    private Expression parseLet() throws IOException, ParseException {
        if (tokenizer.peek() == IDENT && tokenizer.getIdent().equals("let")) {
            tokenizer.consume();
            consume(IDENT);
            String name = tokenizer.getIdent();
            consume(EQUAL);
            return new NamedExpression(name, parseOr());
        } else
            return parseOr();
    }

    private Expression parseOr() throws IOException, ParseException {
        Expression ex = parseAnd();
        while (tokenizer.peek() == OR || tokenizer.peek() == XOR) {
            if (tokenizer.next() == OR)
                ex = Operation.or(ex, parseAnd());
            else
                ex = Operation.xor(ex, parseAnd());
        }
        return ex;
    }

    private Expression parseAnd() throws IOException, ParseException {
        Expression ex = parseSimpleExp();
        while (true) {
            if (tokenizer.peek() == AND) {
                tokenizer.consume();
                ex = Operation.and(ex, parseSimpleExp());
            } else if (isSimpleEx(tokenizer.peek())) {
                ex = Operation.and(ex, parseSimpleExp());
            } else
                return ex;
        }
    }

    private boolean isSimpleEx(Tokenizer.Token tok) {
        return tok == NOT || tok == OPEN || tok == IDENT || tok == ONE || tok == ZERO;
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

    private void consume(Tokenizer.Token token) throws IOException, ParseException {
        if (!tokenizer.next().equals(token))
            throw new ParseException(Lang.get("err_parserUnexpectedToken_N", tokenizer.toString()));
    }

}

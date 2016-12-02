package de.neemann.digital.testing.parser;

import de.neemann.digital.testing.Value;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

/**
 * Parser to parse test data.
 * The constructor takes a string, and after a call to parse()
 * the names of the signals and the test vectors are generated.
 * <p>
 * Created by Helmut.Neemann on 02.12.2016.
 */
public class Parser {

    private final ArrayList<String> names;
    private final ArrayList<Value[]> lines;
    private final ArrayList<Value> values;
    private final Tokenizer tok;

    /**
     * Creates a new instance
     *
     * @param data the test data string
     */
    public Parser(String data) {
        names = new ArrayList<>();
        lines = new ArrayList<>();
        values = new ArrayList<>();
        tok = new Tokenizer(new BufferedReader(new StringReader(data)));
    }

    /**
     * Parses the data
     *
     * @return this for chained calls
     * @throws IOException     IOException
     * @throws ParserException ParserException
     */
    public Parser parse() throws IOException, ParserException {
        try {
            parseHeader();

            parseValues();

            return this;
        } catch (NumberFormatException e) {
            throw new ParserException("Invalid number:" + e.getMessage(), Tokenizer.Token.NUMBER, tok.getLine());
        }

    }

    private void parseValues() throws IOException, ParserException {
        while (true) {
            Tokenizer.Token t = tok.peek();
            switch (t) {
                case EOL:
                    break;
                case EOF:
                    return;
                case NUMBER:
                    parseLine();
                    break;
                case IDENT:
                    if (tok.getIdent().equals("for")) {
                        tok.consume();
                        expect(Tokenizer.Token.OPEN);
                        expect(Tokenizer.Token.NUMBER);
                        int count = Integer.parseInt(tok.getIdent());
                        expect(Tokenizer.Token.CLOSE);
                        parseForLine(count);
                    } else {
                        parseLine();
                    }
                    break;
            }
        }
    }

    private void parseForLine(int count) throws IOException, ParserException {
        ArrayList<Entry> entries = new ArrayList<>();
        while (true) {
            Tokenizer.Token token = tok.next();
            switch (token) {
                case NUMBER:
                case IDENT:
                    Value v = new Value(tok.getIdent().toUpperCase());
                    entries.add(n -> v);
                    break;
                case OPEN:
                    Expression exp = parseExpression();
                    entries.add(n -> new Value((int) exp.value(n)));
                    expect(Tokenizer.Token.CLOSE);
                    break;
                case EOF:
                case EOL:
                    for (int n = 0; n < count; n++) {
                        for (Entry entry : entries) values.add(entry.getValue(n));
                        addLine();
                    }
                    return;
                default:
                    throw new ParserException("unexpected token", token, tok.getLine());
            }
        }
    }

    private Tokenizer.Token parseLine() throws IOException, ParserException {
        while (true) {
            Tokenizer.Token token = tok.next();
            switch (token) {
                case IDENT:
                case NUMBER:
                    values.add(new Value(tok.getIdent().toUpperCase()));
                    break;
                case EOF:
                case EOL:
                    addLine();
                    return token;
                default:
                    throw new ParserException("unexpected token", token, tok.getLine());
            }
        }
    }

    private void addLine() throws ParserException {
        if (values.size() > 0) {

            if (values.size() != names.size())
                throw new ParserException("unexpected number of values", tok.getLine());

            lines.add(values.toArray(new Value[names.size()]));
            values.clear();
        }
    }

    private void parseHeader() throws IOException, ParserException {
        while (tok.peek() == Tokenizer.Token.EOL) {
            tok.consume();
        }
        while (true) {
            Tokenizer.Token token = tok.next();
            switch (token) {
                case IDENT:
                    names.add(tok.getIdent());
                    break;
                case EOL:
                    return;
                default:
                    throw new ParserException("unexpected token", token, tok.getLine());
            }
        }
    }

    private void expect(Tokenizer.Token token) throws IOException, ParserException {
        Tokenizer.Token t = tok.next();
        if (t != token)
            throw new ParserException("unexpected token", token, tok.getLine());

    }

    /**
     * @return the used variables
     */
    public ArrayList<String> getNames() {
        return names;
    }

    /**
     * @return the test vectors
     */
    public ArrayList<Value[]> getLines() {
        return lines;
    }

    private interface Entry {
        Value getValue(long n);
    }

    private boolean isToken(Tokenizer.Token t) throws IOException {
        if (tok.peek() == t) {
            tok.next();
            return true;
        }
        return false;
    }

    /**
     * Parses a string to a simple expression
     *
     * @return the expression
     * @throws IOException     IOException
     * @throws ParserException IOException
     */
    Expression parseExpression() throws IOException, ParserException {
        Expression ac = parseGreater();
        while (isToken(Tokenizer.Token.SMALER)) {
            Expression a = ac;
            Expression b = parseGreater();
            ac = (n) -> a.value(n) < b.value(n) ? 1 : 0;
        }
        return ac;
    }

    private Expression parseGreater() throws IOException, ParserException {
        Expression ac = parseEquals();
        while (isToken(Tokenizer.Token.GREATER)) {
            Expression a = ac;
            Expression b = parseEquals();
            ac = (n) -> a.value(n) > b.value(n) ? 1 : 0;
        }
        return ac;
    }

    private Expression parseEquals() throws IOException, ParserException {
        Expression ac = parseOR();
        while (isToken(Tokenizer.Token.EQUAL)) {
            Expression a = ac;
            Expression b = parseOR();
            ac = (n) -> a.value(n) == b.value(n) ? 1 : 0;
        }
        return ac;
    }

    private Expression parseOR() throws IOException, ParserException {
        Expression ac = parseAND();
        while (isToken(Tokenizer.Token.OR)) {
            Expression a = ac;
            Expression b = parseAND();
            ac = (n) -> a.value(n) | b.value(n);
        }
        return ac;
    }

    private Expression parseAND() throws IOException, ParserException {
        Expression ac = parseShiftRight();
        while (isToken(Tokenizer.Token.AND)) {
            Expression a = ac;
            Expression b = parseShiftRight();
            ac = (n) -> a.value(n) & b.value(n);
        }
        return ac;
    }

    private Expression parseShiftRight() throws IOException, ParserException {
        Expression ac = parseShiftLeft();
        while (isToken(Tokenizer.Token.SHIFTRIGHT)) {
            Expression a = ac;
            Expression b = parseShiftLeft();
            ac = (n) -> a.value(n) >> b.value(n);
        }
        return ac;
    }

    private Expression parseShiftLeft() throws IOException, ParserException {
        Expression ac = parseAdd();
        while (isToken(Tokenizer.Token.SHIFTLEFT)) {
            Expression a = ac;
            Expression b = parseAdd();
            ac = (n) -> a.value(n) << b.value(n);
        }
        return ac;
    }

    private Expression parseAdd() throws IOException, ParserException {
        Expression ac = parseSub();
        while (isToken(Tokenizer.Token.ADD)) {
            Expression a = ac;
            Expression b = parseSub();
            ac = (n) -> a.value(n) + b.value(n);
        }
        return ac;
    }

    private Expression parseSub() throws IOException, ParserException {
        Expression ac = parseMul();
        while (isToken(Tokenizer.Token.SUB)) {
            Expression a = ac;
            Expression b = parseMul();
            ac = (n) -> a.value(n) - b.value(n);
        }
        return ac;
    }

    private Expression parseMul() throws IOException, ParserException {
        Expression ac = parseDiv();
        while (isToken(Tokenizer.Token.MUL)) {
            Expression a = ac;
            Expression b = parseDiv();
            ac = (n) -> a.value(n) * b.value(n);
        }
        return ac;
    }

    private Expression parseDiv() throws IOException, ParserException {
        Expression ac = parseIdent();
        while (isToken(Tokenizer.Token.DIV)) {
            Expression a = ac;
            Expression b = parseIdent();
            ac = (n) -> a.value(n) / b.value(n);
        }
        return ac;
    }

    private Expression parseIdent() throws IOException, ParserException {
        Tokenizer.Token t = tok.next();
        switch (t) {
            case IDENT:
                if (tok.getIdent().equals("n")) {
                    return (n) -> n;
                } else
                    throw new ParserException("only var n allowed!, not " + tok.getIdent(), t, tok.getLine());
            case NUMBER:
                long num = Long.parseLong(tok.getIdent());
                return (n) -> num;
            case SUB:
                Expression negExp = parseIdent();
                return (n) -> -negExp.value(n);
            case NOT:
                Expression notExp = parseIdent();
                return (n) -> ~notExp.value(n);
            case OPEN:
                Expression exp = parseExpression();
                expect(Tokenizer.Token.CLOSE);
                return exp;
            default:
                throw new ParserException("invalid token", t, tok.getLine());
        }
    }
}

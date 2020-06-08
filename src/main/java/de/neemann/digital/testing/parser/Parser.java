/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser;

import de.neemann.digital.core.Bits;
import de.neemann.digital.lang.Lang;
import de.neemann.digital.data.Value;
import de.neemann.digital.testing.parser.functions.Function;
import de.neemann.digital.testing.parser.functions.IfThenElse;
import de.neemann.digital.testing.parser.functions.Random;
import de.neemann.digital.testing.parser.functions.SignExtend;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Parser to parse test data.
 * The constructor takes a string, and after a call to parse()
 * the names of the signals and the test vectors are generated.
 * Via the {@link #getValue()} or the {@link #getValue(Context)} functions you can utilize
 * the parser to evaluate integer functions.
 * If you want to evaluate an expression several times you should use the {@link #parseExpression()} function.
 * <p>
 * Created by Helmut.Neemann on 02.12.2016.
 */
public class Parser {

    private final ArrayList<String> names;
    private final Tokenizer tok;
    private final HashMap<String, Function> functions = new HashMap<>();
    private LineEmitter emitter;

    /**
     * Creates a new instance
     *
     * @param data the test data string
     */
    public Parser(String data) {
        functions.put("signExt", new SignExtend());
        functions.put("random", new Random());
        functions.put("ite", new IfThenElse());
        names = new ArrayList<>();
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
        parseHeader();
        emitter = parseRows(null);
        expect(Tokenizer.Token.EOF);
        return this;
    }

    private void parseHeader() throws IOException, ParserException {
        tok.skipEmptyLines();
        while (true) {
            Tokenizer.Token token = tok.simpleIdent();
            switch (token) {
                case IDENT:
                    final String name = tok.getIdent();
                    if (names.contains(name))
                        throw new ParserException(Lang.get("err_nameUsedTwice_N", name));
                    names.add(name);
                    break;
                case EOL:
                    return;
                default:
                    throw newUnexpectedToken(token);
            }
        }
    }

    private ParserException newUnexpectedToken(Tokenizer.Token token) {
        String name = token == Tokenizer.Token.IDENT ? tok.getIdent() : token.name();
        return new ParserException(Lang.get("err_unexpectedToken_N0_inLine_N1", name, tok.getLine()));
    }

    private LineEmitter parseRows(Tokenizer.Token endToken) throws IOException, ParserException {
        LineEmitterList list = new LineEmitterList();
        while (true) {
            Tokenizer.Token t = tok.peek();
            switch (t) {
                case EOL:
                    break;
                case EOF:
                    if (endToken != null)
                        throw newUnexpectedToken(t);
                    return list.minimize();
                case BITS:
                case OPEN:
                case IDENT:
                case NUMBER:
                    list.add(parseSingleRow());
                    break;
                case END:
                    tok.consume();
                    expect(endToken);
                    return list.minimize();
                case LET:
                    tok.consume();
                    expect(Tokenizer.Token.IDENT);
                    final String varName = tok.getIdent();
                    expect(Tokenizer.Token.EQUAL);
                    final Expression intValue = parseExpression();
                    expect(Tokenizer.Token.SEMICOLON);
                    list.add((listener, context) -> context.setVar(varName, intValue.value(context)));
                    break;
                case REPEAT:
                    tok.consume();
                    expect(Tokenizer.Token.OPEN);
                    long count = parseInt();
                    expect(Tokenizer.Token.CLOSE);
                    list.add(new LineEmitterRepeat("n", count, parseSingleRow()));
                    break;
                case LOOP:
                    tok.consume();
                    expect(Tokenizer.Token.OPEN);
                    expect(Tokenizer.Token.IDENT);
                    String var = tok.getIdent();
                    expect(Tokenizer.Token.COMMA);
                    count = parseInt();
                    expect(Tokenizer.Token.CLOSE);
                    list.add(new LineEmitterRepeat(var, count, parseRows(Tokenizer.Token.LOOP)));
                    break;
                case WHILE:
                    tok.consume();
                    expect(Tokenizer.Token.OPEN);
                    final Expression condition = parseExpression();
                    expect(Tokenizer.Token.CLOSE);
                    list.add(new LineEmitterWhile(condition, parseRows(Tokenizer.Token.WHILE)));
                    break;
                default:
                    throw newUnexpectedToken(t);
            }
        }
    }

    private LineEmitter parseSingleRow() throws IOException, ParserException {
        LineEmitterSimple line = null;
        while (true) {
            Tokenizer.Token token = tok.next();
            if (line == null)
                line = new LineEmitterSimple(names.size(), tok.getLine());
            switch (token) {
                case NUMBER:
                    Value num = new Value(convToLong(tok.getIdent()));
                    line.add((vals, context) -> vals.add(num));
                    break;
                case BITS:
                    expect(Tokenizer.Token.OPEN);
                    int bitCount = (int) parseInt();
                    expect(Tokenizer.Token.COMMA);
                    Expression exp = parseExpression();
                    line.add(new ValueAppenderBits(bitCount, exp));
                    expect(Tokenizer.Token.CLOSE);
                    break;
                case IDENT:
                    try {
                        final Value value = new Value(tok.getIdent().toUpperCase());
                        line.add((vals, context) -> vals.add(value));
                    } catch (Bits.NumberFormatException e) {
                        throw new ParserException(Lang.get("err_notANumber_N0_inLine_N1", tok.getIdent(), tok.getLine()));
                    }
                    break;
                case OPEN:
                    exp = parseExpression();
                    line.add((vals, context) -> vals.add(new Value(exp.value(context))));
                    expect(Tokenizer.Token.CLOSE);
                    break;
                case EOF:
                case EOL:
                    return line;
                default:
                    throw newUnexpectedToken(token);
            }
        }
    }

    private long convToLong(String num) throws ParserException {
        try {
            return Bits.decode(num);
        } catch (Bits.NumberFormatException e) {
            throw new ParserException(Lang.get("err_notANumber_N0_inLine_N1", tok.getIdent(), tok.getLine()));
        }
    }

    private long parseInt() throws ParserException, IOException {
        return parseExpression().value(new Context());
    }

    private void expect(Tokenizer.Token token) throws IOException, ParserException {
        Tokenizer.Token t = tok.next();
        if (t != token)
            throw newUnexpectedToken(t);

    }

    /**
     * @return the used variables
     */
    public ArrayList<String> getNames() {
        return names;
    }

    /**
     * @return the line emitter
     */
    public LineEmitter getLines() {
        return emitter;
    }

    private boolean isToken(Tokenizer.Token t) throws IOException {
        if (tok.peek() == t) {
            tok.next();
            return true;
        }
        return false;
    }

    /**
     * Returns the value of the expression
     *
     * @return the value
     * @throws IOException     IOException
     * @throws ParserException ParserException
     */
    public long getValue() throws IOException, ParserException {
        return getValue(new Context());
    }

    /**
     * Returns the value of the expression
     *
     * @param context the context of the evaluation
     * @return the value
     * @throws IOException     IOException
     * @throws ParserException ParserException
     */
    public long getValue(Context context) throws IOException, ParserException {
        final long value = parseExpression().value(context);
        expect(Tokenizer.Token.EOF);
        return value;
    }

    /**
     * Parses a string to a simple expression
     *
     * @return the expression
     * @throws IOException     IOException
     * @throws ParserException IOException
     */
    private Expression parseExpression() throws IOException, ParserException {
        Expression ac = parseSmalerEqual();
        while (isToken(Tokenizer.Token.SMALER)) {
            Expression a = ac;
            Expression b = parseGreater();
            ac = (c) -> a.value(c) < b.value(c) ? 1 : 0;
        }
        return ac;
    }

    private Expression parseSmalerEqual() throws IOException, ParserException {
        Expression ac = parseGreater();
        while (isToken(Tokenizer.Token.SMALEREQUAL)) {
            Expression a = ac;
            Expression b = parseGreater();
            ac = (c) -> a.value(c) <= b.value(c) ? 1 : 0;
        }
        return ac;
    }

    private Expression parseGreater() throws IOException, ParserException {
        Expression ac = parseGreaterEqual();
        while (isToken(Tokenizer.Token.GREATER)) {
            Expression a = ac;
            Expression b = parseEquals();
            ac = (c) -> a.value(c) > b.value(c) ? 1 : 0;
        }
        return ac;
    }

    private Expression parseGreaterEqual() throws IOException, ParserException {
        Expression ac = parseEquals();
        while (isToken(Tokenizer.Token.GREATEREQUAL)) {
            Expression a = ac;
            Expression b = parseEquals();
            ac = (c) -> a.value(c) >= b.value(c) ? 1 : 0;
        }
        return ac;
    }

    private Expression parseEquals() throws IOException, ParserException {
        Expression ac = parseNotEquals();
        while (isToken(Tokenizer.Token.EQUAL)) {
            Expression a = ac;
            Expression b = parseNotEquals();
            ac = (c) -> a.value(c) == b.value(c) ? 1 : 0;
        }
        return ac;
    }

    private Expression parseNotEquals() throws IOException, ParserException {
        Expression ac = parseOR();
        while (isToken(Tokenizer.Token.LOG_NOT)) {
            expect(Tokenizer.Token.EQUAL);
            Expression a = ac;
            Expression b = parseOR();
            ac = (c) -> a.value(c) == b.value(c) ? 0 : 1;
        }
        return ac;
    }

    private Expression parseOR() throws IOException, ParserException {
        Expression ac = parseXOR();
        while (isToken(Tokenizer.Token.OR)) {
            Expression a = ac;
            Expression b = parseXOR();
            ac = (c) -> a.value(c) | b.value(c);
        }
        return ac;
    }

    private Expression parseXOR() throws IOException, ParserException {
        Expression ac = parseAND();
        while (isToken(Tokenizer.Token.XOR)) {
            Expression a = ac;
            Expression b = parseAND();
            ac = (c) -> a.value(c) ^ b.value(c);
        }
        return ac;
    }

    private Expression parseAND() throws IOException, ParserException {
        Expression ac = parseShiftRight();
        while (isToken(Tokenizer.Token.AND)) {
            Expression a = ac;
            Expression b = parseShiftRight();
            ac = (c) -> a.value(c) & b.value(c);
        }
        return ac;
    }

    private Expression parseShiftRight() throws IOException, ParserException {
        Expression ac = parseShiftLeft();
        while (isToken(Tokenizer.Token.SHIFTRIGHT)) {
            Expression a = ac;
            Expression b = parseShiftLeft();
            ac = (c) -> a.value(c) >> b.value(c);
        }
        return ac;
    }

    private Expression parseShiftLeft() throws IOException, ParserException {
        Expression ac = parseAdd();
        while (isToken(Tokenizer.Token.SHIFTLEFT)) {
            Expression a = ac;
            Expression b = parseAdd();
            ac = (c) -> a.value(c) << b.value(c);
        }
        return ac;
    }

    private Expression parseAdd() throws IOException, ParserException {
        Expression ac = parseSub();
        while (isToken(Tokenizer.Token.ADD)) {
            Expression a = ac;
            Expression b = parseSub();
            ac = (c) -> a.value(c) + b.value(c);
        }
        return ac;
    }

    private Expression parseSub() throws IOException, ParserException {
        Expression ac = parseMul();
        while (isToken(Tokenizer.Token.SUB)) {
            Expression a = ac;
            Expression b = parseMul();
            ac = (c) -> a.value(c) - b.value(c);
        }
        return ac;
    }

    private Expression parseMul() throws IOException, ParserException {
        Expression ac = parseDiv();
        while (isToken(Tokenizer.Token.MUL)) {
            Expression a = ac;
            Expression b = parseDiv();
            ac = (c) -> a.value(c) * b.value(c);
        }
        return ac;
    }

    private Expression parseDiv() throws IOException, ParserException {
        Expression ac = parseMod();
        while (isToken(Tokenizer.Token.DIV)) {
            Expression a = ac;
            Expression b = parseMod();
            ac = (c) -> a.value(c) / b.value(c);
        }
        return ac;
    }

    private Expression parseMod() throws IOException, ParserException {
        Expression ac = parseIdent();
        while (isToken(Tokenizer.Token.MOD)) {
            Expression a = ac;
            Expression b = parseIdent();
            ac = (c) -> a.value(c) % b.value(c);
        }
        return ac;
    }

    private Expression parseIdent() throws IOException, ParserException {
        Tokenizer.Token t = tok.next();
        switch (t) {
            case IDENT:
                String name = tok.getIdent();
                if (tok.peek() == Tokenizer.Token.OPEN) {
                    ArrayList<Expression> args = new ArrayList<>();
                    do {
                        tok.consume();
                        args.add(parseExpression());
                    } while (tok.peek() == Tokenizer.Token.COMMA);
                    expect(Tokenizer.Token.CLOSE);
                    return findFunction(name, args);
                } else
                    return (c) -> c.getVar(name);
            case NUMBER:
                long num = convToLong(tok.getIdent());
                return (c) -> num;
            case SUB:
                Expression negExp = parseIdent();
                return (c) -> -negExp.value(c);
            case BIN_NOT:
                Expression notExp = parseIdent();
                return (c) -> ~notExp.value(c);
            case LOG_NOT:
                Expression boolNotExp = parseIdent();
                return (c) -> boolNotExp.value(c) == 0 ? 1 : 0;
            case OPEN:
                Expression exp = parseExpression();
                expect(Tokenizer.Token.CLOSE);
                return exp;
            default:
                throw newUnexpectedToken(t);
        }
    }

    private Expression findFunction(String name, ArrayList<Expression> args) throws ParserException {
        Function f = functions.get(name);
        if (f == null)
            throw new ParserException(Lang.get("err_function_N0_notFoundInLine_N1", name, tok.getLine()));
        if (f.getArgCount() != args.size())
            throw new ParserException(Lang.get("err_wrongNumOfArgsIn_N0_InLine_N1_found_N2_expected_N3", name, tok.getLine(), args.size(), f.getArgCount()));

        return (c) -> f.calcValue(c, args);
    }
}

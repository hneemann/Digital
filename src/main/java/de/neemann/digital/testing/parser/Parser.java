/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser;

import de.neemann.digital.core.*;
import de.neemann.digital.core.memory.DataField;
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
    private final ModelInitializer modelInit;
    private final ArrayList<VirtualSignal> virtualSignals;
    private final Tokenizer tok;
    private final HashMap<String, Function> functions = new HashMap<>();
    private final Random random;
    private LineEmitter emitter;

    /**
     * Creates a new instance
     *
     * @param data the test data string
     */
    public Parser(String data) {
        functions.put("signExt", new SignExtend());
        random = new Random();
        functions.put("random", random);
        functions.put("ite", new IfThenElse());
        names = new ArrayList<>();
        virtualSignals = new ArrayList<>();
        modelInit = new ModelInitializer();
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
                case INIT:
                    tok.consume();
                    expect(Tokenizer.Token.IDENT);
                    final String sName = tok.getIdent();
                    expect(Tokenizer.Token.EQUAL);
                    int sign = 1;
                    if (tok.peek() == Tokenizer.Token.SUB) {
                        tok.consume();
                        sign = -1;
                    }
                    expect(Tokenizer.Token.NUMBER);
                    long n = convToLong(tok.getIdent());
                    expect(Tokenizer.Token.SEMICOLON);
                    modelInit.initSignal(sName, sign * n);
                    break;
                case MEMORY:
                    tok.consume();
                    expect(Tokenizer.Token.IDENT);
                    final String ramName = tok.getIdent();
                    expect(Tokenizer.Token.OPEN);
                    expect(Tokenizer.Token.NUMBER);
                    long addr = convToLong(tok.getIdent());
                    expect(Tokenizer.Token.CLOSE);
                    expect(Tokenizer.Token.EQUAL);
                    expect(Tokenizer.Token.NUMBER);
                    long val = convToLong(tok.getIdent());
                    expect(Tokenizer.Token.SEMICOLON);
                    modelInit.initMemory(ramName, (int) addr, val);
                    break;
                case PROGRAM:
                    tok.consume();
                    modelInit.initProgramMemory(parseData());
                    break;
                case DECLARE:
                    tok.consume();
                    expect(Tokenizer.Token.IDENT);
                    final String sigName = tok.getIdent();
                    expect(Tokenizer.Token.EQUAL);
                    final Expression sigExpression = parseExpression();
                    expect(Tokenizer.Token.SEMICOLON);
                    addVirtualSignal(new VirtualSignal(sigName, sigExpression));
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
                case RESETRANDOM:
                    tok.consume();
                    expect(Tokenizer.Token.SEMICOLON);
                    list.add((listener, context) -> context.resetRandom());
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

    private DataField parseData() throws IOException, ParserException {
        expect(Tokenizer.Token.OPEN);
        DataField df = new DataField();
        int addr = 0;
        while (true) {
            expect(Tokenizer.Token.NUMBER);
            df.setData(addr, convToLong(tok.getIdent()));
            addr++;
            Tokenizer.Token t = tok.next();
            switch (t) {
                case COMMA:
                    break;
                case CLOSE:
                    return df;
                default:
                    throw newUnexpectedToken(t);
            }
        }
    }

    private void addVirtualSignal(VirtualSignal vs) throws ParserException {
        for (VirtualSignal v : virtualSignals)
            if (v.getName().equals(vs.getName()))
                throw new ParserException(Lang.get("err_virtualSignal_N_DeclaredTwiceInLine_N", vs.getName(), tok.getLine()));
        virtualSignals.add(vs);
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
            return Bits.decode(num, true);
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
     * @return the list  of declared virtual signals
     */
    public ArrayList<VirtualSignal> getVirtualSignals() {
        return virtualSignals;
    }

    /**
     * @return the model init actions
     */
    public ModelInitializer getModelInitializer() {
        return modelInit;
    }

    /**
     * @return the random function
     */
    public Random getRandom() {
        return random;
    }

    /**
     * @return the line emitter
     */
    public LineEmitter getLines() {
        return emitter;
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

    private Expression parseExpression() throws IOException, ParserException {
        return parseExpression(OperatorPrecedence.lowest());
    }

    private Expression parseExpression(OperatorPrecedence op) throws IOException, ParserException {
        Next next = getNextParser(op.getNextHigherPrecedence());
        Expression ac = next.next();
        while (tok.peek().getPrecedence() == op) {
            Tokenizer.Binary function = tok.next().getFunction();
            Expression a = ac;
            Expression b = next.next();
            ac = (c) -> function.op(a.value(c), b.value(c));
        }
        return ac;
    }

    private Next getNextParser(OperatorPrecedence pr) {
        if (pr == null)
            return this::parseIdent;
        else
            return () -> parseExpression(pr);
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

    private interface Next {
        Expression next() throws IOException, ParserException;
    }
}

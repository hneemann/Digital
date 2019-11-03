/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

import de.neemann.digital.core.Bits;
import de.neemann.digital.hdl.hgs.function.FirstClassFunction;
import de.neemann.digital.hdl.hgs.function.FirstClassFunctionCall;
import de.neemann.digital.hdl.hgs.refs.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import static de.neemann.digital.hdl.hgs.Tokenizer.Token.*;

/**
 * Parser to evaluate text templates
 */
public class Parser {

    /**
     * Creates a statement from the jar file using ClassLoader.getSystemResourceAsStream(path).
     *
     * @param path the path of the file to load
     * @param cl   the classloader used to load the template. If set to null, the SystemClassLoader is used
     * @return the statement
     * @throws IOException     IOException
     * @throws ParserException ParserException
     */
    public static Statement createFromJar(String path, ClassLoader cl) throws IOException, ParserException {
        if (cl == null)
            cl = ClassLoader.getSystemClassLoader();
        InputStream in = cl.getResourceAsStream(path);
        if (in == null)
            throw new FileNotFoundException("file not found: " + path);
        try (Reader r = new InputStreamReader(in, StandardCharsets.UTF_8)) {
            Parser p = new Parser(r, path);
            return p.parse();
        }
    }

    /**
     * Creates a statement from the jar file using ClassLoader.getSystemResourceAsStream(path).
     * Throws only a RuntimeExcaption so use with care!
     *
     * @param path the path of the file to load
     * @return the statement
     */
    public static Statement createFromJarStatic(String path) {
        try {
            return createFromJar(path, null);
        } catch (IOException | ParserException e) {
            throw new RuntimeException("could not parse: " + path, e);
        }
    }


    private ArrayList<Reference> refRead;
    private final Tokenizer tok;

    /**
     * Create a new instance
     *
     * @param code the code to parse
     */
    public Parser(String code) {
        this(new StringReader(code), "");
    }

    /**
     * Creates a new instance
     *
     * @param reader  the reader to parse
     * @param srcFile the source file name if any
     */
    public Parser(Reader reader, String srcFile) {
        tok = new Tokenizer(reader, srcFile);
    }

    /**
     * If called all read references are collected.
     */
    public void enableRefReadCollection() {
        refRead = new ArrayList<>();
    }

    /**
     * @return returns the references read
     */
    public ArrayList<Reference> getRefsRead() {
        return refRead;
    }

    private Statement lino(Statement statement) {
        if (statement instanceof StatementWithLine)
            return statement;
        else
            return new StatementWithLine(statement, tok.getLine());
    }

    private Expression linoE(Expression expression) {
        if (expression instanceof ExpressionWithLine)
            return expression;
        else
            return new ExpressionWithLine(expression, tok.getLine());
    }

    /**
     * Parses the given template source
     *
     * @return the Statement to execute
     * @throws IOException     IOException
     * @throws ParserException ParserException
     */
    public Statement parse() throws IOException, ParserException {
        return parse(true);
    }

    /**
     * Parses the given template source
     *
     * @param startsWithText true if code starts with text.
     * @return the Statement to execute
     * @throws IOException     IOException
     * @throws ParserException ParserException
     */
    public Statement parse(boolean startsWithText) throws IOException, ParserException {
        Statements s = new Statements();
        if (startsWithText) {
            String text = tok.readText();
            if (nextIs(SUB))
                text = Value.trimRight(text);

            if (text.length() > 0) {
                String t = text;
                s.add(c -> c.print(t));
            }
        }
        while (!nextIs(EOF))
            s.add(parseStatement());

        return s.optimize();
    }

    private Statement parseStatement() throws IOException, ParserException {
        return parseStatement(true);
    }

    /**
     * If 'isRealStatement' is false, the statement is parsed like an expression.
     * This mode is needed to implement the 'for' loop. In a C style for loop the pre and the
     * post code are expressions which modify state. This is not supported by HGS. In the HGS
     * for loop the pre and the post code are statements where the semicolon at the end is omitted.
     */
    private Statement parseStatement(boolean isRealStatement) throws IOException, ParserException {
        final Tokenizer.Token token = tok.next();
        boolean export = false;
        switch (token) {
            case EXPORT:
                export = true;
                Tokenizer.Token ti = tok.next();
                if (!ti.equals(IDENT))
                    throw newParserException("export must be followed by an identifier!");
            case IDENT:
                final Reference ref = parseReference(tok.getIdent());
                Tokenizer.Token refToken = tok.next();
                switch (refToken) {
                    case COLON:
                        expect(EQUAL);
                        final Expression initVal = parseExpression();
                        if (isRealStatement) expect(SEMICOLON);
                        if (export)
                            return lino(c -> ref.exportVar(c, initVal.value(c)));
                        else
                            return lino(c -> ref.declareVar(c, initVal.value(c)));
                    case EQUAL:
                        if (export)
                            throw newParserException("export is only allowed at variable declaration!");
                        final Expression val = parseExpression();
                        if (isRealStatement) expect(SEMICOLON);
                        return lino(c -> {
                            final Object value = val.value(c);
                            if (value == null)
                                throw new HGSEvalException("There is no value to assign!");
                            ref.set(c, value);
                        });
                    case ADD:
                        expect(ADD);
                        if (export)
                            throw newParserException("export is only allowed at variable declaration!");
                        if (isRealStatement) expect(SEMICOLON);
                        return lino(c -> ref.set(c, Value.toLong(ref.get(c)) + 1));
                    case SUB:
                        expect(SUB);
                        if (export)
                            throw newParserException("export is only allowed at variable declaration!");
                        if (isRealStatement) expect(SEMICOLON);
                        return lino(c -> ref.set(c, Value.toLong(ref.get(c)) - 1));
                    case SEMICOLON:
                        if (export)
                            throw newParserException("export is only allowed at variable declaration!");
                        return lino(ref::get);
                    default:
                        throw newUnexpectedToken(refToken);
                }
            case CODEEND:
                String str = tok.readText();
                if (nextIs(SUB))
                    str = Value.trimRight(str);
                final String strc = str;
                return c -> c.print(strc);
            case SUB:
                expect(CODEEND);
                final String strt = Value.trimLeft(tok.readText());
                return c -> c.print(strt);
            case EQUAL:
                final Expression exp = parseExpression();
                if (tok.peek() != CODEEND) expect(SEMICOLON);
                return lino(c -> c.print(exp.value(c).toString()));
            case IF:
                expect(OPEN);
                final Expression ifCond = toBool(parseExpression());
                expect(CLOSE);
                final Statement ifStatement = parseStatement();
                if (nextIs(ELSE)) {
                    final Statement elseStatement = parseStatement();
                    return c -> {
                        Context iC = new Context(c, false);
                        if ((boolean) ifCond.value(iC))
                            ifStatement.execute(iC);
                        else
                            elseStatement.execute(iC);
                    };
                } else
                    return c -> {
                        Context iC = new Context(c, false);
                        if ((boolean) ifCond.value(iC))
                            ifStatement.execute(iC);
                    };
            case FOR:
                expect(OPEN);
                Statement init = parseStatement(false); // parse like an expression
                expect(SEMICOLON);
                final Expression forCond = toBool(parseExpression());
                expect(SEMICOLON);
                Statement inc = parseStatement(false); // parse like an expression
                expect(CLOSE);
                Statement inner = parseStatement();
                return c -> {
                    Context iC = new Context(c, false);
                    init.execute(iC);
                    while ((boolean) forCond.value(iC)) {
                        inner.execute(new Context(iC, false));
                        inc.execute(iC);
                    }
                };
            case WHILE:
                expect(OPEN);
                final Expression whileCond = toBool(parseExpression());
                expect(CLOSE);
                inner = parseStatement();
                return c -> {
                    Context iC = new Context(c, false);
                    while ((boolean) whileCond.value(iC)) inner.execute(iC);
                };
            case REPEAT:
                final Statement repeatInner = parseStatement();
                expect(UNTIL);
                final Expression repeatCond = toBool(parseExpression());
                if (isRealStatement) expect(SEMICOLON);
                return c -> {
                    Context iC = new Context(c, false);
                    do {
                        repeatInner.execute(iC);
                    } while (!(boolean) repeatCond.value(iC));
                };
            case OPENBRACE:
                Statements s = new Statements();
                while (!nextIs(CLOSEDBRACE))
                    s.add(parseStatement());
                return s.optimize();
            case RETURN:
                Expression retExp = parseExpression();
                expect(SEMICOLON);
                return lino(c -> FirstClassFunctionCall.returnFromFunc(retExp.value(c)));
            case FUNC:
                expect(IDENT);
                String funcName = tok.getIdent();
                FirstClassFunction funcDecl = parseFunction();
                return lino(c -> c.declareVar(funcName, new FirstClassFunctionCall(funcDecl, c)));
            default:
                throw newUnexpectedToken(token);
        }
    }

    private Expression toBool(Expression expression) {
        return linoE(c -> Value.toBool(expression.value(c)));
    }

    private ArrayList<Expression> parseArgList() throws IOException, ParserException {
        ArrayList<Expression> args = new ArrayList<>();
        if (!nextIs(CLOSE)) {
            args.add(parseExpression());
            while (nextIs(COMMA))
                args.add(parseExpression());
            expect(CLOSE);
        }
        return args;
    }

    private Reference parseReference(String var) throws IOException, ParserException {
        Reference r = new ReferenceToVar(var);
        while (true) {
            if (nextIs(OPENSQUARE)) {
                r = new ReferenceToArray(r, parseExpression());
                expect(CLOSEDSQUARE);
            } else if (nextIs(OPEN)) {
                r = new ReferenceToFunc(r, parseArgList());
            } else if (nextIs(DOT)) {
                expect(IDENT);
                r = new ReferenceToStruct(r, tok.getIdent());
            } else
                return r;
        }
    }

    private boolean nextIs(Tokenizer.Token t) throws IOException {
        if (tok.peek() == t) {
            tok.next();
            return true;
        }
        return false;
    }

    private Tokenizer.Token nextIsIn(Tokenizer.Token... ts) throws IOException {
        Tokenizer.Token next = tok.peek();
        for (Tokenizer.Token t : ts)
            if (next == t)
                return tok.next();
        return null;
    }

    private void expect(Tokenizer.Token token) throws IOException, ParserException {
        Tokenizer.Token t = tok.next();
        if (t != token)
            throw newParserException("expected: " + token + ", but found: " + t);
    }

    private long convToLong(String num) throws ParserException {
        try {
            return Bits.decode(num);
        } catch (Bits.NumberFormatException e) {
            throw newParserException("not a number: " + tok.getIdent());
        }
    }

    private double convToDouble(String num) throws ParserException {
        try {
            return Double.parseDouble(num);
        } catch (NumberFormatException e) {
            throw newParserException("not a number: " + tok.getIdent());
        }
    }

    private ParserException newUnexpectedToken(Tokenizer.Token token) {
        String name = token == IDENT ? tok.getIdent() : token.name();
        return newParserException("unexpected Token: " + name);
    }

    private ParserException newParserException(String s) {
        return new ParserException(s + " (" + tok.getSrcFile() + ":" + tok.getLine() + ")");
    }

    /**
     * Parses a string to a simple expression
     *
     * @return the expression
     * @throws IOException     IOException
     * @throws ParserException IOException
     */
    public Expression parseExp() throws IOException, ParserException {
        Expression ex = parseExpression();
        expect(EOF);
        return ex;
    }

    private Expression parseExpression() throws IOException, ParserException {
        Expression a = parseOR();
        Tokenizer.Token t = nextIsIn(LESS, LESSEQUAL, EQUAL, NOTEQUAL, GREATER, GREATEREQUAL);
        if (t != null) {
            Expression b = parseOR();
            switch (t) {
                case EQUAL:
                    return c -> Value.equals(a.value(c), b.value(c));
                case NOTEQUAL:
                    return c -> !Value.equals(a.value(c), b.value(c));
                case LESS:
                    return c -> Value.less(a.value(c), b.value(c));
                case LESSEQUAL:
                    return c -> Value.lessEqual(a.value(c), b.value(c));
                case GREATER:
                    return c -> Value.less(b.value(c), a.value(c));
                case GREATEREQUAL:
                    return c -> Value.lessEqual(b.value(c), a.value(c));
                default:
                    throw newUnexpectedToken(t);
            }
        } else
            return a;
    }

    private Expression parseOR() throws IOException, ParserException {
        Expression ac = parseXOR();
        while (nextIs(OR)) {
            Expression a = ac;
            Expression b = parseXOR();
            ac = c -> Value.or(a.value(c), b.value(c));
        }
        return ac;
    }

    private Expression parseXOR() throws IOException, ParserException {
        Expression ac = parseAND();
        while (nextIs(XOR)) {
            Expression a = ac;
            Expression b = parseAND();
            ac = c -> Value.xor(a.value(c), b.value(c));
        }
        return ac;
    }

    private Expression parseAND() throws IOException, ParserException {
        Expression ac = parseShiftRight();
        while (nextIs(AND)) {
            Expression a = ac;
            Expression b = parseShiftRight();
            ac = c -> Value.and(a.value(c), b.value(c));
        }
        return ac;
    }

    private Expression parseShiftRight() throws IOException, ParserException {
        Expression ac = parseShiftLeft();
        while (nextIs(SHIFTRIGHT)) {
            Expression a = ac;
            Expression b = parseShiftLeft();
            ac = c -> Value.toLong(a.value(c)) >> Value.toLong(b.value(c));
        }
        return ac;
    }

    private Expression parseShiftLeft() throws IOException, ParserException {
        Expression ac = parseAdd();
        while (nextIs(SHIFTLEFT)) {
            Expression a = ac;
            Expression b = parseAdd();
            ac = c -> Value.toLong(a.value(c)) << Value.toLong(b.value(c));
        }
        return ac;
    }

    private Expression parseAdd() throws IOException, ParserException {
        Expression ac = parseSub();
        while (nextIs(ADD)) {
            Expression a = ac;
            Expression b = parseSub();
            ac = c -> Value.add(a.value(c), b.value(c));
        }
        return ac;
    }

    private Expression parseSub() throws IOException, ParserException {
        Expression ac = parseMul();
        while (nextIs(SUB)) {
            Expression a = ac;
            Expression b = parseMul();
            ac = c -> Value.sub(a.value(c), b.value(c));
        }
        return ac;
    }

    private Expression parseMul() throws IOException, ParserException {
        Expression ac = parseDiv();
        while (nextIs(MUL)) {
            Expression a = ac;
            Expression b = parseDiv();
            ac = c -> Value.mul(a.value(c), b.value(c));
        }
        return ac;
    }

    private Expression parseDiv() throws IOException, ParserException {
        Expression ac = parseMod();
        while (nextIs(DIV)) {
            Expression a = ac;
            Expression b = parseMod();
            ac = c -> Value.div(a.value(c), b.value(c));
        }
        return ac;
    }

    private Expression parseMod() throws IOException, ParserException {
        Expression ac = parseIdent();
        while (nextIs(MOD)) {
            Expression a = ac;
            Expression b = parseIdent();
            ac = c -> Value.toLong(a.value(c)) % Value.toLong(b.value(c));
        }
        return ac;
    }

    private Expression parseIdent() throws IOException, ParserException {
        Tokenizer.Token t = tok.next();
        switch (t) {
            case IDENT:
                String name = tok.getIdent();
                Reference r = parseReference(name);
                if (refRead != null)
                    refRead.add(r);
                return r::get;
            case NUMBER:
                long num = convToLong(tok.getIdent());
                return c -> num;
            case DOUBLE:
                double d = convToDouble(tok.getIdent());
                return c -> d;
            case TRUE:
                return c -> true;
            case FALSE:
                return c -> false;
            case STRING:
                String s = tok.getIdent();
                return c -> s;
            case SUB:
                Expression negExp = parseIdent();
                return c -> Value.neg(negExp.value(c));
            case NOT:
                Expression notExp = parseIdent();
                return c -> Value.not(notExp.value(c));
            case OPEN:
                Expression exp = parseExpression();
                expect(CLOSE);
                return exp;
            case FUNC:
                FirstClassFunction func = parseFunction();
                return c -> new FirstClassFunctionCall(func, c);
            default:
                throw newUnexpectedToken(t);
        }
    }

    private FirstClassFunction parseFunction() throws IOException, ParserException {
        expect(OPEN);
        ArrayList<String> args = new ArrayList<>();
        if (!nextIs(CLOSE)) {
            expect(IDENT);
            args.add(tok.getIdent());
            while (!nextIs(CLOSE)) {
                expect(COMMA);
                expect(IDENT);
                args.add(tok.getIdent());
            }
        }
        Statement st = parseStatement();
        return new FirstClassFunction(args, st);
    }

    private static final class StatementWithLine implements Statement {
        private final Statement statement;
        private final int line;

        private StatementWithLine(Statement statement, int line) {
            this.statement = statement;
            this.line = line;
        }

        @Override
        public void execute(Context context) throws HGSEvalException {
            try {
                statement.execute(context);
            } catch (HGSEvalException e) {
                e.setLinNum(line);
                throw e;
            }
        }
    }

    private static final class ExpressionWithLine implements Expression {
        private final Expression expression;
        private final int line;

        private ExpressionWithLine(Expression expression, int line) {
            this.expression = expression;
            this.line = line;
        }

        @Override
        public Object value(Context c) throws HGSEvalException {
            try {
                return expression.value(c);
            } catch (HGSEvalException e) {
                e.setLinNum(line);
                throw e;
            }
        }
    }

}

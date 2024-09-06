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
import de.neemann.digital.testing.parser.OperatorPrecedence;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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
     * Throws only a RuntimeException so use with care!
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
                    while ((boolean) whileCond.value(c)) {
                        inner.execute(new Context(c, false));
                    }
                };
            case REPEAT:
                final Statement repeatInner = parseStatement();
                expect(UNTIL);
                final Expression repeatCond = toBool(parseExpression());
                if (isRealStatement) expect(SEMICOLON);
                return c -> {
                    do {
                        repeatInner.execute(new Context(c, false));
                    } while (!(boolean) repeatCond.value(c));
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
        return parseExpression(OperatorPrecedence.lowest());
    }

    private Expression parseExpression(OperatorPrecedence op) throws IOException, ParserException {
        Next next = getNextParser(op.getNextHigherPrecedence());
        Expression ac = next.next();
        while (tok.peek().getPrecedence() == op) {
            Tokenizer.Binary function = tok.next().getBinary();
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
            case OPENBRACE:
                return parseStructLiteral();
            case OPENSQUARE:
                return parseListLiteral();
            case FUNC:
                FirstClassFunction func = parseFunction();
                return c -> new FirstClassFunctionCall(func, c);
            default:
                throw newUnexpectedToken(t);
        }
    }

    private Expression parseListLiteral() throws IOException, ParserException {
        ArrayList<Expression> al = new ArrayList<>();
        while (true) {
            if (tok.peek() == CLOSEDSQUARE) {
                tok.consume();
                return c -> {
                    ArrayList<Object> l = new ArrayList<>();
                    for (Expression e : al)
                        l.add(e.value(c));
                    return l;
                };
            } else {
                al.add(parseExpression());
                if (tok.peek() == COMMA)
                    tok.consume();
            }
        }
    }

    private Expression parseStructLiteral() throws IOException, ParserException {
        StructLiteral sl = new StructLiteral();
        while (true) {
            Tokenizer.Token t = tok.next();
            switch (t) {
                case CLOSEDBRACE:
                    return sl;
                case IDENT:
                    String key = tok.getIdent();
                    expect(COLON);
                    Expression exp = parseExpression();
                    sl.add(key, exp);
                    if (nextIs(COMMA))
                        tok.consume();
                    else {
                        if (tok.peek() != CLOSEDBRACE)
                            throw newUnexpectedToken(t);
                    }
                    break;
                default:
                    throw newUnexpectedToken(t);
            }
        }
    }

    private interface Next {
        Expression next() throws IOException, ParserException;
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

    private static final class StructLiteral implements Expression {
        private final HashMap<String, Expression> map;

        private StructLiteral() {
            map = new HashMap<>();
        }

        private void add(String key, Expression exp) {
            map.put(key, exp);
        }

        @Override
        public Object value(Context c) throws HGSEvalException {
            HashMap<String, Object> vmap = new HashMap<>();
            for (Map.Entry<String, Expression> e : map.entrySet())
                vmap.put(e.getKey(), e.getValue().value(c));
            return vmap;
        }

    }
}

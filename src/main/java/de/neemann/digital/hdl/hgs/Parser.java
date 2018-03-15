/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

import de.neemann.digital.core.Bits;
import de.neemann.digital.hdl.hgs.function.FirstClassFunction;
import de.neemann.digital.hdl.hgs.function.FunctionFormat;
import de.neemann.digital.hdl.hgs.refs.*;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;

import static de.neemann.digital.hdl.hgs.Tokenizer.Token.*;

/**
 * Parser to evaluate text templates
 */
public class Parser {

    private final Tokenizer tok;
    private Context staticContext;

    /**
     * Create a new instance
     *
     * @param code the code to parse
     */
    public Parser(String code) {
        this(new StringReader(code));
    }

    /**
     * Creates a new instance
     *
     * @param reader the reader to parse
     */
    public Parser(Reader reader) {
        tok = new Tokenizer(reader);
        staticContext = new Context();
    }

    /**
     * Parses the given template source
     *
     * @return the Statement to execute
     * @throws IOException     IOException
     * @throws ParserException ParserException
     */
    public Statement parse() throws IOException, ParserException {
        Statements s = new Statements();
        String text = tok.readText();
        if (text.length() > 0)
            s.add(c -> c.print(text));
        while (!isToken(EOF)) {
            if (isToken(STATIC)) {
                Statement stat = parseStatement();
                try {
                    stat.execute(staticContext);
                } catch (HGSEvalException e) {
                    throw newParserException("error evaluating static code: " + e.getMessage());
                }
            } else
                s.add(parseStatement());
        }
        return s.optimize();
    }

    /**
     * @return the static context of this template
     */
    public Context getStaticContext() {
        return staticContext;
    }

    private Statement parseStatement() throws IOException, ParserException {
        return parseStatement(true);
    }

    private Statement parseStatement(boolean semicolon) throws IOException, ParserException {
        if (isToken(IDENT)) {
            Reference ref = parseReference(tok.getIdent());
            if (isToken(EQUAL)) {
                Expression val = parseExpression();
                if (semicolon) expect(SEMICOLON);
                return c -> ref.set(c, val.value(c));
            } else if (isToken(ADD)) {
                expect(ADD);
                if (semicolon) expect(SEMICOLON);
                return c -> ref.set(c, Value.toLong(ref.get(c)) + 1);
            } else if (isToken(SUB)) {
                expect(SUB);
                if (semicolon) expect(SEMICOLON);
                return c -> ref.set(c, Value.toLong(ref.get(c)) - 1);
            } else if (isToken(SEMICOLON)) {
                return ref::get;
            } else
                throw newUnexpectedToken(tok.next());
        } else if (isToken(CODEEND)) {
            String str = tok.readText();
            return c -> c.print(str);
        } else if (isToken(EQUAL)) {
            Expression exp = parseExpression();
            if (tok.peek() != CODEEND) expect(SEMICOLON);
            return c -> c.print(exp.value(c).toString());
        } else if (isToken(PRINT)) {
            expect(OPEN);
            ArrayList<Expression> args = parseArgList();
            if (semicolon) expect(SEMICOLON);
            return c -> {
                for (Expression e : args)
                    c.print(e.value(c).toString());
            };
        } else if (isToken(PRINTF)) {
            expect(OPEN);
            ArrayList<Expression> args = parseArgList();
            if (semicolon) expect(SEMICOLON);
            return c -> c.print(FunctionFormat.format(c, args));
        } else if (isToken(IF)) {
            expect(OPEN);
            Expression cond = parseExpression();
            expect(CLOSE);
            Statement ifPart = parseStatement();
            if (isToken(ELSE)) {
                Statement elsePart = parseStatement();
                return c -> {
                    if (Value.toBool(cond.value(c)))
                        ifPart.execute(c);
                    else
                        elsePart.execute(c);
                };
            } else
                return c -> {
                    if (Value.toBool(cond.value(c)))
                        ifPart.execute(c);
                };

        } else if (isToken(FOR)) {
            expect(OPEN);
            Statement init = parseStatement(false);
            expect(SEMICOLON);
            Expression cond = parseExpression();
            expect(SEMICOLON);
            Statement inc = parseStatement(false);
            expect(CLOSE);
            Statement inner = parseStatement();
            return c -> {
                init.execute(c);
                while (Value.toBool(cond.value(c))) {
                    inner.execute(c);
                    inc.execute(c);
                }
            };
        } else if (isToken(OPENBRACE)) {
            Statements s = new Statements();
            while (!isToken(CLOSEDBRACE))
                s.add(parseStatement());
            return s.optimize();
        } else if (isToken(PANIC)) {
            expect(OPEN);
            Expression message = parseExpression();
            expect(CLOSE);
            if (semicolon) expect(SEMICOLON);
            return c -> {
                throw new HGSEvalException(message.value(c).toString());
            };
        } else
            throw newUnexpectedToken(tok.next());
    }

    private ArrayList<Expression> parseArgList() throws IOException, ParserException {
        ArrayList<Expression> args = new ArrayList<>();
        if (!isToken(CLOSE)) {
            args.add(parseExpression());
            while (isToken(COMMA))
                args.add(parseExpression());
            expect(CLOSE);
        }
        return args;
    }

    private Reference parseReference(String var) throws IOException, ParserException {
        Reference r = new ReferenceToVar(var);
        while (true) {
            if (isToken(OPENSQUARE)) {
                Expression index = parseExpression();
                expect(CLOSEDSQUARE);
                r = new ReferenceToArray(r, index);
            } else if (isToken(OPEN)) {
                ArrayList<Expression> args = parseArgList();
                r = new ReferenceToFunc(r, args);
            } else if (isToken(DOT)) {
                expect(IDENT);
                r = new ReferenceToStruct(r, tok.getIdent());
            } else
                return r;
        }
    }

    private boolean isToken(Tokenizer.Token t) throws IOException {
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

    private ParserException newUnexpectedToken(Tokenizer.Token token) {
        String name = token == IDENT ? tok.getIdent() : token.name();
        return newParserException("unexpected Token: " + name);
    }

    private ParserException newParserException(String s) {
        return new ParserException(s + " (" + tok.getLine() + ")");
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
        Expression ac = parseLessEquals();
        while (isToken(Tokenizer.Token.LESS)) {
            Expression a = ac;
            Expression b = parseLessEquals();
            ac = c -> Value.toLong(a.value(c)) < Value.toLong(b.value(c));
        }
        return ac;
    }

    private Expression parseLessEquals() throws IOException, ParserException {
        Expression ac = parseGreater();
        while (isToken(Tokenizer.Token.LESSEQUAL)) {
            Expression a = ac;
            Expression b = parseGreater();
            ac = c -> Value.toLong(a.value(c)) <= Value.toLong(b.value(c));
        }
        return ac;
    }

    private Expression parseGreater() throws IOException, ParserException {
        Expression ac = parseGreaterEquals();
        while (isToken(Tokenizer.Token.GREATER)) {
            Expression a = ac;
            Expression b = parseGreaterEquals();
            ac = c -> Value.toLong(a.value(c)) > Value.toLong(b.value(c));
        }
        return ac;
    }

    private Expression parseGreaterEquals() throws IOException, ParserException {
        Expression ac = parseEquals();
        while (isToken(Tokenizer.Token.GREATEREQUAL)) {
            Expression a = ac;
            Expression b = parseEquals();
            ac = c -> Value.toLong(a.value(c)) >= Value.toLong(b.value(c));
        }
        return ac;
    }

    private Expression parseEquals() throws IOException, ParserException {
        Expression ac = parseNotEquals();
        while (isToken(Tokenizer.Token.EQUAL)) {
            Expression a = ac;
            Expression b = parseNotEquals();
            ac = c -> Value.equals(a.value(c), b.value(c));
        }
        return ac;
    }

    private Expression parseNotEquals() throws IOException, ParserException {
        Expression ac = parseOR();
        while (isToken(Tokenizer.Token.NOTEQUAL)) {
            Expression a = ac;
            Expression b = parseOR();
            ac = c -> !Value.equals(a.value(c), b.value(c));
        }
        return ac;
    }

    private Expression parseOR() throws IOException, ParserException {
        Expression ac = parseXOR();
        while (isToken(Tokenizer.Token.OR)) {
            Expression a = ac;
            Expression b = parseXOR();
            ac = c -> Value.or(a.value(c), b.value(c));
        }
        return ac;
    }

    private Expression parseXOR() throws IOException, ParserException {
        Expression ac = parseAND();
        while (isToken(Tokenizer.Token.XOR)) {
            Expression a = ac;
            Expression b = parseAND();
            ac = c -> Value.xor(a.value(c), b.value(c));
        }
        return ac;
    }

    private Expression parseAND() throws IOException, ParserException {
        Expression ac = parseShiftRight();
        while (isToken(Tokenizer.Token.AND)) {
            Expression a = ac;
            Expression b = parseShiftRight();
            ac = c -> Value.and(a.value(c), b.value(c));
        }
        return ac;
    }

    private Expression parseShiftRight() throws IOException, ParserException {
        Expression ac = parseShiftLeft();
        while (isToken(Tokenizer.Token.SHIFTRIGHT)) {
            Expression a = ac;
            Expression b = parseShiftLeft();
            ac = c -> Value.toLong(a.value(c)) >> Value.toLong(b.value(c));
        }
        return ac;
    }

    private Expression parseShiftLeft() throws IOException, ParserException {
        Expression ac = parseAdd();
        while (isToken(Tokenizer.Token.SHIFTLEFT)) {
            Expression a = ac;
            Expression b = parseAdd();
            ac = c -> Value.toLong(a.value(c)) << Value.toLong(b.value(c));
        }
        return ac;
    }

    private Expression parseAdd() throws IOException, ParserException {
        Expression ac = parseSub();
        while (isToken(Tokenizer.Token.ADD)) {
            Expression a = ac;
            Expression b = parseSub();
            ac = c -> Value.add(a.value(c), b.value(c));
        }
        return ac;
    }

    private Expression parseSub() throws IOException, ParserException {
        Expression ac = parseMul();
        while (isToken(Tokenizer.Token.SUB)) {
            Expression a = ac;
            Expression b = parseMul();
            ac = c -> Value.toLong(a.value(c)) - Value.toLong(b.value(c));
        }
        return ac;
    }

    private Expression parseMul() throws IOException, ParserException {
        Expression ac = parseDiv();
        while (isToken(Tokenizer.Token.MUL)) {
            Expression a = ac;
            Expression b = parseDiv();
            ac = c -> Value.toLong(a.value(c)) * Value.toLong(b.value(c));
        }
        return ac;
    }

    private Expression parseDiv() throws IOException, ParserException {
        Expression ac = parseMod();
        while (isToken(Tokenizer.Token.DIV)) {
            Expression a = ac;
            Expression b = parseMod();
            ac = c -> Value.toLong(a.value(c)) / Value.toLong(b.value(c));
        }
        return ac;
    }

    private Expression parseMod() throws IOException, ParserException {
        Expression ac = parseIdent();
        while (isToken(Tokenizer.Token.MOD)) {
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
                return r::get;
            case NUMBER:
                long num = convToLong(tok.getIdent());
                return c -> num;
            case STRING:
                String s = tok.getIdent();
                return c -> s;
            case SUB:
                Expression negExp = parseIdent();
                return c -> -Value.toLong(negExp.value(c));
            case NOT:
                Expression notExp = parseIdent();
                return c -> Value.not(notExp.value(c));
            case OPEN:
                Expression exp = parseExpression();
                expect(Tokenizer.Token.CLOSE);
                return exp;
            case FUNC:
                FirstClassFunction func = parseFunction();
                return c -> func;
            default:
                throw newUnexpectedToken(t);
        }
    }

    private FirstClassFunction parseFunction() throws IOException, ParserException {
        expect(OPEN);
        ArrayList<String> args = new ArrayList<>();
        if (!isToken(CLOSE)) {
            expect(IDENT);
            args.add(tok.getIdent());
            while (!isToken(CLOSE)) {
                expect(COMMA);
                expect(IDENT);
                args.add(tok.getIdent());
            }
        }
        Statement st = parseStatement();
        return new FirstClassFunction(args, st);
    }

}

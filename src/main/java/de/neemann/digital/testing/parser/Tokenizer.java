/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.testing.parser;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

/**
 * Simple tokenizer to tokenize boolean expressions.
 */
public class Tokenizer {

    interface Binary {
        long op(long a, long b);
    }

    enum Token {
        UNKNOWN, IDENT, BIN_NOT, LOG_NOT, OPEN, CLOSE, NUMBER, EOL, EOF, COMMA,
        AND(OperatorPrecedence.AND, (a, b) -> a & b),
        OR(OperatorPrecedence.OR, (a, b) -> a | b),
        XOR(OperatorPrecedence.XOR, (a, b) -> a ^ b),
        SHIFT_LEFT(OperatorPrecedence.SHIFT, (a, b) -> a << b),
        SHIFT_RIGHT(OperatorPrecedence.SHIFT, (a, b) -> a >> b),
        EQUAL(OperatorPrecedence.EQUAL, (a, b) -> (a == b) ? 1 : 0),
        NOT_EQUAL(OperatorPrecedence.EQUAL, (a, b) -> (a != b) ? 1 : 0),
        ADD(OperatorPrecedence.ADD, (a, b) -> a + b),
        SUB(OperatorPrecedence.ADD, (a, b) -> a - b),
        MUL(OperatorPrecedence.MUL, (a, b) -> a * b),
        DIV(OperatorPrecedence.MUL, (a, b) -> a / b),
        MOD(OperatorPrecedence.MUL, (a, b) -> a % b),
        GREATER(OperatorPrecedence.COMPARE, (a, b) -> (a > b) ? 1 : 0),
        GREATER_EQUAL(OperatorPrecedence.COMPARE, (a, b) -> (a >= b) ? 1 : 0),
        SMALLER(OperatorPrecedence.COMPARE, (a, b) -> (a < b) ? 1 : 0),
        SMALLER_EQUAL(OperatorPrecedence.COMPARE, (a, b) -> (a <= b) ? 1 : 0),
        END, LOOP, REPEAT, BITS, SEMICOLON,
        LET, DECLARE, PROGRAM, INIT, MEMORY, WHILE, RESETRANDOM;

        private final OperatorPrecedence precedence;
        private final Binary function;

        Token() {
            this(null, null);
        }

        Token(OperatorPrecedence precedence, Binary function) {
            this.precedence = precedence;
            this.function = function;
        }

        public OperatorPrecedence getPrecedence() {
            return precedence;
        }

        public Binary getFunction() {
            return function;
        }
    }

    private final static HashMap<String, Token> STATEMENT_MAP = new HashMap<>();

    static {
        STATEMENT_MAP.put("end", Token.END);
        STATEMENT_MAP.put("loop", Token.LOOP);
        STATEMENT_MAP.put("repeat", Token.REPEAT);
        STATEMENT_MAP.put("bits", Token.BITS);
        STATEMENT_MAP.put("let", Token.LET);
        STATEMENT_MAP.put("resetRandom", Token.RESETRANDOM);
        STATEMENT_MAP.put("while", Token.WHILE);
        STATEMENT_MAP.put("declare", Token.DECLARE);
        STATEMENT_MAP.put("program", Token.PROGRAM);
        STATEMENT_MAP.put("init", Token.INIT);
        STATEMENT_MAP.put("memory", Token.MEMORY);
    }

    private final Reader in;
    private final StringBuilder builder;
    private Token token;
    private boolean isToken;
    private boolean isUnreadChar = false;
    private int unreadChar;
    private int line = 1;

    /**
     * Creates a new instance
     *
     * @param in the reader
     */
    public Tokenizer(Reader in) {
        this.in = in;
        token = Token.UNKNOWN;
        isToken = false;
        builder = new StringBuilder();
    }

    /**
     * Reads the next token
     *
     * @return the token
     * @throws IOException IOException
     */
    public Token next() throws IOException {
        Token token = peek();
        consume();
        return token;
    }

    /**
     * Consumes the token after a peek call
     */
    public void consume() {
        isToken = false;
    }

    /**
     * Peeks the next token.
     * The token is kept in the stream, so next() or peek() will return this token again!
     *
     * @return the token
     * @throws IOException IOException
     */
    public Token peek() throws IOException {
        if (isToken)
            return token;

        int c;
        do {
            c = readChar();
        } while (isWhiteSpace(c));

        switch (c) {
            case -1:
                token = Token.EOF;
                break;
            case '\n':
            case '\r':
                return Token.EOL;
            case '(':
                token = Token.OPEN;
                break;
            case ')':
                token = Token.CLOSE;
                break;
            case ';':
                token = Token.SEMICOLON;
                break;
            case '&':
                token = Token.AND;
                break;
            case '|':
                token = Token.OR;
                break;
            case '^':
                token = Token.XOR;
                break;
            case '+':
                token = Token.ADD;
                break;
            case '-':
                token = Token.SUB;
                break;
            case '*':
                token = Token.MUL;
                break;
            case '%':
                token = Token.MOD;
                break;
            case '/':
                token = Token.DIV;
                break;
            case '<':
                if (isNextChar('<')) {
                    token = Token.SHIFT_LEFT;
                } else if (isNextChar('=')) {
                    token = Token.SMALLER_EQUAL;
                } else {
                    token = Token.SMALLER;
                }
                break;
            case '>':
                if (isNextChar('>')) {
                    token = Token.SHIFT_RIGHT;
                } else if (isNextChar('=')) {
                    token = Token.GREATER_EQUAL;
                } else {
                    token = Token.GREATER;
                }
                break;
            case '~':
                token = Token.BIN_NOT;
                break;
            case '!':
                if (isNextChar('=')) {
                    token = Token.NOT_EQUAL;
                } else {
                    token = Token.LOG_NOT;
                }
                break;
            case ',':
                token = Token.COMMA;
                break;
            case '=':
                token = Token.EQUAL;
                break;
            default:
                if (isIdentChar(c)) {
                    token = Token.IDENT;
                    builder.setLength(0);
                    builder.append((char) c);
                    boolean wasChar = true;
                    do {
                        c = readChar();
                        if (isIdentChar(c) || isNumberChar(c)) {
                            builder.append((char) c);
                        } else {
                            unreadChar(c);
                            wasChar = false;
                        }
                    } while (wasChar);
                    token = STATEMENT_MAP.get(builder.toString());
                    if (token == null) token = Token.IDENT;
                } else if (isNumberChar(c)) {
                    token = Token.NUMBER;
                    builder.setLength(0);
                    builder.append((char) c);
                    boolean wasChar = true;
                    do {
                        c = readChar();
                        if (isNumberChar(c) || isHexChar(c) || c == 'x' || c == 'X' || c == ':' || c == '.') {
                            builder.append((char) c);
                        } else {
                            unreadChar(c);
                            wasChar = false;
                        }
                    } while (wasChar);
                } else {
                    token = Token.UNKNOWN;
                    builder.setLength(0);
                    builder.append((char) c);
                }
        }

        isToken = true;
        return token;
    }

    private boolean isNextChar(char should) throws IOException {
        int c = readChar();
        if (c == should)
            return true;
        unreadChar(c);
        return false;
    }

    /**
     * @return the identifier
     */
    public String getIdent() {
        return builder.toString();
    }

    private int readIntChar() throws IOException {
        if (isUnreadChar) {
            isUnreadChar = false;
            return unreadChar;
        } else {
            final int c = in.read();
            if (c == '\n') line++;
            return c;
        }
    }

    private int readChar() throws IOException {
        int c = readIntChar();
        if (c == '#') {
            do {
                c = readIntChar();
            } while (!((c == '\n') || (c < 0)));
        }
        return c;
    }

    private void unreadChar(int c) {
        unreadChar = c;
        isUnreadChar = true;
    }

    private boolean isIdentChar(int c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (c == '_');
    }

    private boolean isHexChar(int c) {
        return (c >= 'a' && c <= 'f')
                || (c >= 'A' && c <= 'F');
    }

    private boolean isNumberChar(int c) {
        return (c >= '0' && c <= '9');
    }

    private boolean isWhiteSpace(int c) {
        return c == ' ' || c == '\t';
    }

    @Override
    public String toString() {
        if (token == Token.IDENT || token == Token.UNKNOWN)
            return getIdent();
        else
            return token.name();
    }

    /**
     * @return the parsed test vectors
     */
    public int getLine() {
        return line;
    }

    /**
     * Skips empty lines in the beginning of the file
     *
     * @throws IOException IOException
     */
    public void skipEmptyLines() throws IOException {
        int c;
        do {
            c = readChar();
        } while (c == '\n' || c == '\r' || c == ' ');
        unreadChar(c);
    }

    /**
     * Special reader to parse the header.
     * For the identifiers in the header apply other rules as for identifiers in the test data.
     *
     * @return the Token
     * @throws IOException IOException
     */
    public Token simpleIdent() throws IOException {
        builder.setLength(0);
        while (true) {
            int c = readChar();
            switch (c) {
                case -1:
                    return Token.EOF;
                case '\n':
                case '\r':
                    if (builder.length() > 0) {
                        unreadChar(c);
                        return Token.IDENT;
                    } else
                        return Token.EOL;
                case '\t':
                case ' ':
                    if (builder.length() > 0) {
                        return Token.IDENT;
                    }
                    break;
                default:
                    builder.append((char) c);
            }
        }
    }


}

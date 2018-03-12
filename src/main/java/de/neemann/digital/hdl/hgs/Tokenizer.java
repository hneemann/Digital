/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

/**
 * Simple tokenizer to tokenize boolean expressions.
 */
public class Tokenizer {

    enum Token {
        UNKNOWN, IDENT, AND, OR, XOR, NOT, OPEN, CLOSE, NUMBER, EOL, EOF, SHIFTLEFT, SHIFTRIGHT, COMMA, EQUAL,
        ADD, SUB, MUL, GREATER, SMALER, DIV, MOD, END, IF, ELSE, FOR, WHILE, SEMICOLON, NOTEQUAL, STRING,
        OPENBRACE, CLOSEDBRACE, CODEEND, OPENSQUARE, CLOSEDSQUARE, DOT, PRINT, STATIC, FUNC, PRINTF
    }

    private static HashMap<String, Token> statementMap = new HashMap<>();

    static {
        statementMap.put("if", Token.IF);
        statementMap.put("else", Token.ELSE);
        statementMap.put("for", Token.FOR);
        statementMap.put("while", Token.FOR);
        statementMap.put("print", Token.PRINT);
        statementMap.put("printf", Token.PRINTF);
        statementMap.put("func", Token.FUNC);
    }

    private final Reader in;
    private Token token;
    private boolean isToken;
    private StringBuilder builder;
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
        while (true) {
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
                case '(':
                    token = Token.OPEN;
                    break;
                case ')':
                    token = Token.CLOSE;
                    break;
                case '{':
                    token = Token.OPENBRACE;
                    break;
                case '}':
                    token = Token.CLOSEDBRACE;
                    break;
                case '[':
                    token = Token.OPENSQUARE;
                    break;
                case ']':
                    token = Token.CLOSEDSQUARE;
                    break;
                case '.':
                    token = Token.DOT;
                    break;
                case '@':
                    token = Token.STATIC;
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
                    if (isNextChar('/')) {
                        token = null;
                        skipLine();
                    } else
                        token = Token.DIV;
                    break;
                case '<':
                    if (isNextChar('<')) {
                        token = Token.SHIFTLEFT;
                    } else {
                        token = Token.SMALER;
                    }
                    break;
                case '>':
                    if (isNextChar('>')) {
                        token = Token.SHIFTRIGHT;
                    } else {
                        token = Token.GREATER;
                    }
                    break;
                case '~':
                    token = Token.NOT;
                    break;
                case ',':
                    token = Token.COMMA;
                    break;
                case '=':
                    token = Token.EQUAL;
                    break;
                case '!':
                    if (isNextChar('=')) {
                        token = Token.NOTEQUAL;
                    } else {
                        token = Token.NOT;
                    }
                    break;
                case '"':
                    token = Token.STRING;
                    builder.setLength(0);
                    while ((c = readChar()) != '"')
                        builder.append((char) c);
                    break;
                case '?':
                    if (isNextChar('>'))
                        token = Token.CODEEND;
                    else
                        token = Token.UNKNOWN;
                    break;
                default:
                    if (isIdentChar(c)) {
                        readIdent(c);
                    } else if (isNumberChar(c)) {
                        readNumber(c);
                    } else {
                        token = Token.UNKNOWN;
                        builder.setLength(0);
                        builder.append((char) c);
                    }
            }
            if (token != null) {
                isToken = true;
                return token;
            }
        }
    }

    private void readNumber(int c) throws IOException {
        token = Token.NUMBER;
        builder.setLength(0);
        builder.append((char) c);
        boolean wasChar = true;
        do {
            c = readChar();
            if (isNumberChar(c) || isHexChar(c) || c == 'x' || c == 'X') {
                builder.append((char) c);
            } else {
                unreadChar(c);
                wasChar = false;
            }
        } while (wasChar);
    }

    private void readIdent(int c) throws IOException {
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
        token = statementMap.get(builder.toString());
        if (token == null) token = Token.IDENT;
    }

    private void skipLine() throws IOException {
        while (true) {
            int c = readChar();
            if (c < 0 || c == '\n')
                return;
        }
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

    private int readChar() throws IOException {
        if (isUnreadChar) {
            isUnreadChar = false;
            return unreadChar;
        } else {
            final int c = in.read();
            if (c == '\n') line++;
            return c;
        }
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
        return c == ' ' || c == '\t' || c == '\n' || c == '\r';
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
     * Reads pure text
     *
     * @return the string
     * @throws IOException IOException
     */
    public String readText() throws IOException {
        isToken = false;
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = in.read()) > 0) {
            if (c == '<') {
                c = in.read();
                if (c == '?') {
                    return sb.toString();
                } else {
                    sb.append((char) c);
                }
            } else {
                if (c == '\n')
                    line++;
                sb.append((char) c);
            }
        }
        return sb.toString();
    }

}

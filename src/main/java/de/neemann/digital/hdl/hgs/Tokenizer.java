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
class Tokenizer {

    enum Token {
        UNKNOWN, IDENT, AND, OR, XOR, NOT, OPEN, CLOSE, NUMBER, EOL, EOF, SHIFTLEFT, SHIFTRIGHT, COMMA, EQUAL,
        ADD, SUB, MUL, GREATER, LESS, DIV, MOD, END, IF, ELSE, FOR, WHILE, SEMICOLON, NOTEQUAL, STRING,
        OPENBRACE, CLOSEDBRACE, CODEEND, OPENSQUARE, CLOSEDSQUARE, DOT, FUNC, GREATEREQUAL, LESSEQUAL,
        REPEAT, RETURN, COLON, UNTIL, DOUBLE, EXPORT, TRUE, FALSE
    }

    private static HashMap<String, Token> statementMap = new HashMap<>();

    static {
        statementMap.put("if", Token.IF);
        statementMap.put("else", Token.ELSE);
        statementMap.put("for", Token.FOR);
        statementMap.put("while", Token.WHILE);
        statementMap.put("func", Token.FUNC);
        statementMap.put("repeat", Token.REPEAT);
        statementMap.put("until", Token.UNTIL);
        statementMap.put("return", Token.RETURN);
        statementMap.put("export", Token.EXPORT);
        statementMap.put("true", Token.TRUE);
        statementMap.put("false", Token.FALSE);
    }

    private final Reader in;
    private Token token;
    private boolean isToken;
    private StringBuilder builder;
    private boolean isUnreadChar = false;
    private int unreadChar;
    private String srcFile;
    private int line = 1;

    /**
     * Creates a new instance
     *
     * @param in the reader
     */
    Tokenizer(Reader in, String srcFile) {
        this.in = in;
        this.srcFile = srcFile;
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
     * Returns the source file name if any
     *
     * @return the source file name
     */
    public String getSrcFile() {
        return srcFile;
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
                case ':':
                    token = Token.COLON;
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
                    } else if (isNextChar('=')) {
                        token = Token.LESSEQUAL;
                    } else {
                        token = Token.LESS;
                    }
                    break;
                case '>':
                    if (isNextChar('>')) {
                        token = Token.SHIFTRIGHT;
                    } else if (isNextChar('=')) {
                        token = Token.GREATEREQUAL;
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
                    readString();
                    break;
                case '\'':
                    token = Token.IDENT;
                    builder.setLength(0);
                    while ((c = readChar()) != '\'') {
                        builder.append((char) c);
                        if (c < 0)
                            throw new IOException("EOF detected while scanning escaped var name");
                    }
                    break;
                case '?':
                    if (isNextChar('>'))
                        token = Token.CODEEND;
                    else if (isNextChar('}'))
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

    private void readString() throws IOException {
        int c;
        builder.setLength(0);
        while ((c = readChar()) != '"') {
            if (c == '\\') {
                c = readChar();
                switch (c) {
                    case '\\':
                        c = '\\';
                        break;
                    case 'n':
                        c = '\n';
                        break;
                    case 'r':
                        c = '\r';
                        break;
                    case 't':
                        c = '\t';
                        break;
                    case '"':
                        c = '"';
                        break;
                    default:
                        throw new IOException("not allowed in string: \\" + (char) c);
                }
            }
            builder.append((char) c);
            if (c < 0)
                throw new IOException("EOF detected while scanning a string");
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
            } else if (c == '.') {
                builder.append((char) c);
                token = Token.DOUBLE;
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

    /**
     * Returns true if the given character is a white space.
     *
     * @param c the character to test
     * @return true in c is a white space
     */
    public static boolean isWhiteSpace(int c) {
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
        while ((c = readChar())>0) {
            if (c == '<' || c == '{') {
                if (isNextChar('?')) {
                    return sb.toString();
                } else
                    sb.append((char) c);
            } else {
                sb.append((char) c);
            }
        }
        return sb.toString();
    }

}

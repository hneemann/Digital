/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

import de.neemann.digital.testing.parser.OperatorPrecedence;

import java.io.IOException;
import java.io.Reader;
import java.util.HashMap;

/**
 * Simple tokenizer to tokenize boolean expressions.
 */
class Tokenizer {

    interface Binary {
        Object op(Object a, Object b) throws HGSEvalException;
    }

    enum Token {
        UNKNOWN, IDENT, OPEN, CLOSE, NUMBER, EOL, EOF, COMMA, NOT,
        OR(OperatorPrecedence.OR, Value::or),
        XOR(OperatorPrecedence.XOR, Value::xor),
        AND(OperatorPrecedence.AND, Value::and),
        EQUAL(OperatorPrecedence.EQUAL, Value::equals),
        NOTEQUAL(OperatorPrecedence.EQUAL, (a, b) -> !Value.equals(a, b)),
        ADD(OperatorPrecedence.ADD, Value::add),
        SUB(OperatorPrecedence.ADD, Value::sub),
        MUL(OperatorPrecedence.MUL, Value::mul),
        DIV(OperatorPrecedence.MUL, Value::div),
        MOD(OperatorPrecedence.MUL, (a, b) -> Value.toLong(a) % Value.toLong(b)),
        LESS(OperatorPrecedence.COMPARE, Value::less),
        LESSEQUAL(OperatorPrecedence.COMPARE, Value::lessEqual),
        GREATER(OperatorPrecedence.COMPARE, (a, b) -> Value.less(b, a)),
        GREATEREQUAL(OperatorPrecedence.COMPARE, (a, b) -> Value.lessEqual(b, a)),
        SHIFTLEFT(OperatorPrecedence.SHIFT, (a, b) -> Value.toLong(a) << Value.toLong(b)),
        SHIFTRIGHT(OperatorPrecedence.SHIFT, (a, b) -> Value.toLong(a) >>> Value.toLong(b)),
        END, IF, ELSE, FOR, WHILE, SEMICOLON, STRING,
        OPENBRACE, CLOSEDBRACE, CODEEND, OPENSQUARE, CLOSEDSQUARE, DOT, FUNC,
        REPEAT, RETURN, COLON, UNTIL, DOUBLE, EXPORT, TRUE, FALSE;

        private final OperatorPrecedence precedence;
        private final Binary binary;

        Token() {
            this(null, null);
        }

        Token(OperatorPrecedence precedence, Binary binary) {
            this.precedence = precedence;
            this.binary = binary;
        }

        public OperatorPrecedence getPrecedence() {
            return precedence;
        }

        public Binary getBinary() {
            return binary;
        }
    }

    private static final HashMap<String, Token> STATEMENT_MAP = new HashMap<>();

    static {
        STATEMENT_MAP.put("if", Token.IF);
        STATEMENT_MAP.put("else", Token.ELSE);
        STATEMENT_MAP.put("for", Token.FOR);
        STATEMENT_MAP.put("while", Token.WHILE);
        STATEMENT_MAP.put("func", Token.FUNC);
        STATEMENT_MAP.put("repeat", Token.REPEAT);
        STATEMENT_MAP.put("until", Token.UNTIL);
        STATEMENT_MAP.put("return", Token.RETURN);
        STATEMENT_MAP.put("export", Token.EXPORT);
        STATEMENT_MAP.put("true", Token.TRUE);
        STATEMENT_MAP.put("false", Token.FALSE);
    }

    private final Reader in;
    private final StringBuilder builder;
    private final String srcFile;
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
        token = STATEMENT_MAP.get(builder.toString());
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
        while ((c = readChar()) > 0) {
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

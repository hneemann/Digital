/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern;

import de.neemann.digital.core.ExceptionWithOrigin;

import java.io.IOException;
import java.io.Reader;

import static de.neemann.digital.core.extern.VHDLTokenizer.Token.IDENT;
import static de.neemann.digital.core.extern.VHDLTokenizer.Token.NUMBER;

/**
 * Simple tokenizer to tokenize boolean expressions.
 */
public class VHDLTokenizer {

    enum Token {UNKNOWN, IDENT, OPEN, CLOSE, NUMBER, COMMA, COLON, SEMICOLON}

    private final Reader in;
    private Token token;
    private boolean isToken;
    private StringBuilder builder;
    private boolean isUnreadChar = false;
    private int unreadChar;

    /**
     * Creates a new instance
     *
     * @param in the reader
     */
    public VHDLTokenizer(Reader in) {
        this.in = in;
        token = Token.UNKNOWN;
        isToken = false;
        builder = new StringBuilder();
    }

    /**
     * Reads the next token
     *
     * @return the token
     * @throws IOException        IOException
     * @throws TokenizerException TokenizerException
     */
    public Token next() throws IOException, TokenizerException {
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
     * Consumes the given token
     *
     * @param t the token to consume
     * @throws IOException        IOException
     * @throws TokenizerException TokenizerException
     */
    public void consume(Token t) throws TokenizerException, IOException {
        if (next() != t)
            throw new TokenizerException("ident expected");
    }

    /**
     * Peeks the next token.
     * The token is kept in the stream, so next() or peek() will return this token again!
     *
     * @return the token
     * @throws IOException        IOException
     * @throws TokenizerException TokenizerException
     */
    public Token peek() throws IOException, TokenizerException {
        if (isToken)
            return token;

        int c;
        do {
            c = readChar();
            if (c == '-') {
                int cc = readChar();
                if (cc == '-') {
                    do {
                        c = readChar();
                    } while (c != '\n');
                } else
                    unreadChar(cc);
            }
        } while (isWhiteSpace(c));

        switch (c) {
            case -1:
                throw new TokenizerException("unexpected EOF");
            case '(':
                token = Token.OPEN;
                break;
            case ')':
                token = Token.CLOSE;
                break;
            case ';':
                token = Token.SEMICOLON;
                break;
            case ',':
                token = Token.COMMA;
                break;
            case ':':
                token = Token.COLON;
                break;
            default:
                if (isIdentChar(c)) {
                    token = IDENT;
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
                } else if (isNumberChar(c)) {
                    token = NUMBER;
                    builder.setLength(0);
                    builder.append((char) c);
                    boolean wasChar = true;
                    do {
                        c = readChar();
                        if (isNumberChar(c)) {
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

    /**
     * @return the identifier
     * @throws IOException        IOException
     * @throws TokenizerException TokenizerException
     */
    public String consumeIdent() throws IOException, TokenizerException {
        if (next() != IDENT)
            throw new TokenizerException("ident expected");
        return builder.toString();
    }

    /**
     * Consumes an identifier
     *
     * @param ident the identifier to consume
     * @throws IOException        IOException
     * @throws TokenizerException TokenizerException
     */
    public void consumeIdent(String ident) throws IOException, TokenizerException {
        if (next() != IDENT)
            throw new TokenizerException("ident expected");
        if (builder.toString().equalsIgnoreCase(ident))
            return;
        throw new TokenizerException("ident " + ident + " expected");
    }

    /**
     * @return the identifier
     * @throws IOException        IOException
     * @throws TokenizerException TokenizerException
     */
    public int consumeNumber() throws TokenizerException, IOException {
        if (next() != NUMBER)
            throw new TokenizerException("ident expected");
        try {
            return Integer.parseInt(builder.toString());
        } catch (NumberFormatException e) {
            throw new TokenizerException("not a number " + builder.toString());
        }
    }

    /**
     * @return the value of the last parsed token
     */
    public String value() {
        return builder.toString();
    }

    private int readChar() throws IOException {
        if (isUnreadChar) {
            isUnreadChar = false;
            return unreadChar;
        } else
            return in.read();
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

    private boolean isNumberChar(int c) {
        return (c >= '0' && c <= '9');
    }

    private boolean isWhiteSpace(int c) {
        return c == ' ' || c == '\n' || c == '\r' || c == '\t';
    }

    @Override
    public String toString() {
        if (token == NUMBER || token == IDENT || token == Token.UNKNOWN)
            return builder.toString();
        else
            return token.name();
    }

    /**
     * The tokenizer exception
     */
    public static final class TokenizerException extends ExceptionWithOrigin {
        private TokenizerException(String message) {
            super(message);
        }
    }
}

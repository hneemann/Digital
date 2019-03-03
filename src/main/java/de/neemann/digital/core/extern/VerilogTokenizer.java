/*
 * Copyright (c) 2016 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.core.extern;

import de.neemann.digital.core.ExceptionWithOrigin;

import java.io.IOException;
import java.io.Reader;


/**
 * Simple tokenizer to tokenize boolean expressions.
 */
public class VerilogTokenizer {

    enum Token {UNKNOWN, MODULE, INPUT, OUTPUT, INOUT, REG, WIRE, ENDMODULE,
                EOF, NUMBER, IDENT, OPENPAR, CLOSEPAR, OPENBRACKET, CLOSEBRACKET,
                SEMICOLON, COLON, COMMA, ERROR};

    private final Reader in;
    private String value;
    private boolean isUnreadChar = false;
    private int unreadChar;

    /**
     * Creates a new instance
     *
     * @param in the reader
     */
    public VerilogTokenizer(Reader in) {
        this.in = in;
    }

    /**
     * Look for the end of the module
     * @return true if the end of the module was found, false otherwise.
     * @throws IOException IOException
     */
    public Token lookEndModule() throws IOException {
        Token tk;
        try {
            do {
                tk = nextToken();
            } while ((tk != Token.ENDMODULE) && (tk != Token.EOF));
        } catch (TokenizerException ex) {
            tk = Token.ERROR;
        }
        return tk;
    }

    /**
     * Reads the next token
     *
     * @return the token
     * @throws IOException        IOException
     * @throws TokenizerException TokenizerException
     */
    public Token nextToken() throws IOException, TokenizerException {
        while (true) {
            int ch = readChar();

            if (ch == '/') {
                int cc = readChar();
                switch (cc) {
                    case '*':
                        boolean hasAsterisk = false;

                        while (true) {
                            cc = readChar();
                            if (cc == -1)
                                throw new TokenizerException("unexpected EOF");
                            if (cc == '/' && hasAsterisk)
                                break;

                            hasAsterisk = (cc == '*');
                        }
                        continue;
                    case '/':
                        while (cc != '\n' && cc != -1) {
                            cc = readChar();
                        }

                        continue;
                    default:
                        unreadChar(cc);
                        break;
                }
            }

            value = "" + (char) ch;

            switch (ch) {
                case ' ':
                case '\t':
                case '\r':
                case '\n':
                    continue;
                case -1: return Token.EOF;
                case '(': return Token.OPENPAR;
                case ')': return Token.CLOSEPAR;
                case '[': return Token.OPENBRACKET;
                case ']': return Token.CLOSEBRACKET;
                case ';': return Token.SEMICOLON;
                case ':': return Token.COLON;
                case ',': return Token.COMMA;
                case '`':
                    while (ch != '\n' && ch != -1) {
                        ch = readChar();
                    }
                    break;
                case '\\':
                    StringBuilder sb1 = new StringBuilder();
                    while (ch != ' ' && ch != '\n' && ch != -1) {
                        sb1.append((char) ch);
                        ch = readChar();
                    }
                    if (ch != ' ')
                        unreadChar(ch);
                    else
                        sb1.append(' ');
                    value = sb1.toString();
                    return Token.IDENT;
                default:
                    if (isNumberChar(ch)) {
                        StringBuilder sb = new StringBuilder();

                        while (isNumberChar(ch)) {
                            sb.append((char) ch);
                            ch = readChar();
                        }
                        unreadChar(ch);
                        value = sb.toString();
                        return Token.NUMBER;
                    } else if (isIdentChar(ch)) {
                        StringBuilder sb = new StringBuilder();

                        while (isIdentChar(ch) || isNumberChar(ch) || ch == '$') {
                            sb.append((char) ch);
                            ch = readChar();
                        }
                        unreadChar(ch);
                        value = sb.toString();

                        return lookUpKeyword(value);
                    } else {
                        return Token.UNKNOWN;
                    }
            }
        }
    }

    private Token lookUpKeyword(String str) {
        switch (str) {
            case "module": return Token.MODULE;
            case "input": return Token.INPUT;
            case "output": return Token.OUTPUT;
            case "inout": return Token.INOUT;
            case "reg": return Token.REG;
            case "wire": return Token.WIRE;
            case "endmodule": return Token.ENDMODULE;
            default:
                return Token.IDENT;
        }
    }


    /**
     * @return the value of the last parsed token
     */
    public String value() {
        return value;
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

    /**
     * The tokenizer exception
     */
    public static final class TokenizerException extends ExceptionWithOrigin {
        private TokenizerException(String message) {
            super(message);
        }
    }
}

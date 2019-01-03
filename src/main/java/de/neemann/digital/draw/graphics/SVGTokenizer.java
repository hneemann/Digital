/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

/**
 * A tokenizer to parse svg strings
 */
public class SVGTokenizer {
    /**
     * token types
     */
    public enum Token {
        /**
         * EOF
         */
        EOF,
        /**
         * a command
         */
        COMMAND,
        /**
         * a number
         */
        NUMBER,
        /**
         * a character
         */
        CHAR
    }

    private final String code;
    private int lastTokenPos;
    private int pos;
    private StringBuilder command;
    private float value;
    private char character;

    /**
     * Creates a new instance
     *
     * @param code the code to parse
     */
    public SVGTokenizer(String code) {
        this.code = code;
        command = new StringBuilder();
        pos = 0;
    }

    private Token next() throws TokenizerException {
        lastTokenPos = pos;
        while (pos < code.length() && (Character.isWhitespace(code.charAt(pos)) || code.charAt(pos) == ','))
            pos++;
        if (pos == code.length())
            return Token.EOF;

        character = code.charAt(pos);
        if (Character.isAlphabetic(character)) {
            command.setLength(0);
            pos++;
            command.append(character);
            while (pos < code.length() && Character.isAlphabetic(code.charAt(pos))) {
                command.append(code.charAt(pos));
                pos++;
            }
            return Token.COMMAND;
        }
        if (Character.isDigit(character) || character == '-' || character == '+') {
            value = parseNumber();
            return Token.NUMBER;
        } else {
            pos++;
            return Token.CHAR;
        }
    }

    private char peekChar() {
        return code.charAt(pos);
    }

    private float parseNumber() throws TokenizerException {
        int p0 = pos;
        if (peekChar() == '+' || peekChar() == '-')
            pos++;

        while (pos < code.length() && (Character.isDigit(peekChar()) || peekChar() == '.'))
            pos++;

        if (pos < code.length() && (peekChar() == 'e' || peekChar() == 'E')) {
            pos++;
            if (peekChar() == '+' || peekChar() == '-' || Character.isDigit(peekChar())) {
                pos++;
                while (pos < code.length() && (Character.isDigit(peekChar()) || peekChar() == '.'))
                    pos++;
            } else
                pos--;
        }

        try {
            return Float.parseFloat(code.substring(p0, pos));
        } catch (NumberFormatException e) {
            throw new TokenizerException("not a number " + code.substring(p0, pos));
        }
    }


    private void unreadToken() {
        pos = lastTokenPos;
    }

    /**
     * Expect the given character c
     *
     * @param c the expected character
     * @throws TokenizerException TokenizerException
     */
    public void expect(char c) throws TokenizerException {
        if (next() != Token.CHAR)
            throw new TokenizerException("expected character " + c);
        if (character != c)
            throw new RuntimeException("expected " + c + " found " + character);
    }

    /**
     * Reads a float
     *
     * @return the float
     * @throws TokenizerException TokenizerException
     */
    public float readFloat() throws TokenizerException {
        if (next() != Token.NUMBER)
            throw new TokenizerException("expected a number");
        return value;
    }

    /**
     * Reads a command
     *
     * @return the command
     * @throws TokenizerException TokenizerException
     */
    public String readCommand() throws TokenizerException {
        if (next() != Token.COMMAND)
            throw new TokenizerException("expected a command");
        return command.toString();
    }

    /**
     * @return true if the next token is a number
     * @throws TokenizerException TokenizerException
     */
    public boolean nextIsNumber() throws TokenizerException {
        if (next() == Token.NUMBER) {
            unreadToken();
            return true;
        } else {
            unreadToken();
            return false;
        }
    }

    /**
     * Checks if the next char is the given char.
     *
     * @param c the char
     * @return true if next char is the given char.
     * @throws TokenizerException TokenizerException
     */
    public boolean nextIsChar(char c) throws TokenizerException {
        if (next() == Token.CHAR) {
            if (character == c) {
                return true;
            } else {
                unreadToken();
                return false;
            }
        } else {
            unreadToken();
            return false;
        }
    }


    /**
     * @return true if the EOF is reached
     * @throws TokenizerException TokenizerException
     */
    public boolean isEOF() throws TokenizerException {
        if (next() == Token.EOF) {
            return true;
        } else {
            unreadToken();
            return false;
        }
    }

    @Override
    public String toString() {
        return code + " (" + pos + ")";
    }

    /**
     * @return the remainig string
     */
    public String remaining() {
        final String s = code.substring(pos).trim();
        pos = code.length();
        return s;
    }

    /**
     * Reads all up to the given character
     *
     * @param c the character
     * @return the data string
     */
    public String readTo(char c) {
        int p = pos;
        int brace = 0;
        while (pos < code.length() && (code.charAt(pos) != c || brace != 0)) {
            switch (code.charAt(pos)) {
                case '(':
                    brace++;
                    break;
                case ')':
                    brace--;
                    break;
            }
            pos++;
        }

        final String r = code.substring(p, pos).trim();

        while (pos < code.length() && code.charAt(pos) == c)
            pos++;

        return r;
    }

    /**
     * Exception thrown by the tokenizer
     */
    public static final class TokenizerException extends Exception {
        private TokenizerException(String message) {
            super(message);
        }
    }

}

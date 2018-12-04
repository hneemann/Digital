/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.shapes.custom.svg;

import de.neemann.digital.draw.graphics.Transform;
import de.neemann.digital.draw.graphics.TransformMatrix;
import de.neemann.digital.draw.graphics.TransformTranslate;
import de.neemann.digital.draw.graphics.VectorFloat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class TransformParser {
    private static final Logger LOGGER = LoggerFactory.getLogger(TransformParser.class);

    enum Token {EOF, COMMAND, NUMBER, CHAR}

    private final String transform;
    private int lastTokenPos;
    private int pos;
    private StringBuilder command;
    private float value;
    private char character;

    /**
     * Creates a new instance
     *
     * @param transform the path to parse
     */
    TransformParser(String transform) {
        this.transform = transform;
        command = new StringBuilder();
        pos = 0;
    }

    private Token next() {
        lastTokenPos = pos;
        while (pos < transform.length() && (transform.charAt(pos) == ' ' || transform.charAt(pos) == ','))
            pos++;
        if (pos == transform.length())
            return Token.EOF;

        character = transform.charAt(pos);
        if (Character.isAlphabetic(character)) {
            command.setLength(0);
            pos++;
            command.append(character);
            while (pos < transform.length() && Character.isAlphabetic(transform.charAt(pos))) {
                command.append(transform.charAt(pos));
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
        return transform.charAt(pos);
    }

    private float parseNumber() {
        int p0 = pos;
        if (peekChar() == '+' || peekChar() == '-')
            pos++;

        while (pos < transform.length() && (Character.isDigit(peekChar()) || peekChar() == '.'))
            pos++;

        if (pos < transform.length() && (peekChar() == 'e' || peekChar() == 'E')) {
            pos++;
            if (peekChar() == '+' || peekChar() == '-')
                pos++;

            while (pos < transform.length() && (Character.isDigit(peekChar()) || peekChar() == '.'))
                pos++;
        }

        return Float.parseFloat(transform.substring(p0, pos));
    }


    private void unreadToken() {
        pos = lastTokenPos;
    }

    private String getCommand() {
        return command.toString();
    }

    private float getValue() {
        return value;
    }

    private void expect(char c) {
        if (next() != Token.CHAR)
            throw new RuntimeException("expected character " + c);
        if (character != c)
            throw new RuntimeException("expected " + c + " found " + character);
    }

    private float readFloat() {
        if (next() != Token.NUMBER)
            throw new RuntimeException("expected a number");
        return value;
    }

    private float readOptionalFloat(float def) {
        if (next() != Token.NUMBER) {
            unreadToken();
            return def;
        } else
            return value;
    }

    /**
     * Parses the transformation
     *
     * @return the transformation
     */
    public Transform parse() {
        Transform combined = Transform.IDENTITY;
        try {
            Transform t;
            while (true) {
                final Token tok = next();
                if (tok == Token.EOF)
                    break;
                if (tok != Token.COMMAND)
                    throw new RuntimeException("invalid transform", null);
                switch (getCommand()) {
                    case "translate":
                        expect('(');
                        final float x = readFloat();
                        float y = readOptionalFloat(0);
                        expect(')');
                        t = new TransformTranslate(new VectorFloat(x, y));
                        break;
                    case "scale":
                        expect('(');
                        final float xs = readFloat();
                        float ys = readOptionalFloat(xs);
                        expect(')');
                        t = new TransformMatrix(xs, 0, 0, ys, 0, 0);
                        break;
                    case "matrix":
                        expect('(');
                        final float ma = readFloat();
                        final float mb = readFloat();
                        final float mc = readFloat();
                        final float md = readFloat();
                        final float mx = readFloat();
                        final float my = readFloat();
                        expect(')');
                        t = new TransformMatrix(
                                ma,
                                mc,
                                mb,
                                md,
                                mx,
                                my);
                        break;
                    case "rotate":
                        expect('(');
                        float w = readFloat();
                        if (next() == Token.NUMBER) {
                            t = TransformMatrix.rotate(w);
                            float xc = getValue();
                            float yc = readFloat();
                            t = Transform.mul(new TransformTranslate(-xc, -yc), t);
                            t = Transform.mul(t, new TransformTranslate(xc, yc));
                        } else {
                            unreadToken();
                            t = TransformMatrix.rotate(w);
                        }
                        expect(')');
                        break;
                    default:
                        throw new RuntimeException("unknown transform: " + value, null);
                }
                combined = Transform.mul(t, combined);
            }
        } catch (RuntimeException e) {
            LOGGER.warn(transform + ": " + e.getMessage());
        }
        return combined;
    }

}

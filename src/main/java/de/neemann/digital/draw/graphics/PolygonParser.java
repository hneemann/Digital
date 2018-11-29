/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics;

/**
 * Creates a polygon from a path
 */
public class PolygonParser {
    enum Token {EOF, COMMAND, NUMBER}

    private final String path;
    private int lastTokenPos;
    private int pos;
    private char command;
    private float value;
    private float x;
    private float y;
    private VectorInterface lastQuadraticControlPoint;
    private VectorInterface lastCubicControlPoint;

    /**
     * Creates a new instance
     *
     * @param path the path to parse
     */
    PolygonParser(String path) {
        this.path = path;
        pos = 0;
    }

    Token next() {
        lastTokenPos = pos;
        while (pos < path.length() && (path.charAt(pos) == ' ' || path.charAt(pos) == ','))
            pos++;
        if (pos == path.length())
            return Token.EOF;

        char c = path.charAt(pos);
        if (Character.isAlphabetic(c)) {
            pos++;
            command = c;
            return Token.COMMAND;
        } else {
            value = parseNumber();
            return Token.NUMBER;
        }
    }

    private char peekChar() {
        return path.charAt(pos);
    }

    private float parseNumber() {
        int p0 = pos;
        if (peekChar() == '+' || peekChar() == '-')
            pos++;

        while (pos < path.length() && (Character.isDigit(peekChar()) || peekChar() == '.'))
            pos++;

        if (pos < path.length() && (peekChar() == 'e' || peekChar() == 'E')) {
            pos++;
            if (peekChar() == '+' || peekChar() == '-')
                pos++;

            while (pos < path.length() && (Character.isDigit(peekChar()) || peekChar() == '.'))
                pos++;
        }

        return Float.parseFloat(path.substring(p0, pos));
    }


    private void unreadToken() {
        pos = lastTokenPos;
    }

    char getCommand() {
        return command;
    }

    double getValue() {
        return value;
    }

    private float nextValue() throws ParserException {
        if (next() != Token.NUMBER)
            throw new ParserException("expected a number at pos " + pos + " in '" + path + "'");
        return value;
    }

    private VectorFloat nextVector() throws ParserException {
        x = nextValue();
        y = nextValue();
        return new VectorFloat(x, y);
    }

    private VectorFloat nextVectorInc() throws ParserException {
        x += nextValue();
        y += nextValue();
        return new VectorFloat(x, y);
    }

    private VectorFloat nextVectorRel() throws ParserException {
        return new VectorFloat(x + nextValue(), y + nextValue());
    }

    /**
     * Creates a polygon from the given path.
     *
     * @return the polygon
     * @throws ParserException ParserException
     */
    public Polygon create() throws ParserException {
        Polygon p = new Polygon(false);
        Token tok;
        boolean closedPending = false;
        while ((tok = next()) != Token.EOF) {
            if (tok == Token.NUMBER) {
                unreadToken();
                if (command == 'm')
                    command = 'l';
                else if (command == 'M')
                    command = 'L';
            }
            switch (command) {
                case 'M':
                    if (closedPending) {
                        closedPending = false;
                        p.addClosePath();
                    }
                    p.addMoveTo(nextVector());
                    clearControl();
                    break;
                case 'm':
                    if (closedPending) {
                        closedPending = false;
                        p.addClosePath();
                    }
                    p.addMoveTo(nextVectorInc());
                    clearControl();
                    break;
                case 'V':
                    y = nextValue();
                    p.add(getCurrent());
                    clearControl();
                    break;
                case 'v':
                    y += nextValue();
                    p.add(getCurrent());
                    clearControl();
                    break;
                case 'H':
                    x = nextValue();
                    p.add(getCurrent());
                    clearControl();
                    break;
                case 'h':
                    x += nextValue();
                    p.add(getCurrent());
                    clearControl();
                    break;
                case 'l':
                    p.add(nextVectorInc());
                    clearControl();
                    break;
                case 'L':
                    p.add(nextVector());
                    clearControl();
                    break;
                case 'c':
                    p.add(nextVectorRel(), setLastC3(nextVectorRel()), nextVectorInc());
                    break;
                case 'C':
                    p.add(nextVector(), setLastC3(nextVector()), nextVector());
                    break;
                case 'q':
                    p.add(setLastC2(nextVectorRel()), nextVectorInc());
                    break;
                case 'Q':
                    p.add(setLastC2(nextVector()), nextVector());
                    break;
                case 's':
                    addCubicWithReflect(p, getCurrent(), nextVectorRel(), nextVectorInc());
                    break;
                case 'S':
                    addCubicWithReflect(p, getCurrent(), nextVector(), nextVector());
                    break;
                case 't':
                    addQuadraticWithReflect(p, getCurrent(), nextVectorInc());
                    break;
                case 'T':
                    addQuadraticWithReflect(p, getCurrent(), nextVector());
                    break;
                case 'a':
                    addArc(p, nextVectorInc(), nextValue(), nextValue() != 0, nextValue() != 0, nextVectorInc());
                    clearControl();
                    break;
                case 'A':
                    addArc(p, nextVector(), nextValue(), nextValue() != 0, nextValue() != 0, nextVector());
                    clearControl();
                    break;
                case 'Z':
                case 'z':
                    closedPending = true;
                    clearControl();
                    break;
                default:
                    throw new ParserException("unsupported path command " + command);
            }
        }
        if (closedPending)
            p.setClosed(true);
        return p;
    }

    private VectorInterface getCurrent() {
        return new VectorFloat(x, y);
    }

    private VectorInterface setLastC2(VectorInterface p) {
        lastQuadraticControlPoint = p;
        lastCubicControlPoint = null;
        return p;
    }

    private VectorInterface setLastC3(VectorInterface p) {
        lastCubicControlPoint = p;
        lastQuadraticControlPoint = null;
        return p;
    }

    private void clearControl() {
        lastQuadraticControlPoint = null;
        lastCubicControlPoint = null;
    }

    private VectorInterface getLastC2() {
        if (lastQuadraticControlPoint == null)
            return getCurrent();
        return lastQuadraticControlPoint;
    }

    private VectorInterface getLastC3() {
        if (lastCubicControlPoint == null)
            return getCurrent();
        return lastCubicControlPoint;
    }

    private void addArc(Polygon p, VectorFloat rad, float rot, boolean large, boolean sweep, VectorFloat pos) {
        p.add(pos);
    }

    private void addQuadraticWithReflect(Polygon poly, VectorInterface start, VectorInterface p) {
        VectorInterface c = start.add(start.sub(getLastC2()));
        poly.add(setLastC2(c), p);
    }

    private void addCubicWithReflect(Polygon poly, VectorInterface start, VectorInterface c2, VectorInterface p) {
        VectorInterface c1 = start.add(start.sub(getLastC3()));
        poly.add(c1, setLastC3(c2), p);
    }

    /**
     * The parser exception
     */
    public static final class ParserException extends Exception {
        private ParserException(String message) {
            super(message);
        }
    }
}

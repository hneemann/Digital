/*
 * Copyright (c) 2018 Helmut Neemann
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.graphics.text;

import de.neemann.digital.draw.graphics.text.text.*;
import de.neemann.digital.draw.graphics.text.text.Character;

import java.util.HashMap;

/**
 * The text parser
 */
public class Parser {
    private static final HashMap<String, java.lang.Character> COMMANDS = new HashMap<>();

    static {
        COMMANDS.put("sum", '∑');
        COMMANDS.put("prod", '∏');
        COMMANDS.put("wedge", '∧');
        COMMANDS.put("vee", '∨');
        COMMANDS.put("neg", '¬');
        COMMANDS.put("oplus", '⊕');
        COMMANDS.put("odot", '⊙');
        COMMANDS.put("pm", '±');
        COMMANDS.put("mp", '∓');
        COMMANDS.put("div", '÷');
        COMMANDS.put("cdot", '·');
        COMMANDS.put("times", '×');
        COMMANDS.put("otimes", '⊗');
    }

    private final String text;
    private int pos;


    /**
     * Creates a new instance
     *
     * @param text the text to parse
     */
    public Parser(String text) {
        this.text = text.trim();
        pos = 0;
    }

    private char getChar() throws ParseException {
        char c = peekChar();
        pos++;
        return c;
    }

    private char peekChar() throws ParseException {
        if (pos >= text.length())
            throw new ParseException("unexpected EOF");
        return text.charAt(pos);
    }

    private void expect(char c) throws ParseException {
        if (getChar() != c)
            throw new ParseException("unexpected token " + c);
    }

    private String readWord() throws ParseException {
        StringBuilder sb = new StringBuilder();
        while (hasMore() && isNormal(peekChar()))
            sb.append(getChar());
        return sb.toString();
    }

    private boolean isNormal(char c) {
        return (c >= 'a' && c <= 'z')
                || (c >= 'A' && c <= 'Z')
                || (c >= '0' && c <= '9');
    }

    private boolean hasMore() {
        return pos < text.length();
    }

    /**
     * Parses the text
     *
     * @return the text classes
     * @throws ParseException ParseException
     */
    public Text parse() throws ParseException {
        return parse('\0');
    }

    private Text parse(char endChar) throws ParseException {
        Sentence sentence = new Sentence();
        while (hasMore() && peekChar() != endChar) {
            switch (peekChar()) {
                case ' ':
                case '\n':
                case '\r':
                case '\t':
                    getChar();
                    if (!sentence.lastIsBlank())
                        sentence.add(Blank.BLANK);
                    break;
                case '~':
                    getChar();
                    sentence.add(new Decorate(parseBrace(), Decorate.Style.OVERLINE));
                    break;
                case '$':
                    getChar();
                    sentence.add(new Decorate(parse('$'), Decorate.Style.MATH));
                    break;
                case '_':
                    getChar();
                    sentence.getIndex().addSub(parseBrace());
                    break;
                case '{':
                    getChar();
                    expect('}');
                    break;
                case '^':
                    getChar();
                    sentence.getIndex().addSuper(parseBrace());
                    break;
                case '\\':
                    getChar();
                    char p = peekChar();
                    switch (p) {
                        case '\\':
                        case '^':
                        case '_':
                            sentence.add(new Character(getChar()));
                            break;
                        default:
                            String command = readWord();
                            java.lang.Character t = COMMANDS.get(command);
                            if (t == null)
                                sentence.add(new Simple('\\' + command));
                            else {
                                sentence.add(new Character(t));
                                if (peekChar() == ' ')
                                    getChar();
                            }
                    }

                    break;
                default:
                    if (isNormal(peekChar()))
                        sentence.add(new Simple(readWord()).simplify());
                    else
                        sentence.add(new Character(getChar()));
            }
        }
        if (endChar != 0)
            expect(endChar);
        else if (pos != text.length())
            throw new ParseException("EOF expected");
        return sentence.simplify();
    }

    private Text parseBrace() throws ParseException {
        if (peekChar() == '{') {
            getChar();
            return parse('}');
        } else {
            return new Simple(readWord());
        }
    }


}

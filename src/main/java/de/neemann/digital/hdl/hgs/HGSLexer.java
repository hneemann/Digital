package de.neemann.digital.hdl.hgs;

import de.neemann.digital.lang.Lang;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author ideras
 */
public class HGSLexer {
    private int line;
    private StringBuilder literalSb;
    private State lexState;
    private final InputStream in;
    private boolean printExpr;

    private enum State {LITERAL, CODE, EXPR};

    /**
     * Creates a new instance.
     *
     * @param in the input stream.
     */
    public HGSLexer(InputStream in) {
        this.in = in;
        literalSb = new StringBuilder();
        lexState = State.LITERAL;
        printExpr = false;
        line = 1;
    }

    /**
     * Returns the input stream.
     *
     * @return the input stream.
     */
    public InputStream getInputStream() {
        return in;
    }

    private String getSequence(CharFilter charFilter) throws IOException {
        StringBuilder sb = new StringBuilder();

        in.mark(2);

        int nextChar = in.read();
        while (charFilter.match(nextChar)) {
            sb.append((char) nextChar);

            in.mark(2);
            nextChar = in.read();
        }
        in.reset();

        return sb.toString();
    }

    private Token lookUpIdentifier(String ident) {
        switch (ident) {
            case "if":
                return new Token(Token.IF, ident);
            case "else":
                return new Token(Token.ELSE, ident);
            case "while":
                return new Token(Token.WHILE, ident);
            case "for":
                return new Token(Token.FOR, ident);
            case "printf":
                return new Token(Token.PRINTF, ident);
            default:
                return new Token(Token.IDENTIFIER, ident);
        }
    }

    private Token handleLexerLiteralState(int nextCh) throws IOException {
        if (nextCh == '<') {
            int ch = in.read();
            if (ch == '?') {

                in.mark(2);
                ch = in.read();
                if (ch == '=') {
                    printExpr = true;
                    lexState = State.EXPR;
                } else {
                    lexState = State.CODE;
                    in.reset();
                }
                String str = literalSb.toString();

                if (!str.isEmpty()) {
                    literalSb = new StringBuilder();
                    return new Token(Token.OUTPUT_LITERAL, str);
                } else if (printExpr) {
                    printExpr = false;
                    return new Token(Token.PRINTEXPR, "");
                }
            } else {
                literalSb.append((char) nextCh).append((char) ch);
            }
        } else {
            literalSb.append((char) nextCh);
        }

        return null;
    }

    private boolean handleComments() throws IOException, HGSException {
        in.mark(2);

        int nextCh = in.read();
        switch (nextCh) {
            case '*': // Block comment
                boolean keepReading = true;

                while (keepReading) {
                    nextCh = in.read();
                    switch (nextCh) {
                        case -1:
                            throw new HGSException(line, Lang.get("notClosedBlockComment"));
                        case '*':
                            in.mark(2);
                            nextCh = in.read();
                            if (nextCh == '/') {
                                keepReading = false;
                            } else {
                                in.reset();
                            }
                            break;
                        case '\n':
                            line++;
                    }
                }
                return true;

            case '/': // Line comment
                while (nextCh != '\n' && nextCh != -1) {
                    nextCh = in.read();
                }

                line++;
                return true;

            default:
                in.reset();
                return false;
        }
    }

    private Token handleConstant(int chr) throws IOException, HGSException {
        String str;

        if (chr == '0') {
            in.mark(2);
            int ch = in.read();
            if (ch == 'x' || ch == 'X') {
                str = getSequence(c -> (Character.isDigit(c)
                                    || (c >= 'a' && c <= 'f')
                                    || (c >= 'A' && c <= 'F')));

                if (str.isEmpty()) {
                    throw new HGSException(line, Lang.get("invalidHexConstant"));
                }

                return new Token(Token.HEX_CONSTANT, str);
            }
            in.reset();
        }
        str = (char) chr + getSequence(ch -> Character.isDigit(ch));

        return new Token(Token.CONSTANT, str);
    }

    /**
     * Returns the next token in the input stream.
     *
     * @return the next token.
     * @throws IOException IOException
     * @throws HGSException HDLGenException
     */
    public Token getNextToken() throws IOException, HGSException {
        int nextCh;

        if (printExpr) {
            printExpr = false;
            return new Token(Token.PRINTEXPR, "");
        }

        while (true) {
            nextCh = in.read();

            if (nextCh == '\n') {
                line++;
            }

            if (nextCh == -1) {
                String str = literalSb.toString();
                literalSb = new StringBuilder();

                if (!str.isEmpty()) {
                    return new Token(Token.OUTPUT_LITERAL, str);
                }
                return new Token(Token.EOF, "End of File");
            }

            if (lexState == State.LITERAL) {
                Token tk = handleLexerLiteralState(nextCh);
                if (tk != null) {
                    return tk;
                }
                continue;
            }

            if (nextCh == ' '
                || nextCh == '\t'
                || nextCh == '\r'
                || nextCh == '\n') {
                continue;
            }

            /* Check for comments */
            if (nextCh == '/') {
                if (handleComments()) {
                    continue;
                }
            }

            String str;

            switch (nextCh) {
                case '?':
                    int ch = in.read();
                    if (ch == '>') {
                        State oldState = lexState;

                        lexState = State.LITERAL;
                        if (oldState == State.EXPR) {
                            return new Token(Token.ENDOFEXPR, "");
                        }
                        continue;
                    }
                    throw new HGSException(line, Lang.get("invalidCharacter", '?'));

                case '.': return new Token('.', ".");
                case '*': return new Token('*', "*");
                case '/': return new Token('/', "/");
                case '%': return new Token('%', "%");
                case '(': return new Token('(', "(");
                case ')': return new Token(')', ")");
                case '{': return new Token('{', "{");
                case '}': return new Token('}', "}");
                case '[': return new Token('[', "[");
                case ']': return new Token(']', "]");
                case ';': return new Token(';', ";");
                case ',': return new Token(',', ",");
                case '=':
                    return matchNext('=', Token.EQ_OP, "==", '=', "=");
                case '!':
                    return matchNext('=', Token.NE_OP, "!=", '!', "!");
                case '+':
                    return matchNext('+', Token.INC_OP, "++", '+', "+");
                case '-':
                    return matchNext('-', Token.DEC_OP, "--", '-', "-");
                case '>':
                    in.mark(2);
                    nextCh = in.read();

                    switch (nextCh) {
                        case '=': return new Token(Token.GE_OP, ">=");
                        case '>': return new Token(Token.SR_OP, ">>");
                        default:
                            in.reset();
                    }
                    return new Token('>', ">");
                case '<':
                    in.mark(2);
                    nextCh = in.read();

                    switch (nextCh) {
                        case '=': return new Token(Token.LE_OP, "<=");
                        case '<': return new Token(Token.SL_OP, "<<");
                        default:
                            in.reset();
                    }
                    return new Token('<', "<");
                case '&':
                    return matchNext('&', Token.AND_OP, "&&", '&', "&");
                case '|':
                    return matchNext('|', Token.OR_OP, "||", '|', "|");
                case '^':
                    return new Token('^', "^");
                case '~':
                    return new Token('~', "~");
                case '"':
                    str = getSequence(chr -> (chr != '"' && chr != -1 && chr != '\n'));
                    nextCh = in.read();

                    if (nextCh != '"') {
                        throw new HGSException(line, Lang.get("missingDoubleQuote"));
                    }

                    return new Token(Token.STRING_LITERAL, str);
                default:
                    if (Character.isDigit(nextCh)) {
                        return handleConstant(nextCh);
                    } else if (nextCh == '_' || Character.isLetter(nextCh)) {
                        str = (char) nextCh
                                     + getSequence(chr -> (chr == '_'
                                                          || Character.isDigit(chr)
                                                          || Character.isLetter(chr))
                                                  );

                        return lookUpIdentifier(str);

                    } else {
                        throw new HGSException(line, Lang.get("invalidCharacter", (char) nextCh));
                    }

            }
        }
    }

    /**
     * Returns the current source line.
     *
     * @return the current source line.
     */
    public int getLine() {
        return line;
    }

    private Token matchNext(int nextSymbol, int tokenID1, String lex1, int tokenID2, String lex2) throws IOException {
        in.mark(2);
        int ch = in.read();

        if (ch == nextSymbol) {
            return new Token(tokenID1, lex1);
        }
        in.reset();
        return new Token(tokenID2, lex2);
    }

    private interface CharFilter {
        boolean match(int ch);
    }
}

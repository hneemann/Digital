/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs;

/**
 *
 * @author ideras
 */
public class Token {
    private final int id;
    private final String lexeme;

    /**
     * Creates a new instance.
     *
     * @param id    the id of the token
     * @param lexeme the lexeme of the token
     */
    public Token(int id, String lexeme) {
        this.id = id;
        this.lexeme = lexeme;
    }

    /**
     * Returns the token id.
     *
     * @return the token id
     */
    public int getId() {
        return id;
    }

    /**
     * Returns the token lexeme.
     *
     * @return the token lexeme
     */
    public String getLexeme() {
        return lexeme;
    }

    /**
     * Checks if the token is one of the provided list.
     *
     * @param tokenIds the list of token ids.
     *
     * @return true if the token appears on the list, false otherwise.
     */
    public boolean isOneOf(int... tokenIds) {
        for (int tokenId : tokenIds) {
            if (id == tokenId) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks the token id is equal to the provided.
     *
     * @param tokenId the provided token id.
     * @return true if the token id is equal to provided id, false otherwise.
     */
    public boolean is(int tokenId) {
        return (id == tokenId);
    }

    @Override
    public String toString() {
        return lexeme;
    }

    /* Token IDs */
    /**
     * Output literal token.
     */
    public static final int OUTPUT_LITERAL = 257;
    /**
     * Identifier token.
     */
    public static final int IDENTIFIER = 258;
    /**
     * String literal.
     */
    public static final int STRING_LITERAL = 259;
    /**
     * Decimal constant token.
     */
    public static final int CONSTANT = 260;
    /**
     * Hexadecimal constant token.
     */
    public static final int HEX_CONSTANT = 261;
    /**
     * Increment operator (++).
     */
    public static final int INC_OP = 262;
    /**
     * Decrement operator (--).
     */
    public static final int DEC_OP = 263;
    /**
     * Shift left operator.
     */
    public static final int SL_OP = 264;
    /**
     * Shift right operator.
     */
    public static final int SR_OP = 265;
    /**
     * Less or equal operator.
     */
    public static final int LE_OP = 266;
    /**
     * Greater or equal operator.
     */
    public static final int GE_OP = 267;
    /**
     * Equal operator.
     */
    public static final int EQ_OP = 268;
    /**
     * Not equal operator.
     */
    public static final int NE_OP = 269;
    /**
     * And operator.
     */
    public static final int AND_OP = 270;
    /**
     * Or operator.
     */
    public static final int OR_OP = 271;
    /**
     * "if" keyword.
     */
    public static final int IF = 272;
    /**
     * "else" keyword.
     */
    public static final int ELSE = 273;
    /**
     * "while" keyword.
     */
    public static final int WHILE = 274;
    /**
     * "for" keyword.
     */
    public static final int FOR = 275;
    /**
     * "printf" keyword.
     */
    public static final int PRINTF = 276;
    /**
     * Special token for print expression.
     */
    public static final int PRINTEXPR = 280;
    /**
     * Special token end of expression.
     */
    public static final int ENDOFEXPR = 290;
    /**
     * End of File token.
     */
    public static final int EOF = 999;
}

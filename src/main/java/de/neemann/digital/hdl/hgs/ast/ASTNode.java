/*
 * Copyright (c) 2018 Ivan Deras
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.hgs.ast;

/**
 *
 * @author ideras
 */
public abstract class ASTNode {
    private final int line;

    /**
     * Base constructor for all AST nodes.
     *
     * @param line the source line
     */
    public ASTNode(int line) {
        this.line = line;
    }

    /**
     * Returns the source line.
     *
     * @return the source line.
     */
    public int getLine() {
        return line;
    }

}

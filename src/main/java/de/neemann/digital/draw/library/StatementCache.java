/*
 * Copyright (c) 2019 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.draw.library;

import de.neemann.digital.hdl.hgs.Parser;
import de.neemann.digital.hdl.hgs.ParserException;
import de.neemann.digital.hdl.hgs.Statement;

import java.io.IOException;
import java.util.HashMap;

/**
 * Implements a cache for statements
 */
public class StatementCache {
    private final HashMap<String, Statement> map;

    /**
     * Creates a new instance
     */
    public StatementCache() {
        map = new HashMap<>();
    }

    /**
     * Gets the analysed statements.
     * If the statements are not present in the cache, they are generated.
     *
     * @param code the code
     * @return the statements
     * @throws IOException     IOException
     * @throws ParserException ParserException
     */
    public Statement getStatement(String code) throws IOException, ParserException {
        Statement genS = map.get(code);
        if (genS == null) {
            genS = new Parser(code).parse(false);
            map.put(code, genS);
        }
        return genS;
    }

}

/*
 * Copyright (c) 2018 Helmut Neemann.
 * Use of this source code is governed by the GPL v3 license
 * that can be found in the LICENSE file.
 */
package de.neemann.digital.hdl.vhdl2;

import java.util.Collections;
import java.util.HashSet;

/**
 * List of vhdl keywords
 */
public final class VHDLKeywords {
    private static final HashSet<String> KEYWORDS = new HashSet<>();

    static {
        Collections.addAll(KEYWORDS,
                "abs", "access", "after", "alias", "all", "and", "architecture", "array", "assert",
                "attribute", "begin", "block", "body", "buffer", "bus", "case", "component", "configuration",
                "constant", "disconnect", "downto", "else", "elsif", "end", "entity", "exit", "file",
                "for", "function", "generate", "generic", "group", "guarded", "if", "impure", "in",
                "inertial", "inout", "is", "label", "library", "linkage", "literal", "loop",
                "map", "mod", "nand", "new", "next", "nor", "not", "null", "of",
                "on", "open", "or", "others", "out", "package", "port", "postponed", "procedure",
                "process", "pure", "range", "record", "register", "reject", "rem", "report", "return",
                "rol", "ror", "select", "severity", "signal", "shared", "sla", "sll", "sra",
                "srl", "subtype", "then", "to", "transport", "type", "unaffected", "units", "until",
                "use", "variable", "wait", "when", "while", "with", "xnor", "xor");
    }

    private VHDLKeywords() {
    }

    static boolean isKeyword(String str) {
        return KEYWORDS.contains(str.toLowerCase());
    }
}


